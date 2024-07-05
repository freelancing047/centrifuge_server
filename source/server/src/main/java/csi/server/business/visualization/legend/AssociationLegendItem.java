package csi.server.business.visualization.legend;

import csi.server.common.graphics.shapes.ShapeType;

public class AssociationLegendItem extends LegendItem {
   public String shape = ShapeType.LINE.toString();
   public int color;
   public int totalCount;

   public AssociationLegendItem() {
      super();
   }
}
