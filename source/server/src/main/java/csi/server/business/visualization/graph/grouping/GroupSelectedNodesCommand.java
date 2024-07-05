package csi.server.business.visualization.graph.grouping;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.model.visualization.graph.GraphConstants;

public class GroupSelectedNodesCommand implements Callable<Void> {
   private static final Logger LOG = LogManager.getLogger(GroupSelectedNodesCommand.class);

    protected Graph graph;

    protected String groupName;

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void call() throws Exception {
        if (graph == null) {
            throw new NullPointerException();
        }
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        List<Node> nodes = (List<Node>) graph.getClientProperty(GraphConstants.SELECTED_NODES);
        if ((nodes == null) || nodes.isEmpty()) {
           LOG.info("No nodes selected for grouping, skipping command execution");
            return null;
        }

        if (groupName != null) {
            groupName = groupName.trim();
        }

        if ((groupName == null) || (groupName.length() == 0)) {
           LOG.debug("No group name provided, using the first node selected.");
            Node node = nodes.get(0);
            NodeStore nodeStore = GraphManager.getNodeDetails(node);
            groupName = nodeStore.getLabel();
        }

        Node groupNode = createNode(groupName);

        Collection<Edge> edgesToVisit = new HashSet<Edge>();

        NodeStore groupDetails = GraphManager.getNodeDetails(groupNode);
        for (Node node : nodes) {
            NodeStore nodeDetails = GraphManager.getNodeDetails(node);
            groupDetails.addChild(nodeDetails);

            Iterator<Edge> edges = node.edges();
            while (edges.hasNext()) {
                edgesToVisit.add(edges.next());
            }
        }

        createBundleLinks(edgesToVisit);
        stopWatch.stop();

        if (LOG.isDebugEnabled()) {
           LOG.debug("Group selected nodes took : " + stopWatch.getTime() + " ms.");
        }
        return null;
    }

    protected Node createNode(String nodeName) {
        Node node = graph.addNode();
        // NB: ordering of explicitly setting attr prior to getting the row id!
        NodeStore nodeStore = new NodeStore();
        nodeStore.setDocId(ObjectId.get());

        GraphManager.setNodeDetails(node, nodeStore);

        String id = Integer.toString(node.getRow());

        nodeStore.addLabel(nodeName);
        nodeStore.setKey(id);
        nodeStore.setBundle(true);

        Map<String, Node> nodeMap = getIdNodeMap();
        nodeMap.put(id, node);

        return node;
    }

    protected Map<String, Node> getIdNodeMap() {
        Map<String, Node> map = (Map<String, Node>) graph.getClientProperty("nodeHashTable");
        return map;
    }

    /**
     * Cycles through all concrete edges in our graph. For each end-point of a link we determine if either end-point is now part of a bundle. If either end-point is bundled, we
     * find the root of each node's hierarchy (only applicable if both nodes are bundled) and create a new <i>meta</i> link to represent the relation between the nodes due to the
     * bundling operation.
     */
    @SuppressWarnings("unchecked")
    protected void createBundleLinks(Collection<Edge> edges) {
        // use own hash table for new derived links
        Map<String, Edge> linkBundleTable = new HashMap<String, Edge>();
        Map<String, Node> nodeTable = getIdNodeMap();
        Iterator<Edge> links = edges.iterator();

        while (links.hasNext()) {
            Edge e = links.next();
            LinkStore linkStore = GraphManager.getEdgeDetails(e);

            // no need to consider a link that has been created due to bundling.
            if (linkStore.isBundle()) {
                continue;
            }

            NodeStore nodeStore1 = linkStore.getFirstEndpoint();
            NodeStore nodeStore2 = linkStore.getSecondEndpoint();

            if (nodeStore1.isBundled() || nodeStore2.isBundled()) {
                while (nodeStore1.getParent() != null) {
                  nodeStore1 = (NodeStore) nodeStore1.getParent();
               }
                while (nodeStore2.getParent() != null) {
                  nodeStore2 = (NodeStore) nodeStore2.getParent();
               }

                // end-points bundled in the same node don't require a link.
                // if the nodes are unbundled (via manual un-bundling the links
                // are created at that time)
                if (nodeStore1 == nodeStore2) {
                  continue;
               }

                Node bundle1 = nodeTable.get(nodeStore1.getKey());
                Node bundle2 = nodeTable.get(nodeStore2.getKey());
                String nodeKey1 = ((Integer) bundle1.getRow()).toString();
                String nodeKey2 = ((Integer) bundle2.getRow()).toString();
                String bundleKey = nodeKey1 + "+" + nodeKey2;
                Edge link = linkBundleTable.get(bundleKey);
                LinkStore bundleStore = null;

                // Desktop grouping allows 'bi-directional' type links...
                if (link == null) {
                    String bundleKeyReverse = nodeKey2 + "+" + nodeKey1;
                    link = linkBundleTable.get(bundleKeyReverse);
                }

                if (link == null) {
                    link = getGraph().addEdge(bundle1, bundle2);
                    linkBundleTable.put(bundleKey, link);

                    bundleStore = new LinkStore();
                    bundleStore.setDocId(ObjectId.get());

                    bundleStore.setKey(bundleKey);
                    bundleStore.setFirstEndpoint(nodeStore1);
                    bundleStore.setSecondEndpoint(nodeStore2);
                    GraphManager.setLinkDetails(link, bundleStore);
                } else {
                    bundleStore = GraphManager.getEdgeDetails(link);
                }

                // set parent and child pointers
                linkStore.setParent(bundleStore);
                bundleStore.addChild(linkStore);
            }
        }

    }

}
