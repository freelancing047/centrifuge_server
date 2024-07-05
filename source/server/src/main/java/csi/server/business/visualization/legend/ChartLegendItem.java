package csi.server.business.visualization.legend;

/**
 * Legend item used for chart legends.
 */
public class ChartLegendItem extends LegendItem {
   /**
    * The shape of the legend item.
    */
   public String shape;

   /**
    * The color of the shape.
    */
   public long color;

   /**
    * The URI for the legend item's icon.
    */
   public String iconURI;

   /**
    * The number of visible legend items.
    */
   public int count;
}
