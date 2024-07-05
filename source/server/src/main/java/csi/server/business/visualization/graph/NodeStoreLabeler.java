package csi.server.business.visualization.graph;

import java.util.List;

import org.apache.commons.collections15.Transformer;

import csi.server.business.visualization.graph.base.NodeStore;

public class NodeStoreLabeler implements Transformer<NodeStore,String> {
   /**
    * Returns v.toString()
    */
   public String transform(NodeStore v) {
      List<String> labels = v.getLabels();

      return ((labels != null) && !labels.isEmpty()) ? labels.get(0) : "";
   }
}
