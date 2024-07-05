package csi.server.business.visualization.graph.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.config.Configuration;
import csi.config.RelGraphConfig;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

public class BundleUtil {

    public static boolean hasBundleOperations(RelGraphViewDef def) {
        BundleDef bundlingDef = null;
        for (BundleDef bdef : def.getBundleDefs()) {

            int opCnt = bdef.getOperations().size();
            if (opCnt > 0) {
                bundlingDef = bdef;
                break;
            }
        }

        return bundlingDef != null;
    }

   public static boolean shouldBundle(Graph visibleGraph, RelGraphViewDef rgDef) {
      RelGraphConfig config = Configuration.getInstance().getGraphConfig();
      int autoBundleThreshold = config.getAutoBundleThreshold();
      int nodeCount = 0;

      for (Iterator<Node> nodes = visibleGraph.nodes(); nodes.hasNext();) {
         Node node = nodes.next();

         if (GraphContext.Predicates.IsNodeDisplayable.test(node)) {
            nodeCount++;
         }
      }
      boolean flag = (nodeCount > autoBundleThreshold);

      if (flag) {
         // determine whether we really have bundle operations.
         flag = hasBundleOperations(rgDef);
      }
      return flag;
   }

    /**
     * Updates the node's associated graph to create edges representing the 'bundled'/meta links from the bundle to
     * other nodes.
     * <p>
     * If this node is not a bundle, no actions are taken.
     * <p>
     * The steps taken are to discover all leaf nodes for this bundle, edges are then created from this bundle node to
     * the adjacent nodes.
     * <p>
     *
     * @param node
     */
    @SuppressWarnings("unchecked")
    public static void augmentEdges(Node node) {
        if (!node.isValid()) {
            return;
        }

        Graph graph = node.getGraph();
        VisualGraph visualGraph = (VisualGraph) graph.getClientProperty(GraphConstants.ROOT_GRAPH);

        Map<String, Node> lookupTable = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        NodeStore nodeStore = GraphManager.getNodeDetails(node);

        // Need to handle the case where a node is not a bundle, but we still
        // need to 'fix up' the edges e.g. the node is connected
        // to a node that is part of a bundle -- these links may not yet exist...

        if (!nodeStore.isBundle()) {
            Iterator neighbors = node.edges();
            while (neighbors.hasNext()) {
                Edge link = (Edge) neighbors.next();

                LinkStore origLinkStore = GraphManager.getEdgeDetails(link);
                Node other = link.getAdjacentNode(node);
                NodeStore otherStore = GraphManager.getNodeDetails(other);
                if (otherStore.isBundled()) {
                    while (otherStore.getParent() != null) {
                        otherStore = (NodeStore) otherStore.getParent();
                    }
                }

                other = lookupTable.get(otherStore.getKey());

                Edge edge = graph.getEdge(node, other);
                if (edge == null) {
                    edge = graph.getEdge(other, node);
                }

                // we're dealing with an edge that is connecting a node to some type of bundle; otherwise
                // we would already have an edge.
                if (edge == null) {
                    edge = graph.addEdge(node, other);
                    LinkStore linkStore = new LinkStore();
                    linkStore.setDocId(ObjectId.get());

                    String linkKey = nodeStore.getKey() + "+" + otherStore.getKey();
                    linkStore.setKey(linkKey);
                    linkStore.setFirstEndpoint(nodeStore);
                    linkStore.setSecondEndpoint(otherStore);
                    linkStore.setType(GraphConstants.BUNDLED_LINKS);
                    edge.setBoolean(GraphContext.IS_VISUALIZED, true);
                    GraphManager.setLinkDetails(edge, linkStore);
                }

                LinkStore bundleLink = GraphManager.getEdgeDetails(edge);
                if (bundleLink.getColor() == null) {
                    bundleLink.setColor(origLinkStore.getColor());
                }

                if ((edge != null) && nodeStore.isDisplayable() && otherStore.isDisplayable() && other.getBoolean(GraphContext.IS_VISUALIZED)) {
                    VisualItem ve = (VisualItem) visualGraph.getEdge(edge.getRow());
                    ve.setVisible(true);
                    bundleLink.setHidden(false);
                }

                GraphManager.setLinkDetails(edge, bundleLink);

            }
            return;
        }

        /*
         * remove all existing edges for this bundle -- we'll recreate the edges based off the existing children
         * contained by this bundle node.
         */
        List<Edge> existingEdges = Lists.newArrayList(node.edges());

        Iterator<Edge> edgeIterator = existingEdges.iterator();
        while (edgeIterator.hasNext()) {
            Edge edge = edgeIterator.next();
            graph.removeEdge(edge);
        }

        List<NodeStore> allLeaves = new ArrayList<NodeStore>();

		//For the unbundling of a node that contains other bundles as children,
		//only the edges connected to these nodes must be considered (we should ignore the
		//leaf nodes of the sub-bundles, ant their corresponding edges).

		//findLeafChildren(nodeStore, allLeaves);

        Iterator<AbstractGraphObjectStore> iter = nodeStore.getChildren().iterator();
        while (iter.hasNext()) {
            NodeStore child = (NodeStore) iter.next();
            allLeaves.add(child);
        }

        // make sure we don't have this current node!
        allLeaves.remove(nodeStore);
        Iterator<NodeStore> iterator = allLeaves.iterator();
        while (iterator.hasNext()) {
            NodeStore leafNS = iterator.next();
            Node leaf = lookupTable.get(leafNS.getKey());

            if (!leaf.isValid()) {
                continue;
            }
            Node neighbor;
            NodeStore neighborNS;
            Iterator<Edge> edges = leaf.edges();

            while (edges.hasNext()) {
                Edge leafEdge = edges.next();
                LinkStore leafLinkDetails = GraphManager.getEdgeDetails(leafEdge);

                neighbor = leafEdge.getAdjacentNode(leaf);
                if (!neighbor.isValid()) {
                    continue;
                }

                neighborNS = GraphManager.getNodeDetails(neighbor);
                if (neighborNS.isBundled()) {
                    while (neighborNS.getParent() != null) {
                        neighborNS = (NodeStore) neighborNS.getParent();
                    }
                }

                neighbor = lookupTable.get(neighborNS.getKey());
                // don't have the root parent as a valid node or the neighbor is
                // the node we're dealing with.
                if ((neighbor == null) || (neighbor == node) || !neighbor.isValid()) {
                    continue;
                }

                boolean neighborVisualized = neighbor.getBoolean(GraphContext.IS_VISUALIZED);

                Edge edge = graph.getEdge(node, neighbor);

                LinkStore bundleStore = null;
                if (edge == null) {
                    edge = graph.addEdge(node, neighbor);

                    edge.setBoolean(GraphContext.IS_VISUALIZED, neighborVisualized);

                    bundleStore = new LinkStore();
                    bundleStore.setDocId(ObjectId.get());

                    String linkKey = nodeStore.getKey() + "+" + neighborNS.getKey();
                    bundleStore.setKey(linkKey);
                    bundleStore.setFirstEndpoint(nodeStore);
                    bundleStore.setSecondEndpoint(neighborNS);
                    bundleStore.setType(GraphConstants.BUNDLED_LINKS);

                    //linkStore.setBundle(true);



                    if (leafLinkDetails != null) {
                        Integer color = leafLinkDetails.getColor();
                        if (color != null) {
                            bundleStore.setColor(color);
                        }
                    }

                    GraphManager.setLinkDetails(edge, bundleStore);

                    if (nodeStore.isDisplayable() && neighborNS.isDisplayable() && neighborVisualized) {
                        VisualItem ve = (VisualItem) visualGraph.getEdge(edge.getRow());
                        ve.setVisible(true);
                    }
                } else {
                    bundleStore = GraphManager.getEdgeDetails(edge);
                }

                // set parent and child pointers; this is needed so that isBundled()
				// returns false (meaning this edge can be represented).

                leafLinkDetails.setParent(bundleStore);
                bundleStore.addChild(leafLinkDetails);

                updateLinkDirection(edge, leafEdge);
            }
        }
    }

