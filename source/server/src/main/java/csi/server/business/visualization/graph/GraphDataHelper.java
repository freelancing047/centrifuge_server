package csi.server.business.visualization.graph;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.BundleUtil;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.dto.CsiMap;
import csi.server.common.model.SortOrder;
import csi.server.common.model.dataview.DataView;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;

public class GraphDataHelper {

    public class PageRequest {

        public int componentId;

        public long offset;

        public long pageSize;

        public String property;

        public SortOrder sort;
    }

    public class SimpleTuple {

        public long id;

        public Object value;
    }

    static final String NODES_BY_ATTRIBUTE = "SELECT distinct node_id, value FROM NodeAttrs_{0} where name = {1}\n" + "ORDER BY value {2} LIMIT {3} OFFSET {4}";

    static final String NODE_ATTRIBUTE_QUERY = "SELECT distinct node_id, first_value( {1} ) OVER (PARTITION BY node_id) as propValue\n"
            + "FROM NodeAttrs_{0} WHERE name = {2} order by propValue";

//    private DataSource dataSource;

//    private DataView dataview;

    private String dbDataviewUUID;

    private JdbcTemplate jdbcHelper;

    public GraphDataHelper(DataSource dataSource, DataView dataview) {
//        this.dataSource = dataSource;
//        this.dataview = dataview;
        String uuid = dataview.getUuid();

        dbDataviewUUID = CacheUtil.toDbUuid(uuid);

        jdbcHelper = new JdbcTemplate(dataSource);
    }

    public Collection<SimpleTuple> getNodesBy(PageRequest request) {
        String[] parameters = fillParameters(request);
        String nodeQuery = MessageFormat.format(NODES_BY_ATTRIBUTE, (Object[]) parameters);

        List<SimpleTuple> results = jdbcHelper.query(nodeQuery, new SimpleTupleRowHandler());

        return results;
    }

    private String[] fillParameters(PageRequest request) {
        List<String> params = new ArrayList<String>();
        params.add(dbDataviewUUID);
        params.add(SqlUtil.quoteString(request.property, '\''));
        params.add(request.sort.toString());
        params.add(Long.toString(request.pageSize));
        params.add(Long.toString(request.offset));

        return params.toArray(new String[0]);
    }

    class SimpleTupleRowHandler implements org.springframework.jdbc.core.RowMapper<SimpleTuple> {

        @Override
        public SimpleTuple mapRow(ResultSet rs, int rowNum) throws SQLException {
            SimpleTuple tuple = new SimpleTuple();
            tuple.id = rs.getLong(1);
            tuple.value = rs.getObject(2);
            return tuple;
        }

    }

    public static CsiMap getNodeAsMap(Graph graph, Node item) {
        int itemId = item.getRow();

        NodeStore detail = GraphManager.getNodeDetails(item);
        List<NodeStore> leaves = new ArrayList<NodeStore>();
        BundleUtil.findLeafChildren(detail, leaves);

        CsiMap<String, Object> prop = null;

        if (!leaves.isEmpty()) {
            prop = new CsiMap<String, Object>();
            prop.put("ID", itemId);
            prop.put("csi.object.type", "nodes");
            String label = detail.getLabel();
            List<AbstractGraphObjectStore> children = detail.getChildren();
            label += "(" + children.size() + ", " + leaves.size() + ")";
            prop.put("label", label);

            Map<String, Node> idToNodeMap = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);

            List<CsiMap> subBundles = new ArrayList<CsiMap>();

            for (AbstractGraphObjectStore gos : children) {
                NodeStore child = (NodeStore) gos;

                Node bundledChild = idToNodeMap.get(child.getKey());
                CsiMap childMap = getNodeAsMap(graph, bundledChild);
                if (childMap != null) {
                    subBundles.add(childMap);
                }

            }

            prop.put("children", subBundles);

        }

        return prop;

    }
}
