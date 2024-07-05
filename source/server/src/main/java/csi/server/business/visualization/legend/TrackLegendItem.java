package csi.server.business.visualization.legend;

import csi.server.common.graphics.shapes.ShapeType;

public class TrackLegendItem extends LegendItem {
   public int trackId;
   public String shape = ShapeType.LINE.toString();
   public int color;
   public int totalCount;

   public String getKey() {
      return typeName;
   }

   public TrackLegendItem() {
      super();
   }
}
