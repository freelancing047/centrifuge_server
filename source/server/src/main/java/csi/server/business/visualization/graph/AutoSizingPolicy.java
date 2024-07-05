package csi.server.business.visualization.graph;

import java.awt.Dimension;
import java.util.function.Predicate;

import prefuse.Display;
import prefuse.util.display.RenderingQueue;

public class AutoSizingPolicy {
   public static Predicate<GraphContext> createPercentage(double value) {
      return new PercentageVisible(value);
   }

   public static class PercentageVisible implements Predicate<GraphContext> {
      private double percentage;

      public PercentageVisible(double value) {
         percentage = value;
      }

      @Override
      public boolean test(GraphContext context) {
         Display display = context.getDisplay();
         Dimension dim = display.getSize();

         GraphManager.getInstance().renderGraph(context, dim);
         GraphManager.getInstance().renderGraph(context, dim);

         RenderingQueue renderQueue = display.getRenderingQueue();
         double fraction = ((double) renderQueue.rsize / (double) renderQueue.ritems.length);

         return (fraction < percentage);
      }
   }
}
