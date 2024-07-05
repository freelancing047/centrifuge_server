package csi.server.business.visualization.graph.actions;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

import prefuse.data.Graph;

import csi.security.Authorization;
import csi.security.CsiSecurityManager;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.ModelObject;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;

/**
 *
 * Action class to perform graph data construction. This class processes the
 * visualization definition for each node definition. The SQL syntax is
 * currently predicated on using a PostgreSQL dialect e.g. COALESCE() function.
 * <p>
 * First pass is to extract out all nodes based on the node definitons. This
 * takes into account the potential absence of either the ID or
 * Label...depending on the data. If this results in null, the query to extract
 * the nodes from the temp table will filter them out. This temp table is
 * constructed as a UNION of all node definition queries.
 * <p>
 * The set of nodes is determined by extracting out the distinct node
 * ids--inserted into a table of the pattern Nodes_{viz uuid}. An additional
 * table is then created to represent the supporting rows of each node
 * (NodesXWalk_{viz_uuid}).
 * <p>
 * Once we've determined the rows in which nodes are present, we process the
 * link defs. We perform a self-join on the node crosswalk table where the
 * supporting row id is the same and each node is created by the required node
 * definition. This immediately gives us our link cross-walk table, and from
 * that table we are able to discern the unique set of links.
 * <p>
 *
 * With the links, we can then determine the initial connected components of the
 * data set. Choose the first node and query the link table for any other node
 * id that is connected to the chosen node. All nodes that can be reached are
 * assigned a sub-graph id of 1. Find the next node that isn't assigned to a
 * sub-graph, and repeat the process.
 *
 * @author Centrifuge Systems Inc.
 *
 */

public class GraphBuilder implements Callable<Void> {

    private static final String UNION_ALL = "\n UNION ALL \n";

    private static String ALL_NODES_QUERY = "INSERT INTO tNodes( user_node_id, definition_id, row_id ) ( {0} );";

    private static String DISTINCT_NODES = "INSERT INTO Nodes_{0} (node_key)\n" + "\t(SELECT DISTINCT user_node_id FROM tNodes);";

    private static String POPULATE_NODE_XWALK_QUERY = "INSERT INTO NodesXWalk_{0} (node_id, definition_id, row_id)\n"
            + " (SELECT Nodes_{0}.node_id, tNodes.definition_id, tNodes.row_id FROM Nodes_{0} JOIN tNodes ON Nodes_{0}.node_key = tNodes.user_node_id);";

    private static String NODE_CREATION_TEMPLATE = "SELECT coalesce( {2} ), TEXT( ''{1}'' ), \"internal_id\" FROM {0}\n" + "\tWHERE coalesce( {2} ) IS NOT NULL\n";

    private static String ALL_LINKS_QUERY = "INSERT INTO tLinks( source_id, target_id, row_id, definition_id )\n\t{0};\n";

    private static String DISTINCT_LINKS_QUERY = "INSERT INTO Links_{0} ( source_id, target_id )\n" + "\t(SELECT source_id, target_id FROM tLinks);";

    private static String POPULATE_LINKS_XWALK_QUERY = "INSERT INTO LinksXWalk_{0} (link_id, definition_id, row_id)\n"
            + "\t( SELECT l.link_id, tl.definition_id, tl.row_id FROM Links_{0} l JOIN tLinks tl ON l.source_id = tl.source_id AND l.target_id = tl.target_id );\n";

    private static String LINK_CREATION_TEMPLATE = "SELECT source.node_id as s_id, target.node_id as t_id, source.row_id as row_id, TEXT( ''{3}'' ) as definition_id\n"
            + "\tFROM NodesXWalk_{0} source, NodesXWalk_{0} target\n"
            + "\tWHERE source.row_id = target.row_id AND source.definition_id = TEXT( ''{1}'' ) AND target.definition_id = TEXT( ''{2}'' )";

    // params
    // 0 = dataview uuid
    // 1 = vizualization uuid
    // 2 = attribute name
    // 3 = attribute value
    // 4 = attribute type
    // 5 = nodedef uuid
    private static String NODE_ATTRIBUTE_TEMPLATE = "SELECT distinct node.node_id, node.row_id, TEXT({2}), {3}, TEXT({4})\n"
            + "FROM\n\tNodesXWalk_{1} node\n\tJOIN\n\tcache_{0} cache\n" + "ON\n\tnode.row_id = cache.internal_id AND\n" + "\tnode.definition_id = {5}\n";

