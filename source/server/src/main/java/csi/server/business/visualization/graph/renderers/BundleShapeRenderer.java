package csi.server.business.visualization.graph.renderers;

import java.awt.Shape;

import prefuse.render.ShapeRenderer;
import prefuse.visual.VisualItem;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;

public class BundleShapeRenderer extends ShapeRenderer {

    @Override
    protected Shape getRawShape(VisualItem item) {

    	LinkStore details = GraphManager.getEdgeDetails(item);
    	boolean bySize = details.isBySize() || details.isByStatic();
    	
        double x = item.getX();
        if (Double.isNaN(x) || Double.isInfinite(x))
            x = 0;
        double y = item.getY();
        if (Double.isNaN(y) || Double.isInfinite(y))
            y = 0;
        double width = getBaseSize() * (bySize ? item.getSize() : 1.0);

        // Center the shape around the specified x and y
        if (width > 1) {
            x = x - width / 2;
            y = y - width / 2;
        }

        return hexagon((float) x, (float) y, (float) width);
    }

}
