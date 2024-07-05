package csi.server.business.visualization.graph.plunk;

import org.bson.types.ObjectId;

import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.model.visualization.graph.PlunkedNode;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PlunkedNodeConversion {

    public static PlunkedNode convert(Node node){
        PlunkedNode plunkedNode = new PlunkedNode();

        NodeStore nodeStore = GraphManager.getNodeDetails(node);
        plunkedNode.setNodeName(nodeStore.getLabel());
        plunkedNode.setNodeType(nodeStore.getType());
        plunkedNode.setNodeKey(nodeStore.getKey());
        plunkedNode.setSize(nodeStore.getRelativeSize());
        plunkedNode.setTransparency((int)nodeStore.getTransparency());
        plunkedNode.setShape(nodeStore.getShape());
        plunkedNode.setColor(nodeStore.getColor());
        plunkedNode.setIcon((nodeStore.getIcon()));

        return plunkedNode;
    }

    public static NodeStore convert(PlunkedNode plunkedNode){
        NodeStore nodeStore = new NodeStore();
        nodeStore.setDocId(ObjectId.get());

        nodeStore.addLabel(plunkedNode.getNodeName());
        nodeStore.setKey(plunkedNode.getNodeKey());
        nodeStore.addType(plunkedNode.getNodeType());
        nodeStore.setColor(plunkedNode.getColor());
        nodeStore.setShape(plunkedNode.getShape());
        nodeStore.setIcon(encodeIcon(plunkedNode.getIcon()));
        nodeStore.setPlunked(true);
        nodeStore.setVisualized(true);

        nodeStore.setTransparency(plunkedNode.getTransparency());
        nodeStore.setSizeMode(ObjectAttributes.CSI_INTERNAL_SIZE_BY_SIZE);
        nodeStore.setRelativeSize(plunkedNode.getSize());
        return nodeStore;
    }

    private static String encodeIcon(String icon) {
        return (icon == null) ? null : icon.replace(" ", "%20");
    }

    private static String decodeIcon(String icon) {
        return (icon == null) ? null : icon.replace("%20", " ");
    }

}