    private static final String LINK_ATTRIBUTE_TEMPLATE = "SELECT distinct link.link_id, link.row_id, TEXT({2}), {3}, TEXT({4})\n" + "FROM\n\tLinksXWalk_{1} link\n" + "JOIN\n"
            + "\tcache_{0} cache\n" + "ON\n" + "\tlink.row_id = cache.internal_id AND\n" + "\tlink.definition_id = {5}\n";

    public static final String PROCESSED = "graph.data.processed";

    private static String NODE_ATTRIBUTE_UPDATE_TEMPLATE = "INSERT INTO NodeAttrs_{0} ( node_id, row_id, name, value, type )\n\t{1};";

    private static String LINK_ATTRIBUTE_UPDATE_TEMPLATE = "INSERT INTO LinkAttrs_{0} ( link_id, row_id, name, value, type )\n\t{1};";

    protected RelGraphViewDef settings;

    protected Graph graph;

    protected DataSource source;

    protected String cacheTable;

    protected String vizUUID;

    private DataView dataview;
    private Authorization authorization;

    private DataViewDef dvDef;
    protected StringBuilder batchStatements;

    public synchronized DataView getDataview() {
        return dataview;
    }

    public synchronized void setDataview(DataView dataview) {
        this.dataview = dataview;
    }

    public GraphBuilder(DataView dataview, RelGraphViewDef settings, Graph graph) {
        this.dataview = dataview;
        // this.dvDef = dvDef;
        this.settings = settings;
        this.graph = graph;
        vizUUID = settings.getUuid();

        batchStatements = new StringBuilder();
    }

    public synchronized DataSource getSource() {
        return source;
    }

    public synchronized void setSource(DataSource source) {
        this.source = source;
    }

