package csi.server.business.visualization.graph.renderers;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import prefuse.render.AbstractShapeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.visual.VisualItem;

public class CompositeRenderer extends AbstractShapeRenderer {

    protected LabelRenderer labelRenderer;
    protected NodeRenderer iconShapeRenderer;

    protected double minimumNodeSizeForLabels = 50.0;

    public CompositeRenderer() {
        iconShapeRenderer = new NodeRenderer();

        final NodeRenderer nr = iconShapeRenderer;
        labelRenderer = new RestrictedLabelRenderer(nr);
    }

    @Override
    protected Shape getRawShape(VisualItem item) {
        Shape rawShape = iconShapeRenderer.getRawShape(item);
        Shape labelShape = labelRenderer.getShape(item);

        Rectangle bounds = new Rectangle();
        bounds.setFrame(rawShape.getBounds());
        bounds.add(labelShape.getBounds());
        Float rectShape = new Rectangle2D.Float();
        rectShape.setFrame(bounds);
        return rectShape;
    }

    @Override
    public void render(Graphics2D g, VisualItem item) {

        iconShapeRenderer.render(g, item);
        Shape shape = iconShapeRenderer.getShape(item);
        Rectangle bounds = shape.getBounds();

        AffineTransform transform = g.getTransform();
        java.awt.geom.Point2D.Float pt = new Point2D.Float();
        pt.x = bounds.width;

        transform.deltaTransform(pt, pt);

        if (shouldRenderLabel(pt.x)) {
            labelRenderer.render(g, item);
        }
    }

    private boolean shouldRenderLabel(float nodeSize) {
       return !hideLabels && (nodeSize > minimumNodeSizeForLabels);
    }

    public double getMinimumNodeSizeForLabels() {
        return minimumNodeSizeForLabels;
    }

    public void setMinimumNodeSizeForLabels(double minimumNodeSizeForLabels) {
        this.minimumNodeSizeForLabels = minimumNodeSizeForLabels;
    }
}
