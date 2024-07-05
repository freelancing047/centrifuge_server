package csi.server.business.visualization.graph.renderers;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.StrokeLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;
import prefuse.visual.tuple.TableNodeItem;

import csi.config.Configuration;
import csi.server.business.visualization.graph.GraphAttributeHelper;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.ImageFactory;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.TypeInfo;
import csi.server.business.visualization.graph.renderers.drawer.AbstractAreaShapeDrawer;
import csi.server.business.visualization.graph.renderers.drawer.ArrowDrawer;
import csi.server.business.visualization.graph.renderers.drawer.BorderShapeDrawer;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.shared.gwt.viz.graph.LinkDirection;

/**
 * <p>
 * Renderer that draws edges as lines connecting nodes. Both straight and curved lines are supported. Curved lines are
 * drawn using cubic Bezier curves. Subclasses can override the
 * {@link #getCurveControlPoints(EdgeItem, Point2D[], double, double, double, double)} method to provide custom control
 * point assignment for such curves.
 * </p>
 * <p/>
 * <p>
 * This class also supports arrows for directed edges. See the {@link #setArrowType(int)} method for more.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @version 1.0
 */
public class EdgeRenderer extends AbstractShapeRenderer {
    private static final int ORIGINAL_ICON_SIZE = 105;
    private static final double ICON_SIZE = 10d;
    private static final Pattern EDGES_OBJECT_PATTERN = Pattern.compile(ObjectAttributes.EDGES_OBJECT_TYPE);

    protected static final double HALF_PI = Math.PI / 2;
    private static final Color DEFAULT_BASE_COLOR = Configuration.getInstance().getGraphAdvConfig().getDefaultLinkColor();
    protected Line2D line = new Line2D.Float();
    protected CubicCurve2D cubicCurve = new CubicCurve2D.Float();
    protected int edgeType = Constants.EDGE_TYPE_LINE;
    protected int xAlign1 = Constants.CENTER;
    protected int yAlign1 = Constants.CENTER;
    protected int xAlign2 = Constants.CENTER;
    protected int yAlign2 = Constants.CENTER;
    protected double width = 1;
    protected float curWidth = 1;
    protected Point2D tmpPoints[] = new Point2D[2];
    protected Point2D ctrlPoints[] = new Point2D[2];

    protected Point2D intersectionPoints[] = new Point2D[2];

    // arrow head handling
    protected int edgeArrow = Constants.EDGE_ARROW_FORWARD;

    protected int arrowWidth = 8;

    protected int arrowHeight = 8;

    protected Polygon arrowHead = updateArrowHead(arrowWidth, arrowHeight);

    protected Shape sourceArrow;

    protected Shape targetArrow;
    private ImageFactory m_images;

    /**
     * Create a new EdgeRenderer.
     */
    public EdgeRenderer() {
        tmpPoints[0] = new Point2D.Float();
        tmpPoints[1] = new Point2D.Float();
        ctrlPoints[0] = new Point2D.Float();
        ctrlPoints[1] = new Point2D.Float();
        intersectionPoints[0] = new Point2D.Float();
        intersectionPoints[1] = new Point2D.Float();
    }

    /**
     * Create a new EdgeRenderer with the given edge type.
     *
     * @param edgeType the edge type, one of {@link prefuse.Constants#EDGE_TYPE_LINE} or
     *                 {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     */
    public EdgeRenderer(int edgeType) {
        this(edgeType, Constants.EDGE_ARROW_FORWARD);
    }

    /**
     * Create a new EdgeRenderer with the given edge and arrow types.
     *
     * @param edgeType  the edge type, one of {@link prefuse.Constants#EDGE_TYPE_LINE} or
     *                  {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     * @param arrowType the arrow type, one of {@link prefuse.Constants#EDGE_ARROW_FORWARD},
     *                  {@link prefuse.Constants#EDGE_ARROW_REVERSE}, or {@link prefuse.Constants#EDGE_ARROW_NONE}.
     * @see #setArrowType(int)
     */
    public EdgeRenderer(int edgeType, int arrowType) {
        this();
        setEdgeType(edgeType);
        setArrowType(arrowType);
    }

    private static LinkStore getEdgeDetails(VisualItem item) {
        LinkStore detail = GraphManager.getEdgeDetails(item);
        return detail;
    }

