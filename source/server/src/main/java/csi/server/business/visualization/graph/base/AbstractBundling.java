package csi.server.business.visualization.graph.base;

import prefuse.data.Graph;

public abstract class AbstractBundling implements Runnable {
   protected Graph graph;
   protected String dvUuid;
   protected String vizUuid;

   public AbstractBundling(String dvUuid, String vizUuid, Graph graph) {
      super();
      this.graph = graph;
      this.dvUuid = dvUuid;
      this.vizUuid = vizUuid;
   }

   protected Graph getGraph() {
      return graph;
   }
}