    @Override
    public Void call() throws Exception {
        /*
         * construct various sql queries to do the following:
         *
         * i) query the data set to extract the unique nodes--based on the
         * NodeDefs that we are provided. this requires that we execute a UNION
         * query based on each NodeDef provided. This is required since we may
         * have a node that is <i>defined</i> by multiple NodeDefs. This query
         * provides the total set of unique nodes that are defined in the data.
         *
         * First pass of this stage involves building the list of all nodes and
         * their corresponding supporting rows. The unique nodes are determined
         * by extracting the distinct values from this result.
         *
         * ii) Given the unique set of nodes, construct SQL statements that
         * construct/materialize the links of the graph for the data set. Again
         * defer the materialization of link attributes by discovering the
         * supporting rows.
         */

        /**
         * 1) create temp table to hold node identifier, definition name, and
         * supporting row -- ordered by the node name query is along the lines
         * of (PostgreSQL syntax!)
         *
         * select coalesce( <nodespec id attr>, <nodespec label attr> ),
         * <NodeSpec uuid>, row_id from <Cache Data> 2) update Nodes table with
         * the unique values 3) update Node_CrossWalk with assigned node_id,
         * Definition, and Supporting row
         *
         * 4) (Optional?) Construct fully materialized attribute table for all
         * attributes of each node(?)
         *
         * 5) Create temp links table -- (almost same behavior as links...) 6)
         * for each link spec,
         *
         * insert into node_xwalk( node_identifier, row_id, definition ) (
         * select <node identifier>, row_id, <node spec> from <cache_data> where
         * <node_identifier> is not null )
         *
         * insert into nodes ( node_identifier, node_iid ) ( select distinct
         * <node_identifier> from node_xwalk where <node_identifier> )
         *
         *
         */

        try {
            CsiSecurityManager.setAuthorization(this.authorization);
            dataview = CsiPersistenceManager.findObject(DataView.class, dataview.getUuid());
            dvDef = dataview.getMeta();
            settings = CsiPersistenceManager.findObject(RelGraphViewDef.class, settings.getUuid());

            synchronized (graph) {
                Boolean flag = (Boolean) graph.getClientProperty(PROCESSED);

                if (flag == null) {
                    graph.putClientProperty(PROCESSED, Boolean.FALSE);
                }
                graph.notifyAll();
            }
            try (Connection conn = source.getConnection()) {
               conn.setAutoCommit(false);

               try (Statement stmt = conn.createStatement()) {
                  initializeTables(stmt);
                  createGraph(stmt);
                  initializeRootGraph(stmt);

                  StringTokenizer tokenizer = new StringTokenizer(batchStatements.toString(), ";");

                  while (tokenizer.hasMoreTokens()) {
                     String command = tokenizer.nextToken();
                     stmt.addBatch(command);
                  }
                  stmt.executeBatch();
                  conn.commit();

                  batchStatements = new StringBuilder();

                  processNodeAttributes(stmt);
                  processLinkAttributes(stmt);
               }
               try (Statement stmt = conn.createStatement()) {
                  StringTokenizer tokenizer = new StringTokenizer(batchStatements.toString(), ";");

                  while (tokenizer.hasMoreTokens()) {
                     String command = tokenizer.nextToken();
                     stmt.addBatch(command);
                  }
                  stmt.executeBatch();
                  conn.commit();
               }
            }
            synchronized (graph) {
                graph.putClientProperty(PROCESSED, Boolean.TRUE);
                graph.notifyAll();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;

    }

    private void processLinkAttributes(Statement stmt) {
        List<LinkDef> linkDefs = settings.getLinkDefs();

        List<String> skip = new ArrayList<String>();

        for (LinkDef link : linkDefs) {
            MessageFormat insertTemplate = new MessageFormat(LINK_ATTRIBUTE_UPDATE_TEMPLATE);
            String[] args = new String[2];
            args[0] = getSafeVisualizationId();
            Set<AttributeDef> attributeDefs = link.getAttributeDefs();
            for (AttributeDef attr : attributeDefs) {

                if (skip.contains(attr.getName())) {
                    continue;
                }

                if (attr.getName().equals(ObjectAttributes.CSI_INTERNAL_TYPE)) {
                    continue;
                }
                args[1] = fillAttributeTemplate(link, attr, LINK_ATTRIBUTE_TEMPLATE);
                String attributeQuery = insertTemplate.format(args);
                batchStatements.append(attributeQuery);
            }

            args[1] = processTypeAttribute(link);

            String typeQuery = insertTemplate.format(args);
            batchStatements.append(typeQuery);

        }

    }

    private void processNodeAttributes(Statement stmt) {
        List<NodeDef> nodeDefs = settings.getNodeDefs();

        List<String> skip = new ArrayList<String>();
        skip.add(ObjectAttributes.CSI_INTERNAL_X_POS);
        skip.add(ObjectAttributes.CSI_INTERNAL_Y_POS);

        for (NodeDef node : nodeDefs) {
            MessageFormat insertTemplate = new MessageFormat(NODE_ATTRIBUTE_UPDATE_TEMPLATE);
            String[] args = new String[2];
            args[0] = getSafeVisualizationId();
            Set<AttributeDef> attributeDefs = node.getAttributeDefs();
            for (AttributeDef attr : attributeDefs) {

                if (skip.contains(attr.getName())) {
                    continue;
                }

                // special handling of the type attribute!
                if (attr.getName().equals("csi.internal.Type")) {
                    continue;

                }
                args[1] = fillAttributeTemplate(node, attr, NODE_ATTRIBUTE_TEMPLATE);
                String attributeQuery = insertTemplate.format(args);
                batchStatements.append(attributeQuery);
            }

            args[1] = processTypeAttribute(node);

            String typeQuery = insertTemplate.format(args);
            batchStatements.append(typeQuery);
        }
    }

    private String processTypeAttribute(NodeDef node) {
        AttributeDef typeAttribute = node.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        AttributeDef altType = node.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
        if (altType == null) {
            altType = node.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID);
        }

        String objectId = node.getUuid();

        return generateTypeQueryFromAttributes(NODE_ATTRIBUTE_TEMPLATE, typeAttribute, altType, objectId);

    }

    private String processTypeAttribute(LinkDef link) {
        AttributeDef typeAttribute = link.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        AttributeDef altType = link.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
        if (altType == null) {
            altType = link.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID);
        }

        String objectId = link.getUuid();

        return generateTypeQueryFromAttributes(LINK_ATTRIBUTE_TEMPLATE, typeAttribute, altType, objectId);

    }

