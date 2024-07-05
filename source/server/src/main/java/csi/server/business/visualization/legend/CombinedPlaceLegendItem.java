package csi.server.business.visualization.legend;

public class CombinedPlaceLegendItem extends LegendItem {
   public String shape;
   public int color;
   public String iconURI;
   public int totalCount;
   public boolean visible;
   public boolean clickable;

   public CombinedPlaceLegendItem() {
      super();
      typeName = "Combined place";
   }
}
