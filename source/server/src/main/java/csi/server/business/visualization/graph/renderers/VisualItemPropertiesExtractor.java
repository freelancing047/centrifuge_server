package csi.server.business.visualization.graph.renderers;

import java.util.Map;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.selection.SelectionModel;

public class VisualItemPropertiesExtractor {

    private VisualItem item;

    public VisualItemPropertiesExtractor(VisualItem item) {
        this.item = item;
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

    public boolean isPlunked() {
        return getLinkDetails().isPlunked();
    }

    public boolean isSelected() {
        boolean isSelected = false;
        int itemRow = item.getRow();
        Visualization vis = item.getVisualization();
        Graph graph = (Graph) vis.getSourceData("graph");
        Map<String, SelectionModel> selections = (Map<String, SelectionModel>) graph.getClientProperty("selections");
        if (selections != null) {
            SelectionModel defSelectionModel = selections.get("default.selection");
            if (defSelectionModel != null) {
                isSelected = defSelectionModel.links.contains(itemRow);
            }
        }
        return isSelected;
    }

    public boolean isPathHighlighted() {
        boolean isHighlighted = false;
        int itemRow = item.getRow();
        Visualization vis = item.getVisualization();
        Graph graph = (Graph) vis.getSourceData("graph");
        Map<String, SelectionModel> selections = (Map<String, SelectionModel>) graph.getClientProperty("selections");
        if (selections != null) {
            SelectionModel defSelectionModel = selections.get(GraphConstants.PATH_HIGHLIGHT);
            if (defSelectionModel != null) {
                isHighlighted = defSelectionModel.links.contains(itemRow);
            }
        }
        return isHighlighted;
    }

    public TupleSet getSourceGraph() {
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

//    public OptionSet getOptionSet() {
//        TupleSet sourceGraph = getSourceGraph();
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
//            //            log.warn("Error retrieving option set: " + optionName);
//            return null;
//        }
//    }

    public LinkStore getLinkDetails() {
        return GraphManager.getEdgeDetails(item);
    }

    //TODO: Graphic color will be used from now on, moved some of this logic to
    // LinkRenderer, but will not be porting the highvalue logic, but need to check to make sure there are no
    // adverse effects.
//    public Color getColor() {
//        Integer linkColor = (Integer) getAttribute(ObjectAttributes.CSI_INTERNAL_COLOR);
//
//        Color color;
//        if (linkColor == null) {
//            color = Configuration.getInstance().getGraphAdvConfig().getDefaultLinkColor();
//        } else {
//            if(isPlunked() && getLinkDetails().getColor() != null){
//                color = ColorLib.getColor(ColorLib.setAlpha(getLinkDetails().getColor(), 255));
//            }
//            else{
//                color = ColorLib.getColor(ColorLib.setAlpha(linkColor, 255));
//            }
//        }
//
//        LinkStore details = getLinkDetails();
//        Map<String, Property> attributes = details.getAttributes();
//        Property property = attributes.get("isHighValue");
//        if (property != null && property.getValues() != null && property.getValues().size() > 0) {
//            Boolean flag = (Boolean) property.getValues().get(0);
//            if (flag) {
//                Property colorProp = attributes.get("highValueColor");
//                if (colorProp != null && colorProp.getValues() != null && colorProp.getValues().size() > 0) {
//                    int c = (Integer) colorProp.getValues().get(0);
//                    color = ColorLib.getColor(c);
//                }
//            }
//        }
//        return color;
//    }

    public double getThickness() {
        Double value = (Double) getAttribute(ObjectAttributes.CSI_INTERNAL_WIDTH);
        if (value == null) {
//        boolean bySize = getLinkDetails().isBySize() || getLinkDetails().isByStatic();

//        if (value != null) {
//            LinkStore details = getLinkDetails();
//            value = details.getScale() * (bySize ? details.getWidth() : 1);
//        }

//           if (value == 0) {
              value = Double.valueOf(2);
//           }
        }
        return value.doubleValue();
    }

   private Object getAttribute(String attribute) {
      Object value = null;

      if (attribute != null) {
         LinkStore detail = getLinkDetails();

         if (detail != null) {
//        OptionSet optionSet = getOptionSet();
//        Options options = (optionSet != null) ? optionSet.getOptions(OptionSet.LINK_TYPE, detail.getType()) : null;
//
//        Object value = null;
//        if (options != null) {
//            if (attribute.equals(ObjectAttributes.CSI_INTERNAL_WIDTH)) {
//                value = toInt(options.getOption(Options.WIDTH_ATTRIBUTE));
//
//            } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
//                value = toInt(options.getOption(Options.COLOR_ATTRIBUTE));
//
//            } else {
//                value = options.getOption(attribute);
//            }
//        }
            if (attribute.equals(ObjectAttributes.CSI_INTERNAL_WIDTH)) {
               value = Double.valueOf(detail.getWidth());
            } else if (attribute.equals(ObjectAttributes.CSI_INTERNAL_COLOR)) {
               value = detail.getColor();
            }
         }
      }
      return value;
   }
}
