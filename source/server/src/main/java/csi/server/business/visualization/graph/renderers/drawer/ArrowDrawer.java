package csi.server.business.visualization.graph.renderers.drawer;

import java.awt.Graphics2D;
import java.awt.Shape;

import prefuse.visual.VisualItem;

public class ArrowDrawer extends AbstractAreaShapeDrawer {
   public ArrowDrawer(Graphics2D g, Shape shape, VisualItem item, int alpha) {
      super(g, shape, item, alpha);
   }

   @Override
   protected void drawSimpleShape(Graphics2D g, Shape shape, VisualItem item, int alpha) {
      g.draw(shape);
      g.fill(shape);
   }
}
