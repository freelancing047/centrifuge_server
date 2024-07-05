package csi.server.common.service.api;


import java.util.ArrayList;

import com.google.common.collect.Table;

import csi.server.business.visualization.graph.stat.GraphStatistic;
import csi.server.business.visualization.graph.stat.GraphStatisticalPopulation;
import csi.shared.gwt.vortex.VortexService;

public interface GraphStatisticsServiceProtocol extends VortexService {

    Table<GraphStatisticalPopulation, GraphStatistic, Double> getGraphStatistics(
            ArrayList<GraphStatistic> graphStatistics, ArrayList<GraphStatisticalPopulation> populations, String vizUuid);
}
