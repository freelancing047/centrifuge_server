package csi.server.business.visualization.graph.base;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.visual.NodeItem;
import csi.server.common.model.visualization.graph.GraphConstants;

public class CoordinateTransform {

    private static final int X = 0;
    private static final int Y = 0;

    private double fraction;

    private int dimension;

    // represents a rectangular region that we're transforming

    private Graph graph;

    private Rectangle2D.Double source;
    private Rectangle2D.Double target;

    private double scaleX;
    private double scaleY;

    public CoordinateTransform() {
        source = new Rectangle2D.Double();
        target = new Rectangle2D.Double();
    }

    public double[] transform(double x, double y) {

        double[] point = new double[2];

        point[X] = x - source.getMinX() * scaleX;
        point[Y] = y - source.getMinY() * scaleY;

        return point;
    }

    public double[] invertTransform(double x, double y) {
        double point[] = new double[2];

        point[X] = x - target.getMinX() / scaleX;
        point[Y] = y - target.getMinY() / scaleY;

        return point;

    }

    public double getFraction() {
        return fraction;
    }

    public void setFraction(double fraction) {
        this.fraction = fraction;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public Rectangle2D getPatch() {
        return target;

    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph value) {
        this.graph = value;
        if (graph != null) {
            computeBounds();

            target.x = (Integer) graph.getClientProperty(GraphConstants.LOWER_XCOORD);
            target.y = (Integer) graph.getClientProperty(GraphConstants.LOWER_YCOORD);
            target.width = target.height = (Integer) graph.getClientProperty(GraphConstants.PATCH_DIMENSION);
        }
    }

    @SuppressWarnings("unchecked")
    private void computeBounds() {
        if (this.graph == null) {
            return;
        }

        double lowX = Double.MAX_VALUE;
        double lowY = Double.MAX_VALUE;
        double highX = Double.MIN_VALUE;
        double highY = Double.MIN_VALUE;

        Iterator<NodeItem> nodes = graph.nodes();
        while (nodes.hasNext()) {
            NodeItem ni = nodes.next();
            if (ni.isVisible()) {
                double loc = ni.getX();
                if (loc < lowX) {
                    lowX = loc;
                } else if (loc > highX) {
                    highX = loc;
                }

                loc = ni.getY();
                if (loc < lowY) {
                    lowY = loc;
                } else if (loc > highY) {
                    highY = loc;
                }
            }
        }

        double rangeX = highX - lowX;
        double rangeY = highY - lowY;

        source.x = lowX;
        source.y = lowY;
        source.width = rangeX;
        source.height = rangeY;

        int dimension = (Integer) graph.getClientProperty(GraphConstants.PATCH_DIMENSION);
        scaleX = dimension / rangeX;
        scaleY = dimension / rangeY;
    }
}
