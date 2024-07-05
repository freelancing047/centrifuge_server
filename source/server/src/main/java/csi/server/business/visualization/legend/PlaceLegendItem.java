package csi.server.business.visualization.legend;

public class PlaceLegendItem extends LegendItem {
   public String placeName;
   public int placeId;
   public String shape;
   public int color;
   public String iconURI;
   public int totalCount;

   public String getKey() {
      return typeName;
   }

   public PlaceLegendItem() {
      super();
   }
}
