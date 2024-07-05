package csi.server.business.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.GraphMLWriter;

import csi.config.Configuration;
import csi.config.RelGraphConfig;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.PayloadParam;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.business.visualization.graph.search.GraphSearchService;
import csi.server.business.visualization.graph.search.MemoryGraphSearchService;
import csi.server.common.dto.graph.search.AttributeCriterion;
import csi.server.common.dto.graph.search.GraphSearch;
import csi.server.common.dto.graph.search.GraphSearchResults;
import csi.server.common.dto.graph.search.NodeSearchCriterion;
import csi.server.common.dto.graph.search.SearchType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.service.api.GraphSearchActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.task.api.TaskSession;

/*
 * TODO:
 *
 * 1. injection -- things are broken out to easily allow DI for the GraphSearchService and TaskManager instances 2.
 * isolation of DTO from search results. reusing the DTO as the search result container for convenience. need to revisit
 * this when moving to a search on persisted data.
 */
@Service(path = "/services/graphs/search")
public class GraphSearchActionsService extends AbstractService implements GraphSearchActionsServiceProtocol {

    static final String DATAVIEW = "dvuuid";

    static final String VIZ = "vizuuid";

    static final String SEARCH_RESULTS = "graph.search.results";

    public GraphSearchActionsService() {
    }

    // @inject me!
    private GraphSearchService getGraphSearchService() {
        return new MemoryGraphSearchService();
    }

    @Override
    public void initMarshaller(XStream xstream) {
        super.initMarshaller(xstream);

    }

    @Override
   @Operation
    public GraphSearchResults query(@QueryParam(DATAVIEW) String dvUuid, @QueryParam(VIZ) String vizUuid, @PayloadParam GraphSearch searchCriteria) throws CentrifugeException {

        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

        if (graphContext == null) {
            throw new CentrifugeException("Graph context not found");
        }
        synchronized (graphContext) {

            Graph graph = (searchCriteria.isVisibleGraphSearch()) ? graphContext.getVisibleGraph() : graphContext.getGraphData();

            // TODO: error handling.
            // verify we have a graph to perform a search against!

            GraphSearchService service = getGraphSearchService();

            if (searchCriteria.isVisibleGraphSearch()) {
                service.setNodeFilter(GraphContext.Predicates.IsNodeVisualized);
                service.setEdgeFilter(GraphContext.Predicates.IsEdgeVisualized);
            }

            GraphSearchResults results = service.search(graph, searchCriteria);
            // FIXME: this needs to be 'viz' safe i.e. handle multiple results from several different RG searches.
            TaskSession taskSession = TaskHelper.getCurrentSession();
            taskSession.setAttribute(SEARCH_RESULTS, results);

            RelGraphConfig graphConfig = Configuration.getInstance().getGraphConfig();
            int resultsLimit = graphConfig.getSearchResultsLimit();
            boolean searchNodes = searchCriteria.getSearchType() == SearchType.NODES;

            results.total = (searchNodes) ? results.getNodes().size() : results.getLinks().size();

            // results are truncated only if we're searching data and exceeded the threshold.
            // A search against the visible graph will always return all results.
            boolean requiresTruncation = !searchCriteria.isVisibleGraphSearch() && (resultsLimit < results.total);
            if (requiresTruncation) {
                GraphSearchResults truncated = new GraphSearchResults();
                truncated.truncatedResults = true;
                if (searchCriteria.getSearchType() == SearchType.NODES) {
                    truncated.setNodes(results.getNodes().subList(0, resultsLimit));
                } else {
                    truncated.setLinks(results.getLinks().subList(0, resultsLimit));
                }
                truncated.total = results.total;
                results = truncated;
            }
            return results;
        }
    }

    @Override
   @Operation
    public GraphSearchResults testQuery(@QueryParam(DATAVIEW) String dvUuid, @QueryParam(VIZ) String vizUuid) throws CentrifugeException {

        RelGraphViewDef graphDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);

        List<NodeDef> nodeDefs = graphDef.getNodeDefs();
        int index = Double.valueOf(Math.floor(Math.random() * nodeDefs.size())).intValue();
        NodeDef target = null;
        target = nodeDefs.get(index);

        GraphSearch search = new GraphSearch();
        search.setSearchType(SearchType.NODES);
        search.setNodeCriteria(new ArrayList<NodeSearchCriterion>());
        List<NodeSearchCriterion> listNodes = search.getNodeCriteria();

        NodeSearchCriterion criteria = new NodeSearchCriterion();
        listNodes.add(criteria);
        criteria.setNodeDef(target);
        criteria.setAttributeCriteria(new ArrayList<AttributeCriterion>());

