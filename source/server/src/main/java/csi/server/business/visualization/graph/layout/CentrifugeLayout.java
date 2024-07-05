package csi.server.business.visualization.graph.layout;

import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.PrefuseLib;
import prefuse.util.force.ForceSimulator;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.ElectCentrifugeNodes;
import csi.server.business.visualization.graph.base.NodeStore;

public class CentrifugeLayout extends CsiForceDirectedLayout {
   private static final Logger LOG = LogManager.getLogger(CentrifugeLayout.class);

    private List<Node> electedNodes;

    public CentrifugeLayout(String group, boolean enforceBounds, boolean runonce) {
        super(group, enforceBounds, runonce);
    }

    public CentrifugeLayout(String group, ForceSimulator fsim, boolean enforceBounds, boolean runonce) {
        super(group, fsim, enforceBounds, runonce);
    }

    public List<Node> getElectedNodes() {
        return electedNodes;
    }

    public void setElectedNodes(List<Node> electedNodes) {
        this.electedNodes = electedNodes;
    }

    @Override
    public void run(double frac) {

        if ((electedNodes == null) || electedNodes.isEmpty()) {
            // centrifuge has special processing requirements....we need to
            // anchor nodes. unfortunately the layout algorithms don't
            // consult anchoring during the force simulation...
            Graph graph = getSourceGraph();
            ElectCentrifugeNodes election = new ElectCentrifugeNodes(graph);
            setElectedNodes(election.getNodes());
        }

        if (!hasAnchors()) {
           LOG.info("Centrifuge layout requires two or more nodes to operate properly");
           LOG.debug("Force Directed Layout will be used with no anchoring");
        } else {
            setElectedNodesAsFixed(true);
            arrangeNodes();
        }

        super.run(frac);

        if (hasAnchors()) {
            setElectedNodesAsFixed(false);
        }
    }

    private void arrangeNodes() {
        Rectangle2D bounds = this.getLayoutBounds();
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();
        double radius = bounds.getWidth() / 3;

        VisualGraph visualGraph = getVisualGraph();

        int numAnchors = electedNodes.size();

        for (int i = 0; i < numAnchors; i++) {
            Node node = electedNodes.get(i);
            NodeItem nodeItem = (NodeItem) visualGraph.getNode(node.getRow());

            double angle = ((double) i / (double) numAnchors) * 2d * Math.PI;
            double ax = (radius * Math.cos(angle)) + centerX;
            double ay = (radius * Math.sin(angle)) + centerY;

            PrefuseLib.setX(nodeItem, null, ax);
            PrefuseLib.setY(nodeItem, null, ay);

            nodeItem.setX(ax);
            nodeItem.setY(ay);

        }

    }

    private Graph getSourceGraph() {
        Graph graph = (Graph) m_vis.getSourceData(m_group);
        return graph;
    }

    private VisualGraph getVisualGraph() {
        VisualGraph visualGraph = (VisualGraph) m_vis.getVisualGroup(m_group);
        return visualGraph;

    }

    private void setElectedNodesAsFixed(boolean flag) {
        VisualGraph visualGraph = getVisualGraph();
        for (Node node : electedNodes) {
            NodeItem nodeItem = (NodeItem) visualGraph.getNode(node.getRow());
            nodeItem.setFixed(flag);

            NodeStore details = GraphManager.getNodeDetails(node);
            if (details.isDisplayable()) {
                details.setAnchored(flag);
            }
        }

    }

    protected boolean hasAnchors() {
        return (electedNodes != null) && (electedNodes.size() > 2);
    }

}
