package csi.server.business.visualization.graph.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphManager;

public class UnbundleAll extends AbstractBundling {
   protected static final Logger LOG = LogManager.getLogger(UnbundleAll.class);

    public UnbundleAll(String dvUuid, String vizUuid, Graph graph) {
        super(dvUuid, vizUuid, graph);
    }

    @SuppressWarnings("unchecked")
    public void run() {

        List<Node> bundleNodes = new ArrayList<Node>();

        // NB: we need two passes on the graph nodes. the first
        // to discover which nodes are bundles
        // second pass to unbundle any nodes that are bundled.
        // If we unbundle on the first pass, we run into the possibility
        // that a node that was originally a bundle (e.g. State for I94) no
        // longer has any children--which causes isBundle() to fail

        // FIXME: Graph Updates
        // this requirement will disappear once we support multi-grouping/bundling
        // for a node; since it'll require a significantly different Graph/Node type.

        Graph g = getGraph();
        Iterator<Node> iterator = g.nodes();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            NodeStore store = GraphManager.getNodeDetails(node);

            if (store.isBundle()) {
                bundleNodes.add(node);
            }
        }

        iterator = g.nodes();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            NodeStore store = GraphManager.getNodeDetails(node);
            if (store.isBundled()) {
                store.unBundle();
            }
        }

        Map<String, Node> nodeTable = (Map<String, Node>) g.getClientProperty("nodeHashTable");

        iterator = bundleNodes.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            NodeStore store = GraphManager.getNodeDetails(node);

            if (LOG.isDebugEnabled()) {
               LOG.debug("Removing bundle node: " + store.getKey());
            }
            g.removeNode(node);
            nodeTable.remove(store.getKey());
        }

        g.putClientProperty("dirty", Boolean.TRUE);

    }

}
