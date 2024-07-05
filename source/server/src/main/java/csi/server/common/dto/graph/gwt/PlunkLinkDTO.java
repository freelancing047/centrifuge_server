package csi.server.common.dto.graph.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.visualization.graph.LinkDef;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PlunkLinkDTO implements IsSerializable {

    private String vizUuid;
    private double sourceNodeCenterX;
    private double sourceNodeCenterY;
    private double targetNodeX;
    private double targetNodeY;
    private LinkDef linkDef;

    public static PlunkLinkDTO create(String vizUuid, double sourceNodeCenterX, double sourceNodeCenterY, double targetNodeX, double targetNodeY, LinkDef linkDef){
        PlunkLinkDTO plunkLinkDTO = new PlunkLinkDTO();
        plunkLinkDTO.setVizUuid(vizUuid);
        plunkLinkDTO.setSourceNodeCenterX(sourceNodeCenterX);
        plunkLinkDTO.setSourceNodeCenterY(sourceNodeCenterY);
        plunkLinkDTO.setTargetNodeX(targetNodeX);
        plunkLinkDTO.setTargetNodeY(targetNodeY);
        plunkLinkDTO.setLinkDef(linkDef);
        return plunkLinkDTO;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    public void setVizUuid(String vizUuid) {
        this.vizUuid = vizUuid;
    }

    public double getSourceNodeCenterX() {
        return sourceNodeCenterX;
    }

    public void setSourceNodeCenterX(double sourceNodeCenterX) {
        this.sourceNodeCenterX = sourceNodeCenterX;
    }

    public double getSourceNodeCenterY() {
        return sourceNodeCenterY;
    }

    public void setSourceNodeCenterY(double sourceNodeCenterY) {
        this.sourceNodeCenterY = sourceNodeCenterY;
    }

    public double getTargetNodeX() {
        return targetNodeX;
    }

    public void setTargetNodeX(double targetNodeX) {
        this.targetNodeX = targetNodeX;
    }

    public double getTargetNodeY() {
        return targetNodeY;
    }

    public void setTargetNodeY(double targetNodeY) {
        this.targetNodeY = targetNodeY;
    }

    public LinkDef getLinkDef() {
        return linkDef;
    }

    public void setLinkDef(LinkDef linkDef) {
        this.linkDef = linkDef;
    }
}
