package csi.server.business.visualization.legend;

import csi.server.common.graphics.shapes.ShapeType;

/**
 * Each GraphNodeLegendItem represents a graph node.
 */
public class GraphNodeLegendItem extends LegendItem {
   /**
    * The shape corresponding to this graph node.
    */
   public ShapeType shape;

   /**
    * The color of the shape.
    */
   public long color;

   /**
    * The URI of the icon corresponding to this graph node.
    */
   public String iconId;

   /**
    * The total count of the nodes (including the hidden ones).
    */
   public int totalCount;
   /**
    * Icon Scale relative to shape.
    */
   public float iconScale;

   public String getKey() {
      return key;
   }

   public GraphNodeLegendItem() {
      super();
   }
}
