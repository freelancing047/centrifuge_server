package csi.server.business.visualization.graph.renderers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.math.DoubleMath;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.visual.VisualItem;

import csi.config.Configuration;
import csi.server.business.visualization.graph.GraphAttributeHelper;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.ImageFactory;
import csi.server.business.visualization.graph.base.BundleUtil;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.TypeInfo;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.business.visualization.graph.optionset.OptionSet;
import csi.server.business.visualization.graph.optionset.OptionSetManager;
import csi.server.business.visualization.graph.pattern.selection.PatternSelection;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeFactory;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;

public class NodeRenderer extends AbstractShapeRenderer {
   private static final Logger LOG = LogManager.getLogger(NodeRenderer.class);

   private static final BigDecimal BIG_DECIMAL_FIFTY = BigDecimal.valueOf(50);
   private static final Pattern REGEX = Pattern.compile("[\\.\\\\\\/\\:]");
   private static final Pattern NODES_OBJECT_PATTERN = Pattern.compile(ObjectAttributes.NODES_OBJECT_TYPE);
   private static final double BUNDLE_SCALE_DEFAULT = .25;
   public static final String DEFAULT_OPTION_SET = "Baseline";
   public static final int DEFAULT_BASE_SIZE = 40;
   private int m_baseSize = DEFAULT_BASE_SIZE;
   private static final double STOP_DRAWING_ICONS = .27;
   private static final Color DEFAULT_BASE_COLOR = ColorLib.getColor(0, 255, 0);
   private static final String LINK_ICON_URL = "/Centrifuge/images/link.png";
   private static final int MAX_IMG_DIM = 512;
   private static final Map<String, ShapeType> shapeLookup = new HashMap<String, ShapeType>();
   static {
      shapeLookup.put("none", ShapeType.NONE);
      shapeLookup.put("circle", ShapeType.CIRCLE);
      shapeLookup.put("diamond", ShapeType.DIAMOND);
      shapeLookup.put("hexagon", ShapeType.HEXAGON);
      shapeLookup.put("pentagon/house", ShapeType.HOUSE);
      shapeLookup.put("octagon", ShapeType.OCTAGON);
      shapeLookup.put("pentagon", ShapeType.PENTAGON);
      shapeLookup.put("star", ShapeType.STAR);
      shapeLookup.put("square", ShapeType.SQUARE);
      shapeLookup.put("triangle", ShapeType.TRIANGLE_UP);
   }

   private static final int MAX_LABEL_LEN = Configuration.getInstance().getGraphConfig().getMaxLabelLength();
    // really a scaling function...
   private static final Function<VisualItem, Double> DEFAULT_SIZE_FUNCTION =
      new Function<VisualItem, Double>() {
         @Override
         public Double apply(VisualItem item) {
            return Double.valueOf(item.getSize());
         }
      };
   protected Function<VisualItem, Double> sizeFunction = DEFAULT_SIZE_FUNCTION;
    protected ShapeFactory shapeFactory = new ShapeFactory();
    protected ImageFactory m_images = new ImageFactory();
    protected String m_delim = "\n";
    protected String m_labelName = "label";
    protected String m_imageName = null;
    protected int m_xAlign = Constants.CENTER;
    protected int m_yAlign = Constants.CENTER;
    protected int m_hTextAlign = Constants.CENTER;
    protected int m_vTextAlign = Constants.CENTER;
    protected int m_hImageAlign = Constants.CENTER;
    protected int m_vImageAlign = Constants.CENTER;
    protected int m_imagePos = Constants.LEFT;
    protected int m_horizBorder = 2;
    protected int m_vertBorder = 0;
    protected int m_imageMargin = 2;
    protected int m_arcWidth = 0;
    protected int m_arcHeight = 0;
    protected int m_maxTextWidth = -1;
    /** The holder for the currently computed bounding box */
    protected RectangularShape m_bbox = new Rectangle2D.Double();
    protected Shape m_shape = null;
    protected Font m_font; // temp font holder
    protected String m_text; // label text
    protected Dimension m_textDim = new Dimension(); // text width / height
    /** Transform used to scale and position images */
    AffineTransform m_transform = new AffineTransform();
    private BufferedImage linkImg;

    private BufferedImage bundleImg;


    /**
     * Create a new LabelRenderer. By default the field "label" is used as the
     * field name for looking up text, and no image is used.
     */
    public NodeRenderer() {
        m_images.setMaxImageDimensions(MAX_IMG_DIM, MAX_IMG_DIM);
    }

    private static NodeStore getNodeDetails(VisualItem item) {
        NodeStore detail = GraphManager.getNodeDetails(item);
        return detail;
    }

    /**
     * Paints a white square with black border as default shape when no shape and no icon is specified.
     *
     * @param graphics    a graphics instance
     * @param x           x coordinate representing upper left corner of the square.
     * @param y           y coordinate representing upper left corner of the square.
     * @param width       width of square.
     * @param height      height of square.
     */
    @Deprecated
    public static void paintDefaultNode(Graphics2D graphics, double x, double y, double width, double height) {
        throw new NotImplementedException("This method has been deprecated.");
        //        graphics.setStroke(new BasicStroke(3f));
        //        Rectangle2D textRect = new Rectangle2D.Double(x, y, width, height);
        //        graphics.setPaint(ColorLib.getColor(254, 254, 254, 255));
        //        graphics.fill(textRect);
        //        graphics.setPaint(Color.BLACK);
        //        graphics.draw(textRect);
    }

    /**
     * Get the field name to use for text labels.
     *
     * @return the data field for text labels, or null for no text
     */
    public String getTextField() {
        return m_labelName;
    }

    /**
     * Set the field name to use for text labels.
     *
     * @param textField
     *            the data field for text labels, or null for no text
     */
    public void setTextField(String textField) {
        m_labelName = textField;
    }

    /**
     * Sets the maximum width that should be allowed of the text label. A value
     * of -1 specifies no limit (this is the default).
     *
     * @param maxWidth
     *            the maximum width of the text or -1 for no limit
     */
    public void setMaxTextWidth(int maxWidth) {
        m_maxTextWidth = maxWidth;
    }


    // ------------------------------------------------------------------------
    // Image Handling

    /**
     * Returns the text to draw. Subclasses can override this class to perform
     * custom text selection.
     *
     * @param item
     *            the item to represent as a <code>String</code>
     * @return a <code>String</code> to draw
     */
    protected String getText(VisualItem item) {
        NodeStore detail = getNodeDetails(item);
        if (detail == null) {
            return null;
        }

        if (detail.isBundle()) {
            return BundleUtil.buildBundleLabel(detail);
        }
        return detail.getLabel();
    }

    /**
     * Tells if the labels should be displayed for this item, based on the setting defined on NodeStore (and implicitly on NodeDef).
     * @param item  the visual item for which we need to know if labels should be displayed or not.
     * @return false if labels should be displayed, true otherwise.
     */
    protected boolean hideLabelsOnNodeType(VisualItem item) {
        NodeStore detail = getNodeDetails(item);
        return (detail != null) && detail.isHideLabels();
    }

    /**
     * Get the data field for image locations. The value stored in the data
     * field should be a URL, a file within the current classpath, a file on the
     * filesystem, or null for no image.
     *
     * @return the data field for image locations, or null for no images
     */
    public String getImageField() {
        return m_imageName;
    }

    /**
     * Set the data field for image locations. The value stored in the data
     * field should be a URL, a file within the current classpath, a file on the
     * filesystem, or null for no image. If the <code>imageField</code>
     * parameter is null, no images at all will be drawn.
     *
     * @param imageField
     *            the data field for image locations, or null for no images
     */
    public void setImageField(String imageField) {
        if (imageField != null) {
            m_images = new ImageFactory();
        }
        m_imageName = imageField;
    }

    /**
     * Sets the maximum image dimensions, used to control scaling of loaded
     * images. This scaling is enforced immediately upon loading of the image.
     *
     * @param width
     *            the maximum width of images (-1 for no limit)
     * @param height
     *            the maximum height of images (-1 for no limit)
     */
    public void setMaxImageDimensions(int width, int height) {
        if (m_images == null) {
            m_images = new ImageFactory();
        }
        m_images.setMaxImageDimensions(width, height);
    }

    /**
     * Returns a location string for the image to draw. Subclasses can override
     * this class to perform custom image selection beyond looking up the value
     * from a data field.
     *
     * @param item
     *            the item for which to select an image to draw
     * @return the location string for the image to use, or null for no image
     */
    protected ImageLocation getImageLocation(VisualItem item) {
        String icon = getIconId(item, null);
        if (icon != null) {
            return new ImageLocation(icon);
        } else {
            return null;
        }
    }