    /**
     * Helper method, which calculates the top-left co-ordinate of a rectangle
     * given the rectangle's alignment.
     */
    protected static void getAlignedPoint(Point2D p, Rectangle2D r, int xAlign, int yAlign) {
        double x = r.getX(), y = r.getY(), w = r.getWidth(), h = r.getHeight();
        if (xAlign == Constants.CENTER) {
            x = x + (w / 2);
        } else if (xAlign == Constants.RIGHT) {
            x = x + w;
        }
        if (yAlign == Constants.CENTER) {
            y = y + (h / 2);
        } else if (yAlign == Constants.BOTTOM) {
            y = y + h;
        }
        p.setLocation(x, y);
    }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getRenderType(prefuse.visual.VisualItem)
     */
    public int getRenderType(VisualItem item) {
        return RENDER_TYPE_DRAW_AND_FILL;
    }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
    protected Shape getRawShape(VisualItem item) {
        EdgeItem edge = (EdgeItem) item;
        VisualItem item1 = edge.getSourceItem();
        VisualItem item2 = edge.getTargetItem();
        LinkStore edgeDetails = GraphManager.getEdgeDetails(edge);

        boolean bySize = edgeDetails.isBySize() || edgeDetails.isByStatic();

        tmpPoints[0].setLocation(item1.getX(), item1.getY());
        tmpPoints[1].setLocation(item2.getX(), item2.getY());

        curWidth = (float) (width * (bySize ? getLineWidth(item) : 1.0));

        // create the arrow head, if needed
        EdgeItem e = (EdgeItem) item;

        LinkDirection direction = edgeDetails.getDirection();
        switch (direction) {
            case FORWARD:
                targetArrow = createArrow(e, true);
                sourceArrow = null;
                break;
            case REVERSE:
                sourceArrow = createArrow(e, false);
                targetArrow = null;
                break;
            case BOTH:
                sourceArrow = createArrow(e, false);
                targetArrow = createArrow(e, true);
                break;
            case NONE:
                targetArrow = null;
                sourceArrow = null;
        }

        // create the edge shape
        Shape shape = null;
        double n1x = tmpPoints[0].getX();
        double n1y = tmpPoints[0].getY();
        double n2x = tmpPoints[1].getX();
        double n2y = tmpPoints[1].getY();
        switch (Constants.EDGE_TYPE_LINE) {
            case Constants.EDGE_TYPE_LINE:
                line.setLine(n1x, n1y, n2x, n2y);
                shape = line;
                break;
            case Constants.EDGE_TYPE_CURVE:
                getCurveControlPoints(edge, ctrlPoints, n1x, n1y, n2x, n2y);
                cubicCurve.setCurve(n1x, n1y, ctrlPoints[0].getX(), ctrlPoints[0].getY(), ctrlPoints[1].getX(), ctrlPoints[1].getY(), n2x, n2y);
                shape = cubicCurve;
                break;
            default:
                throw new IllegalStateException("Unknown edge type");
        }

        return shape;
    }

    private Shape createArrow(EdgeItem e, boolean forward) {
        // get starting and ending edge endpoints
        Point2D start = null, end = null;
        start = tmpPoints[forward ? 0 : 1];
        end = tmpPoints[forward ? 1 : 0];

        // compute the intersection with the target bounding box
        VisualItem dest = forward ? e.getTargetItem() : e.getSourceItem();

        // --- bounding without label ---
        NodeRenderer nodeRender = (NodeRenderer) dest.getRenderer();
        //        Point2D itemPoint = nodeRender.getItemPoint(dest);
        //        double x = itemPoint.getX();
        //        double y = itemPoint.getY();
        double nodeWidth = nodeRender.getBaseSize() * nodeRender.getItemSize(dest);
        //        Rectangle2D  bounds = new Rectangle2D.Double(x - width/2, y - width/2, width, width);

        // --- bounding box with label ---
        //        Shape baseShape = ((NodeRenderer) dest.getRenderer()).getBaseShape(dest);
        //        if (baseShape != null) {
        //        	bounds = baseShape.getBounds();
        //        } else {
        //        	bounds = dest.getBounds();
        //        }
        //
        //       int i = GraphicsLib.intersectLineRectangle(start, end,
        //	 				bounds, intersectionPoints);
        //       if (i > 0) {
        //           arrowPoint = intersectionPoints[0];
        //       } else {
        //        	arrowPoint = end;
        //    	 }

        //stop the arrow at a circle around the end node
        double r = Math.sqrt(((nodeWidth / 2) * (nodeWidth / 2)) + ((nodeWidth / 2) * (nodeWidth / 2)));
        //double r = width/2;
        double lineLenght = end.distance(start);
        //find the position of the arrow
        double xa = end.getX() + ((r * (start.getX() - end.getX())) / lineLenght);
        double ya = end.getY() + ((r * (start.getY() - end.getY())) / lineLenght);
        Point2D arrowPoint = new Point2D.Double(xa, ya);

        //make the line stop in the arrow
        //        double arrowLenght = curWidth*arrowHeight/4;
        //        double xl  = end.getX() + ((r + arrowLenght/2) * (start.getX() - end.getX()))/lineLenght;
        //        double yl  = end.getY() + ((r + arrowLenght/2) * (start.getY() - end.getY()))/lineLenght;
        //        end.setLocation(xl,yl);

        // create the arrow head shape
        AffineTransform at = getArrowTrans(start, arrowPoint, curWidth);
        return at.createTransformedShape(arrowHead);
    }

