package csi.server.business.visualization.graph.stat;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.GraphStatisticsType;

public interface GraphStatistic extends IsSerializable {
   public String getName();

   public class GraphStatisticImpl implements GraphStatistic {
      @Override
      public String getName() {
         return "?";
      }
   }

   public class VisibleGraphStatistic implements GraphStatistic {
      @Override
      public String getName() {
         return GraphStatisticsType.VISIBLE.getLabel();
      }
   }

   public class TotalGraphStatistic implements GraphStatistic {
      @Override
      public String getName() {
         return GraphStatisticsType.TOTAL.getLabel();
      }
   }
}