    private static void updateLinkDirection(Edge bundleEdge, Edge previousEdge) {

         /*
         * This logic is needed because of the way the new bundle-edge is created (the edge
           connecting the bundle with the neighbours of the bundled nodes:
           the new bundle-edge always has the bundle as source (even in the case when
           the bundled node was the target of the edge)

           So, here we "normalize" the direction, reversing countForward with countReverse,
           if the bundled node was target and not source of the edge.
         */

        LinkStore bundleLinkStore = GraphManager.getEdgeDetails(bundleEdge);
        LinkStore previousLinkStore = GraphManager.getEdgeDetails(previousEdge);


        int countForwardBundle;
        int countReverseBundle;
        int countNone = previousLinkStore.getCountNone();

        if (bundleEdge.getTargetNode().getRow() == previousEdge.getTargetNode().getRow()) {
            countForwardBundle = previousLinkStore.getCountForward();
            countReverseBundle = previousLinkStore.getCountReverse();
        }  else if (bundleEdge.getTargetNode().getRow() == previousEdge.getSourceNode().getRow()) {
            countForwardBundle = previousLinkStore.getCountReverse();
            countReverseBundle = previousLinkStore.getCountForward();
        } else {
            return;
        }

        bundleLinkStore.incrementCountForward(countForwardBundle);
        bundleLinkStore.incrementCountReverse(countReverseBundle);
        bundleLinkStore.incrementCountNone(countNone);

        GraphManager.setLinkDetails(bundleEdge, bundleLinkStore);
        GraphManager.setLinkDetails(previousEdge, previousLinkStore);
    }