    /**
     * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
     */
    public void render(Graphics2D g, VisualItem item) {
        GraphContext gc = GraphContext.Current.get();
        RelGraphViewDef rgdef = gc.getVisualizationDef();
        //GraphTheme theme = gc.getTheme();
        int linkAlpha = rgdef.getLinkTransparency();
        int labelAlpha = rgdef.getLabelTransparency();
        LinkStore details = GraphManager.getEdgeDetails((EdgeItem) item);
        //        boolean byTransparency = details.isByTransparency();
        boolean byTransparency = true;
        boolean multiType = false;
        boolean isNew = isNew(item);
        boolean isUpdated = isUpdated(item);

        Stroke origStroke = g.getStroke();
        Color origColor = g.getColor();
        Paint origPaint = g.getPaint();

        Map<String, GraphLinkLegendItem> legend = gc.getLinkLegend();
        String linkType = details.getType();

        if((details.getTypes() != null) && !linkType.equals(GraphConstants.BUNDLED_LINKS) &&
           (details.getTypes().size() > 1) && (rgdef.getState() != null) && (rgdef.getState().getLegendOrder() != null)){
            List<String> priorityList = rgdef.getState().getLegendOrder();
            Set<String> currentTypes = details.getTypes().keySet();

            if(currentTypes.size() > 1){
                multiType = true;
                for(String priorityItem: priorityList){
                    priorityItem = EDGES_OBJECT_PATTERN.matcher(priorityItem).replaceAll("");
                    if(currentTypes.contains(priorityItem)){
                        linkType = priorityItem;
                        details.setType(linkType);
                        break;
                    }
                }
            } else {

            }


        }




        GraphLinkLegendItem legendItem = legend.get(linkType);

        Color sc = getBaseColor(item, legendItem);
        if ((legendItem != null) && "Link".equals(legendItem.getKey())) {
            String value = rgdef.getSettings().getPropertiesMap().get("csi.relgraph.backgroundColor");
            if (value != null) {
                try {
                    int color = Integer.parseInt(value);
                    Color bgc = new Color(color);
                    float bgLuminance = (float) ((.2126 * bgc.getRed()) + (.7152 * bgc.getGreen()) + (.0722 * bgc.getBlue())) / 255;
                    if (bgLuminance < .5) {
                        Color color1 = ColorLib.getColor(255, 255, 255, 255);
                        sc = color1;
                        legendItem.color = 16777215;
                    }
                }catch (Exception ignore){}
            }
        }
        Color shapeColor = ColorLib.getColor(sc.getRed(), sc.getGreen(), sc.getBlue(),
                (byTransparency ? computeTransparency(linkAlpha, item) : linkAlpha));
        Color textColor = ColorLib.getColor(0, 0, 0, labelAlpha);

        Shape shape = getShape(item);
        g.setColor(shapeColor);
        BorderShapeDrawer borderDrawer = new BorderShapeDrawer(g, shape, item, (byTransparency ? computeTransparency(linkAlpha, item) : linkAlpha));
        borderDrawer.drawShape();
        //labels
        double centerX = shape.getBounds2D().getCenterX();
        double centerY = shape.getBounds2D().getCenterY();

        g.setColor(textColor);
        String text = details.getLabel();
        if ((text != null) && !text.isEmpty() && !details.isHideLabels()) {
            g.setStroke(new BasicStroke());
            Font m_font = item.getFont();
            FontMetrics fm = g.getFontMetrics(m_font);
            int stringWidth = fm.stringWidth(text);

            double tx = centerX - (stringWidth / 2);
            double ty = centerY - (fm.getHeight() / 2);

            double x = centerX - (stringWidth / 2);
            double y = centerY - (fm.getHeight() / 2);

            Rectangle2D textRect = new Rectangle2D.Double(x - 5, y - 2,
                    stringWidth + 10, fm.getHeight() + 4);

            g.setPaint(ColorLib.getColor(254, 254, 254, labelAlpha));
            g.fill(textRect);
            g.setPaint(textColor);
            g.draw(textRect);

            ty = ty + fm.getAscent();
            g.setPaint(textColor);
            g.setFont(m_font);
            g.drawString(text, (int) tx, (int) ty);
        }

        g.setColor(shapeColor);
        AbstractAreaShapeDrawer arrowDrawer;

        if (sourceArrow != null) {
            arrowDrawer = new ArrowDrawer(g, sourceArrow, item, (byTransparency ? computeTransparency(linkAlpha, item) : linkAlpha));
            arrowDrawer.drawShape();
        }
        if (targetArrow != null) {
            arrowDrawer = new ArrowDrawer(g, targetArrow, item, (byTransparency ? computeTransparency(linkAlpha, item) : linkAlpha));
            arrowDrawer.drawShape();
        }

        if (multiType) {
           renderMultiTypeLine(g, item, details, (byTransparency ? computeTransparency(linkAlpha, item) : linkAlpha));
           // renderMultiTypeIcon(g, item, details);
        }

        if (isNew) {
            renderNewLine(g, item, details, (byTransparency ? computeTransparency(linkAlpha, item) : linkAlpha));

        }
        else if (isUpdated) {
            renderUpdatedLine(g, item, details, (byTransparency ? computeTransparency(linkAlpha, item) : linkAlpha));

        }

        g.setStroke(origStroke);
        g.setPaint(origPaint);
        g.setColor(origColor);
    }

