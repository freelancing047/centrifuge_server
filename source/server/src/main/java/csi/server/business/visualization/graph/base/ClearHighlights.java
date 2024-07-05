package csi.server.business.visualization.graph.base;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import prefuse.data.Graph;
import prefuse.data.Node;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.property.Property;

public class ClearHighlights implements Callable<Void> {
   protected Graph graph;

   @SuppressWarnings("unchecked")
   public Void call() {
      if (graph != null) {
         Iterator<Node> nodes = graph.nodes();

         while (nodes.hasNext()) {
            Node node = nodes.next();
            NodeStore store = GraphManager.getNodeDetails(node);
            Map<String,Property> attributes = store.getAttributes();

            attributes.remove(AbstractGraphObjectStore.HIGHLIGHT_PROPERTY);
         }
      }
      return null;
   }

   public Graph getGraph() {
      return graph;
   }

   public void setGraph(Graph graph) {
      this.graph = graph;
   }
}
