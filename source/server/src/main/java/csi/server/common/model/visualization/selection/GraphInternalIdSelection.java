package csi.server.common.model.visualization.selection;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.collect.Multimap;

import csi.server.business.selection.Pair;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.model.visualization.graph.GraphConstants;

/**
 * Maintains selections across different graph instantiations by storing nodes and edges by names.
 * Provides a way to get back and forth between a SelectionModel, which is actually used by the graph.
 * @author Centrifuge Systems, Inc.
 */
public class GraphInternalIdSelection implements Selection {

    private final Set<String> ids = new HashSet<String>();
    private final Set<Pair> edges = new HashSet<Pair>();

    private void addId(String id){
        ids.add(id);
    }

    private void addEdge(Pair pair){
        edges.add(pair);
    }

    public Set<String> getIds() {
        return ids;
    }

    @Override
    public boolean isCleared() {
        return ids.isEmpty();
    }

    @Override
    public void clearSelection() {
        ids.clear();
    }

    @Override
    public void setFromSelection(Selection selection) {

    }

    public SelectionModel createSelectionModel(Graph graph){
        SelectionModel selectionModel = new SelectionModel();

        Map<String, Node> idNodeMap = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        addNodesToSelectionModel(selectionModel, idNodeMap);

        Multimap<String, Edge> linksByDef = (Multimap<String, Edge>) graph.getClientProperty(GraphManager.LINKS_BY_DEFINITION);
        if(linksByDef == null){
            return selectionModel;
        }
        addEdgesToSelectionModel(selectionModel, idNodeMap, linksByDef);

        return selectionModel;
    }

    private void addNodesToSelectionModel(SelectionModel selectionModel, Map<String, Node> idNodeMap) {
        for(String id : getIds()){
            Node node = idNodeMap.get(id);
            if(node != null)
                selectionModel.nodes.add(node.getRow());
        }
    }

    private void addEdgesToSelectionModel(SelectionModel selectionModel, Map<String, Node> idNodeMap, Multimap<String, Edge> linksByDef) {
        for(Pair pair : edges){
            Node sourceNode = idNodeMap.get(pair.x);
            Node targetNode = idNodeMap.get(pair.y);

            if(sourceNode != null && targetNode != null) {
                String edgeKey = sourceNode.getRow() + "+" + targetNode.getRow();
                for (Edge edge : linksByDef.values()) {
                    if (GraphManager.getEdgeDetails(edge).getKey().equals(edgeKey)) {
                        selectionModel.links.add(edge.getRow());
                    }
                }
            }
        }
    }

    public static GraphInternalIdSelection createGraphInternalIdSelection(SelectionModel selectionModel, Graph graph){
        GraphInternalIdSelection graphInternalIdSelection = new GraphInternalIdSelection();

        addNodeIdsToInternalSelection(selectionModel, graph, graphInternalIdSelection);
        addEdgesToInternalSelection(selectionModel, graph, graphInternalIdSelection);

        return graphInternalIdSelection;
    }

    private static void addNodeIdsToInternalSelection(SelectionModel selectionModel, Graph graph, GraphInternalIdSelection graphInternalIdSelection) {
        for(Integer id : selectionModel.nodes){
            Node node = graph.getNode(id);
            NodeStore details = (NodeStore) node.get(GraphConstants.NODE_DETAIL);
            graphInternalIdSelection.addId(details.getKey());
        }
    }

    private static void addEdgesToInternalSelection(SelectionModel selectionModel, Graph graph, GraphInternalIdSelection graphInternalIdSelection) {
        for(Integer id : selectionModel.links){
            Edge edge = graph.getEdge(id);
            NodeStore sourceDetails = (NodeStore)(edge.getSourceNode().get(GraphConstants.NODE_DETAIL));
            NodeStore targetDetails = (NodeStore)(edge.getTargetNode().get(GraphConstants.NODE_DETAIL));

            Pair pair = new Pair(sourceDetails.getKey(), targetDetails.getKey());
            graphInternalIdSelection.addEdge(pair);
        }
    }

    @Override
	public Selection copy() {
    	GraphInternalIdSelection selection = new GraphInternalIdSelection();
		selection.setFromSelection(this);
		return selection;
	}
}