        return query(dvUuid, vizUuid, search);
    }

    /*
     *
     * Helper classes for XStream codec -- similar to GraphConverter, but doesn't have all of the context of the graph
     * when handling each node.
     *
     * May need to change this over to a GraphConverter. The search results (potentially) contain nodes and edges--which
     * is really just a sub-graph of the original....
     */

    interface Tokens extends GraphMLReader.Tokens {

    }

    abstract class AbstractGraphElementConverter implements Converter {

        protected void writeTag(HierarchicalStreamWriter writer, String tag, String value) {
            writer.startNode(tag);
            writer.setValue(value);
            writer.endNode();
        }

        protected void writeDetails(HierarchicalStreamWriter writer, AbstractGraphObjectStore graph) {
            writeTag(writer, "key", graph.getKey());
            writeTag(writer, "type", graph.getType());

            Integer color = graph.getColor();
            if (color != null) {
                writeTag(writer, "color", color.toString());
            }

            if (graph.isHidden()) {
                writeTag(writer, "hidden", "true");
            }

            if (graph.isBundled()) {
                writeTag(writer, "bundled", "true");
            }

            String compositeLabel = graph.getLabel();
            if (compositeLabel.length() > 0) {
                writeTag(writer, "label", compositeLabel);
            }

            writer.startNode("types");

            Iterator<Map.Entry<String, Integer>> iter = graph.getTypes().entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Integer> entry = iter.next();
                writer.startNode(GraphMLWriter.Tokens.DATA);
                writer.addAttribute("type", entry.getKey());
                writer.setValue(entry.getValue().toString());
                writer.endNode();
            }
            writer.endNode();

            int nestedLevel = 0;
            AbstractGraphObjectStore myParent = graph.getParent();
            while (myParent != null) {
                nestedLevel += 1;
                myParent = myParent.getParent();
            }

            writeTag(writer, "nestedLevel", ((Integer) nestedLevel).toString());

            for (Map.Entry<String, Property> entry : graph.getAttributes().entrySet()) {
                Property prop = entry.getValue();
                List<Object> values = prop.getValues();

                writer.startNode(GraphMLWriter.Tokens.DATA);
                writer.addAttribute("key", prop.getName());
                for (Object o : values) {
                    if (o != null) {
                        writeTag(writer, GraphMLWriter.Tokens.DATA, o.toString());
                    }
                }
                writer.endNode();

            }
        }

        protected void writeGraphML(HierarchicalStreamWriter writer, NodeStore nodeStore) {

            writer.startNode("object");

            writer.addAttribute("type", "NodeStore");

            if (nodeStore.getIcon() != null) {
               writeTag(writer, "icon", nodeStore.getIcon());
            }
            if (nodeStore.getShape() != null) {
               writeTag(writer, "shape", nodeStore.getShape());
            }
            if (nodeStore.isAnchored()) {
               writeTag(writer, "anchored", "true");
            }
            if (nodeStore.isHideLabels()) {
                writeTag(writer, "hideLabels", "false");
            }
            writeDetails(writer, nodeStore);
            writer.endNode();
        }

    }

    class NodeConverter extends AbstractGraphElementConverter {

        @Override
        public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {

            Node node = (Node) o;
            NodeStore details = GraphManager.getNodeDetails(node);
            int columnCount = node.getColumnCount();
            writer.startNode(Tokens.NODE);
            {
                if (!details.isBundled() && (columnCount > 0)) {
                    for (int i = 0; i < columnCount; i++) {
                        String name = node.getColumnName(i);
                        if (!name.equals(GraphConstants.NODE_DETAIL)) {
                            writer.startNode(Tokens.DATA);
                            writer.addAttribute(Tokens.KEY, name);
                            writer.setValue(node.getString(name));
                            writer.endNode();
                        }
                    }

                    writeGraphML(writer, details);
                }
            }

            writer.endNode();

            // TODO: should we convert children as well?

        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            /*
             * NO-OP -- no need to handle receiving a 'node' right now.
             */
            return null;
        }

        @Override
        public boolean canConvert(Class clazz) {
            return (Node.class.isAssignableFrom(clazz));
        }

    }

    class EdgeConverter extends AbstractGraphElementConverter {

        @Override
        public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
            // TODO Auto-generated method stub

        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            /*
             * NO-OP -- no need to handle receiving a 'link'
             */
            return null;
        }

        @Override
        public boolean canConvert(Class clazz) {
            return (Edge.class.isAssignableFrom(clazz));
        }

    }

}
