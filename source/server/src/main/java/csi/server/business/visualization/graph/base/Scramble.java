package csi.server.business.visualization.graph.base;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.common.model.visualization.graph.GraphConstants;

/**
 *
 * Operation to perform a positional scramble on the provided Graph.
 *
 * <p>
 * NB: This performs a scramble on the most recent layout performed for the graph's trees/components. There is no context maintained across layouts!
 *
 *
 * @author Centrifuge Systems
 *
 */

// NB: this really doesn't do anything at this point! We currently recreate
// graphs for layout purposes
// which kind of scrambles it anyway....
public class Scramble implements Runnable {
   protected static final Logger LOG = LogManager.getLogger(Scramble.class);

    protected Graph graph;

    protected List<Graph> subGraphs;

    private Visualization _visualization;

    private int _numComponents;

    protected List<Rectangle> patchRegions;

    private VisualGraph rootVG;

    public Scramble() {
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        setup();

        Random random = new Random();
        for (int i = 0; i < _numComponents; i++) { // each component in the
            // visualization is stored as a
            // group
            Graph subGraph = subGraphs.get(i);
            Rectangle region = (Rectangle) subGraph.getClientProperty(GraphConstants.PATCH_REGION);
            double rangeX = region.getWidth();
            double rangeY = region.getHeight();
            Iterator<Tuple>iterator = subGraph.nodes();

            while (iterator.hasNext()) {
                Node node = (Node) iterator.next();
                VisualItem vi = (VisualItem) rootVG.getNode(node.getInt(GraphConstants.ORIG_NODE_ID));
                NodeStore store = GraphManager.getNodeDetails(node);
                // randomization
                if (!store.isAnchored() || vi.isFixed()) {
                    vi.setX((random.nextDouble() * rangeX));
                    vi.setY((random.nextDouble() * rangeY));
                }
            }
            // check to see if we've been
            // cancelled!

            if (Thread.interrupted()) {
                // FIXME: update this w/ a bundle ref!
               LOG.info("scramble.cancelled");
                // TODO: do we need a rollback ?!? unless we are storing
                // position in
                // our cachedb, we're screwed!
                // RESOLUTION: for now we'll leave the updated component's new
                // locations...
                // since a cancellation is (typically) generated via the user.
                // the next request for current positions reflects the in-progress
                // scramble.
                break;
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void setup() {
        _visualization = (Visualization) graph.getClientProperty(Constants.VISUALIZATION);
        graph.getClientProperty(Constants.COMPONENT_COUNT);

        rootVG = (VisualGraph) _visualization.getVisualGroup(GraphConstants.ROOT_GRAPH);

        _numComponents = (Integer) graph.getClientProperty(Constants.COMPONENT_COUNT);
        subGraphs = (List<Graph>) graph.getClientProperty(GraphConstants.COMPONENTS);
    }

}
