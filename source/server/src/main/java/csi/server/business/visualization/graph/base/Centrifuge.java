package csi.server.business.visualization.graph.base;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import prefuse.Visualization;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.util.PrefuseLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableNodeItem;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.common.model.visualization.graph.GraphConstants;

public class Centrifuge implements Runnable {
    protected Graph graph;

    protected List<Node> electedNodes;

    protected int iterations;

    public Centrifuge() {
        // updated # of iterations for the Particle Simulation --
        // no longer need to run iterations outside of the simulator w/ updates
        // for particles not moving when locked.
        iterations = 1;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    /**
     * List of nodes elected for anchoring during a centrifuge layout.
     *
     * @return
     */
    public List<Node> getElectedNodes() {
        return electedNodes;
    }

    public void setElectedNodes(List<Node> electedNodes) {
        this.electedNodes = electedNodes;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        if ((electedNodes == null) || electedNodes.isEmpty()) {
            return;
            // run default election of nodes
        }

        if (electedNodes.size() < 2) {
            // we don't centrifuge less than two nodes!
            return;
        }

        Visualization visualization = (Visualization) graph.getClientProperty(GraphConstants.VISUALIZATION);
        VisualGraph rootVG = (VisualGraph) visualization.getVisualGroup(GraphConstants.ROOT_GRAPH);

        GraphConstants.eLayoutAlgorithms algorithm = GraphConstants.eLayoutAlgorithms.forceDirected;

        AnchorEachNode(true);

        List<NodeItem> currentNodes = new ArrayList<NodeItem>();

        Integer componentCount = (Integer) graph.getClientProperty(GraphConstants.COMPONENT_COUNT);
        List<Graph> subGraphs = (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);

        for (int i = 0; i < componentCount; i++) {
            currentNodes.clear();
            Graph subGraph = subGraphs.get(i);
            // retrieve this sub graph's patch region for the overall layout
            Rectangle region = (Rectangle) subGraph.getClientProperty(GraphConstants.PATCH_REGION);
            double ww = region.getWidth() * 5;

            Rectangle2D.Double layoutBounds = new Rectangle2D.Double(-ww, -ww, 2 * ww, 2 * ww);

            double layoutScaling = layoutBounds.width / region.width;
            if (Double.isNaN(layoutScaling)) {
                layoutScaling = 1.0;
            }

            Visualization localVis = new Visualization();

            String graphName = "tempSubGraph";
            VisualGraph visualGraph = localVis.addGraph(graphName, subGraph);

            Iterator iterator = visualGraph.nodes();
            while (iterator.hasNext()) {
                TableNodeItem tni = (TableNodeItem) iterator.next();
                NodeStore nodeStore = GraphManager.getNodeDetails(tni);
                tni.set(VisualItem.VISIBLE, nodeStore.isDisplayable());
                tni.setFixed(nodeStore.isAnchored());

                if (nodeStore.isAnchored()) {
                    currentNodes.add(tni);
                }

                Point position = nodeStore.getPosition(algorithm);
                if (position != null) {
                    PrefuseLib.setX(tni, null, position.getX() * layoutScaling);
                    PrefuseLib.setY(tni, null, position.getY() * layoutScaling);
                } else {
                    PrefuseLib.setX(tni, null, 500);
                    PrefuseLib.setY(tni, null, 500);
                }
            }

            arrangeNodes(currentNodes, layoutBounds);

            ForceDirectedLayout layout = new ForceDirectedLayout(graphName, false, true);
            layout.setVisualization(localVis);
            layout.setLayoutBounds(layoutBounds);
            layout.setLayoutAnchor(new Point2D.Double(0, 0));
            layout.setGroup(graphName);

            for (int loop = 0; loop < iterations; loop++) {
                layout.run(0.0);
            }

            Rectangle2D mapRegion = new Rectangle2D.Double();

            iterator = visualGraph.nodes();
            if (iterator.hasNext()) {
                VisualItem vi = (VisualItem) iterator.next();
                mapRegion = new Rectangle2D.Double(vi.getX(), vi.getY(), 0, 0);
                mapRegion.add(vi.getX(), vi.getY());
                while (iterator.hasNext()) {
                    vi = (VisualItem) iterator.next();
                    double vix = vi.getX();
                    double viy = vi.getY();

                    mapRegion.add(vix, viy);
                }
            }

            double minX = mapRegion.getMinX();
            double minY = mapRegion.getMinY();

            double mapWidth = mapRegion.getWidth();
            if (Double.isNaN(mapWidth) ||
                (BigDecimal.valueOf(mapWidth).compareTo(BigDecimal.ZERO) == 0)) {
                mapWidth = 0.1;
            }

            double mapHeight = mapRegion.getHeight();
            if (Double.isNaN(mapHeight) ||
                (BigDecimal.valueOf(mapHeight).compareTo(BigDecimal.ZERO) == 0)) {
                mapHeight = 0.1;
            }

            double gap = region.height * 0.1;

            double offX = region.x + (int) gap;
            double offY = region.y + (int) gap;

            double offDimension = region.width - (2 * gap);
            double scaleX = offDimension / mapWidth;
            double scaleY = offDimension / mapHeight;

            iterator = visualGraph.nodes();
            while (iterator.hasNext()) {
                VisualItem vi = (VisualItem) iterator.next();
                Tuple sourceTuple = vi.getSourceTuple();
                NodeStore nodeStore = GraphManager.getNodeDetails(sourceTuple);
                Point position = new Point();
                double x = vi.getX();
                if (Double.isNaN(x)) {
                    x = minX;
                }

                double y = vi.getY();
                if (Double.isNaN(y)) {
                    y = minY;
                }

                x = ((x - minX) * scaleX) + offX;
                y = ((y - minY) * scaleY) + offY;

                position.setLocation(x, y);
                nodeStore.setPosition(algorithm, position);

                long key = visualGraph.getKey(vi.getRow());
                VisualItem rootNI = (VisualItem) rootVG.getNodeFromKey(key);
                rootNI.setX(position.x);
                rootNI.setY(position.y);
            }
        }

        AnchorEachNode(false);
    }

    private static void arrangeNodes(List<NodeItem> nodeList, Rectangle2D bounds) {
        // need to determine circular layout relative to current coordinate
        // size.
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();
        double radius = bounds.getWidth() / 3;
        int howMany = nodeList.size();

        for (int index = 0; index < howMany; index++) {
            NodeItem node = nodeList.get(index);
            double angle = ((double) index / (double) nodeList.size()) * 2d * Math.PI;
            double ax = (radius * Math.cos(angle)) + centerX;
            double ay = (radius * Math.sin(angle)) + centerY;

            // NB: need to set prior to lib call to ensure that we start the
            // next
            // layout iteration at the anchored locations!
            // use prefuse lib to set additional coordinates...
            PrefuseLib.setX(node, null, ax);
            PrefuseLib.setY(node, null, ay);

            node.setX(ax);
            node.setY(ay);

        }
    }

    private void AnchorEachNode(boolean flag) {
        for (Node item : electedNodes) {
            NodeStore nodeStore = GraphManager.getNodeDetails(item);
            if (nodeStore.isDisplayable()) {
                nodeStore.setAnchored(flag);
            }
        }
    }

}
