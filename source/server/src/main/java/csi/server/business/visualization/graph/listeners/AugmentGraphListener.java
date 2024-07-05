package csi.server.business.visualization.graph.listeners;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphProcessorListener;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.selection.SelectionModel;

public class AugmentGraphListener implements GraphProcessorListener {
    private GraphContext context;
    private VisualGraph vgraph;
    private boolean makeVisible;
    private SelectionModel newNodes;
    private SelectionModel updatedNodes;
    private Set<Integer> createdNodes;
    private Set<Integer> createdEdges;

    public AugmentGraphListener(GraphContext context, boolean makeVisible) {
        this.context = context;
        this.makeVisible = makeVisible;
        this.createdNodes = new HashSet<Integer>();
        this.createdEdges = new HashSet<Integer>();

        if (context != null) {
            vgraph = context.getVisualGraph();

//            this.newNodes = this.context.getSelection(GraphConstants.NEW_GENERATION);
//            if (this.newNodes == null) {
                this.newNodes = new SelectionModel();
                this.context.addSelectionModel(GraphConstants.NEW_GENERATION, this.newNodes);
//            }

//            this.updatedNodes = this.context.getSelection(GraphConstants.UPDATED_GENERATION);
//            if (this.updatedNodes == null) {
                this.updatedNodes = new SelectionModel();
                this.context.addSelectionModel(GraphConstants.UPDATED_GENERATION, this.updatedNodes);
//            }
        }
    }

    public void handleNode(Node node, boolean exists) {
        int nodeId = node.getRow();
        if (createdNodes.contains(nodeId)) {
            return;
        }

        NodeStore nodeStore = GraphManager.getNodeDetails(node);
        String hilightLabel = (exists) ? "existing" : "created";
        Map<String, Property> attributes = nodeStore.getAttributes();

        Property property = attributes.get(AbstractGraphObjectStore.HIGHLIGHT_PROPERTY);
        if (property == null) {
            property = new Property(AbstractGraphObjectStore.HIGHLIGHT_PROPERTY);
            property.setIncludeInTooltip(false);
            attributes.put(AbstractGraphObjectStore.HIGHLIGHT_PROPERTY, property);
            property.getValues().add(hilightLabel);
        }

        if (vgraph != null) {
            NodeItem visual = (NodeItem) vgraph.getNode(nodeId);
            if (!exists) {
                if (makeVisible) {
                    visual.setBoolean(GraphContext.IS_VISUALIZED, true);
                    visual.setVisible(true);
                }

                newNodes.nodes.add(nodeId);
                createdNodes.add(nodeId);
            } else {
                updatedNodes.nodes.add(nodeId);
                newNodes.nodes.remove(nodeId);
            }
        }
    }

    public boolean isNewNode(Node node) {
        return createdNodes.contains(node.getRow());
    }


    public void handleEdge(Edge edge, boolean exists) {

        if (createdEdges.contains(edge.getRow())) {
            return;
        }
        String highlightLabel = (exists) ? "existing" : "created";
        LinkStore linkStore = GraphManager.getEdgeDetails(edge);
        Map<String, Property> attributes = linkStore.getAttributes();
        Property property = attributes.get(AbstractGraphObjectStore.HIGHLIGHT_PROPERTY);
        if (property == null) {
            property = new Property(AbstractGraphObjectStore.HIGHLIGHT_PROPERTY);
            property.setIncludeInTooltip(false);
            attributes.put(AbstractGraphObjectStore.HIGHLIGHT_PROPERTY, property);
            property.getValues().add(highlightLabel);

            GraphManager.setLinkDetails(edge, linkStore);

        }

        if (vgraph != null) {

            EdgeItem visual = (EdgeItem) vgraph.getEdge(edge.getRow());
            if(!exists) {
                if (makeVisible && linkStore.isDisplayable()) {
                    visual.setBoolean(GraphContext.IS_VISUALIZED, true);
                    visual.setVisible(true);
                }
                newNodes.links.add(edge.getRow());
                createdEdges.add(edge.getRow());
            } else {
                updatedNodes.links.add(edge.getRow());
                newNodes.links.remove(edge.getRow());
            }
        }
    }

    @Override
    public boolean isNewEdge(Edge edge) {
        return createdEdges.contains(edge.getRow());
    }
}