    private TypeInfo getItemTypeInfo(VisualItem item, String nodeType) {

        TupleSet sourceGraph = getSourceGraph(item);
        if (sourceGraph == null) {
            return null;
        }

        Map<String, TypeInfo> typeMap = (Map<String, TypeInfo>) sourceGraph
                .getClientProperty(NodeStore.NODE_LEGEND_INFO);
        if (typeMap == null) {
            return null;
        }

        TypeInfo typeInfo = typeMap.get(nodeType);
        if (typeInfo == null) {
            typeInfo = typeMap.get(nodeType.toLowerCase());
        }
        return typeInfo;
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
//            return OptionSetManager.getOptionSet("Circular");
//        } catch (CentrifugeException e) {
//            logger.warn("Error retrieving option set: " + optionName);
//            return null;
//        }
//    }

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
     * Get the image to include in the label for the given VisualItem.
     *
     * @param item
     *            the item to get an image for
     * @return the image for the item, or null for no image
     */
    protected Image getImage(VisualItem item, int i) {
        String icon = getIconId(item, null);

        return ((icon == null) || icon.isEmpty() ? null : m_images.getImage(icon, i, i));
    }

    private String getIconId(VisualItem item, NodeStyle nodeStyle) {
        NodeStore detail = getNodeDetails(item);
        if (detail == null) {
            return null;
        }

        if (detail.isBundle() && (detail.getTypes().size() > 1)) {
            return null;
        }

        String icon = (String) getAttribute(item, ObjectAttributes.CSI_INTERNAL_ICON, nodeStyle, null);
        return icon;
    }

    // ------------------------------------------------------------------------
    // Rendering

    // Note: This might not work if the Image is sent to render centered image, since it is not pre-scaled.
    protected Image getImage(VisualItem item, NodeStyle nodeStyle) {
        String icon = getIconId(item, nodeStyle);
        return ((icon == null) || icon.isEmpty() ? null : m_images.getImage(icon));
    }


   private String computeTextDimensions(VisualItem item, String s, double size) {
      String text = null;

      if ((s != null) && (s.trim().length() > 0)) {
         text = s.trim();

         if (text.length() > MAX_LABEL_LEN) {
            text = text.substring(0, MAX_LABEL_LEN - 3) + "...";
         }
         // put item font in temp member variable
         m_font = item.getFont();
         // scale the font as needed

         if (BigDecimal.valueOf(size).compareTo(BigDecimal.ONE) != 0) {
            m_font = FontLib.getFont(m_font.getName(), m_font.getStyle(), size * m_font.getSize());
         }
         FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);
         m_textDim.width = 0;
         int w = fm.stringWidth(text);
         m_textDim.width = Math.max(m_textDim.width, w) + 10;// (int) (8 * size);
         m_textDim.height = fm.getHeight() - (2 * fm.getLeading());
      }
      return text;
   }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
    @Override
    protected Shape getRawShape(VisualItem item) {
        Point2D itemPoint = getItemPoint(item);

        NodeStore details = getNodeDetails(item);
        boolean bySize = details.isBySize() || details.isByStatic();
        double size = getItemSize(item);

        m_text = getText(item);
        Shape baseShape = getBaseShape(item, bySize, null);
        //OptionSet optionSet = getOptionSet(item);

        double tw = 0, th = 0;
        if (m_text != null) {
            m_text = computeTextDimensions(item, m_text, (bySize ? size : 1.0));
            th = m_textDim.height;
            tw = m_textDim.width;
        }

        double h = 0;
        double w = 0;

        double itemsize = m_baseSize * (bySize ? size : 1.0);
        if (isBundle(item)) {
            Image bimg = null;

            GraphContext gc = GraphContext.Current.get();
            if(gc != null){
                GraphTheme theme = gc.getTheme();
                if((theme != null) && (theme.getBundleStyle() != null) && (theme.getBundleStyle().getIconId() != null)){
                    bimg = m_images.getImage(theme.getBundleStyle().getIconId());
                }
            }

            if(bimg == null){

                OptionSet optionSet;
                try {
                    optionSet = OptionSetManager.getOptionSet(DEFAULT_OPTION_SET);
                    bimg = m_images.getImage(new ImageLocation(OptionSetManager
                            .toResourceUrl(optionSet.getBundleIcon())));
                } catch (CentrifugeException e) {
                   LOG.error("Default Optionset is missing");
                }
            }

            if(bimg != null){
                double iw = bimg.getHeight(null);
                double ih = bimg.getWidth(null);

                if ((iw > itemsize) || (ih > itemsize)) {
                    double zoom;
                    if (iw < ih) {
                        zoom = itemsize / iw;
                    } else {
                        zoom = itemsize / ih;
                    }
                    w = iw * zoom;
                    h = ih * zoom;
                }
            }

        } else if (baseShape != null) {
            Rectangle bounds = baseShape.getBounds();
            w = bounds.getWidth();
            h = bounds.getHeight();

        } else {
            //Instead of using image here, we just assume a default shape
            double x = itemPoint.getX();
            double y = itemPoint.getY();
            double width = getBaseSize() * (bySize ? getItemSize(item) : 1.0);

            Shape shape = shapeFactory.getShape(ShapeType.SQUARE, (float) x, (float) y, (float) width, (float) width);
            w = shape.getBounds().getWidth();
            h = shape.getBounds().getHeight();
        }

        w = Math.max(w, tw);
        // h = Math.max( h, th );

        double bx = itemPoint.getX();
        double by = itemPoint.getY();
        boolean textOnly = ((BigDecimal.valueOf(w).compareTo(BigDecimal.ZERO) == 0) ||
                            (BigDecimal.valueOf(h).compareTo(BigDecimal.ZERO) == 0));

        if (textOnly) {
            w = tw;
            h = th;
            bx = bx - (w / 2);
            by = by - (h / 2);
        } else {
            bx = bx - (w / 2);
            by = by - (h / 2) - th;
            h = h + th;
        }

        m_bbox.setFrame(bx - 5, by - 5, w + 10, h + 10);
        return m_bbox;
    }

    public Point2D getItemPoint(VisualItem item) {
        double x = item.getX();
        double y = item.getY();
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            x = 0;
            item.setX(x);
        }
        if (Double.isNaN(y) || Double.isInfinite(y)) {
            y = 0;
            item.setY(y);
        }