    private void renderUpdatedLine(Graphics2D g, VisualItem item, LinkStore details, int linkAlpha) {

        VisualItemPropertiesExtractor extractor = new VisualItemPropertiesExtractor(item);
        TableEdgeItem edge = (TableEdgeItem) item;
        TableNodeItem source = (TableNodeItem) edge.getSourceNode();
        TableNodeItem target = (TableNodeItem) edge.getTargetNode();


        Stroke origStroke = g.getStroke();
        Color origColor = g.getColor();
        Paint origPaint = g.getPaint();

        double width = extractor.getThickness();

        g.setStroke(new BasicStroke((float)width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

        double startX = source.getX();
        double startY = source.getY();
        double endX = target.getX();
        double endY = target.getY();


        AffineTransform t = new AffineTransform();
        t.setToTranslation(startX, startY);
        double theta = -HALF_PI + Math.atan2(endY - startY, endX - startX);
        t.rotate(theta);


        Color updateGenColor = Configuration.getInstance().getGraphAdvConfig().getDefaultUpdateGenColor();
        g.setPaint(updateGenColor);
        g.setStroke(new BasicStroke(3));

        Rectangle2D.Double sRect = new Rectangle2D.Double(-5-(item.getSize()/2.0), (((theta<2?1:-1)*10)+(source.getSize()*29)+(item.getSize()*4))-3, 10+item.getSize(), 26);
        Shape tsRect = t.createTransformedShape(sRect);
        g.draw(tsRect);

        Rectangle2D.Double eRect = new Rectangle2D.Double(-5-(item.getSize()/2.0), Math.sqrt(Math.pow(startX - endX,2)+Math.pow(startY - endY,2))-((theta<2?1:-1)*30)-(target.getSize()*28)-(item.getSize()*4)-3, 10+item.getSize(), 26);
        Shape teRect = t.createTransformedShape(eRect);
        g.draw(teRect);


        g.setStroke(origStroke);
        g.setPaint(origPaint);
        g.setColor(origColor);

    }

    private void renderNewLine(Graphics2D g, VisualItem item, LinkStore details, int linkAlpha) {
        Stroke origStroke = g.getStroke();
        Color origColor = g.getColor();
        Paint origPaint = g.getPaint();

        Color newGenColor = Configuration.getInstance().getGraphAdvConfig().getDefaultNewGenColor();
        g.setPaint(newGenColor);

        VisualItemPropertiesExtractor extractor = new VisualItemPropertiesExtractor(item);

        TableEdgeItem edge = (TableEdgeItem) item;
        TableNodeItem source = (TableNodeItem) edge.getSourceNode();
        TableNodeItem target = (TableNodeItem) edge.getTargetNode();


        double width = extractor.getThickness();

        g.setStroke(new BasicStroke((float) width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

        double startX = source.getX();
        double startY = source.getY();
        double endX = target.getX();
        double endY = target.getY();

        AffineTransform t = new AffineTransform();
        t.setToTranslation(startX, startY);
        double theta = -HALF_PI + Math.atan2(endY - startY, endX - startX);
        t.rotate(theta);


        g.setStroke(new BasicStroke(3));

        Rectangle2D.Double sRect = new Rectangle2D.Double(-4 - (item.getSize() / 2.0), (((theta < 2 ? 1 : -1) * 10) + (source.getSize() * 29) + (item.getSize() * 4)) - 2, 8 + item.getSize(), 24);
        Shape tsRect = t.createTransformedShape(sRect);
        g.draw(tsRect);

        Rectangle2D.Double eRect = new Rectangle2D.Double(-4 - (item.getSize() / 2.0), Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2)) - ((theta < 2 ? 1 : -1) * 30) - (target.getSize() * 28) - (item.getSize() * 4) - 2, 8 + item.getSize(), 24);
        Shape teRect = t.createTransformedShape(eRect);
        g.draw(teRect);

        g.setStroke(origStroke);
        g.setPaint(origPaint);
        g.setColor(origColor);

    }

    private void renderMultiTypeLine(Graphics2D g, VisualItem item, LinkStore details, int linkAlpha) {

//        VisualItemPropertiesExtractor extractor = new VisualItemPropertiesExtractor(item);
        //Stroke stroke = g.getStroke();
        TableEdgeItem edge = (TableEdgeItem) item;
        TableNodeItem source = (TableNodeItem) edge.getSourceNode();
        TableNodeItem target = (TableNodeItem) edge.getTargetNode();


        Stroke origStroke = g.getStroke();
        Color origColor = g.getColor();
        Paint origPaint = g.getPaint();

//        double width = extractor.getThickness();

        g.setStroke(new BasicStroke((float)width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

        double startX = source.getX();
        double startY = source.getY();
        double endX = target.getX();
        double endY = target.getY();
//        double distance = 12;
//        if(width >=5){
//            distance = 16;
//        }

//        double midX =  ((startX + endX)/2);
//        double midY =  ((startY + endY)/2);

//        double angle = Math.atan2(midY-startY, midX-startX);
//        double angle2 = Math.atan2(startY - midY, startX-midX);

//        double endCurveX = midX + (distance * Math.cos(angle));
//        double endCurveY = midY + (distance * Math.sin(angle));

//        double startCurveX = midX + (distance * Math.cos(angle2));
//        double startCurveY = midY + (distance * Math.sin(angle2));


//        double curveAngle = 45;
//        double arcAngle = angle + curveAngle;
//        double xCurve = (midX + (distance * Math.cos(arcAngle)));
//        double yCurve = (midY + (distance * Math.sin(arcAngle)));

//        arcAngle = angle2 - curveAngle;
//        double xCurveNeg = (midX + (distance * Math.cos(arcAngle)));
//        double yCurveNeg = (midY + (distance * Math.sin(arcAngle)));


//            double m=1000000;

//            if (BigDecimal.valueOf(endX- startX).compareTo(BigDecimal.ZERO) != 0) {
//               m = (endY- startY) / (endX- startX);
//            }
//            double a = -m;
//            double b = 1;
//            double c = -startY + (m * endY);
            AffineTransform t = new AffineTransform();
            t.setToTranslation(startX, startY);
        double theta = -HALF_PI + Math.atan2(endY - startY, endX - startX);
        t.rotate(theta);



        g.setStroke(new BasicStroke(3));
      /*  g.setPaint(ColorLib.getColor(0, 255, 0));

        g.setPaint(ColorLib.getColor(255, 0, 0));

*/
        g.setPaint(ColorLib.getColor(0, 0, 255));
        Rectangle2D.Double sRect = new Rectangle2D.Double(-2-(item.getSize()/2.0), ((theta<2?1:-1)*10)+(source.getSize()*29)+(item.getSize()*4), 4+item.getSize(), 20);
        Shape tsRect = t.createTransformedShape(sRect);
        g.setPaint(ColorLib.getColor(0, 255, 0,255));
        g.draw(tsRect);
        g.setPaint(ColorLib.getColor(0, 255, 0,50));
        g.fill(tsRect);

        Rectangle2D.Double eRect = new Rectangle2D.Double(-2-(item.getSize()/2.0), Math.sqrt(Math.pow(startX - endX,2)+Math.pow(startY - endY,2))-((theta<2?1:-1)*30)-(target.getSize()*28)-(item.getSize()*4), 4+item.getSize(), 20);
        Shape teRect = t.createTransformedShape(eRect);
        g.setPaint(ColorLib.getColor(0, 255, 0,255));
        g.draw(teRect);
        g.setPaint(ColorLib.getColor(0, 255, 0,50));
        g.fill(teRect);
/*

        Rectangle2D.Double sInCommonRect = new Rectangle2D.Double(-9, (theta<2?1:-1)*70-9, 18, 18);
        Shape tsInCommonRect = t.createTransformedShape(sInCommonRect);
        g.draw(tsInCommonRect);



        Rectangle2D.Double eNewAddRect = new Rectangle2D.Double(-9, (theta<2?1:-1)* Math.sqrt(Math.pow(startX - endX,2)+Math.pow(startY - endY,2))-80+1, 18, 18);
        Shape teNewAddRect= t.createTransformedShape(eNewAddRect);
        g.draw(teNewAddRect);


*/


//        CubicCurve2D tildaLine = new CubicCurve2D.Double();

/*        tildaLine.setCurve(startCurveX, startCurveY,
                 xCurve, yCurve,
                 xCurveNeg, yCurveNeg,
                endCurveX, endCurveY);


        g.draw(tildaLine);
        */
        g.setStroke(origStroke);
        g.setPaint(origPaint);
        g.setColor(origColor);

    }

    public void renderMultiTypeIcon(Graphics2D graphics, VisualItem item, LinkStore details) {

        boolean multiType = (details.getTypes() != null) && (details.getTypes().size() > 1);
        if (!multiType) {
            return;
        }
//        ImageLocation imageLocation = new ImageLocation(OptionSetManager.toResourceUrl("/plus.png"));
//        Image image = getImageFactory().getImage(imageLocation, ORIGINAL_ICON_SIZE, ORIGINAL_ICON_SIZE);
        int desiredSize = (int) (ICON_SIZE * item.getSize());

        BufferedImage resizedImage = new BufferedImage(desiredSize, desiredSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(details.getTransparency()/100d)));

        //g.drawImage(image, 0, 0, desiredSize, desiredSize, null);

        g.dispose();

        renderEdgeImage(graphics, resizedImage, item);
    }

    private void renderEdgeImage(Graphics2D graphics, BufferedImage buffImg, VisualItem item) {
       double height = buffImg.getHeight(null);
       double width = buffImg.getWidth(null);

       if ((BigDecimal.valueOf(height).compareTo(BigDecimal.ZERO) != 0) &&
           (BigDecimal.valueOf(width).compareTo(BigDecimal.ZERO) != 0)) {
          TableEdgeItem edge = (TableEdgeItem) item;
          TableNodeItem source = (TableNodeItem) edge.getSourceNode();
          TableNodeItem target = (TableNodeItem) edge.getTargetNode();
          double startX = source.getX();
          double startY = source.getY();
          double endX = target.getX();
          double endY = target.getY();
          int edgeX = (int) (((startX + endX)/2) - (height/2));
          int edgeY = (int) (((startY + endY)/2) - (width/2));

          graphics.drawImage(buffImg, null, edgeX, edgeY);
       }
    }
    /**
     * Returns the image factory used by this renderer.
     *
     * @return the image factory
     */
    public ImageFactory getImageFactory() {
        if (m_images == null) {
            m_images = new ImageFactory();
        }
        return m_images;
    }

    private Color getBaseColor(VisualItem item, GraphLinkLegendItem legendItem) {
        Color color;

        LinkStore details = getEdgeDetails(item);
        if ((legendItem != null) && (details != null) && !details.isPlunked()) {
            int legendColor = ((Number) legendItem.color).intValue();
            return ColorLib.getColor(0xff000000 | legendColor);
        }
        if (details != null) {
        	Integer itemColor;

            if (details.isPlunked()) {
                itemColor = (Integer) getAttribute(item, ObjectAttributes.CSI_INTERNAL_COLOR);
            } else {
                itemColor = details.getColor();
            }

            if(itemColor != null){
        		color = ColorLib.getColor(ColorLib.setAlpha(itemColor, 255));
        	} else {
        		color = Configuration.getInstance().getGraphAdvConfig().getDefaultLinkColor();
        	}
        }
        else {
            color = Configuration.getInstance().getGraphAdvConfig().getDefaultLinkColor();
        }

        return color;
    }

    private Object getAttribute(VisualItem item, String attribute) {

        LinkStore detail = getEdgeDetails(item);
        if (detail == null) {
            return null;
        }

        TypeInfo typeInfo = getItemTypeInfo(item, detail.getType());
        if (detail.isBundle() && (detail.getFirstType() != null)) {
            typeInfo = getItemTypeInfo(item, detail.getFirstType());
        }
        //OptionSet optionSet = getOptionSet(item);
        GraphContext context = GraphContext.Current.get();
        if(context == null){
            return null;
        }

        return GraphAttributeHelper.resolveEdgeAttribute(attribute, detail, typeInfo, context.getTheme());
    }

//    private OptionSet getOptionSet(VisualItem item) {
//
//        TupleSet sourceGraph = getSourceGraph(item);
//        if (sourceGraph == null) {
//            return null;
//        }
//
//        String optionName = (String) sourceGraph.getClientProperty(GraphManager.OPTION_SET_NAME);
//        if (optionName == null) {
//            return null;
//        }
//
//        try {
//            return OptionSetManager.getOptionSet(optionName);
//        } catch (CentrifugeException e) {
//            log.warn("Error retrieving option set: " + optionName);
//            return null;
//        }
//    }

    private int computeTransparency(int alpha, VisualItem item) {
        LinkStore details = GraphManager.getEdgeDetails((EdgeItem) item);
        double tmp = alpha;
        double size = details.getTransparency();
        return Double.valueOf((tmp * (size / 100D))).intValue();
    }

    private TypeInfo getItemTypeInfo(VisualItem item, String edgeType) {

        TupleSet sourceGraph = getSourceGraph(item);
        if (sourceGraph == null) {
            return null;
        }

        Map<String, TypeInfo> typeMap = (Map<String, TypeInfo>) sourceGraph
                .getClientProperty(NodeStore.NODE_LEGEND_INFO);
        if (typeMap == null) {
            return null;
        }

        TypeInfo typeInfo = typeMap.get(edgeType);
        if (typeInfo == null) {
            typeInfo = typeMap.get(edgeType.toLowerCase());
        }
        return typeInfo;
    }

    private TupleSet getSourceGraph(VisualItem item) {
        Visualization visualization = item.getVisualization();
        if (visualization == null) {
            return null;
        }

        TupleSet sourceGraph = visualization.getSourceData("graph");
        if (sourceGraph == null) {
            return null;
        }

        return sourceGraph;
    }

    /**
     * Returns an affine transformation that maps the arrowhead shape to the position and orientation specified by the
     * provided line segment end points.
     */
    protected AffineTransform getArrowTrans(Point2D p1, Point2D p2, double width) {
        AffineTransform arrowTransform = new AffineTransform();

        arrowTransform.setToTranslation(p2.getX(), p2.getY());
        arrowTransform.rotate(-HALF_PI + Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX()));
        if (width > 2) { // TODO: Temporary
            double scalar = width / 2;
            arrowTransform.scale(scalar, scalar);
        }
        return arrowTransform;
    }

    /**
     * Update the dimensions of the arrow head, creating a new arrow head if necessary. The return value is also set as
     * the member variable <code>arrowHead</code>
     *
     * @param w the width of the untransformed arrow head base, in pixels
     * @param h the height of the untransformed arrow head, in pixels
     * @return the untransformed arrow head shape
     */
    protected Polygon updateArrowHead(int w, int h) {
        if (arrowHead == null) {
            arrowHead = new Polygon();
        } else {
            arrowHead.reset();
        }
        arrowHead.addPoint(0, 0);
        arrowHead.addPoint(-w / 2, -h);
        arrowHead.addPoint(0, -h / 2);
        arrowHead.addPoint(w / 2, -h);
        return arrowHead;
    }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getTransform(prefuse.visual.VisualItem)
     */
    protected AffineTransform getTransform(VisualItem item) {
        return null;
    }

    /**
     * @see prefuse.render.Renderer#locatePoint(java.awt.geom.Point2D, prefuse.visual.VisualItem)
     */
    public boolean locatePoint(Point2D p, VisualItem item) {
        LinkStore edgeDetails = GraphManager.getEdgeDetails(item);
//        boolean bySize = edgeDetails.isBySize() || edgeDetails.isByStatic();
        Shape s = getShape(item);
        if (s == null) {
            return false;
        } else {
//            double width = Math.max(2, (bySize ? getLineWidth(item) : 1.0));
            // if edge is directed, wee have to also locate point on the arrow.
            boolean intersectArrow = ((sourceArrow != null) && sourceArrow
                    .contains(p))
                    || ((targetArrow != null) && targetArrow.contains(p));
            boolean hitArrowOrLine = intersectArrow
                    || hitLine(p, item);
            if (hitArrowOrLine) {
                return hitArrowOrLine;
            }
            // label detection
            double centerX = s.getBounds2D().getCenterX();
            double centerY = s.getBounds2D().getCenterY();

            String text = edgeDetails.getLabel();
            if ((text != null) && !text.isEmpty()) {
                //FIXME: there is probably a better way to this.
                BufferedImage img = new BufferedImage(1, 1,
                        BufferedImage.BITMASK);
                Graphics2D g = img.createGraphics();
                Font m_font = item.getFont();
                FontMetrics fm = g.getFontMetrics(m_font);
                int stringWidth = fm.stringWidth(text);

                double x = centerX - (stringWidth / 2);
                double y = centerY - (fm.getHeight() / 2);
                // TODO: remove magic numbers
                Rectangle2D textRect = new Rectangle2D.Double(x - 5, y - 2,
                        stringWidth + 10, fm.getHeight() + 4);
                return textRect.contains(p);
            }
            return hitArrowOrLine;
        }
    }

    private boolean hitLine(Point2D p, VisualItem item) {

        double x = p.getX();
        double y = p.getY();
        double lineWidth = getLineWidth(item);
        double halfWidth = lineWidth/2.0;

        if (!inBounds(x,y,item, 0)) {
            //If the point is not in the bounding box for the line it is not on the line.
            return false;
        }

        double distance = distanceFromEdge(x, y, item);

        return halfWidth >distance;
    }

    public static boolean inBounds(double x, double y, VisualItem item, double padding) {
        EdgeItem edge = (EdgeItem) item;
        VisualItem item0 = edge.getSourceItem();
        VisualItem item1 = edge.getTargetItem();

        double x0 = item0.getX();
        double y0 = item0.getY();
        double x1 = item1.getX();
        double y1 = item1.getY();
        double lineWidth = getLineWidth(item);
        double halfWidth = (lineWidth/2.0)+padding;
        boolean out = ((x + halfWidth) < Math.min(x0, x1)) || ((x - halfWidth) > Math.max(x0, x1)) || ((y + halfWidth) < Math.min(y0, y1)) || ((y - halfWidth) > Math.max(y0, y1));
        if (!out) {
            //must be either strictly in x or strictly in y.
            out = (((x <= Math.min(x0, x1)) || (x >= Math.max(x0, x1))) && ((y <= Math.min(y0, y1)) || (y >= Math.max(y0, y1))));
        }
        return !out;
    }

   public static double distanceFromEdge(double x, double y, VisualItem item) {
      double result;
      EdgeItem edge = (EdgeItem) item;
      VisualItem item0 = edge.getSourceItem();
      VisualItem item1 = edge.getTargetItem();
      double x0 = item0.getX();
      double y0 = item0.getY();
      double x1 = item1.getX();
      double y1 = item1.getY();

      if (BigDecimal.valueOf(x1 - x0).compareTo(BigDecimal.ZERO) == 0) {
         result = Math.abs(x1 - x);
      } else {
         double m = (y1 - y0) / (x1 - x0);
         double a = -m;
         double b = 1;
         double c = -y0 + (m * x0);
         result = Math.abs((a * x) + (b * y) + c) / Math.sqrt((a * a) + (b * b));
      }
      return result;
   }

    /**
     * @see prefuse.render.Renderer#setBounds(prefuse.visual.VisualItem)
     */
    public void setBounds(VisualItem item) {
        if (!m_manageBounds) {
            return;
        }
        Shape shape = getShape(item);
        if (shape == null) {
            item.setBounds(item.getX(), item.getY(), 0, 0);
            return;
        }
        GraphicsLib.setBounds(item, shape, getStroke(item));
        if (sourceArrow != null) {
            Rectangle2D bbox = (Rectangle2D) item.get(VisualItem.BOUNDS);
            Rectangle2D.union(bbox, sourceArrow.getBounds2D(), bbox);

            if (targetArrow != null) {
                Rectangle2D.union(bbox, targetArrow.getBounds2D(), bbox);
            }
        }
    }

    /**
     * Returns the line width to be used for this VisualItem. By default, returns the base width value set using the
     * {@link #setDefaultLineWidth(double)} method, scaled by the item size returned by {@link VisualItem#getSize()}.
     * Subclasses can override this method to perform custom line width determination, however, the preferred method is
     * to change the item size value itself.
     *
     * @param item the VisualItem for which to determine the line width
     * @return the desired line width, in pixels
     */
    protected static double getLineWidth(VisualItem item) {
        return item.getSize();
    }

    /**
     * Returns the stroke value returned by {@link VisualItem#getStroke()}, scaled by the current line width determined
     * by the {@link #getLineWidth(VisualItem)} method. Subclasses may override this method to perform custom stroke
     * assignment, but should respect the line width paremeter stored in the {@link #curWidth} member variable, which
     * caches the result of <code>getLineWidth</code>.
     *
     * @see prefuse.render.AbstractShapeRenderer#getStroke(prefuse.visual.VisualItem)
     */
    protected BasicStroke getStroke(VisualItem item) {
        return StrokeLib.getDerivedStroke(item.getStroke(), curWidth);
    }

    /**
     * Determines the control points to use for cubic (Bezier) curve edges. Override this method to provide custom curve
     * specifications. To reduce object initialization, the entries of the Point2D array are already initialized, so use
     * the <tt>Point2D.setLocation()</tt> method rather than <tt>new Point2D.Double()</tt> to more efficiently set
     * custom control points.
     *
     * @param eitem the EdgeItem we are determining the control points for
     * @param cp    array of Point2D's (length >= 2) in which to return the control points
     * @param x1    the x co-ordinate of the first node this edge connects to
     * @param y1    the y co-ordinate of the first node this edge connects to
     * @param x2    the x co-ordinate of the second node this edge connects to
     * @param y2    the y co-ordinate of the second node this edge connects to
     */
    protected void getCurveControlPoints(EdgeItem eitem, Point2D[] cp, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        cp[0].setLocation(x1 + ((2 * dx) / 3), y1);
        cp[1].setLocation(x2 - (dx / 8), y2 - (dy / 8));
    }

    /**
     * Returns the type of the drawn edge. This is one of {@link prefuse.Constants#EDGE_TYPE_LINE} or
     * {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     *
     * @return the edge type
     */
    public int getEdgeType() {
        return edgeType;
    }

    /**
     * Sets the type of the drawn edge. This must be one of {@link prefuse.Constants#EDGE_TYPE_LINE} or
     * {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     *
     * @param type the new edge type
     */
    public void setEdgeType(int type) {
        if ((type < 0) || (type >= Constants.EDGE_TYPE_COUNT)) {
            throw new IllegalArgumentException("Unrecognized edge curve type: " + type);
        }
        edgeType = type;
    }

    /**
     * Returns the type of the drawn edge. This is one of {@link prefuse.Constants#EDGE_ARROW_FORWARD},
     * {@link prefuse.Constants#EDGE_ARROW_REVERSE}, or {@link prefuse.Constants#EDGE_ARROW_NONE}.
     *
     * @return the edge type
     */
    public int getArrowType() {
        return edgeArrow;
    }

    /**
     * Sets the type of the drawn edge. This is either {@link prefuse.Constants#EDGE_ARROW_NONE} for no edge arrows,
     * {@link prefuse.Constants#EDGE_ARROW_FORWARD} for arrows from source to target on directed edges, or
     * {@link prefuse.Constants#EDGE_ARROW_REVERSE} for arrows from target to source on directed edges.
     *
     * @param type the new arrow type
     */
    public void setArrowType(int type) {
        if ((type < 0) || (type >= Constants.EDGE_ARROW_COUNT)) {
            throw new IllegalArgumentException("Unrecognized edge arrow type: " + type);
        }
        edgeArrow = type;
    }

    /**
     * Sets the dimensions of an arrow head for a directed edge. This specifies the pixel dimensions when both the zoom
     * level and the size factor (a combination of item size value and default stroke width) are 1.0.
     *
     * @param width  the untransformed arrow head width, in pixels. This specifies the span of the base of the arrow head.
     * @param height the untransformed arrow head height, in pixels. This specifies the distance from the point of the
     *               arrow to its base.
     */
    public void setArrowHeadSize(int width, int height) {
        arrowWidth = width;
        arrowHeight = height;
        arrowHead = updateArrowHead(width, height);
    }

    /**
     * Get the height of the untransformed arrow head. This is the distance, in pixels, from the tip of the arrow to its
     * base.
     *
     * @return the default arrow head height
     */
    public int getArrowHeadHeight() {
        return arrowHeight;
    }

    /**
     * Get the width of the untransformed arrow head. This is the length, in pixels, of the base of the arrow head.
     *
     * @return the default arrow head width
     */
    public int getArrowHeadWidth() {
        return arrowWidth;
    }

    /**
     * Get the horizontal aligment of the edge mount point with the first node.
     *
     * @return the horizontal alignment, one of {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public int getHorizontalAlignment1() {
        return xAlign1;
    }

    /**
     * Set the horizontal aligment of the edge mount point with the first node.
     *
     * @param align the horizontal alignment, one of {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     *              {@link prefuse.Constants#CENTER}.
     */
    public void setHorizontalAlignment1(int align) {
        xAlign1 = align;
    }

    /**
     * Get the vertical aligment of the edge mount point with the first node.
     *
     * @return the vertical alignment, one of {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public int getVerticalAlignment1() {
        return yAlign1;
    }

    /**
     * Set the vertical aligment of the edge mount point with the first node.
     *
     * @param align the vertical alignment, one of {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     *              {@link prefuse.Constants#CENTER}.
     */
    public void setVerticalAlignment1(int align) {
        yAlign1 = align;
    }

    /**
     * Get the horizontal aligment of the edge mount point with the second node.
     *
     * @return the horizontal alignment, one of {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public int getHorizontalAlignment2() {
        return xAlign2;
    }

    /**
     * Set the horizontal aligment of the edge mount point with the second node.
     *
     * @param align the horizontal alignment, one of {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     *              {@link prefuse.Constants#CENTER}.
     */
    public void setHorizontalAlignment2(int align) {
        xAlign2 = align;
    }

    /**
     * Get the vertical aligment of the edge mount point with the second node.
     *
     * @return the vertical alignment, one of {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public int getVerticalAlignment2() {
        return yAlign2;
    }

    /**
     * Set the vertical aligment of the edge mount point with the second node.
     *
     * @param align the vertical alignment, one of {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     *              {@link prefuse.Constants#CENTER}.
     */
    public void setVerticalAlignment2(int align) {
        yAlign2 = align;
    }

    /**
     * Gets the default width of lines. This width value that will be scaled by the value of an item's size data field.
     * The default base width is 1.
     *
     * @return the default line width, in pixels
     */
    public double getDefaultLineWidth() {
        return width;
    }

    /**
     * Sets the default width of lines. This width value will be scaled by the value of an item's size data field. The
     * default base width is 1.
     *
     * @param w the desired default line width, in pixels
     */
    public void setDefaultLineWidth(double w) {
        width = w;
    }

    private boolean isNew(VisualItem item) {
        boolean isNew = false;
        int itemRow = item.getRow();
        Visualization vis = item.getVisualization();
        Graph graph = (Graph) vis.getSourceData("graph");
        Map<String, SelectionModel> selections = (Map<String, SelectionModel>) graph
                .getClientProperty(GraphConstants.SELECTIONS);
        if (selections != null) {
            SelectionModel newGenSelectionModel = selections.get(GraphConstants.NEW_GENERATION);

            if (newGenSelectionModel != null) {
                isNew = newGenSelectionModel.links.contains(itemRow);
            }
        }
        return isNew;
    }

    private boolean isUpdated(VisualItem item) {
        boolean isNew = false;
        int itemRow = item.getRow();
        Visualization vis = item.getVisualization();
        Graph graph = (Graph) vis.getSourceData("graph");
        Map<String, SelectionModel> selections = (Map<String, SelectionModel>) graph
                .getClientProperty(GraphConstants.SELECTIONS);
        if (selections != null) {
            SelectionModel newGenSelectionModel = selections.get(GraphConstants.UPDATED_GENERATION);
            if (newGenSelectionModel != null) {
                isNew = newGenSelectionModel.links.contains(itemRow);
            }
        }
        return isNew;
    }

}
