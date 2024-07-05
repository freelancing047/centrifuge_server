package csi.server.business.visualization.graph.stat;

import java.util.Collection;

public interface GraphStatisticalProcessor {
   public Double process(Collection<GraphStatisticalEntity> entities);

   public class VisibleGraphStatisticProcessor implements GraphStatisticalProcessor {
      @Override
      public Double process(Collection<GraphStatisticalEntity> entities) {
         int count = 0;

         for (GraphStatisticalEntity entity : entities) {
            if (entity.isVisible()) {
               count++;
            }
         }
         return Double.valueOf(count);
      }
   }

   public class TotalGraphStatisticProcessor implements GraphStatisticalProcessor {
      @Override
      public Double process(Collection<GraphStatisticalEntity> entities) {
         return Double.valueOf(entities.size());
      }
   }
}
