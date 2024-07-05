package csi.server.business.visualization.graph.metrics.centrality;

import javax.xml.namespace.QName;

import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.graph.Graph;

public class EigenvectorCentralityTask<V,E> extends AbstractCentralityTask<V,E> {
   public static final QName PROP_NAME = new QName("urn:csi/graph/centrality", "EIGENVECTOR");

   protected EigenvectorCentrality<V,E> centrality;

   public EigenvectorCentralityTask(Graph<V,E> graph) {
      super(graph);

   }

   @Override
   protected VertexScorer<V,? extends Number> getScorer() {
      centrality = new EigenvectorCentrality<V,E>(graph);
      centrality.evaluate();
      return centrality;
   }

   @Override
   protected QName getPropertyName() {
      return PROP_NAME;
   }
}
