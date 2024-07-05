package csi.server.business.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.Display;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.player.AbstractPlaybackHandler;
import csi.server.business.visualization.graph.player.GraphPlayer;
import csi.server.business.visualization.graph.player.GraphPlayer.GraphState;

public class GraphNavigationObserver implements Observer {
   private static final Logger LOG = LogManager.getLogger(GraphNavigationObserver.class);

   private GraphManager helper = GraphManager.getInstance();

   @Override
   public void update(Observable o, Object arg) {
      if (o instanceof GraphContext) {
         // FYI -- this could be done a better way.
         // if we come back to refactoring the rendering of the graph
         // we should investigate using a flag on nodes that indicates
         // how it should be rendered instead of arbitrarily marking it
         // as hidden.
         GraphContext context = (GraphContext) o;
         GraphPlayer player = context.getPlayer();
         VisualGraph visualGraph = context.getVisualGraph();
         GraphState currentState = new GraphState(visualGraph);
         GraphState origState = player.getInitialGraphState();
         Iterator<NodeItem> nodes = visualGraph.nodes();

         while (nodes.hasNext()) {
            NodeItem item = nodes.next();
            if (AbstractPlaybackHandler.IsNodeDetailsVisible.test(item)) {
               item.setVisible(true);
            }
         }

         Iterator<EdgeItem> edges = visualGraph.edges();
         while (edges.hasNext()) {
            EdgeItem edge = edges.next();
            if (AbstractPlaybackHandler.IsLinkVisible.test(edge)) {
               edge.setVisible(true);
            }
         }

         if (origState != null) {
            origState.reset();
         }

         synchronized (context) {
            Display display = context.getDisplay();
            Color originalBG = display.getBackground();
            display.setBackground(new Color(0x00ffffff, true));
            BufferedImage origGraph = new BufferedImage(display.getWidth(), display.getHeight(),
                  BufferedImage.TYPE_INT_ARGB);
            helper.renderToImage(origGraph, display);

            display.setBackground(originalBG);

            List<BufferedImage> layers = Collections.singletonList(origGraph);
            context.setImageLayers(layers);
         }
         currentState.reset();
      } else {
         LOG.trace("Received notification for an unknown type; skipping image updates");
      }
   }
}
