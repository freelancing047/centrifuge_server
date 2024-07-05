package csi.server.business.visualization.graph.player;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.player.GraphPlayer.GraphData;

public class CumulativePlaybackActionHandler extends AbstractPlaybackHandler {
   @Override
   public void initialize(GraphPlayer player, GraphContext context) {
      super.initialize(player, context);
      initializeVisuals();
   }

   @Override
   public void step(GraphData lastFrame, GraphData currentFrame) {
      checkState();

      // on each step we do the following:
      // de-highlight the previous frames entries
      // highlight the nodes in this frame
      VisualGraph visualGraph = getVisualGraph();
      SetView<Integer> addedNodes = Sets.difference(currentFrame.nodes, lastFrame.nodes);
      SetView<Integer> addedEdges = Sets.difference(currentFrame.edges, lastFrame.edges);

      for (Integer node : lastFrame.nodes) {
         NodeItem ni = (NodeItem) visualGraph.getNode(node);

         ni.setHighlighted(false);
      }

      for (Integer node : addedNodes) {
         NodeItem ni = (NodeItem) visualGraph.getNode(node);

         if (!ni.isVisible()) {
            updateNodeItem(ni, true, true);
         }
      }

      for (Integer edge : lastFrame.edges) {
         EdgeItem ei = (EdgeItem) visualGraph.getEdge(edge);

         ei.setHighlighted(false);
      }
      for (Integer edge : addedEdges) {
         EdgeItem ei = (EdgeItem) visualGraph.getEdge(edge);

         if (!ei.isVisible()) {
            updateEdgeItem(ei, true, true);
         }
      }
   }

   @Override
   public void destroy() {
      cleanupVisuals();
      super.destroy();
   }

   private void checkState() {
      if ((player == null) || (context == null)) {
         throw new IllegalStateException("Player not properly initialized");
      }
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

      for (Integer node : current.nodes) {
         NodeItem item = (NodeItem) visualGraph.getNode(node);

         item.setVisible(true);
         item.setHighlighted(true);
      }
      for (Integer edge : current.edges) {
         EdgeItem item = (EdgeItem) visualGraph.getEdge(edge);

         item.setVisible(true);
         item.setHighlighted(true);
      }
   }
}
