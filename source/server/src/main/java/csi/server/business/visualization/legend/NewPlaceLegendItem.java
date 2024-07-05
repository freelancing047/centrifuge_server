package csi.server.business.visualization.legend;

public class NewPlaceLegendItem extends LegendItem {
   public String shape;
   public int color;
   public String iconURI;
   public int totalCount;
   public boolean visible;
   public boolean clickable;

   public NewPlaceLegendItem() {
      super();
      typeName = "New";
   }
}
