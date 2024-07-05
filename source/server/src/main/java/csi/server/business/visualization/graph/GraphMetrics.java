package csi.server.business.visualization.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;

import javax.xml.namespace.QName;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.importance.Ranking;
import edu.uci.ics.jung.algorithms.scoring.EdgeScorer;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.business.visualization.graph.base.property.SimpleProperty;
import csi.server.business.visualization.graph.metrics.centrality.BetweennessTask;
import csi.server.business.visualization.graph.metrics.centrality.ClosenessCentralityTask;
import csi.server.business.visualization.graph.metrics.centrality.EdgeScoreHelper;
import csi.server.business.visualization.graph.metrics.centrality.EigenvectorCentralityTask;
import csi.server.business.visualization.graph.metrics.centrality.VertexScoreHelper;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;

public class GraphMetrics {
   private static final Logger LOG = LogManager.getLogger(GraphMetrics.class);

    static Map<String, GraphMetricInfo> metrics;

    static public final QName DEGREE_CENTRALITY = new QName("urn:csi/graph/centrality", "DEGREES");
    static public final QName SUBGRAPH_PROP_NAME = new QName("urn:csi/graph/subgraphid", "subGraphId");

    private static final String METRICS_PROPERTY = "snaMetrics";
    static final Comparator RANKING_SCORE = new RankingComparator();

    static class GraphMetricInfo {

        String simpleName;

        String name;

        String description;

        Comparator<Ranking> comparator;
    }

    public static enum MetricsRange {
        COUNT, PERCENT
    }

    static {
        metrics = new HashMap<String, GraphMetricInfo>();
        GraphMetricInfo gmi;
        gmi = new GraphMetricInfo();
        gmi.simpleName = "BETWEENNESS";
        gmi.name = BetweennessTask.PROP_NAME.toString();
        gmi.description = "Betweenness Centrality measures ";
        gmi.comparator = RANKING_SCORE;

        metrics.put(gmi.simpleName, gmi);

        gmi = new GraphMetricInfo();
        gmi.simpleName = "CLOSENESS";
        gmi.name = ClosenessCentralityTask.PROP_NAME.toString();
        gmi.description = "Closeness Centrality";
        gmi.comparator = Collections.reverseOrder(RANKING_SCORE);

        metrics.put(gmi.simpleName, gmi);

        gmi = new GraphMetricInfo();
        gmi.simpleName = "EIGENVECTOR";
        gmi.name = EigenvectorCentralityTask.PROP_NAME.toString();
        gmi.comparator = RANKING_SCORE;

        metrics.put(gmi.simpleName, gmi);

        gmi = new GraphMetricInfo();
        gmi.simpleName = "DEGREES";
        gmi.name = DEGREE_CENTRALITY.toString();
        gmi.comparator = RANKING_SCORE;

        metrics.put(gmi.simpleName, gmi);

    }

    public static String getStandardName(String simple) {
        GraphMetricInfo gmi = metrics.get(simple);
        return gmi.name;
    }

    public static boolean isMetricsComputed(Graph graph) {
        Boolean metricsComputed = (Boolean) graph.getClientProperty(METRICS_PROPERTY);
        return ((metricsComputed != null) && metricsComputed.booleanValue());
    }

    public static void setMetricsComputed(Graph graph) {
        graph.putClientProperty(METRICS_PROPERTY, Boolean.TRUE);
    }

    @SuppressWarnings("unchecked")
    public static void computeMetrics(Graph graph) {

        String vizId = (String) graph.getClientProperty(GraphManager.VIEWDEF_UUID);
        RelGraphViewDef viewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizId);

        PrefuseToJungTransformer transform = new PrefuseToJungTransformer();
        edu.uci.ics.jung.graph.Graph<String, String> jGraph = transform.apply(graph);

        WeakComponentClusterer<String, String> clusterer = new WeakComponentClusterer<String, String>();

        Map<String, Node> nodeMap = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        Map<String, Edge> edgeMap = (Map<String, Edge>) graph.getClientProperty(GraphManager.EDGE_HASH_TABLE);

