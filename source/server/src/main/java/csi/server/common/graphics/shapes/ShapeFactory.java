package csi.server.common.graphics.shapes;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;


public class ShapeFactory {

    private final Rectangle2D theRectangle = new Rectangle2D.Float();
    private final Ellipse2D theEllipse = new Ellipse2D.Float();
    private final RoundRectangle2D theRoundRectangle = new RoundRectangle2D.Float();
    private final GeneralPath thePolygon = new GeneralPath();
    private final static Map<ShapeType, Shape> shapeCache = new HashMap<ShapeType, Shape>();

    public ShapeFactory() {

    }

    public Rectangle2D getRectangle(float width, float height) {
        float h_offset = -(width / 2);
        float v_offset = -(height / 2);
        theRectangle.setFrame(h_offset, v_offset, width, height);
        return theRectangle;
    }

    public Ellipse2D getEllipse(float width, float height) {
        theEllipse.setFrame(getRectangle(width, height));
        return theEllipse;
    }

    public RoundRectangle2D getRoundRectangle(float width, float height) {
        Rectangle2D frame = getRectangle(width, height);
        float arc_size = (float) Math.min(frame.getHeight(), frame.getWidth()) / 2;
        theRoundRectangle.setRoundRect(frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight(), arc_size, arc_size);
        return theRoundRectangle;
    }

    public Shape getRegularPolygon(float width, int num_sides, float angle) {
        if (num_sides < 3)
            throw new IllegalArgumentException("Number of sides must be >= 3");
        Shape s = new RegularPolygon(0, 0, (int) (width / 2), num_sides, angle);
        return scaleShape(s);
    }

    public Shape getRegularStar(float width) {
        Shape s = new StarPolygon(0, 0, (int) (width / 2), (int) ((width / 2) * 0.5f), 5, 49.96f);
        return scaleShape(s);
    }

    /**
     * Returns a up-pointing triangle of the given dimenisions.
     */
    public Shape getTriangle_up(float width, float height) {
        thePolygon.reset();
        thePolygon.moveTo(width / 2, 0);
        thePolygon.lineTo(0, height);
        thePolygon.lineTo(width, height);
        thePolygon.closePath();
        return scaleShape(thePolygon);
    }

    /**
     * Returns a down-pointing triangle of the given dimenisions.
     */
    public Shape getTriangle_down(float width, float height) {
        thePolygon.reset();
        thePolygon.moveTo(0, 0);
        thePolygon.lineTo(0, width);
        thePolygon.lineTo(height / 2, height);
        thePolygon.closePath();
        return scaleShape(thePolygon);
    }

    /**
     * Returns a left-pointing triangle of the given dimenisions.
     */
    public Shape getTriangle_left(float width, float height) {
        thePolygon.reset();
        thePolygon.moveTo(width, 0);
        thePolygon.lineTo(height, height);
        thePolygon.lineTo(0, height / 2);
        thePolygon.closePath();
        return scaleShape(thePolygon);
    }

    /**
     * Returns a right-pointing triangle of the given dimenisions.
     */
    public Shape getTriangle_right(float width, float height) {
        thePolygon.reset();
        thePolygon.moveTo(0, 0);
        thePolygon.lineTo(0, height);
        thePolygon.lineTo(width, height / 2);
        thePolygon.closePath();
        return scaleShape(thePolygon);
    }

    /**
     * Returns a cross shape of the given dimenisions.
     */
    public Shape getCross(float height) {
        float h14 = 3 * height / 8;
        float h34 = 5 * height / 8;
        thePolygon.reset();
        thePolygon.moveTo(h14, 0);
        thePolygon.lineTo(h34, 0);
        thePolygon.lineTo(h34, h14);
        thePolygon.lineTo(height, h14);
        thePolygon.lineTo(height, h34);
        thePolygon.lineTo(h34, h34);
        thePolygon.lineTo(h34, height);
        thePolygon.lineTo(h14, height);
        thePolygon.lineTo(h14, h34);
        thePolygon.lineTo(0, h34);
        thePolygon.lineTo(0, h14);
        thePolygon.lineTo(h14, h14);
        thePolygon.closePath();
        return scaleShape(thePolygon);
    }

