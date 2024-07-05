package csi.server.business.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.stat.GraphObjectProcessor;
import csi.server.business.visualization.graph.stat.GraphObjectProcessorLocator;
import csi.server.business.visualization.graph.stat.GraphStatistic;
import csi.server.business.visualization.graph.stat.GraphStatisticalEntity;
import csi.server.business.visualization.graph.stat.GraphStatisticalPopulation;
import csi.server.business.visualization.graph.stat.GraphStatisticalProcessor;
import csi.server.business.visualization.graph.stat.GraphStatisticalProcessorLocator;
import csi.server.common.service.api.GraphStatisticsServiceProtocol;

public class GraphStatisticsService implements GraphStatisticsServiceProtocol {
   @Override
   public Table<GraphStatisticalPopulation, GraphStatistic, Double> getGraphStatistics(ArrayList<GraphStatistic> graphStatistics,
                                                                                       ArrayList<GraphStatisticalPopulation> populations,
                                                                                       String vizUuid) {
      Table<GraphStatisticalPopulation, GraphStatistic, Double> table = HashBasedTable.create();

      if ((populations != null) && !populations.isEmpty() && (graphStatistics != null) && !graphStatistics.isEmpty()) {
         GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);

         for (GraphStatisticalPopulation population : populations) {
            getGraphStatistics(population, graphStatistics, graphContext, table);
         }
      }
      return table;
   }

    private static void getGraphStatistics(final GraphStatisticalPopulation population,
            final List<GraphStatistic> graphStatistics, GraphContext graphContext,
            Table<GraphStatisticalPopulation, GraphStatistic, Double> table) {
        checkNotNull(population);
        checkNotNull(graphStatistics);
        checkNotNull(graphContext);
        checkNotNull(table);
        GraphObjectProcessor graphObjectProcessor = GraphObjectProcessorLocator.get(population);
        // This list will be the objects the statistics will evaluate.
        List<GraphStatisticalEntity> populationEntities = new ArrayList<GraphStatisticalEntity>();

        Graph graph = graphContext.getGraphData();
        Iterator nodes = graph.nodes();
        if (nodes.hasNext()) {
            while (nodes.hasNext()) {
                Node node = (Node) nodes.next();
                GraphStatisticalEntity e = graphObjectProcessor.process(node);
                if (e != null) {
                    populationEntities.add(e);
                }
            }
        }
        Iterator links = graph.edges();
        if (links.hasNext()) {
            while(links.hasNext()) {
                Edge link = (Edge) links.next();
                GraphStatisticalEntity e = graphObjectProcessor.process(link);
                if (e != null) {
                    populationEntities.add(e);
                }
            }
        }
        GraphStatisticalProcessorLocator processorLocator = new GraphStatisticalProcessorLocator();
        for (GraphStatistic statistic : graphStatistics) {
            GraphStatisticalProcessor processor = processorLocator.get(statistic);
            Double statisticValue = processor.process(populationEntities);
            table.put(population, statistic, statisticValue);
        }
    }
}
