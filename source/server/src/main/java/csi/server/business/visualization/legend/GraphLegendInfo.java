package csi.server.business.visualization.legend;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.business.visualization.graph.base.GraphLegendNodeSummary;

/**
 * Wrapper on different types of legendItems.
 */
public class GraphLegendInfo implements IsSerializable {

   /**
    * List of items that represent link types.
    */
   private List<GraphLinkLegendItem> linkLegendItems;

   /**
    * List of items that represent node types.
    */
   private List<GraphNodeLegendItem> nodeLegendItems;

   public GraphLegendNodeSummary graphLegendNodeSummary;

   public GraphLegendInfo() {

   }

   public List<GraphLinkLegendItem> getLinkLegendItems() {
      return linkLegendItems;
   }

   public void setLinkLegendItems(List<GraphLinkLegendItem> linkLegendItems) {
      this.linkLegendItems = linkLegendItems;
   }

   public List<GraphNodeLegendItem> getNodeLegendItems() {
      return nodeLegendItems;
   }

   public void setNodeLegendItems(List<GraphNodeLegendItem> nodeLegendItems) {
      this.nodeLegendItems = nodeLegendItems;
   }
}
