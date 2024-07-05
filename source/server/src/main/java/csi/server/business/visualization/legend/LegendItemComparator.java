package csi.server.business.visualization.legend;

import java.util.Comparator;

import csi.server.common.model.visualization.graph.GraphConstants;

/**
 * Comparator for legend items.
 */
public class LegendItemComparator implements Comparator<LegendItem> {

   @Override
   public int compare(LegendItem o1, LegendItem o2) {
      if (o1.key.startsWith(GraphConstants.CSI_INTERNAL_NAMESPACE) != o2.key
            .startsWith(GraphConstants.CSI_INTERNAL_NAMESPACE)) {
         return o1.key.startsWith(GraphConstants.CSI_INTERNAL_NAMESPACE) ? 1 : -1;
      }
      return o1.key.compareTo(o2.key);
   }
}
