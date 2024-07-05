package csi.server.business.visualization.graph.metrics.centrality;

import javax.xml.namespace.QName;

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.graph.Graph;

/**
 * Provide a Callable wrapper around the betweenness centrality measure for a
 * given graph.
 * <p>
 * This class presumes that the graph represents a weak-component. No attempts
 * are made to ensure that this is the case.
 * <p>
 * NB: This is a veneer around the centrality task, merely to provide a
 * mechanism to submit the processing onto a thread-pool. i.e. Sharp knifes are
 * sharp. Don't cut yourself; you've been warned.
 *
 */
public class BetweennessTask<V,E> extends AbstractCentralityTask<V,E> {
   public static final QName PROP_NAME = new QName("urn:csi/graph/centrality", "BETWEENNESS");

   public BetweennessTask(Graph<V,E> graph) {
      super(graph);
   }

   @Override
   protected VertexScorer<V,? extends Number> getScorer() {
      return new BetweennessCentrality<V,E>(graph);
   }

   @Override
   protected QName getPropertyName() {
      return PROP_NAME;
   }
}
