package csi.server.business.visualization.graph.player;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;

public abstract class AbstractPlaybackHandler implements PlaybackHandler {
   protected GraphPlayer player;
   protected GraphContext context;

   public static Predicate<EdgeItem> IsBundledLink = new Predicate<EdgeItem>() {
      @Override
      public boolean test(EdgeItem edge) {
         LinkStore details = GraphManager.getEdgeDetails(edge);

         return details.getFirstEndpoint().isBundled() || details.getSecondEndpoint().isBundled();
      }
   };

   public static Predicate<EdgeItem> IsLinkVisible = new Predicate<EdgeItem>() {
      @Override
      public boolean test(EdgeItem edge) {
         LinkStore details = GraphManager.getEdgeDetails(edge);

         return !details.isHidden() && details.getFirstEndpoint().isDisplayable() &&
                details.getSecondEndpoint().isDisplayable();
      }
   };

   public static Predicate<NodeItem> IsNodeDetailsVisible = new Predicate<NodeItem>() {
      @Override
      public boolean test(NodeItem node) {
         NodeStore details = getNodeDetails(node);

         return !details.isHidden() && !details.isBundled();
      }
   };

   public static Predicate<NodeItem> IsVisible = new Predicate<NodeItem>() {
      @Override
      public boolean test(NodeItem node) {
         boolean results = (node != null) && node.isVisible();

         if (results) {
            results = !getNodeDetails(node).isBundled();
         }
         return results;
      }
   };

   public static Predicate<NodeItem> IsBundled = new Predicate<NodeItem>() {
      @Override
      public boolean test(NodeItem node) {
         return getNodeDetails(node).isBundled();
      }
   };

   public AbstractPlaybackHandler() {
      super();
   }

   @Override
   public void initialize(GraphPlayer player, GraphContext context) {
      this.player = player;
      this.context = context;
   }

   @Override
   public void destroy() {
      player = null;
      context = null;
   }

   protected VisualGraph getVisualGraph() {
      return context.getVisualGraph();
   }

   protected void forSelectNodes(Predicate<NodeItem> filter, Function<NodeItem,Void> action) {
      VisualGraph graph = getVisualGraph();
      Iterator<NodeItem> nodes = graph.nodes();

      while (nodes.hasNext()) {
         NodeItem node = nodes.next();

         if (filter.test(node)) {
            action.apply(node);
         }
      }
   }

   protected void forSelectNodes(Predicate<NodeItem> filter, boolean visible, boolean highlight) {
      VisualGraph graph = getVisualGraph();
      Iterator<NodeItem> nodes = graph.nodes();

      while (nodes.hasNext()) {
         NodeItem node = nodes.next();

         if (filter.test(node)) {
            node.setVisible(visible);
            node.setHighlighted(highlight);
         }
      }
   }

   protected void forSelectEdges(Predicate<EdgeItem> filter, Function<EdgeItem,Void> action) {
      Iterator<EdgeItem> edges = getVisualGraph().edges();

      while (edges.hasNext()) {
         EdgeItem edge = edges.next();

         if (filter.test(edge)) {
            action.apply(edge);
         }
      }
   }

   protected void forSelectEdges(Predicate<EdgeItem> filter, boolean visible, boolean highlight) {
      Iterator<EdgeItem> edges = getVisualGraph().edges();

      while (edges.hasNext()) {
         EdgeItem edge = edges.next();

         if (filter.test(edge)) {
            edge.setVisible(visible);
            edge.setHighlighted(highlight);
         }
      }
   }

   protected void forAllNodes(boolean visible, boolean highlight) {
      VisualGraph visualGraph = getVisualGraph();

      for (Iterator<NodeItem> nodes = visualGraph.nodes(); nodes.hasNext();) {
         NodeItem item = nodes.next();

         if (GraphContext.Predicates.IsBundled.test(item)) {
            item.setVisible(visible);
            item.setHighlighted(highlight);
         }
      }
   }

   protected void forAllEdges(boolean visible, boolean highlight) {
      VisualGraph graph = getVisualGraph();
      Iterator<EdgeItem> edges = graph.edges();

      while (edges.hasNext()) {
         EdgeItem item = edges.next();

         item.setVisible(visible);
         item.setHighlighted(highlight);
      }
   }

   @Override
   public void resetVisuals() {
      forAllNodes(false, false);
      forAllEdges(false, false);
   }

   protected LinkStore getLinkDetails(EdgeItem ei) {
      return GraphManager.getEdgeDetails(ei);
   }

   protected static NodeStore getNodeDetails(NodeItem ni) {
      return GraphManager.getNodeDetails(ni);
   }

   protected void updateNodeItem(NodeItem item, boolean visible, boolean highlighted) {
      item.setVisible(visible);
      item.setHighlighted(highlighted);
   }

   protected void updateEdgeItem(EdgeItem item, boolean visible, boolean highlighted) {
      item.setVisible(visible);
      item.setHighlighted(highlighted);
   }
}