        return new Point2D.Double(x, y);
    }

    /******************************************************************************/
    /******************************************************************************/
    /******************************************************************************/

    /**
     * @see prefuse.render.Renderer#render(java.awt.Graphics2D,
     *      prefuse.visual.VisualItem)
     */
    @Override
    public void render(Graphics2D g, VisualItem item) {
        GraphContext gc = GraphContext.Current.get();
        //FIXME: any JPA requests should be avoided inside the renderer
        RelGraphViewDef rgdef = gc.getVisualizationDef();
        GraphTheme theme = gc.getTheme();
        int nodeAlpha = rgdef.getNodeTransparency();
        int labelAlpha = rgdef.getLabelTransparency();
        NodeStore details = getNodeDetails(item);
        if (details.isHidden()) {
            return;
        }
        int sizeMode = details.getSizeMode();
        double size_in = item.getSize();
        double minimumNodeScaleFactor = rgdef.getMinimumNodeScaleFactor();

        if (BigDecimal.valueOf(minimumNodeScaleFactor).compareTo(BIG_DECIMAL_FIFTY) != 0) {
            details.setSizeMode(1);
            item.setSize(size_in / Math.min(1, g.getTransform().getScaleY() * minimumNodeScaleFactor));
        }

        boolean bySize = details.isBySize() || details.isByStatic();
        //        boolean byTransparency = details.isByTransparency();
        boolean byTransparency = true;
        m_text = getText(item);
        Stroke origStroke = g.getStroke();
        Color origColor = g.getColor();
        Paint origPaint = g.getPaint();


        String nodeType = details.getType();

        if((details.getTypes() != null) && (details.getTypes().size() > 1) && (rgdef.getState() != null) && (rgdef.getState().getLegendOrder() != null)){
            List<String> priorityList = rgdef.getState().getLegendOrder();
            Set<String> currentTypes = details.getTypes().keySet();
            for(String priorityItem: priorityList){
                priorityItem = NODES_OBJECT_PATTERN.matcher(priorityItem).replaceAll("");
                if(currentTypes.contains(priorityItem)){
                    nodeType = priorityItem;
                    details.setType(nodeType);
                    break;
                }
            }
        }

        if (details.isBundle() && (details.getFirstType() != null)){
            nodeType = details.getFirstType();
        }

        NodeStyle nodeStyle = null;
        if (theme != null) {
            nodeStyle = theme.findNodeStyle(nodeType);
        }

        try {
            RectangularShape shape = (RectangularShape) getShape(item);

            int baseSize = this.getBaseSize();
            double size = item.getSize();
            Shape baseShape = getBaseShape(item, bySize, nodeStyle);
            Color sc = getBaseColor(item, nodeStyle);
            Color shapeColor = ColorLib.getColor(sc.getRed(), sc.getGreen(), sc.getBlue(),
                    (byTransparency ? computeTransparency(nodeAlpha, item) : nodeAlpha));
            Color textColor = ColorLib.getColor(sc.getRed(), sc.getGreen(), sc.getBlue(), labelAlpha);
            String text = m_text;

            double scaleX = g.getTransform().getScaleX();
            double scaleY = g.getTransform().getScaleY();

            BufferedImage img = null;
            boolean drawIcon = ((((bySize ? size : 1.0) * scaleX) > STOP_DRAWING_ICONS)||(baseShape==null));
            if(drawIcon) {
                img = (BufferedImage) getImage(item, nodeStyle);
            }

            boolean isSelected = isSelected(item);
            boolean isNew = isNew(item);
            boolean isUpdated = isUpdated(item);
            boolean isMultiType = isMultiType(item);
            boolean isBundle = isBundle(item);
            boolean textOnly = ((baseShape == null) && (img == null) && !isBundle);
            boolean hasUrl = hasUrl(item);
            boolean hideLabelsOnNodeType = hideLabelsOnNodeType(item);
            boolean isPathHighlighted = isPathHighlighted(item);

            Area selectionArea = null;

            if ((text == null) && (img == null) && (baseShape == null)) {


                g.setPaint(textColor);
                g.fill(shape);
                return;
            }
//
//            OptionSet optionSet = getOptionSet(item);
            Shape s = null;



            if (isBundle) {

                OptionSet optionSet = null;

                try {
                    optionSet = OptionSetManager.getOptionSet(DEFAULT_OPTION_SET);
                } catch (CentrifugeException e) {
                   LOG.error("Default Optionset is missing");
                }
                int i = (int) (baseSize * (bySize ? size : 1.0) * scaleX);
                BufferedImage bundleImage = null;

                if((theme != null) && (theme.getBundleStyle() != null) && (theme.getBundleStyle().getIconId() != null)){
                    bundleImage = (BufferedImage) m_images.getImage(
                        theme.getBundleStyle().getIconId(), i, i);
                }

                if(bundleImage == null){
                        bundleImage = (BufferedImage) m_images.getImage(new ImageLocation(OptionSetManager
                                .toResourceUrl(optionSet.getBundleIcon())));

                }

                if (bundleImage != null) {
                    ShapeType bundleShape = null;
                    if ((theme != null) && (theme.getBundleStyle() != null) && (theme.getBundleStyle().getShape() != null)) {
                        bundleShape = theme.getBundleStyle().getShape();
                    } else {
                        bundleShape = ShapeType.NONE;
                    }
                    if(bundleShape != null){
                        ShapeType stype = bundleShape;
                        Point2D itemPoint = getItemPoint(item);
                        double w = getBaseSize() * (bySize ? getItemSize(item) : 1.0);
                        s = shapeFactory.getShape(stype, (float) itemPoint.getX(), (float) itemPoint.getY(),
                                (float) w, (float) w);
                        if (s != null) {
                            Color c = ColorLib.getColor(((byTransparency ? computeTransparency(nodeAlpha, item)
                                    : nodeAlpha) << 24) | theme.getBundleStyle().getColor());
                            g.setStroke(new BasicStroke());
                            g.setPaint(c);
                            g.fill(s);
                            if (!isSelected) {
                                g.setPaint(c.darker());
                            }
                            g.draw(s);
                            if ((s != null) && (selectionArea == null)) {
                                selectionArea = new Area(s);
                            }
                        }
                    }
                }
                double itemsize = 0;
                if (drawIcon || (s == null)) {
                    itemsize = baseSize * (bySize ? size : 1.0);

                    Rectangle2D imgRect = renderImage(g, bundleImage, item, itemsize);
                    if ((imgRect != null) && (selectionArea == null)) {
                        selectionArea = new Area(imgRect);
                    }

                    double zoom = BUNDLE_SCALE_DEFAULT;

                    double cx = item.getX();
                    double cy = item.getY();
                    AffineTransform tx = new AffineTransform();
                    tx.translate(cx - (cx * zoom), cy - (cy * zoom));
                    tx.scale(zoom, zoom);
                    baseShape = tx.createTransformedShape(baseShape);
                } else {
                    baseShape = null;
                    img = null;
                }
            }

            // render shape baseShape
            if (baseShape != null) {

                if (selectionArea == null) {
                    selectionArea = new Area(baseShape);
                }
//FIXME: if no shape still need pattern
                renderPatternHighlights(g, item, nodeAlpha, byTransparency, selectionArea, scaleX, scaleY, gc);

                g.setStroke(new BasicStroke());

                g.setPaint(shapeColor);
                g.fill(baseShape);

                if (!isSelected) {
                    g.setPaint(shapeColor.darker());
                }
                g.draw(baseShape);
            }

            // render shape baseShape
            if (baseShape != null) {

                if (selectionArea == null) {
                    selectionArea = new Area(baseShape);
                }

                renderPatternHighlights(g, item, nodeAlpha, byTransparency, selectionArea, scaleX, scaleY, gc);

                g.setStroke(new BasicStroke());

                g.setPaint(shapeColor);
                g.fill(baseShape);

                if (!isSelected) {
                    g.setPaint(shapeColor.darker());
                }
                g.draw(baseShape);
            }

            // render image
            // if there is an shape only render image > 10px
                Rectangle2D imgRect = new Rectangle2D.Float();
            if (drawIcon || ((baseShape == null) && !hasUrl)) {
                double imageShapeScale = Double.parseDouble(getAttribute(item, ObjectAttributes.CSI_INTERNAL_SCALE, nodeStyle, null).toString());
                double itemsize = baseSize * (bySize ? size : 1.0) * scaleX * imageShapeScale;
                if (isBundle(item)) {

                    double sbs = 1.08;
                    if((theme != null) && (theme.getBundleStyle() != null) && (theme.getBundleStyle().getIconScale() != null)){
                        sbs = theme.getBundleStyle().getIconScale();
                    }
                    itemsize *= sbs;
                }
                img = (BufferedImage) getImage(item, DoubleMath.roundToInt(itemsize, RoundingMode.CEILING));
                itemsize /= scaleX; // get back in the graphics mindset.
                Composite orig = g.getComposite();
                float alphaValue = new Float((byTransparency ? computeTransparency(nodeAlpha, item) : nodeAlpha) / 255.0);
                {//sanitize alpha value
                    alphaValue = Math.max(0, alphaValue);
                    alphaValue = Math.min(1,alphaValue);
                }
                AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue);
                g.setComposite(alpha);
                imgRect = renderImage(g, img, item, itemsize);
                if ((imgRect != null) && (selectionArea == null)) {
                    selectionArea = new Area(imgRect);

                }
                g.setComposite(orig);
            } else if(hasUrl && (baseShape == null)) {
                //This is the case where it has a url image but isn't drawing it because the graph is too small, and there is also no shape
                Point2D itemPoint = getItemPoint(item);
                double x = itemPoint.getX();
                double y = itemPoint.getY();
                double width = getBaseSize() * (bySize ? getItemSize(item) : 1.0);
                baseShape = shapeFactory.getShape(ShapeType.SQUARE, (float) x, (float) y, (float) width, (float) width);
                if (selectionArea == null) {
                    selectionArea = new Area(baseShape);
                }
                //FIXME: if no shape still need pattern
                renderPatternHighlights(g, item, nodeAlpha, byTransparency, selectionArea, scaleX, scaleY, gc);

                g.setStroke(new BasicStroke());

                g.setPaint(Color.DARK_GRAY);
                g.fill(baseShape);

                if (!isSelected) {
                    g.setPaint(shapeColor.darker());
                }
                g.draw(baseShape);
            }

            if (isMultiType) {
                Rectangle sabounds = shape.getBounds();
                if (selectionArea != null) {
                    sabounds = selectionArea.getBounds();
                }
                g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                g.setPaint(ColorLib.getColor(0, 255, 0, (byTransparency ? computeTransparency(nodeAlpha, item)
                        : nodeAlpha)));
                g.drawRect((int) sabounds.getX() - 2, (int) sabounds.getY() - 2, (int) sabounds.getWidth() + 4,
                        (int) sabounds.getHeight() + 4);
            }

            // render text
            if ((text != null) && !isHideLabels() && !hideLabelsOnNodeType) {
                g.setStroke(new BasicStroke());
                FontMetrics fm = g.getFontMetrics(m_font);
                double tx = (shape.getCenterX() - (m_textDim.width / 2)) + 5;
                double ty = shape.getY() + 5;

                if (textOnly) {
                    double x = item.getX() - (m_textDim.width / 2);
                    double y = item.getY() - (m_textDim.height / 2);
                    ty = y;

                    Rectangle2D textRect = new Rectangle2D.Double(x, y, m_textDim.width, m_textDim.height);

                    g.setPaint(ColorLib.getColor(255, 255, 255,labelAlpha));
                    g.fill(textRect);
                    g.setPaint(ColorLib.getColor(0, 0, 0, labelAlpha));

                    g.draw(textRect);

                    if (selectionArea == null) {
                        selectionArea = new Area(textRect);
                    } else {
                        selectionArea.add(new Area(textRect));
                    }
                }

                if ((fm.getHeight() * (bySize ? size_in : 1.0) * scaleY) > 6) {
                    ty = ty + fm.getAscent();
                    String value = rgdef.getSettings().getPropertiesMap().get("csi.relgraph.backgroundColor");
                    if (value != null) {
                        try {
                            int color = Integer.parseInt(value);
                            if (color == 16777215) { //TODO:looks like this matters
                                color = 16711422;
                            }
                            Color bgc = new Color(color);
                            //L = 0.2126 * Rg + 0.7152 * Gg + 0.0722 * Bg,
                            float bgluminance = (float) ((.2126 * bgc.getRed()) + (.7152 * bgc.getGreen()) + (.0722 * bgc.getBlue())) / 255;
                            if (bgluminance > .5) {
                                g.setPaint(ColorLib.getColor(0, 0, 0, labelAlpha));
                            } else {
                                g.setPaint(ColorLib.getColor(255, 255, 255, labelAlpha));
                            }
                        } catch (Exception e) {

                        }
                        g.setFont(m_font);
                        g.drawString(text, (int) tx, (int) ty);
                    }
                }
            }
            if (baseShape == null) {
                if (selectionArea == null) {
                    double itemsize = baseSize * (bySize ? size : 1.0) * scaleX;
                    selectionArea = new Area(new Rectangle2D.Double(item.getX()-(itemsize/2.0), item.getY()-(itemsize/2.0), itemsize, itemsize));
                }
                renderPatternHighlights(g, item, nodeAlpha, byTransparency, selectionArea, scaleX, scaleY, gc);
            }

            new PlunkedNodeDrawer(getImageFactory()).renderIconIfPlunked(g, item, details);

            if (isNew || isUpdated) {
                Rectangle sabounds = selectionArea.getBounds();
                double cx = sabounds.getCenterX();
                double cy = sabounds.getCenterY();
                double w = sabounds.getWidth();
                double h = sabounds.getHeight();

                double penSize = 3;
                double zoom = 1.08;
                double absSize = penSize;
                if (w > h) {
                    if (scaleX < 1) {
                        absSize = penSize / scaleX;
                    }
                    zoom = (w + (absSize * 1.5)) / w;
                } else {
                    if (scaleY < 1) {
                        absSize = penSize / scaleY;
                    }
                    zoom = (h + (absSize * 1.5)) / h;
                }

                BasicStroke genStroke = new BasicStroke((int) absSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                g.setStroke(genStroke);
                Color genColor = null;
                if (isNew) {
                    genColor = Configuration.getInstance().getGraphAdvConfig().getDefaultNewGenColor();
                } else {
                    genColor = Configuration.getInstance().getGraphAdvConfig().getDefaultUpdateGenColor();
                }

                AffineTransform tx = new AffineTransform();
                tx.translate(cx - (cx * zoom), cy - (cy * zoom));
                tx.scale(zoom, zoom);
                selectionArea.transform(tx);

                g.setPaint(genColor);
                g.draw(selectionArea);
            }

//            if (hasUrl) {
//                double imgsize = 8 * (bySize ? size : 1.0);
//
//                double itemsize = baseSize * (bySize ? size : 1.0);
//                double cx = item.getX() + ((itemsize - imgsize) / 2);
//                double cy = item.getY() + ((itemsize - imgsize) / 2);
//
//                linkImg = (BufferedImage) m_images.getImage(new ImageLocation(LINK_ICON_URL), (int) imgsize,
//                        (int) imgsize);
//                if (linkImg != null) {
//                    Rectangle2D imgRect = renderCenteredImage(g, linkImg, cx, cy, imgsize);
//                    if ((imgRect != null) && (selectionArea == null)) {
//                        selectionArea = new Area(imgRect);
//                    }
//                }
//            }

            if (isSelected && (selectionArea != null)) {
                Rectangle sabounds = selectionArea.getBounds();
                double cx = sabounds.getCenterX();
                double cy = sabounds.getCenterY();
                double w = sabounds.getWidth();
                double h = sabounds.getHeight();

                double penSize = 3;
                double zoom = 1.0f;
                double absSize = penSize;
                if (w > h) {
                    if (scaleX < 1) {
                        absSize = penSize / scaleX;
                    }
                    zoom = (w + absSize) / w;
                } else {
                    if (scaleY < 1) {
                        absSize = penSize / scaleY;
                    }
                    zoom = (h + absSize) / h;
                }

                AffineTransform tx = new AffineTransform();
                tx.translate(cx - (cx * zoom), cy - (cy * zoom));
                tx.scale(zoom, zoom);
                selectionArea.transform(tx);

                BasicStroke stroke = new BasicStroke((float)Math.abs(absSize), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                g.setStroke(stroke);
                Color defSelect = Configuration.getInstance().getGraphAdvConfig().getDefaultSelectionColor();
                Color c = ColorLib.getColor(defSelect.getRed(), defSelect.getGreen(), defSelect.getBlue(),
                        (byTransparency ? computeTransparency(nodeAlpha, item) : nodeAlpha));
                g.setPaint(c);
                g.draw(selectionArea);
            }

            if (isPathHighlighted && (selectionArea != null)) {
                Rectangle sabounds = selectionArea.getBounds();
                double cx = sabounds.getCenterX();
                double cy = sabounds.getCenterY();
                double w = sabounds.getWidth();
                double h = sabounds.getHeight();

                double penSize = 3;
                double zoom = 1.0f;
                double absSize = penSize;
                if (w > h) {
                    if (scaleX < 1) {
                        absSize = penSize / scaleX;
                    }
                    zoom = (w + absSize) / w;
                } else {
                    if (scaleY < 1) {
                        absSize = penSize / scaleY;
                    }
                    zoom = (h + absSize) / h;
                }

                AffineTransform tx = new AffineTransform();
                tx.translate(cx - (cx * zoom), cy - (cy * zoom));
                tx.scale(zoom, zoom);
                selectionArea.transform(tx);

                BasicStroke stroke = new BasicStroke((int) absSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                g.setStroke(stroke);
                Color defHilite = Configuration.getInstance().getGraphAdvConfig().getPathHighlightColor();
                Color c = ColorLib.getColor(defHilite.getRed(), defHilite.getGreen(), defHilite.getBlue(),
                        (byTransparency ? computeTransparency(nodeAlpha, item) : nodeAlpha));
                g.setPaint(c);
                g.draw(selectionArea);
            }

            if (item.isHighlighted()) {
                Rectangle sabounds = selectionArea.getBounds();
                double cx = sabounds.getCenterX();
                double cy = sabounds.getCenterY();
                double w = sabounds.getWidth();
                double h = sabounds.getHeight();

                double penSize = 2;
                double zoom = 1.0f;
                double absSize = penSize;
                if (w > h) {
                    if (scaleX < 1) {
                        absSize = penSize / scaleX;
                    }
                    zoom = (w + absSize) / w;
                } else {
                    if (scaleY < 1) {
                        absSize = penSize / scaleY;
                    }
                    zoom = (h + absSize) / h;
                }

                AffineTransform tx = new AffineTransform();
                tx.translate(cx - (cx * zoom), cy - (cy * zoom));
                tx.scale(zoom, zoom);
                selectionArea.transform(tx);

                BasicStroke stroke = new BasicStroke((int) absSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

                g.setStroke(stroke);
                Color defHilite = Configuration.getInstance().getGraphAdvConfig().getHighlightColor();
                Color c = ColorLib.getColor(defHilite.getRed(), defHilite.getGreen(), defHilite.getBlue(),
                        (byTransparency ? computeTransparency(nodeAlpha, item) : nodeAlpha));
                g.setPaint(c);
                g.draw(selectionArea);
            }

        } finally {
            details.setSizeMode(sizeMode);
            item.setSize(size_in);
            g.setStroke(origStroke);
            g.setPaint(origPaint);
            g.setColor(origColor);
        }
    }

    private void renderPatternHighlights(Graphics2D g, VisualItem item, int nodeAlpha, boolean byTransparency, Area selectionArea, double scaleX, double scaleY, GraphContext graphContext) {
        AffineTransform oldTransform = g.getTransform();
        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();
        {
            Rectangle bounds = new Rectangle(selectionArea.getBounds());
            double cx = bounds.getCenterX();
            double cy = bounds.getCenterY();
            double w = bounds.getWidth();
            double h = bounds.getHeight();

            double penSize = 4;
            double zoom;
            double absSize = penSize;
            if (w > h) {
                if (scaleX < 1) {
                    absSize = penSize / scaleX;
                }
                zoom = (w + absSize) / w;
            } else {
                if (scaleY < 1) {
                    absSize = penSize / scaleY;
                }
                zoom = (h + absSize) / h;
            }
            AffineTransform tx = new AffineTransform();
            if((cy < 0) && (cx < 0)){
                tx.translate(Math.abs(cx - (cx * zoom)), Math.abs(cy - (cy * zoom)));
            } else {
                tx.translate(cx - (cx * zoom), cy - (cy * zoom));
            }
            tx.scale(zoom, zoom);
            Area a = new Area(selectionArea);
            a.transform(tx);

            absSize = Math.abs(absSize);
            g.setStroke(new BasicStroke((int) absSize));
            List<PatternSelection> matchingPatterns = new ArrayList<PatternSelection>();
            List<PatternSelection> patternHighlights = graphContext.getPatternHighlights();
            if (patternHighlights != null) {

                for (PatternSelection patternSelection : patternHighlights) {
                    int itemRow = item.getRow();
                    SelectionModel defSelectionModel = patternSelection.getSelectionModel();
                    if (defSelectionModel != null) {
                        if (defSelectionModel.nodes.contains(itemRow)) {
                            matchingPatterns.add(patternSelection);
                        }
                    }
                }
            }
            int numberOfMatches = matchingPatterns.size();
            if (isSelected(item)) {
                numberOfMatches++;
            }
            absSize /= 2;
            g.translate(-absSize * (numberOfMatches), absSize * (numberOfMatches));
            for (PatternSelection matchingPattern : matchingPatterns) {
                g.translate(absSize, -absSize);
                try {
                    Color c3 = new Color(Integer.parseInt(matchingPattern.getColor()));
                    g.setPaint(c3);
                    g.draw(a);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        g.setTransform(oldTransform);
        g.setStroke(oldStroke);
        g.setColor(oldColor);
    }

    /********************************************************************/
    /********************************************************************/
    /********************************************************************/
    /********************************************************************/

    /**
     * @param _imageShapeScale ****************************************************************************/
    public void renderSingleNode(Graphics2D g, NodeStore details, boolean isHighlighted, String text, Shape baseShape,
            double _imageShapeScale) {
        if (details == null) {
            return;
        }
        Stroke origStroke = g.getStroke();
        Color origColor = g.getColor();
        Paint origPaint = g.getPaint();
        try {

        } finally {
            g.setStroke(origStroke);
            g.setPaint(origPaint);
            g.setColor(origColor);
        }
    }

    /**
     * @param alpha should be a value from 255 to be blended with the transparency of the nodeItem
     * @return alpha value from 0-255
     */
    private int computeTransparency(int alpha, VisualItem item) {
        NodeStore details = getNodeDetails(item);
        double alphaDouble = alpha;
        double nodeTransparency = details.getTransparency() / 100d;
        return (int) (nodeTransparency * alphaDouble);
    }

    private boolean hasUrl(VisualItem item) {
        NodeStore details = getNodeDetails(item);
        if (details == null) {
            return false;
        }

        Map<String, Property> attributes = details.getAttributes();
        if (attributes == null) {
            return false;
        }

        Property prop = attributes.get(ObjectAttributes.CSI_INTERNAL_URL);
        if (prop == null) {
            if((details.getIcon() != null) && !details.getIcon().isEmpty()) {
                String icon = details.getIcon();
                Matcher regexMatcher = REGEX.matcher(icon);
                return regexMatcher.find();
            }
        } else {
            List<Object> values = prop.getValues();
            return (values != null) && !values.isEmpty();
        }

        return false;

    }

    /**
     * Render an image anchored to (0,0)
     *
     * @param g
     * @param imageLocation
     * @param boxSize
     * @param iconSize
     */
    public void renderImage(Graphics2D g, ImageLocation imageLocation, double boxSize, double iconSize) {
        checkNotNull(imageLocation);
        BufferedImage img = (BufferedImage) m_images.getImage(imageLocation,
                DoubleMath.roundToInt(iconSize, RoundingMode.HALF_EVEN),
                DoubleMath.roundToInt(iconSize, RoundingMode.HALF_EVEN));
        renderCenteredImage(g, img, boxSize / 2D, boxSize / 2D, iconSize);
    }

    /**
     * Render an image anchored to (0,0)
     *
     * @param g
     * @param imageLocation
     * @param boxSize
     * @param iconSize
     * @throws IOException
     */
   public void renderImage(Graphics2D g, String id, String icon, double boxSize, double iconSize) throws IOException {
      byte[] imageData = Base64.getDecoder().decode(icon);

      try (InputStream in = new ByteArrayInputStream(imageData)) {
         BufferedImage image = ImageIO.read(in);
         BufferedImage scaledImage =
            (BufferedImage) m_images.getImage(image, id,
                                              DoubleMath.roundToInt(iconSize, RoundingMode.HALF_EVEN),
                                              DoubleMath.roundToInt(iconSize, RoundingMode.HALF_EVEN));
         renderCenteredImage(g, scaledImage, boxSize / 2D, boxSize / 2D, iconSize);
      }
   }

    public void renderBundleImage(Graphics2D g, double boxSize, double iconSize) throws IOException {
            OptionSet optionSet;
            try {
                optionSet = OptionSetManager.getOptionSet(DEFAULT_OPTION_SET);
                BufferedImage scaledImage = (BufferedImage)m_images.getImage(new ImageLocation(OptionSetManager
                        .toResourceUrl(optionSet.getBundleIcon())),
                        DoubleMath.roundToInt(iconSize, RoundingMode.HALF_EVEN),
                        DoubleMath.roundToInt(iconSize, RoundingMode.HALF_EVEN));

                renderCenteredImage(g, scaledImage, boxSize / 2D, boxSize / 2D, iconSize);
            } catch (CentrifugeException e) {
            }

    }



	public Image renderImage(String icon) throws CentrifugeException, IOException {
		BufferedImage image = null;
      byte[] imageData = Base64.getDecoder().decode(icon);

		try (InputStream in = new ByteArrayInputStream(imageData)) {
		   image = ImageIO.read(in);
		}
		return image;
	}

    public void renderImage(Graphics2D g, String id, Image image, double boxSize, double iconSize) {
        BufferedImage scaledImage = (BufferedImage) m_images.getImage(image, id,
                DoubleMath.roundToInt(iconSize, RoundingMode.HALF_EVEN),
                DoubleMath.roundToInt(iconSize, RoundingMode.HALF_EVEN));
        renderCenteredImage(g, scaledImage, boxSize / 2D, boxSize / 2D, iconSize);
    }

    private Rectangle2D renderImage(Graphics2D g, BufferedImage bimg, VisualItem item, double itemsize) {
        if ((item == null) || (bimg == null)) {
            return null;
        }

        // align image centered around item's x,y
        double cx = item.getX();
        double cy = item.getY();

        return renderCenteredImage(g, bimg, cx, cy, itemsize);
    }

    /**
     *
     * @param g           The graphics
     * @param bimg        The image to render
     * @param cx          The left bound of the image on the graphics
     * @param cy          The upper bound of the image on the graphics
     * @param itemsize    The size at which to render the image
     * @return
     */
   private Rectangle2D renderCenteredImage(Graphics2D g, BufferedImage bimg, double cx, double cy, double itemsize) {
      Rectangle2D result = null;

      if (bimg != null) {
         double iw = bimg.getWidth(null);
         double ih = bimg.getHeight(null);

         if ((BigDecimal.valueOf(iw).compareTo(BigDecimal.ZERO) != 0) &&
             (BigDecimal.valueOf(ih).compareTo(BigDecimal.ZERO) != 0)) {
            double zoom = 0;

            if (iw > ih) {
               zoom = itemsize / iw;
            } else {
               zoom = itemsize / ih;
            }
            double ix = cx - (iw / 2);
            double iy = cy - (ih / 2);
            AffineTransform origTx = g.getTransform();

            try {
               AffineTransform tx = new AffineTransform();

               tx.translate(cx - (cx * zoom), cy - (cy * zoom));
               tx.scale(zoom, zoom);
               g.transform(tx);
               g.drawImage(bimg, null, (int) ix, (int) iy);

               Rectangle2D imgRect = new Rectangle2D.Double(ix, iy, iw, ih);
               result = tx.createTransformedShape(imgRect).getBounds2D();
            } finally {
               g.setTransform(origTx);
            }
         }
      }
      return result;
   }

    private Object getAttribute(VisualItem item, String attribute, NodeStyle nodeStyle, ShapeType shape) {

        NodeStore detail = getNodeDetails(item);
        if (detail == null) {
            return null;
        }

        TypeInfo typeInfo = getItemTypeInfo(item, detail.getType());
        if (detail.isBundle() && (detail.getFirstType() != null)) {
            typeInfo = getItemTypeInfo(item, detail.getFirstType());
        }

        if(nodeStyle == null){

            GraphContext gc = GraphContext.Current.get();
            if(gc != null){
                GraphTheme theme = gc.getTheme();
                nodeStyle = null;
                if (theme != null) {
                    String nodeType = detail.getType();
                    if (detail.isBundle() && (detail.getFirstType() != null)){
                        nodeType = detail.getFirstType();
                    }
                    nodeStyle = theme.findNodeStyle(nodeType);

                    if((nodeStyle == null) && (theme.getDefaultShape() != null)){
                        shape = theme.getDefaultShape();
                    }
                }
            }
        }
        return GraphAttributeHelper.resolveNodeAttribute(attribute, detail, typeInfo, nodeStyle, shape);
    }

    private boolean isMultiType(VisualItem item) {
        NodeStore details = getNodeDetails(item);
        if (details == null) {
            return false;
        }

        return (!details.isBundle() && (details.getTypes().size() > 1));
    }

    private boolean isBundle(VisualItem item) {
        NodeStore details = getNodeDetails(item);
        if (details == null) {
            return false;
        }
        return (details.isBundle());
    }

    private Color getTextColor(VisualItem item) {

        Color textColor = Color.BLACK;

        NodeStore details = getNodeDetails(item);
        if (details == null) {
            return textColor;
        }

        // if (details.isBundle()) {
        // if (details.getTypes().size() == 1) {
        // textColor = Color.BLUE;
        // } else {
        // textColor = Color.GREEN;
        // }
        // }

        return textColor;
    }

    private int getAlpha(VisualItem item) {
        int fillColor = item.getFillColor();
        int alpha = ColorLib.alpha(fillColor);
        return alpha;
    }

    private boolean isPathHighlighted(VisualItem item) {
        boolean isHighlighted = false;
        int itemRow = item.getRow();
        Visualization vis = item.getVisualization();
        Graph graph = (Graph) vis.getSourceData("graph");
        Map<String, SelectionModel> selections = (Map<String, SelectionModel>) graph
                .getClientProperty(GraphConstants.SELECTIONS);
        if (selections != null) {
            SelectionModel defSelectionModel = selections.get(GraphConstants.PATH_HIGHLIGHT);
            if (defSelectionModel != null) {
                isHighlighted = defSelectionModel.nodes.contains(itemRow);
            }
        }
        return isHighlighted;
    }

    private boolean isSelected(VisualItem item) {
        boolean isSelected = false;
        int itemRow = item.getRow();
        Visualization vis = item.getVisualization();
        Graph graph = (Graph) vis.getSourceData("graph");
        Map<String, SelectionModel> selections = (Map<String, SelectionModel>) graph
                .getClientProperty(GraphConstants.SELECTIONS);
        if (selections != null) {
            SelectionModel defSelectionModel = selections.get(GraphManager.DEFAULT_SELECTION);
            if (defSelectionModel != null) {
                isSelected = defSelectionModel.nodes.contains(itemRow);
            }
        }
        return isSelected;
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
                isNew = newGenSelectionModel.nodes.contains(itemRow);
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
                isNew = newGenSelectionModel.nodes.contains(itemRow);
            }
        }
        return isNew;
    }

    private Color getBaseColor(VisualItem item, NodeStyle nodeStyle) {
        NodeStore details = getNodeDetails(item);
        if (details == null) {
            return DEFAULT_BASE_COLOR;
        }

        Integer itemColor = (Integer) getAttribute(item, ObjectAttributes.CSI_INTERNAL_COLOR, nodeStyle, null);

        if (itemColor != null) {
            return ColorLib.getColor(0xff000000 | itemColor);
        } else {
            return DEFAULT_BASE_COLOR;
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


    // ------------------------------------------------------------------------

    /**
     * Sets the image factory used by this renderer.
     *
     * @param ifact
     *            the image factory
     */
    public void setImageFactory(ImageFactory ifact) {
        m_images = ifact;
    }

    /**
     * Get the horizontal text alignment within the layout. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     *
     * @return the horizontal text alignment
     */
    public int getHorizontalTextAlignment() {
        return m_hTextAlign;
    }

    /**
     * Set the horizontal text alignment within the layout. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     *
     * @param halign
     *            the desired horizontal text alignment
     */
    public void setHorizontalTextAlignment(int halign) {
        if ((halign != Constants.LEFT) && (halign != Constants.RIGHT) && (halign != Constants.CENTER)) {
            throw new IllegalArgumentException("Illegal horizontal text alignment value.");
        }
        m_hTextAlign = halign;
    }

    /**
     * Get the vertical text alignment within the layout. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     *
     * @return the vertical text alignment
     */
    public int getVerticalTextAlignment() {
        return m_vTextAlign;
    }

    /**
     * Set the vertical text alignment within the layout. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}. The default is centered text.
     *
     * @param valign
     *            the desired vertical text alignment
     */
    public void setVerticalTextAlignment(int valign) {
        if ((valign != Constants.TOP) && (valign != Constants.BOTTOM) && (valign != Constants.CENTER)) {
            throw new IllegalArgumentException("Illegal vertical text alignment value.");
        }
        m_vTextAlign = valign;
    }

    /**
     * Get the horizontal image alignment within the layout. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}. The default is a centered image.
     *
     * @return the horizontal image alignment
     */
    public int getHorizontalImageAlignment() {
        return m_hImageAlign;
    }

    /**
     * Set the horizontal image alignment within the layout. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}. The default is a centered image.
     *
     * @param halign
     *            the desired horizontal image alignment
     */
    public void setHorizontalImageAlignment(int halign) {
        if ((halign != Constants.LEFT) && (halign != Constants.RIGHT) && (halign != Constants.CENTER)) {
            throw new IllegalArgumentException("Illegal horizontal text alignment value.");
        }
        m_hImageAlign = halign;
    }

    /**
     * Get the vertical image alignment within the layout. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}. The default is a centered image.
     *
     * @return the vertical image alignment
     */
    public int getVerticalImageAlignment() {
        return m_vImageAlign;
    }

    /**
     * Set the vertical image alignment within the layout. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}. The default is a centered image.
     *
     * @param valign
     *            the desired vertical image alignment
     */
    public void setVerticalImageAlignment(int valign) {
        if ((valign != Constants.TOP) && (valign != Constants.BOTTOM) && (valign != Constants.CENTER)) {
            throw new IllegalArgumentException("Illegal vertical text alignment value.");
        }
        m_vImageAlign = valign;
    }

    /**
     * Get the image position, determining where the image is placed with
     * respect to the text. One of {@link Constants#LEFT},
     * {@link Constants#RIGHT}, {@link Constants#TOP}, or
     * {@link Constants#BOTTOM}. The default is left.
     *
     * @return the image position
     */
    public int getImagePosition() {
        return m_imagePos;
    }


    // ------------------------------------------------------------------------

    /**
     * Set the image position, determining where the image is placed with
     * respect to the text. One of {@link Constants#LEFT},
     * {@link Constants#RIGHT}, {@link Constants#TOP}, or
     * {@link Constants#BOTTOM}. The default is left.
     *
     * @param pos
     *            the desired image position
     */
    public void setImagePosition(int pos) {
        if ((pos != Constants.TOP) && (pos != Constants.BOTTOM) && (pos != Constants.LEFT) && (pos != Constants.RIGHT)
                && (pos != Constants.CENTER)) {
            throw new IllegalArgumentException("Illegal image position value.");
        }
        m_imagePos = pos;
    }

    /**
     * Get the horizontal alignment of this node with respect to its x, y
     * coordinates.
     *
     * @return the horizontal alignment, one of {@link prefuse.Constants#LEFT},
     *         {@link prefuse.Constants#RIGHT}, or
     *         {@link prefuse.Constants#CENTER}.
     */
    public int getHorizontalAlignment() {
        return m_xAlign;
    }

    /**
     * Set the horizontal alignment of this node with respect to its x, y
     * coordinates.
     *
     * @param align
     *            the horizontal alignment, one of
     *            {@link prefuse.Constants#LEFT},
     *            {@link prefuse.Constants#RIGHT}, or
     *            {@link prefuse.Constants#CENTER}.
     */
    public void setHorizontalAlignment(int align) {
        m_xAlign = align;
    }

    /**
     * Get the vertical alignment of this node with respect to its x, y
     * coordinates.
     *
     * @return the vertical alignment, one of {@link prefuse.Constants#TOP},
     *         {@link prefuse.Constants#BOTTOM}, or
     *         {@link prefuse.Constants#CENTER}.
     */
    public int getVerticalAlignment() {
        return m_yAlign;
    }

    /**
     * Set the vertical alignment of this node with respect to its x, y
     * coordinates.
     *
     * @param align
     *            the vertical alignment, one of {@link prefuse.Constants#TOP},
     *            {@link prefuse.Constants#BOTTOM}, or
     *            {@link prefuse.Constants#CENTER}.
     */
    public void setVerticalAlignment(int align) {
        m_yAlign = align;
    }

    /**
     * Returns the amount of padding in pixels between the content and the
     * border of this item along the horizontal dimension.
     *
     * @return the horizontal padding
     */
    public int getHorizontalPadding() {
        return m_horizBorder;
    }

    /**
     * Sets the amount of padding in pixels between the content and the border
     * of this item along the horizontal dimension.
     *
     * @param xpad
     *            the horizontal padding to set
     */
    public void setHorizontalPadding(int xpad) {
        m_horizBorder = xpad;
    }

    /**
     * Returns the amount of padding in pixels between the content and the
     * border of this item along the vertical dimension.
     *
     * @return the vertical padding
     */
    public int getVerticalPadding() {
        return m_vertBorder;
    }

    /**
     * Sets the amount of padding in pixels between the content and the border
     * of this item along the vertical dimension.
     *
     * @param ypad
     *            the vertical padding
     */
    public void setVerticalPadding(int ypad) {
        m_vertBorder = ypad;
    }

    /**
     * Get the padding, in pixels, between an image and text.
     *
     * @return the padding between an image and text
     */
    public int getImageTextPadding() {
        return m_imageMargin;
    }

    /**
     * Set the padding, in pixels, between an image and text.
     *
     * @param pad
     *            the padding to use between an image and text
     */
    public void setImageTextPadding(int pad) {
        m_imageMargin = pad;
    }

    public void setSizeFunction(Function<VisualItem, Double> function) {
        sizeFunction = function;

    }


    /**
     * Returns the base size, in pixels, for shapes drawn by this renderer.
     *
     * @return the base size in pixels
     */
    public int getBaseSize() {
        return m_baseSize;
    }

    /**
     * Sets the base size, in pixels, for shapes drawn by this renderer. The
     * base size is the width and height value used when a VisualItem's size
     * value is 1. The base size is scaled by the item's size value to arrive at
     * the final scale used for rendering.
     *
     * @param size
     *            the base size in pixels
     */
    public void setBaseSize(int size) {
        m_baseSize = size;
    }

    public double getItemSize(VisualItem item) {
        return sizeFunction.apply(item);
    }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
   protected Shape getBaseShape(VisualItem item, boolean bySize, NodeStyle nodeStyle) {
      Point2D itemPoint = getItemPoint(item);
      double x = itemPoint.getX();
      double y = itemPoint.getY();
      double width = getBaseSize() * (bySize ? getItemSize(item) : 1.0);
      ShapeType stype = null;
      NodeStore details = getNodeDetails(item);

      if (details == null) {
         stype = ShapeType.SQUARE;
      } else {
         // multi type bundle have no shape
         if (!details.isBundle() || (details.getTypes().size() <= 1)) {
            Object shape = getAttribute(item, ObjectAttributes.CSI_INTERNAL_SHAPE, nodeStyle, null);

            if (shape != null) {
               if (shape instanceof ShapeType) {
                  stype = (ShapeType) shape;
               } else if (shape instanceof String) {
                  try {
                     stype = ShapeType.getShape((String) shape);
                  } catch(Exception e) {
                  }
               }
            }
         }
      }
      return (stype == null) ? null : shapeFactory.getShape(stype, (float) x, (float) y, (float) width, (float) width);
   }

    /**
     * Paints a colored shape.
     *
     * @param graphics    a graphics instance
     * @param shapeColor  color of shape to be drawn.
     * @param shapeType   type of shape to be drawn.
     * @param isSelected  if the shape is not selected it is stroked with a darker color.
     * @param x           x coordinate representing upper left corner of the square.
     * @param y           y coordinate representing upper left corner of the square.
     * @param width       width of the shape, must be greater than 0.
     * @param height      height of the shape, must be greater than 0.
     */
    public void renderShape(Graphics2D graphics, boolean isMap, Color shapeColor, ShapeType shapeType, boolean isSelected, boolean isHighlighted, boolean isCombined, boolean isUseSummary,
    		float x, float y, float width, float height, int strokeSize) {
    	renderShape(graphics, isMap, shapeColor, shapeType, isSelected, isHighlighted, isCombined, isUseSummary, false, false, x, y, width, height, strokeSize);
    }

    public void renderShape(Graphics2D graphics, boolean isMap, Color shapeColor, ShapeType shapeType, boolean isSelected, boolean isHighlighted, boolean isCombined, boolean isUseSummary, boolean isNew, boolean isUpdated,
    		float x, float y, float width, float height, int strokeSize) {
        checkNotNull(graphics);
        checkNotNull(shapeColor);
        checkNotNull(shapeType);
        checkArgument(width > 0, "Width must be greater than zero.");
        checkArgument(height > 0, "Width must be greater than zero.");

        ShapeFactory myShapeFactory = new ShapeFactory();
        float newWidth = width;
        float newHeight = height;
        if (isMap) {
        	if (isSelected || isNew || isUpdated) {
        		if (isSelected && isNew && isUpdated) {
        			newWidth -= 12;
    				newHeight -= 12;
        		} else if ((isSelected && isNew) || (isSelected && isUpdated) || (isNew && isUpdated)) {
        			newWidth -= 10;
    				newHeight -= 10;
        		} else {
        			newWidth -= 8;
    				newHeight -= 8;
        		}
    		} else {
    			if (isHighlighted) {
    				newWidth -= 6;
    				newHeight -= 6;
    			}
    		}
    		if (isCombined) {
    			newWidth -= 2;
    			newHeight -= 2;
    		}
        }
        Shape baseShape = myShapeFactory.getShape(shapeType, x, y, newWidth, newHeight);
        if (baseShape != null) {
            graphics.setStroke(new BasicStroke(strokeSize));
            graphics.setPaint(shapeColor);
            if (isUseSummary) {
               graphics.draw(baseShape);
            } else {
               graphics.fill(baseShape);
            }
        }

        if (isCombined) {
        	if (baseShape == null) {
        		baseShape = myShapeFactory.getShape(ShapeType.SQUARE, x, y, newWidth, newHeight);
        	}
            Rectangle sabounds = baseShape.getBounds();
            graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.setPaint(ColorLib.getColor(0, 255, 0));
            if (isMap) {
            	int diff = 1;
            	if (isSelected || isNew || isUpdated) {
            		if (isSelected && isNew && isUpdated) {
            			diff += 6;
            		} else if ((isSelected && isNew) || (isSelected && isUpdated) || (isNew && isUpdated)) {
            			diff += 5;
            		} else {
            			diff += 4;
            		}
        		} else {
        			if (isHighlighted) {
        				diff += 3;
        			}
        		}

                graphics.drawRect((int) sabounds.getX() - diff, (int) sabounds.getY() - diff, (int) sabounds.getWidth() + (2 * diff),
                        (int) sabounds.getHeight() + (2 * diff));
            } else {
            	Area selectionArea = new Area(baseShape);
            	sabounds = selectionArea.getBounds();
                graphics.drawRect((int) sabounds.getX() - 2, (int) sabounds.getY() - 2, (int) sabounds.getWidth() + 4,
                        (int) sabounds.getHeight() + 4);
            }
        }

        if (isUpdated) {
            Color c = Configuration.getInstance().getGraphAdvConfig().getDefaultUpdateGenColor();
        	int addition = 4;
        	if (isNew) {
            addition++;
         }
            drawShapeOnGraphics(graphics, c, addition, myShapeFactory, shapeType, x, y, newWidth, newHeight);
        }

        if (isNew) {
            Color c = Configuration.getInstance().getGraphAdvConfig().getDefaultNewGenColor();
        	int addition = 4;
            drawShapeOnGraphics(graphics, c, addition, myShapeFactory, shapeType, x, y, newWidth, newHeight);
        }

        if (isHighlighted) {
            Color defHilite = Configuration.getInstance().getGraphAdvConfig().getHighlightColor();
            Color c = ColorLib.getColor(defHilite.getRed(), defHilite.getGreen(), defHilite.getBlue());
        	int addition = 3;
            drawShapeOnGraphics(graphics, c, addition, myShapeFactory, shapeType, x, y, newWidth, newHeight);
        }

        // NOTE: behavior carried forward from render()
        if (isSelected) {
            Color defSelect = Configuration.getInstance().getGraphAdvConfig().getDefaultSelectionColor();
            Color c = ColorLib.getColor(defSelect.getRed(), defSelect.getGreen(), defSelect.getBlue());
        	int addition = 4;
        	if (isUpdated) {
            addition++;
         }
        	if (isNew) {
            addition++;
         }
            drawShapeOnGraphics(graphics, c, addition, myShapeFactory, shapeType, x, y, newWidth, newHeight);
        } else {
        	if (!isMap) {
        		graphics.setPaint(shapeColor.darker());
        		graphics.draw(baseShape);
        	}
        }
    }

	private void drawShapeOnGraphics(Graphics2D graphics, Color color, int addition, ShapeFactory myShapeFactory,
			ShapeType shapeType, float x, float y, float newWidth, float newHeight) {
		graphics.setPaint(color);

		double absSize = 3;

		BasicStroke stroke = new BasicStroke((float)Math.abs(absSize), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		graphics.setStroke(stroke);

		Shape shape;
		if ((shapeType == null) || (shapeType == ShapeType.NONE)) {
			shape = myShapeFactory.getShape(ShapeType.SQUARE, x, y, (newWidth+addition)-1, (newHeight+addition)-1);
		} else {
			shape = myShapeFactory.getShape(shapeType, x, y, (newWidth+addition)-1, (newHeight+addition)-1);
		}
		graphics.draw(shape);
	}

    /**
     * Paints a colored shape.
     *
     * @param graphics    a graphics instance
     * @param shapeColor  color of shape to be drawn.
     * @param shapeType   type of shape to be drawn.
     * @param isSelected  if the shape is not selected it is stroked with a darker color.
     * @param x           x coordinate representing upper left corner of the square.
     * @param y           y coordinate representing upper left corner of the square.
     * @param size        size of the shape, must be greater than 0.
     *
     * @see NodeRenderer#renderShape(Graphics2D, Color, ShapeType, boolean, float, float, float, float)
     *
     */
    public void renderShape(Graphics2D graphics, Color shapeColor, ShapeType shapeType, boolean isSelected, boolean isHighlighted, boolean isCombined,
    		float x, float y, float size) {
        renderShape(graphics, false, shapeColor, shapeType, isSelected, isHighlighted, isCombined, false, x, y, size, size, 1);
    }

    @Override
    public boolean locatePoint(Point2D p, VisualItem item) {
        double zoom = item.getVisualization().getDisplay(0).getScale();
        NodeStore details = getNodeDetails(item);
        int sizeMode = details.getSizeMode();
        details.setSizeMode(1);
        double size_in = item.getSize();


        GraphContext gc = GraphContext.Current.get();
        RelGraphViewDef rgdef = gc.getVisualizationDef();

        double minimumNodeScaleFactor = rgdef.getMinimumNodeScaleFactor();
        item.setSize(size_in / Math.min(1, zoom * minimumNodeScaleFactor));
        boolean hit = false;
        try {
        Rectangle2D boundingBox = item.getBounds();
        boolean boundingBoxHit = isPointInBoundingBox(p, boundingBox);
        boolean colorHit = false;
        if(boundingBoxHit){
            colorHit = doesPointHitNodeOrLabel(item, item.getBounds(), p);
        }

            hit = boundingBoxHit && colorHit;


            //backup hit test if the visual item gets out of wack due to negative points
        if(((p.getX() < 0) || (p.getY() < 0)) && boundingBoxHit && !hit){

            RectangularShape shape = (RectangularShape) getShape(item);

            if(shape != null){
                Rectangle fixBounds = shape.getBounds();
                if(fixBounds != null){
                    item.setBounds(fixBounds.getX(), fixBounds.getY(), fixBounds.getWidth(), fixBounds.getHeight());
                }
            }

            return isPointInBoundingBox(p, item.getBounds()) && doesPointHitNodeOrLabel(item, item.getBounds(), p);
        }
        }finally {

            details.setSizeMode(sizeMode);
            item.setSize(size_in);
        }

        return hit;
    }

	private boolean isPointInBoundingBox(Point2D p, Rectangle2D boundingBox) {
		return boundingBox.contains(p);
	}

	private boolean doesPointHitSomething(Point2D p, VisualItem item, Rectangle2D boundingBox) {
		AffineTransform transform = new AffineTransform();
		BufferedImage image = getImageBufferFromVisualItem(item, boundingBox, transform);
		Color c = getColorFromBufferedImage(p, image, transform, boundingBox);
        if(c == null){
            return false;
        }
        return c.getAlpha() > 0;
	}

	private BufferedImage getImageBufferFromVisualItem(VisualItem item, Rectangle2D boundingBox, AffineTransform transform) {
        BufferedImage image = new BufferedImage(MAX_IMG_DIM, MAX_IMG_DIM, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        double increment = 100;
        double sx;

        if(boundingBox.getMaxX() < 0){
            sx = (MAX_IMG_DIM)/(boundingBox.getMinX() - increment);
        } else {
            sx = (MAX_IMG_DIM)/(boundingBox.getMaxX() + increment);
        }

        double sy;
        if(boundingBox.getMaxY() < 0){
            sy = (MAX_IMG_DIM)/(boundingBox.getMinY() - increment);
        } else {
            sy= (MAX_IMG_DIM)/(boundingBox.getMaxY() + increment);
        }
        transform.setToScale(sx, sy);
        g.setTransform(transform);
        render(g, item);
        return image;
    }

	private Color getColorFromBufferedImage(Point2D p, BufferedImage image, AffineTransform transform, Rectangle2D boundingBox) {
		double scaleX = transform.getScaleX();
		double scaleY = transform.getScaleY();

		int x = (int) (scaleX * p.getX());
		int y = (int) (scaleY * p.getY());
        try {
            return new Color(image.getRGB(x, y), true);
        }catch (Exception e){
            return null;
        }
	}

	/*
	* private boolean doesPointHitNodeOrLabel(VisualItem item, Rectangle2D boundingBoxIn, Point2D pointIn) {
        int labelMargin = 0;
        Rectangle2D boundingBox = boundingBoxIn;
        Point2D p = pointIn;
        {//center over point
            boundingBox = new Rectangle2D.Double(boundingBox.getX() - p.getX(), boundingBox.getY() - p.getY(), boundingBox.getWidth(), boundingBox.getHeight());
            p = new Point2D.Double(0, 0);
        }
        double scale=1;
//        if (boundingBox.getWidth() > 200) {
        {
            scale = 200 / boundingBox.getWidth();
            int w = 200;
            double h = boundingBox.getHeight() * (scale);
            double x = boundingBox.getX() - .5 * (w - boundingBox.getWidth());
            double y = boundingBox.getY() - .5 * (h - boundingBox.getHeight());
            boundingBox = new Rectangle2D.Double(x, y, w, h);
        }
        BufferedImage image = new BufferedImage((int) boundingBox.getWidth(), (int) boundingBox.getHeight() + labelMargin, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.getTransform().translate(-pointIn.getX(),-pointIn.getY());
//        g.getTransform().scale(scale, scale);
        item.setBounds(0, 0, boundingBox.getWidth(), boundingBox.getHeight());
        double x = item.getX();
        double y = item.getY();
        item.setX(boundingBox.getWidth() / 2D);
        item.setY(boundingBox.getHeight() / 2D + labelMargin);
        render(g, item);

        item.setX(x);
        item.setY(y);
        item.setBounds(x, y, boundingBox.getWidth(), boundingBox.getHeight());
//        if(m_text.equals("Sabotage Equipment")){
//            try(ByteArrayOutputStream output = new ByteArrayOutputStream()) {
//                ImageIO.write(image, "png", output);
//
//                String base64 = DatatypeConverter.printBase64Binary(output.toByteArray());
//                System.out.println(base64);
//            } catch (IOException e) {
//
//            }
//        }
//
        x = p.getX() - (boundingBox.getX() - (boundingBox.getWidth() / 2D));
        y = p.getY() - (boundingBox.getY() - (boundingBox.getHeight() / 2D));

        try {
            Color color = new Color(image.getRGB((int) 0, (int) 0), true);
            return color.getAlpha() > 0;
        } catch (Exception e) {

        }

        return false;
    }*/

   private boolean doesPointHitNodeOrLabel(VisualItem item, Rectangle2D boundingBox, Point2D p) {
      int labelMargin = 0;
      BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = image.createGraphics();

      item.setBounds(0, 0, boundingBox.getWidth(), boundingBox.getHeight());

      double x = item.getX();
      double y = item.getY();

      item.setX((boundingBox.getWidth() / 2D) - (p.getX() - x));
      item.setY(((boundingBox.getHeight() / 2D) + labelMargin) - (p.getY() - y));
      item.setX(-(p.getX() - x));
      item.setY(-(p.getY() - y));
      render(g, item);
      item.setX(x);
      item.setY(y);
      item.setBounds(x, y, boundingBox.getWidth(), boundingBox.getHeight());
//        if(m_text.equals("Sabotage Equipment")){
//            try(ByteArrayOutputStream output = new ByteArrayOutputStream()) {
//                ImageIO.write(image, "png", output);
//
//                String base64 = DatatypeConverter.printBase64Binary(output.toByteArray());
//                System.out.println(base64);
//            } catch (IOException e) {
//
//            }
//        }
//
//      x = p.getX() - (boundingBox.getX() - (boundingBox.getWidth() / 2D));
//      y = p.getY() - (boundingBox.getY() - (boundingBox.getHeight() / 2D));

      try {
         Color color = new Color(image.getRGB(1, 1), true);
         return color.getAlpha() > 0;
      } catch (Exception e) {
      }
      return false;
   }
}
