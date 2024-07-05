package csi.server.business.visualization.graph.plunk;

import prefuse.data.Edge;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.model.visualization.graph.PlunkedLink;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PlunkedLinkConversion {

    public static PlunkedLink convert(Edge edge){
        PlunkedLink plunkedLink = new PlunkedLink();
        NodeStore sourceNodeDetail = GraphManager.getNodeDetails(edge.getSourceNode());
        NodeStore targetNodeDetail = GraphManager.getNodeDetails(edge.getTargetNode());

        plunkedLink.setSourceNodeKey(sourceNodeDetail.getKey());
        plunkedLink.setTargetNodeKey(targetNodeDetail.getKey());

        return plunkedLink;
    }

}