    public static void mergeLinkDirection(LinkStore bundleLinkStore, LinkStore linkStore, boolean reverse) {
        /*
        * This logic is needed because of the way the new bundle-edge is created (the edge
          connecting the bundle with the neighbours of the bundled nodes:
          the new bundle-edge always has the bundle as source (even in the case when
          the bundled node was the target of the edge)

          So, here we "normalize" the direction, reversing countForward with countReverse,
          if the bundled node was target and not source of the edge.
        */
       if (reverse) {
           bundleLinkStore.incrementCountForward(linkStore.getCountReverse());
           bundleLinkStore.incrementCountReverse(linkStore.getCountForward());
       } else {
           bundleLinkStore.incrementCountForward(linkStore.getCountForward());
           bundleLinkStore.incrementCountReverse(linkStore.getCountReverse());
       }

       bundleLinkStore.incrementCountNone(linkStore.getCountNone());
   }

    // recursive search for all children of the current node -- if we ever get
    // into
    // large bundling depths need to use a fly-weight/context for lookup.
    public static void findLeafChildren(NodeStore node, List<NodeStore> leaves) {
        if (!node.isBundle()) {
            if (!leaves.contains(node)) {
                leaves.add(node);
            }
        } else {

            List<AbstractGraphObjectStore> childList = node.getChildren();
            if (childList != null) {
                Iterator<AbstractGraphObjectStore> iterator = childList.iterator();
                while (iterator.hasNext()) {
                    NodeStore child = (NodeStore) iterator.next();
                    findLeafChildren(child, leaves);
                }
            }
        }

    }

    public static void findChildNodes(Node node, Collection<Node> list) {
        NodeStore bundleDetails = GraphManager.getNodeDetails(node);
        if (!bundleDetails.isBundle()) {
            if (!list.contains(node)) {
                list.add(node);
            }
        } else if (bundleDetails.hasChildren()) {
            Iterator<AbstractGraphObjectStore> children = bundleDetails.getChildren().iterator();
            while (children.hasNext()) {
                NodeStore child = (NodeStore) children.next();
                Node n = getNodeByKey(node.getGraph(), child.getKey());
                findChildNodes(n, list);
            }
        }

    }

    public static Node getNodeByKey(Graph graph, String key) {
        Map<String, Node> idMapper = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        Node node = null;
        if (idMapper != null) {
            node = idMapper.get(key);
        }
        return node;

    }

    public static String buildBundleLabel(NodeStore ns) {
        String label = ns.getLabel();
        List<NodeStore> allChildren = new ArrayList<NodeStore>();
        BundleUtil.findLeafChildren(ns, allChildren);

        int immediateChildren = ns.getChildren().size();

        label += " (" + immediateChildren + "/" + allChildren.size() + ") ";
        return label;
    }

    public static String buildBundleCountInfo(NodeStore ns) {
        List<NodeStore> allChildren = new ArrayList<NodeStore>();
        BundleUtil.findLeafChildren(ns, allChildren);
        int immediateChildren = ns.getChildren().size();
        return " (" + immediateChildren + "/" + allChildren.size() + ") ";
    }

    public static boolean isBundleLink(LinkStore ls) {
        boolean isBundle = ls.getFirstEndpoint().isBundle() || ls.getSecondEndpoint().isBundle();
        return isBundle;
    }

    public static Set<Integer> getBundleLinkRows(LinkStore ls) {
        NodeStore n1 = ls.getFirstEndpoint();
        NodeStore n2 = ls.getSecondEndpoint();
        Set<Integer> n1Rows = getUniques(n1.getRows());
        Set<Integer> n2Rows = getUniques(n2.getRows());

        SetView<Integer> linkRows = Sets.intersection(n1Rows, n2Rows);
        return linkRows;
    }

    private static Set<Integer> getUniques(Map<String, List<Integer>> rows) {
        Set<Integer> set = new HashSet<Integer>();
        for (List<Integer> subset : rows.values()) {
            set.addAll(subset);
        }
        return set;
    }
}