    public Shape getDiamond(float height) {
        thePolygon.reset();
        thePolygon.moveTo(0, 0.5f * height);
        thePolygon.lineTo(0.5f * height, 0);
        thePolygon.lineTo(height, 0.5f * height);
        thePolygon.lineTo(0.5f * height, height);
        thePolygon.closePath();
        return scaleShape(thePolygon);
    }

    public Shape scaleShape(Shape baseShape) {

        // move polygon to center on (0,0)
        Rectangle2D r = baseShape.getBounds2D();
        AffineTransform at = AffineTransform.getTranslateInstance(-r.getCenterX(), -r.getCenterY());
        Shape shape = at.createTransformedShape(baseShape);
        return shape;
    }

    public Shape getHouse(float width, float height) {
        thePolygon.reset();
        thePolygon.moveTo(width * 0.5f, 0);
        thePolygon.lineTo(width, height * 0.33f);
        thePolygon.lineTo(height, height);
        thePolygon.lineTo(0, height);
        thePolygon.lineTo(0, height * 0.33f);
        thePolygon.closePath();
        return scaleShape(thePolygon);
    }

    public Shape getSquare(float width) {
        float h_offset = -(width / 2);
        float v_offset = -(width / 2);
        theRectangle.setFrame(h_offset, v_offset, width, width);
        return theRectangle;
    }

    public Shape getShape(ShapeType stype, float centerX, float centerY, float width, float height) {

        if (stype == null) {
            return null;
        }

        Shape shape = shapeCache.get(stype);
        if (shape == null) {
            shape = createShape(stype);

            if (shape != null) {
//                shapeCache.put(stype, shape);
            }
        }
        return scaleAndCenter(shape, centerX, centerY, width, height);

    }

    private Shape createShape(ShapeType stype) {
        float width = 40F; float height = 40F; Shape shape = null;
        switch (stype) {
            case NONE:
                break;
            case RECTANGLE:
                shape = getRectangle(width, height);
                break;
            case ROUND_RECTANGLE:
                shape = getRoundRectangle(width, height);
                break;
            case CIRCLE:
                shape = getEllipse(width, height);
                break;
            case DIAMOND:
                shape = getDiamond(height);
                break;
            case HEXAGON:
                shape = getRegularPolygon(width, 6, 0);
                break;
            case HOUSE:
                shape = getHouse(width, height);
                break;
            case OCTAGON:
                shape = getRegularPolygon(width, 8, 0);
                break;
            case PENTAGON:
                shape = getRegularPolygon(width, 5, 180);
                break;
            case STAR:
                shape = getRegularStar(width);
                break;
            case SQUARE:
                shape = getSquare(width);
                break;
            case TRIANGLE:
                shape = getTriangle_up(width, height);
                break;
            case TRIANGLE_UP:
                shape = getTriangle_up(width, height);
                break;
            case TRIANGLE_DOWN:
                shape = getTriangle_down(width, height);
                break;
            case TRIANGLE_RIGHT:
                shape = getTriangle_right(width, height);
                break;
            case TRIANGLE_LEFT:
                shape = getTriangle_left(width, height);
                break;
            case CROSS:
                shape = getCross(height);
                break;
            case LINE:
                shape = getLine();
                break;
        }
        return shape;
    }

    private Shape getLine() {
        thePolygon.reset();
        thePolygon.moveTo(4, 16);
        thePolygon.lineTo(36,16);
        thePolygon.lineTo(36,24);
        thePolygon.lineTo(4,24);
        thePolygon.closePath();
        return scaleShape(thePolygon);

    }

    public Shape scaleAndCenter(Shape baseShape, float cx, float cy, float width, float height) {
        if (baseShape == null) {
            return null;
        }
        Rectangle2D r = baseShape.getBounds2D();
        double scale_x = width / r.getWidth();
        double scale_y = height / r.getHeight();
        if(scale_x > scale_y){
            scale_x = scale_y;
        }
        else {
            scale_y = scale_x;
        }
        Shape shape1 = AffineTransform.getScaleInstance(scale_x, scale_y).createTransformedShape(baseShape);
        float deltaCx = (float) (cx - shape1.getBounds2D().getCenterX());
        float deltaCy = (float) (cy - shape1.getBounds2D().getCenterY());
        Shape shape2 = AffineTransform.getTranslateInstance(deltaCx, deltaCy).createTransformedShape(shape1);
        return shape2;
    }
}