        TaskHelper.reportProgress("Computing Network Metrics", 10);
        TaskHelper.checkForCancel();

        Set<Set<String>> components = clusterer.transform(jGraph);

        TaskHelper.reportProgress("Computing Network Metrics", 25);
        TaskHelper.checkForCancel();
        Collection<edu.uci.ics.jung.graph.Graph<String, String>> forest = FilterUtils.createAllInducedSubgraphs(components, jGraph);

        if (forest.isEmpty()) {
            return;
        }
        StopWatch overallStopWatch = new StopWatch();

        overallStopWatch.start();

        TaskHelper.reportProgress("Computing Network Metrics", 30);
        TaskHelper.checkForCancel();

        float sliceSize = 75 / forest.size();
        float stepSize = sliceSize / 4;
        int subnet = 0;

        //reset betweenness values as we sum up the values if there are both direction for a link
        String betweennessKey = BetweennessTask.PROP_NAME.getLocalPart();
        for (Edge edge : edgeMap.values()) {
        	  LinkStore data = GraphManager.getEdgeDetails(edge);
        	  Map<String, Property> attributes = data.getAttributes();

        	  Property property = attributes.get(betweennessKey);

        	  if (property != null) {
        	     if (property instanceof SimpleProperty) {
        	        SimpleProperty prop = (SimpleProperty) property;
        	        prop.setValue(null);
        	        GraphManager.setLinkDetails(edge, data);
        	     } else if (property instanceof Property) {
        	        property.setValues(new ArrayList<Object>());
        	        GraphManager.setLinkDetails(edge, data);
        	     }
        	  }
        }

        int subGraphId = 1;

        VertexScoreHelper<String, String> scoreHelper = new VertexScoreHelper<String, String>(viewDef);
        EdgeScoreHelper<String, String> edgeScoreHelper = new EdgeScoreHelper<String, String>(viewDef);
        StopWatch stopWatch = new StopWatch();

