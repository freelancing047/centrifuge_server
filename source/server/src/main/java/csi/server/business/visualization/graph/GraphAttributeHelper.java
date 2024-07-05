package csi.server.business.visualization.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.TypeInfo;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.graph.NodeStyle;

public class GraphAttributeHelper {
   private static final Logger LOG = LogManager.getLogger(GraphAttributeHelper.class);

   public static Object resolveAttribute(String attribute, TypeInfo typeInfo, LinkStyle style) {
      Object value = null;

      if (attribute != null) {
         if (style != null) {
            if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
               value = style.getColor();
            } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_WIDTH)) {
               value = style.getWidth();
            } else {
               LOG.error("Unrecognized Attribute:" + attribute);
            }
         }
         if ((typeInfo != null) && (value == null) && attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
            value = typeInfo.color;
         }
      }
      return value;
   }

    public static Object resolveAttribute(String attribute, TypeInfo typeInfo, NodeStyle style, ShapeType defaultShape) {
        Object value = null;

        if (attribute == null) {
            return null;
        }

        if (style != null) {
            if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
                value = style.getColor();

            } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_SHAPE)) {
                value = style.getShape();
                if(value == null){
                    value = defaultShape;
                }

                if(value == null) {
                    value = ShapeType.getNextNodeShape();
                }

            } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_ICON)) {
                // return null value if icon is not set so that client will be notified to replace it by a white square with black border.
                if ((style.getIconId() == null) || style.getIconId().isEmpty()) {
                    value = null;
                } else {
                    value = style.getIconId();
//                        //I don't think one can get here. Better safe though.
//                        csi.config.RelGraphConfig rgc = csi.config.Configuration.instance().getGraphConfig();
//                        String defaultTheme = rgc.getDefaultTheme();
//                        value = OptionSetManager.toResourceUrl(defaultTheme+"/"+options.getOption(Options.ICON_ATTRIBUTE));

                }
            } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_SCALE)) {
                //value = .75;
                value = style.getIconScale();
            } else {
               LOG.error("Unrecognized Attribute:" + attribute);
                value = null;
                //value = options.getOption(attribute);
            }
        } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_SHAPE)){
           value = defaultShape;
        }

        if (typeInfo != null) {
            if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR) && (typeInfo.colorOverride || (value == null))) {
                value = typeInfo.color;

            } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_SHAPE) && (typeInfo.shapeOverride || (value == null))) {
                value = typeInfo.shape;

            } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_ICON) && (typeInfo.iconOverride || (value == null))) {
                value = typeInfo.iconId;
            } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_SCALE)  && (typeInfo.shapeOverride || (value == null))) {
                //value = .75;
//                    value = optionSet.IconScale;
            }
        }


        return value;
    }

    public static Object resolveNodeAttribute(String attribute, NodeStore detail, TypeInfo typeInfo, NodeStyle nodeStyle, ShapeType defaultShape) {
        Object value = null;

        if ((detail == null) || (attribute == null)) {
            return null;
        }




        if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
            value = detail.getColor();

        } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_SHAPE)) {
            value = detail.getShape();

        }

        if (value == null) {

            value = resolveAttribute(attribute, typeInfo, nodeStyle, defaultShape);
        }

        if (attribute.equals(ObjectAttributes.CSI_INTERNAL_SCALE) && (value == null)) {
            if(detail.getScale() == 1) {
               value = 1.08;
            } else {
                value = detail.getScale();
            }
        }
//
        if (attribute.equals(ObjectAttributes.CSI_INTERNAL_ICON) && (value == null) && (((typeInfo != null) && typeInfo.iconOverride) || detail.isPlunked())) {
            value = detail.getIcon();
        }

        return value;
    }

    public static Object resolveEdgeAttribute(String attribute, LinkStore detail, TypeInfo typeInfo, GraphTheme theme) {
        Object value = null;

        if ((detail == null) || (attribute == null)) {
            return null;
        }

        String linkType = detail.getType();
        if (detail.isBundle() && (detail.getFirstType() != null)){
        	linkType = detail.getFirstType();
        }
        Property prop = detail.getAttributes().get(attribute);
        if ((prop != null) && (prop.getValues() != null) && !prop.getValues().isEmpty()) {
            if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
                value = toInt(prop.getValues().get(0));
            } else {
                value = prop.getValues().get(0);
            }
        }

        if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
            value = detail.getColor();

        }

        if (value == null) {
            LinkStyle linkStyle = null;
            if (theme != null) {
                linkStyle = theme.findLinkStyle(linkType);
            }
            value = resolveAttribute(attribute, typeInfo, linkStyle);
        }

        return value;
    }

    private static Integer toInt(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                // ignore
                return null;
            }
        }
    }

    /**
     * Transforms the given object into a Long object.
     *
     * @param value the object that needs to be converted to Long.
     * @return null if value is null or cannot be parsed, otherwise the long value of the parameter.
     */
    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
                // ignore
                return null;
            }
        }
    }

    /**
     * Finds a value for the given attribute. It searches first in options object and if not found, it looks for it in the graphLinkLegendItem.
     *
     * @param attribute  the attribute for which the value is searched.
     * @param legendItem the legendItem which might have a value for this attribute.
     * @param options    a collection of attributes read from a configuration file (baseline.xml).
     * @return Null if attribute not specified, or its value is not set anywhere. Otherwise, its value is searched in the options collection, and if not found, is searched in the legendItem.
     */
//    public static Object resolveAttribute(String attribute, GraphLinkLegendItem legendItem, Options options) {
//        Object value = null;
//
//        if (attribute == null) {
//            return null;
//        }
//
//        if (options != null) {
//            if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
//                value = toLong(options.getOption(Options.COLOR_ATTRIBUTE));
//
//            } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_SHAPE)) {
//                value = options.getOption(Options.SHAPE_ATTRIBUTE);
//
//            } else {
//                value = options.getOption(attribute);
//            }
//        }
//
//        if (legendItem != null && value == null) {
//                if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
//                    value = legendItem.color;
//
//                }
//        }
//
//        return value;
//    }
//
    /**
     * Finds a value for the given attribute. It searches first in options object and if not found, it looks for it in the graphLinkLegendItem.
     *
     * @param attribute  the attribute for which the value is searched.
     * @param legendItem the legendItem which might have a value for this attribute.
     * @param options    a collection of attributes read from a configuration file (baseline.xml).
     * @return Null if attribute not specified, or its value is not set anywhere. Otherwise, its value is searched in the options collection, and if not found, is searched in the legendItem.
     */
    public static Object resolveLinkAttribute(String attribute, GraphLinkLegendItem legendItem, LinkStyle linkStyle) {
        Object value = null;

        if (attribute == null) {
            return null;
        }

        if (legendItem != null) {
            if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
                value = legendItem.color;
            }
        }

        if (linkStyle != null) {

//            if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
//                value = new Long(linkStyle.getColor());
//
//            }
//            if (attribute.equals(ObjectAttributes.CSI_INTERNAL_WIDTH)) {
//                value = linkStyle.getWidth();
//
//            }
//            else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_SHAPE)) {
//                value = options.getOption(Options.SHAPE_ATTRIBUTE);
//
//            }
//            else {
//                value = options.getOption(attribute);
//            }
        }



        return value;
    }
}
