package csi.server.business.visualization.graph.grouping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.BundleUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.model.visualization.graph.GraphConstants;

public class UnGroupNodesCommand implements Callable<Void> {
    protected static final Logger LOG = LogManager.getLogger(UnGroupNodesCommand.class);

    protected Graph graph;

    protected Collection<Node> nodes;

    public Collection<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Collection<Node> nodes) {
        this.nodes = nodes;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public Void call() throws Exception {
        if (graph == null) {
            throw new NullPointerException();
        }

        if ((nodes == null) || nodes.isEmpty()) {
           LOG.warn("No nodes selected for grouping, skipping command execution");
            return null;
        }

        Collection<NodeStore> updateList = new HashSet<NodeStore>();

        VisualGraph visualGraph = (VisualGraph) graph.getClientProperty(GraphConstants.ROOT_GRAPH);

        Map<String, Node> idNodeMap = getIdNodeMap();
        // Un-Grouping is no issue for nodes. The complicated stuff comes into
        // how the link processing was/wasn't done. Need to revisit
        // this when we use a different Graph API!
        for (Node node : nodes) {
            if (!node.isValid()) {
                continue;
            }
            NodeStore nodeStore = GraphManager.getNodeDetails(node);

            NodeStore parentStore = (NodeStore) nodeStore.getParent();
            if ((parentStore == null) && !nodeStore.isBundle()) {
                // ignore nodes that aren't a bundle and aren't part of a bundle.
                continue;
            }

            // handle case where the node is a bundle.
            if ((parentStore == null) && nodeStore.isBundle()) {
                // dealing with a top-level bundle. cycle through each child and extract
                // them out.
                for (AbstractGraphObjectStore gos : nodeStore.getChildren()) {
                    updateList.add((NodeStore) gos);
                }

                List<AbstractGraphObjectStore> workingCopy = new ArrayList<AbstractGraphObjectStore>(nodeStore.getChildren());
                for (AbstractGraphObjectStore gos : workingCopy) {
                    ((NodeStore) gos).unBundle();
                }

				//The edges corresponding to the bundle node have to be removed
                ArrayList<Edge> edges = Lists.newArrayList(node.edges());
                for (Edge edge : edges) {
                    graph.removeEdge(edge);
                }

				graph.removeNode(node);
                idNodeMap.remove(nodeStore.getKey());
            } else {
                // target node is part of a bundle.
                // extracting current node from its parent.

                Node parent = idNodeMap.get(parentStore.getKey());

                if (!parent.isValid()) {
                    continue;
                }

                parentStore = GraphManager.getNodeDetails(parent);

                // track up the hierarchy -- this ensures that we get rid
                // of intermediate bundles that get dropped.
                AbstractGraphObjectStore hierarchy = parentStore;
                while (hierarchy != null) {
                    updateList.add((NodeStore) hierarchy);
                    hierarchy = hierarchy.getParent();
                }

                // track nodes that we may need to potentially update....
                for (AbstractGraphObjectStore gos : parentStore.getChildren()) {
                    updateList.add((NodeStore) gos);
                }

                NodeStore tmp = parentStore;
                while (tmp != null) {
                    updateList.add(tmp);
                    tmp = (NodeStore) tmp.getParent();
                }

                nodeStore.unBundle();

                List<AbstractGraphObjectStore> children = parentStore.getChildren();
                Node parentNode = idNodeMap.get(parentStore.getKey());
                if ((children == null) || children.isEmpty()) {
                    // no children present, delete this node
                    // NB: this graph instance removes related links for the
                    // node.
                    if (parentNode.isValid()) {
                        graph.removeNode(parentNode);
                    }
                    idNodeMap.remove(parentStore.getKey());
                } else {
                    BundleUtil.augmentEdges(parentNode);
                }
            }

            for (NodeStore details : updateList) {

                node = idNodeMap.get(details.getKey());

                if ((node == null) || !node.isValid()) {
                    continue;
                }

                if (details.isBundle() && !details.hasChildren()) {
                    graph.removeNode(node);
                    idNodeMap.remove(details.getKey());
                }

				//If the node that needs to be updated is not a bundle, the edges that correspond
				//to it need to be reset to their initial condition (bundle - false, no parent set for LinkStore)
                if (!details.isBundle()) {
                    ArrayList<Edge> edges = Lists.newArrayList(node.edges());
                    for (Edge edge : edges) {
                        LinkStore edgeDetails = GraphManager.getEdgeDetails(edge);
                        AbstractGraphObjectStore parent = edgeDetails.getParent();
                        if (parent != null) {
                            parent.removeChild(edgeDetails);
                            edgeDetails.setParent(null);
                        }

                        GraphManager.setLinkDetails(edge, edgeDetails);
                    }
                }

                if (!details.isDisplayable()) {
                    continue;
                }
                if(!node.isValid()) {
                  continue;
               }

                VisualItem visualItem = (VisualItem) visualGraph.getNode(node.getRow());
                visualItem.setVisible(true);
                visualItem.setBoolean(GraphContext.IS_VISUALIZED, true);
                BundleUtil.augmentEdges(node);
            }

        }

        return null;
    }

   protected Map<String, Node> getIdNodeMap() {
      return (Map<String, Node>) graph.getClientProperty("nodeHashTable");
   }
}
