package csi.server.business.visualization.graph.base;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.Visualization;
import prefuse.action.layout.CircleLayout;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.GridLayout2;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;
import prefuse.visual.tuple.TableNodeItem;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.layout.CircularLayout;
import csi.server.common.model.visualization.graph.GraphConstants;

public class RunPlacement implements Callable<Void> {
   private static final Logger LOG = LogManager.getLogger(RunPlacement.class);

    // FIXME: create factories for each layout and associated properties for
    // each
    // layout algo!
    public static final int DIMENSION_WIDTH = 1000;

    protected Graph graph;

    protected GraphConstants.eLayoutAlgorithms algorithm;

    protected int iterations = 200;

    private Visualization _visualization;

    private Integer _numComponents;

    private List<Graph> subGraphs;

    public RunPlacement() {
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public GraphConstants.eLayoutAlgorithms getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(GraphConstants.eLayoutAlgorithms layout) {
        this.algorithm = layout;
    }

    @SuppressWarnings("unchecked")
    public Void call() {
        setup();

        Layout layout;

        VisualGraph rootVG = (VisualGraph) _visualization.getVisualGroup(GraphConstants.ROOT_GRAPH);

        for (int i = 0; i < _numComponents; i++) {

            Visualization localVis = new Visualization();

            Graph subGraph = subGraphs.get(i);
            Rectangle region = (Rectangle) subGraph.getClientProperty(GraphConstants.PATCH_REGION);

            String graphName = "tempSubGraph";
            VisualGraph visualGraph = localVis.addGraph(graphName, subGraph);

            Iterator<Tuple> iterator = visualGraph.nodes();
            while (iterator.hasNext()) {
                TableNodeItem tni = (TableNodeItem) iterator.next();
                NodeStore nodeStore = GraphManager.getNodeDetails(tni);
                tni.set(VisualItem.VISIBLE, nodeStore.isDisplayable());
                tni.setFixed(nodeStore.isAnchored());

                VisualItem existingNode = (VisualItem) rootVG.getNode(tni.getInt(GraphConstants.ORIG_NODE_ID));
                tni.setX(existingNode.getX());
                tni.setY(existingNode.getY());
            }

            iterator = visualGraph.edges();
            while (iterator.hasNext()) {
                TableEdgeItem tei = (TableEdgeItem) iterator.next();
                LinkStore linkStore = GraphManager.getEdgeDetails(tei);
                tei.set(VisualItem.VISIBLE, linkStore.isDisplayable());
            }

            // FIXME: add in layout factory pattern -- longer term
            // support customer layouts
            if (algorithm == GraphConstants.eLayoutAlgorithms.forceDirected) {
                layout = new ForceDirectedLayout(graphName, false, true);
                ((ForceDirectedLayout) layout).setIterations(200);
            } else if (algorithm == GraphConstants.eLayoutAlgorithms.circular) {
                layout = new CircularLayout(graphName);
            } else if (algorithm == GraphConstants.eLayoutAlgorithms.circle) {
                layout = new CircleLayout(graphName);
            } else if (algorithm == GraphConstants.eLayoutAlgorithms.treeRadial) {
                layout = new RadialTreeLayout(graphName);
            } else if (algorithm == GraphConstants.eLayoutAlgorithms.treeNodeLink) {
                layout = new NodeLinkTreeLayout(graphName);
            } else if (algorithm == GraphConstants.eLayoutAlgorithms.grid) {
                layout = new GridLayout2(graphName);
            } else {
               LOG.warn(String.format("Unrecognized layout choice %s, default to Force Directed", algorithm.toString()));
                layout = new ForceDirectedLayout(graphName, false, true);
                algorithm = GraphConstants.eLayoutAlgorithms.forceDirected;
            }

            layout.setVisualization(localVis);
            layout.setLayoutBounds(new Rectangle2D.Double(0, 0, DIMENSION_WIDTH, DIMENSION_WIDTH));
            layout.setLayoutAnchor(new Point2D.Double(0.5 * DIMENSION_WIDTH, 0.5 * DIMENSION_WIDTH));
            layout.setGroup(graphName);

            layout.run(0.0);
            Rectangle2D mapRegion = new Rectangle2D.Double();

            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;

            iterator = visualGraph.nodes();
            if (iterator.hasNext()) {
                VisualItem vi = (VisualItem) iterator.next();
                double viX = Double.isNaN(vi.getX()) ? region.getCenterX() : vi.getX();
                double viY = Double.isNaN(vi.getY()) ? region.getCenterY() : vi.getY();

                mapRegion = new Rectangle2D.Double(viX, viY, 0, 0);
                mapRegion.add(viX, viY);
                minX = viX;
                double maxX = viX;
                minY = viY;
                double maxY = viY;

                while (iterator.hasNext()) {
                    vi = (VisualItem) iterator.next();
                    double vix = vi.getX();
                    double viy = vi.getY();

                    mapRegion.add(vix, viy);

                    if (vix < minX) {
                        minX = vix;
                    }
                    if (vix > maxX) {
                        maxX = vix;
                    }

                    if (viy < minY) {
                        minY = viy;
                    }

                    if (viy > maxY) {
                        maxY = viy;
                    }
                }
            }

            double gap = region.height * 0.1;

            double offX = region.x + (int) gap;
            double offY = region.y + (int) gap;

            double offDimension = region.width - (2 * gap);

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

            double scaleX = offDimension / mapWidth;
            double scaleY = offDimension / mapHeight;
            if (algorithm == GraphConstants.eLayoutAlgorithms.circular) {
                scaleX = scaleY = Math.min(scaleX, scaleY);
            }

            // now copy out the locations into the root visualization graph
            iterator = visualGraph.nodes();
            while (iterator.hasNext()) {
                VisualItem vi = (VisualItem) iterator.next();
                Tuple sourceTuple = vi.getSourceTuple();
                NodeStore nodeStore = GraphManager.getNodeDetails(sourceTuple);

                Point position = new Point();

                // account for singular nodes and/or positions that are not defined!
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

        return null;

    }

    // TODO: refactor for common graph operations
    @SuppressWarnings("unchecked")
    private void setup() {
        _visualization = (Visualization) graph.getClientProperty(Constants.VISUALIZATION);
        _numComponents = (Integer) graph.getClientProperty(Constants.COMPONENT_COUNT);

        subGraphs = (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);

    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int value) {
        if (value > 0) {
            this.iterations = value;
        }
    }

}
