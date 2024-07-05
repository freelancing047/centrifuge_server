package csi.server.common.dto.graph.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.visualization.graph.NodeDef;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PlunkNodeDTO implements IsSerializable {

    private String vizUuid;
    private String nodeName;
    private String nodeType;
    private int clientPositionX;
    private int clientPositionY;
    private NodeDef nodeDef;

    public static PlunkNodeDTO create(String vizUuid, String nodeName, String nodeType, int clientX, int clientY, NodeDef nodeDef){
        PlunkNodeDTO plunkNodeDTO = new PlunkNodeDTO();
        plunkNodeDTO.setVizUuid(vizUuid);
        plunkNodeDTO.setNodeName(nodeName);
        plunkNodeDTO.setNodeType(nodeType);
        plunkNodeDTO.setClientPositionX(clientX);
        plunkNodeDTO.setClientPositionY(clientY);
        plunkNodeDTO.setNodeDef(nodeDef);
        return plunkNodeDTO;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    public void setVizUuid(String vizUuid) {
        this.vizUuid = vizUuid;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public int getClientPositionX() {
        return clientPositionX;
    }

    public void setClientPositionX(int clientPositionX) {
        this.clientPositionX = clientPositionX;
    }

    public int getClientPositionY() {
        return clientPositionY;
    }

    public void setClientPositionY(int clientPositionY) {
        this.clientPositionY = clientPositionY;
    }

    public NodeDef getNodeDef() {
        return nodeDef;
    }

    public void setNodeDef(NodeDef nodeDef) {
        this.nodeDef = nodeDef;
    }

}
