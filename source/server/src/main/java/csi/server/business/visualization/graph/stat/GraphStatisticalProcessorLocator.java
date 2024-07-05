package csi.server.business.visualization.graph.stat;

import csi.server.business.visualization.graph.stat.GraphStatistic.VisibleGraphStatistic;
import csi.server.business.visualization.graph.stat.GraphStatisticalProcessor.VisibleGraphStatisticProcessor;

public class GraphStatisticalProcessorLocator {

    public GraphStatisticalProcessor get(GraphStatistic statistic) {
        if(statistic instanceof VisibleGraphStatistic) {
            return new VisibleGraphStatisticProcessor();
        }
        if(statistic instanceof GraphStatistic.TotalGraphStatistic) {
            return new GraphStatisticalProcessor.TotalGraphStatisticProcessor();
        }
        return null;
    }
}