    private String generateTypeQueryFromAttributes(String template, AttributeDef typeAttribute, AttributeDef altType, String objectId) {
        // Do we really want to propagate the hacks done previously for
        // the absence of types? i.e. if there's no type attribute,
        // use the label or id attribute's field def name as the type?
        String[] typeParams = new String[6];
        typeParams[0] = getSafeDataviewUuid();
        typeParams[1] = getSafeVisualizationId();
        typeParams[2] = SqlUtil.quoteString(ObjectAttributes.CSI_INTERNAL_TYPE, '\'');
        // skip 3 for now...
        typeParams[4] = SqlUtil.quoteString("string", '\'');
        typeParams[5] = SqlUtil.quoteString(objectId, '\'');

        List<String> options = new ArrayList<String>();
        if (typeAttribute != null) {
            FieldDef fieldDef = typeAttribute.getFieldDef();
            if (fieldDef.getFieldType() == FieldType.STATIC) {
                options.add(asSQLText(fieldDef.getStaticText()));
            } else {
                // need to perform a coalescing....
                String s = "cache." + CacheUtil.toQuotedDbUuid(fieldDef.getUuid());
                options.add(s);
            }
        }

        if (altType != null) {
            FieldDef fieldDef = altType.getFieldDef();
            String altTypeName = fieldDef.getFieldName();
            if (altTypeName != null) {
                altTypeName = asSQLText(altTypeName);
                options.add(altTypeName);
            }
        }

        options.add(asSQLText("Unspecified"));

        String typeOptions = options.stream().filter(Objects::nonNull).collect(Collectors.joining(", "));

        typeParams[3] = "COALESCE ( " + typeOptions + " )";

        String attrQuery = MessageFormat.format(template, (Object[]) typeParams);

        return attrQuery;
    }

    private String asSQLText(String value) {
        return "TEXT(" + SqlUtil.quoteString(value, '\'') + ")";
    }

    protected String getSafeDataviewUuid() {
        String uuid = CacheUtil.toDbUuid(dataview.getUuid());
        return uuid;
    }

    protected String getSafeVisualizationId() {
        // FIXME: for now just leverage same uuid as dv.
//        String uuid = CacheUtil.toDbUuid(settings.getUuid());
        return getSafeDataviewUuid();
    }

    private String fillAttributeTemplate(ModelObject node, AttributeDef attr, String template) {
        String[] params = new String[6];
        params[0] = getSafeDataviewUuid();
        params[1] = getSafeVisualizationId();
        params[2] = SqlUtil.quoteString(attr.getName(), '\'');

        FieldDef field = attr.getFieldDef();
        if (field.getFieldType() == FieldType.STATIC) {
            params[3] = "TEXT( " + SqlUtil.quoteString(field.getStaticText(), '\'') + " ) ";
        } else {
            params[3] = "cache." + CacheUtil.toQuotedDbUuid(field.getUuid());
        }

        params[4] = field.getValueType().getLegacyType();
        if ((params[4] == null) || (params[4].length() == 0)) {
            params[4] = "string";
        }

        params[4] = SqlUtil.quoteString(params[4], '\'');
        params[5] = SqlUtil.quoteString(node.getUuid(), '\'');

        String attrQuery = MessageFormat.format(template, (Object[]) params);

        return attrQuery;
    }

    private void createGraph(Statement stmt) throws SQLException {
        String dvuuid = dataview.getUuid();

        Map<NodeDef, Boolean> validNodeDefs = new HashMap<NodeDef, Boolean>();
        processNodeDefinitions(dvuuid, validNodeDefs);
        processLinkDefinitions(dvuuid, validNodeDefs);
    }

