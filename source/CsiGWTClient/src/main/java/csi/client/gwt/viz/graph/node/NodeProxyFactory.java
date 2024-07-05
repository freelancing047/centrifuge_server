package csi.client.gwt.viz.graph.node;

import java.util.Map;

import com.google.common.collect.Maps;

import csi.client.gwt.viz.graph.settings.fielddef.FieldProxy;
import csi.server.common.model.visualization.graph.NodeDef;

public class NodeProxyFactory {

    private Map<String, NodeProxy> uuuidToNodeProxyMap;

    public NodeProxyFactory() {
        uuuidToNodeProxyMap = Maps.newTreeMap();
    }


    public NodeProxy create(FieldProxy fieldProxy) {
        NodeProxy nodeProxy = new NodeProxy(fieldProxy);
        uuuidToNodeProxyMap.put(nodeProxy.getUuid(), nodeProxy);
        return nodeProxy;
    }

    public NodeProxy create(NodeDef nodeDef) {
        NodeProxy nodeProxy;
        nodeProxy = uuuidToNodeProxyMap.get(nodeDef.getUuid());
        if (nodeProxy == null) {
            nodeProxy = new NodeProxy(nodeDef);
            uuuidToNodeProxyMap.put(nodeDef.getUuid(), nodeProxy);
        }
        return nodeProxy;
    }

    public NodeProxy getNodeByUUID(String UUID) {
        return uuuidToNodeProxyMap.get(UUID);
    }
}