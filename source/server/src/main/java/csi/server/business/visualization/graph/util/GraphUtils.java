package csi.server.business.visualization.graph.util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import prefuse.Visualization;
import prefuse.action.layout.CircleLayout;
import prefuse.action.layout.Layout;
import prefuse.action.layout.RandomLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.GridLayout2;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableNodeItem;

import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.layout.CircularLayout;
import csi.server.common.model.visualization.graph.GraphConstants;

public class GraphUtils {

    private GraphConstants.eLayoutAlgorithms m_layout;

    public GraphUtils(GraphConstants.eLayoutAlgorithms layout) {
        m_layout = layout;
    }

   public static List<Graph> getComponents(Graph graph) {
      return (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);
   }

    /**
     * This method expects a graph that contains connected subgraphs--as computed by the computeComponents() method.
     * Each connected graph is extracted from the original graph due to behavior in the underlying data representations.
     * <p>
     * The
     *
     * @param graph
     */
    @SuppressWarnings("unchecked")
    public void layout(Graph graph) {
        Layout layout = null;
        Visualization viz = (Visualization) graph.getClientProperty(GraphConstants.VISUALIZATION);
        Integer componentCount = (Integer) graph.getClientProperty(GraphConstants.COMPONENT_COUNT);
        int howMany = componentCount.intValue();

        for (int i = 0; i < howMany; i++) {
            String componentName = "component_" + i;

            // set anchored nodes
            VisualGraph visualGraph = (VisualGraph) viz.getVisualGroup(componentName);
            Iterator<Tuple> iterator = visualGraph.nodes();

            while (iterator.hasNext()) {
                TableNodeItem tni = (TableNodeItem) iterator.next();

                if (tni.getBoolean(VisualItem.VISIBLE)) {
                    NodeStore nodeStore = (NodeStore) tni.get("object");
                    tni.setFixed(nodeStore.isAnchored());
                }
            }

            if (m_layout == GraphConstants.eLayoutAlgorithms.forceDirected) {
                layout = new ForceDirectedLayout(componentName, false, true);
                ((ForceDirectedLayout) layout).setIterations(100);
            } else if (m_layout == GraphConstants.eLayoutAlgorithms.circular) {
                layout = new CircularLayout(componentName);
            } else if (m_layout == GraphConstants.eLayoutAlgorithms.circle) {
                layout = new CircleLayout(componentName);
            } else if (m_layout == GraphConstants.eLayoutAlgorithms.treeRadial) {
                layout = new RadialTreeLayout(componentName);
            } else if (m_layout == GraphConstants.eLayoutAlgorithms.treeNodeLink) {
                layout = new NodeLinkTreeLayout(componentName);
            } else if (m_layout == GraphConstants.eLayoutAlgorithms.scramble) {
                layout = new RandomLayout(componentName);
            } else if (m_layout == GraphConstants.eLayoutAlgorithms.grid) {
                layout = new GridLayout2(componentName);
            }
            if (layout != null) {
               layout.setVisualization(viz);
               layout.setLayoutBounds(new Rectangle2D.Double(0, 0, GraphConstants.DIMENSION_WIDTH, GraphConstants.DIMENSION_WIDTH));
               layout.setLayoutAnchor(new Point2D.Double(0.5 * GraphConstants.DIMENSION_WIDTH, 0.5 * GraphConstants.DIMENSION_WIDTH));
               layout.setGroup(componentName);

            /*
             * Note: we need to simulate the iterations here. if we try the all at once layout (i.e. runOnce flag on
             * f-d) anchored positions aren't applied till after all iterations are run. The resulting graph looked like
             * a standard f-d run but w/ all the elected nodes 'pinned' to one location.
             *
             * In actuality the 'pinned' look is because all of the other nodes were pushed away
             */
               if (m_layout == GraphConstants.eLayoutAlgorithms.forceDirected) {
                  layout.run(0.0);
               } else {
                  layout.run(0.0);
               }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void layoutComponentSpace(Graph graph) {
        int componentCount = (Integer) graph.getClientProperty(GraphConstants.COMPONENT_COUNT);
        List<Graph> subGraphs = (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);
        Visualization viz = (Visualization) graph.getClientProperty(GraphConstants.VISUALIZATION);
        int totalNodes = 0;

        // get number of nodes in each component
        for (int i = 0; i < componentCount; i++) {
            String componentName = "component_" + i;
            VisualGraph visualGraph = (VisualGraph) viz.getVisualGroup(componentName);
            TupleSet nodes = visualGraph.getNodes();
            int nodeCount = nodes.getTupleCount();
            subGraphs.get(i).putClientProperty(GraphConstants.NODE_COUNT, nodeCount);
            totalNodes += nodeCount;
        }

        int coordSpaceDimension = ((Double) (10d * Math.sqrt(totalNodes))).intValue();
        int lowerXCoord = 0;
        int lowerYCoord = 0;
        int upperXCoord = 0;

        // find the largest unvisited component
        boolean visited[] = new boolean[componentCount];

        while (true) {
            int maxComp = -1;

            for (int i = 0; i < componentCount; i++) {
                if (!visited[i]) {
                    int numNodes = (Integer) subGraphs.get(i).getClientProperty(GraphConstants.NODE_COUNT);
                    if (numNodes > 0) {
                        if (maxComp == -1) {
                           maxComp = i;
                        } else if (numNodes > (Integer) subGraphs.get(maxComp).getClientProperty(GraphConstants.NODE_COUNT)) {
                           maxComp = i;
                        }
                    }
                }
            }
            if (maxComp == -1) {
               break;
            }
            visited[maxComp] = true;

            double fractionOfWhole = Math.sqrt(((Integer) subGraphs.get(maxComp).getClientProperty(GraphConstants.NODE_COUNT)).doubleValue() / totalNodes);
            int patchDimension = ((Double) (coordSpaceDimension * fractionOfWhole)).intValue();

            // exceeded total window height => shift to next column
            if ((lowerYCoord + patchDimension) > coordSpaceDimension) {
                lowerYCoord = 0;
                lowerXCoord = upperXCoord;
                upperXCoord += patchDimension;
            } else if (upperXCoord == 0) {
                upperXCoord = patchDimension;
            }

            subGraphs.get(maxComp).putClientProperty(GraphConstants.LOWER_XCOORD, lowerXCoord);
            subGraphs.get(maxComp).putClientProperty(GraphConstants.LOWER_YCOORD, lowerYCoord);
            subGraphs.get(maxComp).putClientProperty(GraphConstants.PATCH_DIMENSION, patchDimension);

            lowerYCoord += patchDimension;
        }
    }

    @SuppressWarnings("unchecked")
    public void transformCoordinateSpace(Graph graph) {
        /*
         * int componentCount = (Integer)graph.getClientProperty( COMPONENT_COUNT ); List<Graph> subGraphs = (List<Graph>)graph.getClientProperty(
         * COMPONENTS ); Visualization viz = (Visualization)graph.getClientProperty( VISUALIZATION );
         *
         * for( int i = 0; i < componentCount; i++ ) { String componentName = "component_" + i; VisualGraph visualGraph =
         * (VisualGraph)viz.getVisualGroup( componentName );
         *
         * double vizLowerX = Double.MAX_VALUE; double vizLowerY = Double.MAX_VALUE; double vizUpperX =
         * -Double.MIN_VALUE; double vizUpperY = -Double.MIN_VALUE;
         *
         * Iterator iterator = visualGraph.nodes(); while( iterator.hasNext() ) { TableNodeItem tni =
         * (TableNodeItem)iterator.next(); if( tni.getBoolean( VisualItem.VISIBLE ) ) { double x = tni.getX(); if(
         * Double.isNaN( x ) ) { x = 0.0; tni.setX( 0.0 ); } if( x < vizLowerX ) vizLowerX = x; if( x > vizUpperX )
         * vizUpperX = x;
         *
         * double y = tni.getY(); if( Double.isNaN( y ) ) { y = 0.0; tni.setY( 0.0 ); } if( y < vizLowerY ) vizLowerY =
         * y; if( y > vizUpperY ) vizUpperY = y; } }
         *
         * double patchLowerX = (Integer)subGraphs.get( i ).getClientProperty( LOWER_XCOORD ); double patchLowerY =
         * (Integer)subGraphs.get( i ).getClientProperty( LOWER_YCOORD ); double patchDimension =
         * (Integer)subGraphs.get( i ).getClientProperty( PATCH_DIMENSION ); // create gaps around patch double gap =
         * 0.1 * patchDimension; patchLowerX += gap; patchLowerY += gap; patchDimension -= 2.0 * gap;
         *
         * double vizDimension = vizUpperX - vizLowerX; double xScaleFactor = patchDimension / ( vizDimension == 0 ? 0.1 :
         * vizDimension ); vizDimension = vizUpperY - vizLowerY; double yScaleFactor = patchDimension / ( vizDimension ==
         * 0 ? 0.1 : vizDimension );
         *
         * double scaling = Math.min( xScaleFactor, yScaleFactor ); // maintain round circle if( m_layout ==
         * eLayoutAlgorithms.circular || m_layout == eLayoutAlgorithms.circle ) { xScaleFactor = yScaleFactor = scaling; }
         * iterator = visualGraph.nodes(); while( iterator.hasNext() ) { TableNodeItem tni =
         * (TableNodeItem)iterator.next(); if( tni.getBoolean( VisualItem.VISIBLE ) ) { double x = tni.getX(); double y =
         * tni.getY(); // transform original coordinates to patch space x = ( x - vizLowerX ) * xScaleFactor +
         * patchLowerX; y = ( y - vizLowerY ) * yScaleFactor + patchLowerY; // store coordinates in NodeStore NodeStore
         * nodeStore = (NodeStore)tni.get( "object" ); Point point = new Point(); point.setLocation( x, y );
         * nodeStore.setPosition( m_layout, point ); } } }
         */

    }

    @SuppressWarnings("unchecked")
    public void retrieveCoordinates(Graph graph) {
        /*
         * int componentCount = (Integer)graph.getClientProperty( COMPONENT_COUNT ); Visualization viz =
         * (Visualization)graph.getClientProperty( VISUALIZATION );
         *
         * for( int i = 0; i < componentCount; i++ ) { String componentName = "component_" + i; VisualGraph visualGraph =
         * (VisualGraph)viz.getVisualGroup( componentName );
         *
         * Iterator iterator = visualGraph.nodes(); while( iterator.hasNext() ) { TableNodeItem tni =
         * (TableNodeItem)iterator.next(); if( tni.getBoolean( VisualItem.VISIBLE ) ) { NodeStore nodeStore =
         * (NodeStore)tni.get( "object" ); Point point = nodeStore.getPosition( m_layout ); if( point != null ) {
         * tni.setX( point.x ); tni.setY( point.y ); } } } }
         */
    }

    /*
     * Centrifuge placement algorithm: take the most highly connected nodes in a component and place them on a circle,
     * anchor them while running Force Directed or Fruchterman Reingold layout
     */
    @SuppressWarnings("unchecked")
    public void centrifuge(Graph graph) {
        int componentCount = (Integer) graph.getClientProperty(GraphConstants.COMPONENT_COUNT);
        Visualization viz = (Visualization) graph.getClientProperty(GraphConstants.VISUALIZATION);

        // TODO handle case when user pre-selects nodes to centrifuge

        // find most highly connected nodes
        for (int i = 0; i < componentCount; i++) {
            String componentName = "component_" + i;
            VisualGraph visualGraph = (VisualGraph) viz.getVisualGroup(componentName);

            // count neighbors
            int nodeCount = 0;
            int totalNeighbors = 0;
            Iterator<Tuple> iterator = visualGraph.nodes();

            while (iterator.hasNext()) {
                TableNodeItem tni = (TableNodeItem) iterator.next();

                if (tni.getBoolean(VisualItem.VISIBLE)) {
                    NodeStore nodeStore = (NodeStore) tni.get("object");
                    totalNeighbors += nodeStore.calcVisibleNeighbors(tni);
                    nodeCount++;
                }
            }

            // calculate threshold for nomination
            double threshold = (1.75 * totalNeighbors) / nodeCount; // TODO 1.75
            // need not
            // be
            // hard-coded
            // - let
            // user set
            // it?

            // choose the nodes with number of neighbors greater than threshold
            List<TableNodeItem> selectedNodes = new ArrayList<TableNodeItem>();
            iterator = visualGraph.nodes();
            while (iterator.hasNext()) {
                TableNodeItem tni = (TableNodeItem) iterator.next();
                if (tni.getBoolean(VisualItem.VISIBLE)) {
                    NodeStore nodeStore = (NodeStore) tni.get("object");
                    if (nodeStore.getNumVisibleNeighbors() >= threshold) {
                        selectedNodes.add(tni);
                        // tni.setFixed(true);
                        nodeStore.setAnchored(true); // TODO this needs to be
                        // unset as it's
                        // temporary
                        // } else {
                        // tni.setFixed(false);
                    }
                }
            }

            // must have at least two chosen nodes
            double selectedSize = selectedNodes.size();
            if (selectedSize < 2) {
               continue;
            }

            // equally space selected nodes around circle
            double radius = 100d * Math.sqrt(selectedSize);
            double center = 0.5 * GraphConstants.DIMENSION_WIDTH;
            double ithNode = 0;
            for (TableNodeItem tni : selectedNodes) {
                double angle = (ithNode / selectedSize) * 2d * Math.PI;
                tni.setX((radius * Math.cos(angle)) + center);
                tni.setY((radius * Math.sin(angle)) + center);
                ithNode++;
            }
        }
    }

    public GraphConstants.eLayoutAlgorithms getLayout() {
        return m_layout;
    }
}