        for (edu.uci.ics.jung.graph.Graph<String, String> subGraph : forest) {

            int progress = (int) (subnet * sliceSize);
            subnet++;

            String ofTotal = subnet + " of  " + (forest.size());
            int stepState = progress;

            BetweennessTask<String, String> betweennessTask = new BetweennessTask<String, String>(subGraph);
            if (subGraph.getVertexCount() < 2) {

                for (String vkey : subGraph.getVertices()) {
                    Node node = nodeMap.get(vkey);

                    NodeStore data = GraphManager.getNodeDetails(node);
                    Map<String, Property> attributes = data.getAttributes();

                    String specID = data.getSpecID();


                    scoreHelper.clearScore(attributes, BetweennessTask.PROP_NAME, specID);
                    scoreHelper.clearScore(attributes, ClosenessCentralityTask.PROP_NAME, specID);
                    scoreHelper.clearScore(attributes, EigenvectorCentralityTask.PROP_NAME, specID);

//                    Property property = getOrCreateProperty(attributes, BetweennessTask.PROP_NAME, BetweennessTask.PROP_NAME.getLocalPart(), specID);
//                    property.getValues().clear();

                }
                continue;
            }


            VertexScorer scorer = null;
            TaskHelper.reportProgress("Calculating Betweeness for Component " + ofTotal, stepState);
            TaskHelper.checkForCancel();
            stepState += stepSize;
            try {
                scorer = betweennessTask.call();
                scoreHelper.updateVertexScores(subGraph, nodeMap, scorer, BetweennessTask.PROP_NAME, subGraphId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            TaskHelper.reportProgress("Computing Edge Betweenness for Component " + ofTotal, stepState);
            TaskHelper.checkForCancel();

            stepState += stepSize;

            if (EdgeScorer.class.isInstance(scorer) && (edgeMap != null)) {
                edgeScoreHelper.updateEdgeScores(subGraph, edgeMap, (EdgeScorer) scorer, BetweennessTask.PROP_NAME);

            }

            TaskHelper.reportProgress("Computing Closeness for Component " + ofTotal, stepState);
            TaskHelper.checkForCancel();
            stepState += stepSize;

            ClosenessCentralityTask<String, String> closenessTask = new ClosenessCentralityTask<String, String>(subGraph);
            try {
               stopWatch.reset();
               stopWatch.start();
                scorer = closenessTask.call();
                scoreHelper.updateVertexScores(subGraph, nodeMap, scorer, ClosenessCentralityTask.PROP_NAME, subGraphId);
                stopWatch.stop();

                if (LOG.isTraceEnabled()) {
                   LOG.trace("Closeness centrality time: " + stopWatch.getTime() + " ms");
                }
            } catch (Exception e) {
            }
            TaskHelper.reportProgress("Computing Eigenvector for Component " + ofTotal, stepState);
            TaskHelper.checkForCancel();

            EigenvectorCentralityTask<String, String> evTask = new EigenvectorCentralityTask<String, String>(subGraph);
            try {
                scorer = evTask.call();
                scoreHelper.updateVertexScores(subGraph, nodeMap, scorer, EigenvectorCentralityTask.PROP_NAME, subGraphId);
            } catch (Exception e) {
            }
        }

        TaskHelper.reportProgress("Computing Network Metrics", 95);
        overallStopWatch.stop();

        if (LOG.isDebugEnabled()) {
           LOG.debug("SNA metrics computation took : " + overallStopWatch.getTime() + " ms.");
        }

        setMetricsComputed(graph);

    }

   public static Collection<Ranking<String>> getMetrics(Graph component, final String metricName, int threshold, MetricsRange range) {
      final Function<NodeStore, Double> getRanking =
         new Function<NodeStore, Double>() {
            @Override
            public Double apply(NodeStore details) {
               double results = Double.NaN;

               if (details != null) {
                  Property property = details.getAttributes().get(metricName);

                  if (property != null) {
                     results = ((Double) property.getValues().get(0)).doubleValue();
                  }
               }
               return results;
            }
      };
      QName qName = QName.valueOf(metricName);
      GraphMetricInfo metricInfo = metrics.get(qName.getLocalPart());

      if (range == MetricsRange.PERCENT) {
         threshold = ((component.getNodeCount() * threshold) / 100) + 1;
      }
      int pruneThreshold = (int) (threshold * 1.5);
        //
        // NB: using a priority queue here. insertions put the smallest values at the head
        // of the queue. while we cycle through the list, we periodically prune the
        // queue down; leaving the largest values.

      PriorityQueue<Ranking<String>> topMetrics =
         new PriorityQueue<Ranking<String>>(pruneThreshold, metricInfo.comparator);
      boolean isDegree = metricName.equals(DEGREE_CENTRALITY.toString());
      Iterator nodes = component.nodes();
      int pos = 0;

      while (nodes.hasNext()) {
         Node node = (Node) nodes.next();
         NodeStore details = GraphManager.getNodeDetails(node);
         double metric = isDegree ? node.getDegree() : getRanking.apply(details).doubleValue();

         // determine if we can insert this metric, we'll prune the list to ensure
         // we stay within our desired range...
         if (Double.isNaN(metric)) {
            continue;
         }
         Ranking<String> ranking = new Ranking<String>(pos, metric, details.getKey());
         topMetrics.add(ranking);

         if (topMetrics.size() >= pruneThreshold) {
            while (topMetrics.size() > pruneThreshold) {
               topMetrics.poll();
            }
         }
         pos++;
      }
      while (topMetrics.size() > threshold) {
         topMetrics.remove();
      }
      List<Ranking<String>> results = new ArrayList<Ranking<String>>(topMetrics);

      Collections.sort(results, Collections.reverseOrder(metricInfo.comparator));
      return results;
   }

    static class RankingComparator implements Comparator<Ranking> {

        @Override
        public int compare(Ranking that, Ranking other) {
            return Double.compare(that.rankScore, other.rankScore);
        }

    }

    public static boolean isMetricName(String name) {
        boolean validName = metrics.containsKey(name.toUpperCase());
        return validName;
    }
}
