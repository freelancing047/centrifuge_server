package csi.server.business.visualization.graph.plunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mongodb.DBObject;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import csi.graph.AbstractStorageService;
import csi.graph.GraphStorage;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.data.EdgeToDataTransformer;
import csi.server.business.visualization.graph.data.NodeToDataTransformer;
import csi.server.business.visualization.graph.grouping.UnGroupNodesCommand;
import csi.server.common.dto.graph.gwt.PlunkedItemsToDeleteDTO;
import csi.server.common.model.visualization.graph.PlunkedLink;
import csi.server.common.model.visualization.graph.PlunkedNode;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.dao.CsiPersistenceManager;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PlunkDeleter {
   private static final Logger LOG = LogManager.getLogger(PlunkDeleter.class);

    public PlunkedItemsToDeleteDTO delete(String vizUuid, String itemKey, boolean isEdge) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

        if (isEdge) {
         return removeEdge(graphContext, vizUuid, itemKey);
      }

        return removeNode(graphContext, vizUuid, itemKey);

    }

    public List<String> deleteAll(RelGraphViewDef relGraphViewDef) {
        List<String> nodesKeys = getAllNodeKeys(relGraphViewDef.getPlunkedNodes());
        List<String> edgesKeys = getAllEdgeKeys(relGraphViewDef.getPlunkedLinks());

        relGraphViewDef.getPlunkedNodes().clear();
        relGraphViewDef.getPlunkedLinks().clear();

        PlunkDeleter plunkDeleter = new PlunkDeleter();
        for (String nodeKey : nodesKeys) {
            plunkDeleter.delete(relGraphViewDef.getUuid(), nodeKey, false);
        }
        for (String edgeKey : edgesKeys) {
            plunkDeleter.delete(relGraphViewDef.getUuid(), edgeKey, true);
        }

        nodesKeys.addAll(edgesKeys);
        return nodesKeys;
    }

    private PlunkedItemsToDeleteDTO removeEdge(GraphContext graphContext, String vizUuid, String itemKey) {
        Graph graph = graphContext.getVisibleGraph();
        Map<String, Edge> idEdgeMap = graphContext.getEdgeKeyIndex();
        Edge edge = idEdgeMap.remove(itemKey);
        if (edge != null) {
            removeEdgeFromSelection(graphContext, edge);
            removeEdgeFromMongo(graphContext, edge);
            removeEdgeFromVisualGraph(graph, edge);
        }
        return removePlunkedLinkFromRelGraph(vizUuid, itemKey);
    }

    private void removeEdgeFromSelection(GraphContext graphContext, Edge edge) {
        SelectionModel selection = graphContext.getSelection(GraphManager.DEFAULT_SELECTION);
        selection.links.remove(edge.getRow());
    }

    private PlunkedItemsToDeleteDTO removePlunkedLinkFromRelGraph(String vizUuid, String itemKey) {
        RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);
        PlunkedLink plunkedLink = findPlunkedLinkDef(relGraphViewDef, itemKey);
        if (plunkedLink == null) {
         return null;
      }

        PlunkedItemsToDeleteDTO dto = createItemsToDeleteDTO(null, new ArrayList<PlunkedLink>(Arrays.asList(plunkedLink)));

        relGraphViewDef.getPlunkedLinks().remove(plunkedLink);
        CsiPersistenceManager.merge(relGraphViewDef);

        return dto;
    }

    private PlunkedLink findPlunkedLinkDef(RelGraphViewDef relGraphViewDef, String itemKey) {
        for (PlunkedLink plunkedLink : relGraphViewDef.getPlunkedLinks()) {
            if (plunkedLink.buildItemKey().equals(itemKey)) {
                return plunkedLink;
            }
        }
        return null;
    }

    private void removeEdgeFromVisualGraph(Graph graph, Edge edge) {
        edge.setBoolean(GraphContext.IS_VISUALIZED, false);
        graph.removeEdge(edge);
    }

    private void removeEdgeFromMongo(GraphContext graphContext, Edge edge) {
    	AbstractStorageService service = AbstractStorageService.instance();
        GraphStorage storage = service.getGraphStorage(graphContext.getVizUuid());
        if(storage == null){
            storage = service.createEmptyStorage(graphContext.getVizUuid());
        }

        EdgeToDataTransformer edgeToDataTransformer = new EdgeToDataTransformer();
        edgeToDataTransformer.setGraphStorage(storage);

        DBObject data = edgeToDataTransformer.apply(edge);
        storage.removeEdge(data);
        service.saveGraphStorage(graphContext.getVizUuid(), storage);
    }

    @SuppressWarnings("unchecked")
    private PlunkedItemsToDeleteDTO removeNode(GraphContext graphContext, String vizUuid, String itemKey) {
        Graph graph = graphContext.getVisibleGraph();
        Map<String, Node> idNodeMap = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        Node node = idNodeMap.remove(itemKey);
        removeEmptyBundles(graph, node);
        removeNodeFromSelection(graphContext, node);
        removeNodeFromMongo(graphContext, node);
        removeIncidentEdgesFromMongo(graphContext, node);
        removeFromVisualGraph(graph, node);
        return removePlunkedNodeFromRelGraph(vizUuid, itemKey);
    }

    private void removeEmptyBundles(Graph graph, Node node) {
        NodeStore nodeDetails = GraphManager.getNodeDetails(node);
        NodeStore bundleDetails = (NodeStore) nodeDetails.getParent();

        if (bundleDetails == null) {
         return;
      }

        bundleDetails.removeChild(nodeDetails);
        if (bundleDetails.getChildren().isEmpty()) {
            Map<String, Node> idNodeMap = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
            Node bundleNode = idNodeMap.get(bundleDetails.getKey());
            try {
                UnGroupNodesCommand command = new UnGroupNodesCommand();
                command.setGraph(graph);
                command.setNodes(new ArrayList(Arrays.asList(bundleNode)));
                command.call();
            } catch (Exception e) {
            }
        }

    }

    private void removeNodeFromSelection(GraphContext graphContext, Node node) {
        SelectionModel selection = graphContext.getSelection(GraphManager.DEFAULT_SELECTION);
        selection.nodes.remove(node.getRow());
    }

    private void removeIncidentEdgesFromMongo(GraphContext graphContext, Node node) {
        Iterator edges = node.edges();
        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();
            removeEdgeFromMongo(graphContext, edge);
        }
    }

    private void removeFromVisualGraph(Graph graph, Node node) {
        node.setBoolean(GraphContext.IS_VISUALIZED, false);
        graph.removeNode(node);
    }

    private void removeNodeFromMongo(GraphContext graphContext, Node node) {
    	AbstractStorageService service = AbstractStorageService.instance();
        GraphStorage storage = service.getGraphStorage(graphContext.getVizUuid());
        if(storage == null){
            storage = service.createEmptyStorage(graphContext.getVizUuid());
        }
        NodeToDataTransformer nodeTransform = new NodeToDataTransformer();
        nodeTransform.setGraphStorage(storage);
        DBObject data = nodeTransform.apply(node);
        boolean removed = storage.removeVertex(data);
        if(!removed){
           LOG.error("Could not remove plunked item from storage");
        }
        service.saveGraphStorage(graphContext.getVizUuid(), storage);
    }

    private PlunkedItemsToDeleteDTO removePlunkedNodeFromRelGraph(String vizUuid, String itemKey) {
        RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);
        PlunkedNode plunkedNode = findPlunkedNodeDef(relGraphViewDef, itemKey);
        if (plunkedNode == null) {
         return null;
      }

        Collection<PlunkedLink> incidentLinks = findLinksAttachedToNode(relGraphViewDef, itemKey);

        PlunkedItemsToDeleteDTO dto = createItemsToDeleteDTO(plunkedNode, incidentLinks);

        relGraphViewDef.getPlunkedNodes().remove(plunkedNode);
        relGraphViewDef.getPlunkedLinks().removeAll(incidentLinks);
        CsiPersistenceManager.merge(relGraphViewDef);

        return dto;
    }

    private PlunkedItemsToDeleteDTO createItemsToDeleteDTO(PlunkedNode plunkedNode, Collection<PlunkedLink> incidentLinks) {
        PlunkedItemsToDeleteDTO dto = new PlunkedItemsToDeleteDTO();
        if (plunkedNode != null) {
         dto.getNodesToDelete().add(plunkedNode);
      }
        dto.getLinksToDelete().addAll(incidentLinks);
        return dto;
    }

    private Collection<PlunkedLink> findLinksAttachedToNode(RelGraphViewDef relGraphViewDef, String itemKey) {
        List<PlunkedLink> plunkedLinks = new ArrayList<PlunkedLink>();
        for (PlunkedLink plunkedLink : relGraphViewDef.getPlunkedLinks()) {
            if (plunkedLink.buildItemKey().contains(itemKey)) {
                plunkedLinks.add(plunkedLink);
            }
        }
        return plunkedLinks;
    }

    private PlunkedNode findPlunkedNodeDef(RelGraphViewDef relGraphViewDef, String itemKey) {
        for (PlunkedNode plunkedNode : relGraphViewDef.getPlunkedNodes()) {
            if (plunkedNode.getNodeKey().equals(itemKey)) {
                return plunkedNode;
            }
        }
        return null;
    }


    private List<String> getAllEdgeKeys(List<PlunkedLink> plunkedLinks) {
        List<String> keys = new ArrayList<String>();
        for (PlunkedLink plunkedLink : plunkedLinks) {
            keys.add(plunkedLink.buildItemKey());
        }

        return keys;
    }

    private List<String> getAllNodeKeys(List<PlunkedNode> plunkedNodes) {
        List<String> keys = new ArrayList<String>();
        for (PlunkedNode plunkedNode : plunkedNodes) {
            keys.add(plunkedNode.getNodeKey());
        }
        return keys;
    }
}
