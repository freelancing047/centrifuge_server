package csi.server.business.visualization.graph.renderers;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import prefuse.Constants;
import prefuse.render.LabelRenderer;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;

public class RestrictedLabelRenderer extends LabelRenderer {

    private final NodeRenderer nr;
    AffineTransform m_transform;
    final double MaxTextHeight = 20.0;
    final double MinTextHeight = 4.0;

    public RestrictedLabelRenderer(NodeRenderer nr) {
        this.nr = nr;
    }

    @Override
    protected Shape getRawShape(VisualItem item) {
        Shape shape = super.getRawShape(item);
        Rectangle bounds = shape.getBounds();
        Shape graphicShape = nr.getRawShape(item);
        double top = graphicShape.getBounds().getMinY();
        int dy = (int) (top - 5 - bounds.height); // margin of 5 px
        bounds.y = dy;
        return bounds;
    }

   @Override
   public Shape getShape(VisualItem item) {
      return getRawShape(item);
   }

    @Override
    protected String getText(VisualItem item) {
        NodeStore details = GraphManager.getNodeDetails((NodeItem) item);
        if (details != null) {
            return details.getLabel();
        }
        return super.getText(item);
    }

    @Override
    public void render(Graphics2D g, VisualItem item) {

        AffineTransform orig = g.getTransform();
        Shape shape = getShape(item);

        Point pt = new Point();
        pt.x = shape.getBounds().width;
        pt.y = shape.getBounds().height;

        orig.deltaTransform(pt, pt);

        if (pt.y < MinTextHeight) {
            return;
        }
        try {

            double zoom = MaxTextHeight / pt.y;
            if (pt.y < MaxTextHeight) {
                zoom = 1.0d;
            }
            m_transform = new AffineTransform();
            m_transform.scale(zoom, zoom);

            super.render(g, item);
        } finally {
            m_transform = null;
        }

    }

    @Override
    protected void drawString(Graphics2D g, FontMetrics fm, String text, boolean useInt, double x, double y, double w) {
        // compute the x-coordinate
        double tx;
        switch (m_hTextAlign) {
        case Constants.LEFT:
            tx = x;
            break;
        case Constants.RIGHT:
            tx = (x + w) - fm.stringWidth(text);
            break;
        case Constants.CENTER:
            tx = x + ((w - fm.stringWidth(text)) / 2);
            break;
        default:
            throw new IllegalStateException("Unrecognized text alignment setting.");
        }

        double mid = w / 2;
        AffineTransform orig = g.getTransform();
        AffineTransform at = new AffineTransform();
        at.concatenate(orig);
        at.translate(tx, y);
        at.translate(mid, 0);
        if (m_transform != null) {
            Point2D post = new Point2D.Float();

            post.setLocation(m_textDim.width, m_textDim.height);

            m_transform.deltaTransform(post, post);
            double dx = post.getX() / 2;
            double dy = post.getY() / 2;

            at.translate(-dx, dy);
            at.concatenate(m_transform);
        } else {
            at.translate(-mid, 0);
        }
        g.setTransform(at);
        g.drawString(text, 0, 0);

        g.setTransform(orig);

    }
}