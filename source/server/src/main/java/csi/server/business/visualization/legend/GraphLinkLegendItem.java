package csi.server.business.visualization.legend;

import csi.server.common.graphics.shapes.ShapeType;

/**
 * A GraphLinkLegendItem represents an item on the legend corresponding to a
 * link type.
 */
public class GraphLinkLegendItem extends LegendItem {

   /**
    * The shape of the GraphLinkLegendItem object, usually a line
    */
   public String shape = ShapeType.LINE.toString();

   /**
    * The color corresponding to the GraphLinkLegendItem object
    */
   public long color;

   public boolean colorOverride = false;
   /**
    * The total number of links (including the hidden ones)
    */
   public int totalCount;

   public String getKey() {
      return key;
   }

   public GraphLinkLegendItem() {
      super();
   }
}
