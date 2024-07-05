package csi.server.util;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;

public class GeometryUtil {

    public static Point2D centroidOf(Collection<Point2D> points) {
        Iterator<Point2D> iterator = points.iterator();
        double x = 0.0d;
        double y = 0.0d;
        if (iterator.hasNext()) {
            Point2D p = iterator.next();
            x = p.getX();
            y = p.getY();
        }

        while (iterator.hasNext()) {
            Point2D p = iterator.next();
            x += p.getX();
            y += p.getY();
        }

        x = x / (double) points.size();
        y = y / (double) points.size();

        Point2D centroid = new Point2D.Double();
        centroid.setLocation(x, y);

        return centroid;
    }

}
