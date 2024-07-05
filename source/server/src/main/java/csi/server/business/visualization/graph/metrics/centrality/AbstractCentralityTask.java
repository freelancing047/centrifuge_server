package csi.server.business.visualization.graph.metrics.centrality;

import java.util.concurrent.Callable;

import javax.xml.namespace.QName;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.graph.Graph;

public abstract class AbstractCentralityTask<V,E> implements Callable<VertexScorer<V,? extends Number>> {
   private static final Logger LOG = LogManager.getLogger(AbstractCentralityTask.class);

   protected Graph<V,E> graph;
   protected boolean updateGraph;

   public AbstractCentralityTask(Graph<V,E> graph) {
      this.graph = graph;
   }

   /**
    * Subclasses are required to advertise the computed metric name. This is
    * typically used to store the resulting metric with the vertex.
    */
   protected abstract QName getPropertyName();

   protected abstract VertexScorer<V,? extends Number> getScorer();

   /**
    * Standard helper method to retrieve the Vertex scorer from subclasses. This is
    * done this way since some centralities immediately perform the calculation in
    * the constructor.
    * <p>
    * Make the request here to account for instances where this is passed to an
    * ExecutorService.
    */
   public VertexScorer<V,? extends Number> call() throws Exception {
      StopWatch stopWatch = new StopWatch();

      stopWatch.start();
      VertexScorer<V,? extends Number> scorer = getScorer();
      stopWatch.stop();

      LOG.trace(getPropertyName().getLocalPart() + " calculation took: " + stopWatch.getTime() + " ms");
      return scorer;
   }
}
