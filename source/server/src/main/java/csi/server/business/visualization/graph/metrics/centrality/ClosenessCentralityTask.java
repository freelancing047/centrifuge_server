package csi.server.business.visualization.graph.metrics.centrality;

import javax.xml.namespace.QName;

import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.graph.Graph;

public class ClosenessCentralityTask<V,E> extends AbstractCentralityTask<V,E> {
   public static final QName PROP_NAME = new QName("urn:csi/graph/centrality", "CLOSENESS");

   public ClosenessCentralityTask(Graph<V,E> graph) {
      super(graph);
   }

   @Override
   protected VertexScorer<V,? extends Number> getScorer() {
      return new ClosenessCentrality<V,E>(graph);
   }

   @Override
   protected QName getPropertyName() {
      return PROP_NAME;
   }
}