    private void processLinkDefinitions(String dvuuid, Map<NodeDef, Boolean> validNodeDefs) {
        List<String> linkQueries = new ArrayList<String>();
        String safeDataviewUuid = getSafeDataviewUuid();
        for (LinkDef linkDef : settings.getLinkDefs()) {
            NodeDef one = linkDef.getNodeDef1();
            NodeDef two = linkDef.getNodeDef2();

            if (!hasValidDefinitions(validNodeDefs, linkDef)) {
                // log and continue...
                continue;
            }

            String[] params = new String[4];
            params[0] = safeDataviewUuid;
            params[1] = one.getUuid();
            params[2] = two.getUuid();
            params[3] = linkDef.getUuid();

            String partial = MessageFormat.format(LINK_CREATION_TEMPLATE, (Object[]) params);
            linkQueries.add(partial);

        }

        String unionedLinksQuery = linkQueries.stream().collect(Collectors.joining(UNION_ALL));
        String allLinksQuery = MessageFormat.format(ALL_LINKS_QUERY, unionedLinksQuery);
        batchStatements.append(allLinksQuery);

        // now construct unique links and link xwalk from our temp table....

        String distinctLinksQuery = MessageFormat.format(DISTINCT_LINKS_QUERY, safeDataviewUuid);
        String linkInstanceQuery = MessageFormat.format(POPULATE_LINKS_XWALK_QUERY, safeDataviewUuid);

        batchStatements.append(distinctLinksQuery);
        batchStatements.append(linkInstanceQuery);
    }

    private void processNodeDefinitions(String dvuuid, Map<NodeDef, Boolean> validNodeDefs) {

        String dbDVUuid = CacheUtil.toDbUuid(dvuuid);

        List<NodeDef> nodeDefs = settings.getNodeDefs();

        String[] params = new String[3];

        List<String> nodeDefQueries = new ArrayList<String>();
        for (NodeDef nodeDef : nodeDefs) {
            AttributeDef idAttr = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID);
            AttributeDef labelAttr = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);

            if ((idAttr == null) && (labelAttr == null)) {
                // log the fact that we don't have a valid attribute to identify
                // node instances; then remove from the list....
                // validNodeDefs.put( nodeDef, false );
                continue;
            }

            validNodeDefs.put(nodeDef, true);

            params[0] = "cache_" + dbDVUuid;
            params[1] = nodeDef.getUuid();

            String uniqueIDAttr = null;

            if (labelAttr == null) {
                uniqueIDAttr = idAttr.getFieldDef().getUuid();
                uniqueIDAttr = SqlUtil.quote(CacheUtil.toDbUuid(uniqueIDAttr));
            } else if (idAttr == null) {
                uniqueIDAttr = SqlUtil.quote(CacheUtil.toDbUuid(labelAttr.getFieldDef().getUuid()));
            } else {
                StringBuilder tempBuf = new StringBuilder();
                tempBuf.append(SqlUtil.quote(CacheUtil.toDbUuid(idAttr.getFieldDef().getUuid()))).append(", ");
                tempBuf.append(SqlUtil.quote(CacheUtil.toDbUuid(labelAttr.getFieldDef().getUuid())));

                uniqueIDAttr = tempBuf.toString();
            }

            params[2] = uniqueIDAttr;

            String partialQuery = MessageFormat.format(NODE_CREATION_TEMPLATE, (Object[]) params);
            nodeDefQueries.add(partialQuery);

        }
        String unionedNodesQuery = nodeDefQueries.stream().collect(Collectors.joining(UNION_ALL));
        String createNodesQuery = MessageFormat.format(ALL_NODES_QUERY, unionedNodesQuery);
        String distinctNodesQuery = MessageFormat.format(DISTINCT_NODES, dbDVUuid);
        batchStatements.append(createNodesQuery);
        batchStatements.append(distinctNodesQuery);

        String nodeSupportingRows = MessageFormat.format(POPULATE_NODE_XWALK_QUERY, dbDVUuid);
        batchStatements.append(nodeSupportingRows);
    }

    private boolean hasValidDefinitions(Map<NodeDef, Boolean> validNodeDefs, LinkDef linkDef) {
        NodeDef one = linkDef.getNodeDef1();
        NodeDef two = linkDef.getNodeDef2();

        boolean isValid = validNodeDefs.containsKey(one) && validNodeDefs.get(one) && validNodeDefs.containsKey(two) && validNodeDefs.get(two);
        return isValid;
    }

    private void initializeTables(Statement stmt) throws SQLException, IOException {

        InputStream istream = this.getClass().getResourceAsStream("graph_ddl.script");
        String ddlTemplate = IOUtils.toString(istream);

        String safeUUID = CacheUtil.toDbUuid(dataview.getUuid());

        String ddlScript = MessageFormat.format(ddlTemplate, safeUUID);

        batchStatements.append(ddlScript);
    }

    private void initializeRootGraph(Statement stmt) {
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

}
