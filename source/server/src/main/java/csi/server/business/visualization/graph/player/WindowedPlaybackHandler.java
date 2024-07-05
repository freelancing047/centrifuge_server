package csi.server.business.visualization.graph.player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.player.GraphPlayer.GraphData;

public class WindowedPlaybackHandler extends AbstractPlaybackHandler {
   // we use these sets as immutable. don't try to modify them w/ a clear().
   Set<Integer> highlightedNodes = Collections.emptySet();
   Set<Integer> highlightedEdges = Collections.emptySet();

   private boolean hideInactiveNodes;

   @Override
   public void initialize(GraphPlayer player, GraphContext context) {
      super.initialize(player, context);
      hideInactiveNodes = player.isHideInactiveNodes();
      initializeVisuals();
   }

   @Override
   public void step(GraphData previous, GraphData current) {
      VisualGraph visualGraph = getVisualGraph();
      Set<Integer> droppedNodes = Sets.difference(previous.nodes, current.nodes);
      SetView<Integer> addedNodes = Sets.difference(current.nodes, previous.nodes);

      // we clear the highlights here to account for instances where the frames don't
      // capture a reliable
      // transition i.e. when seeking. The highlightedNodes allows us to track exactly
      // what we
      // need to change highlights on....
      Set<Integer> highlightRemoval = highlightedNodes;

      for (Integer node : highlightRemoval) {
         NodeItem nodeItem = (NodeItem) visualGraph.getNode(node);

         nodeItem.setHighlighted(false);
      }
      for (Integer node : addedNodes) {
         NodeItem nodeItem = (NodeItem) visualGraph.getNode(node);

         updateNodeItem(nodeItem, true, false);
      }
      SetView<Integer> requiresHighlights = Sets.difference(addedNodes, highlightedNodes);

      for (Integer node : requiresHighlights) {
         NodeItem item = (NodeItem) visualGraph.getNode(node);

         item.setHighlighted(true);
      }
      highlightedNodes = requiresHighlights;

      for (Integer node : droppedNodes) {
         NodeItem nodeItem = (NodeItem) visualGraph.getNode(node);

         updateNodeItem(nodeItem, false, false);
      }
      Set<Integer> droppedEdges = Sets.difference(previous.edges, current.edges);
      SetView<Integer> addedEdges = Sets.difference(current.edges, previous.edges);
      highlightRemoval = highlightedEdges;

      for (Integer edge : highlightRemoval) {
         EdgeItem item = (EdgeItem) visualGraph.getEdge(edge);

         item.setHighlighted(false);
      }
      for (Integer edge : addedEdges) {
         EdgeItem edgeItem = (EdgeItem) visualGraph.getEdge(edge);

         updateEdgeItem(edgeItem, true, false);
      }
      requiresHighlights = Sets.difference(addedEdges, highlightedEdges);

      for (Integer edge : requiresHighlights) {
         EdgeItem edgeItem = (EdgeItem) visualGraph.getEdge(edge);

         edgeItem.setHighlighted(true);
      }
      highlightedEdges = requiresHighlights;

      for (Integer edge : droppedEdges) {
         EdgeItem edgeItem = (EdgeItem) visualGraph.getEdge(edge);

         updateEdgeItem(edgeItem, false, false);
      }
   }

   @Override
   public void destroy() {
      cleanupVisuals();
      super.destroy();
   }

   private void initializeVisuals() {
      forSelectNodes(IsVisible, false, false);
      forAllEdges(false, false);
   }

   private void cleanupVisuals() {
      forSelectNodes(IsBundled.negate(), true, false);
      forSelectEdges(IsLinkVisible, true, false);
   }

   @Override
   public void seek(GraphData current) {
      VisualGraph visualGraph = getVisualGraph();

      for (Integer node : highlightedNodes) {
         NodeItem item = (NodeItem) visualGraph.getNode(node);

         updateNodeItem(item, false, false);
      }
      for (Integer node : current.nodes) {
         NodeItem item = (NodeItem) visualGraph.getNode(node);

         updateNodeItem(item, true, true);
      }
      highlightedNodes = new HashSet(current.nodes);

      for (Integer edge : highlightedEdges) {
         EdgeItem item = (EdgeItem) visualGraph.getEdge(edge);

         updateEdgeItem(item, false, false);
      }
      for (Integer edge : current.edges) {
         EdgeItem item = (EdgeItem) visualGraph.getEdge(edge);

         updateEdgeItem(item, true, true);
      }
      highlightedEdges = new HashSet<Integer>(current.edges);
   }
}
