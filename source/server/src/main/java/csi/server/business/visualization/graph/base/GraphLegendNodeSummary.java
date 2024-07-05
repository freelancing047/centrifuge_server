package csi.server.business.visualization.graph.base;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GraphLegendNodeSummary implements IsSerializable {

    public Integer totalNodes;
    public Integer totalVisibleNodes;
    public Integer multiTypedNodes;
    public Integer multiTypedLinks;

    public GraphLegendNodeSummary() {
    	
    }
}
