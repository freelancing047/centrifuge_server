package csi.server.common.dto.graph.gwt;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NodeMapDTO implements IsSerializable {

    public Integer ID;
    public Double displayX;
    public Double displayY;
    public Double relativeX;
    public Double relativeY;
    public List<NeighborPropDTO> neighbors;

    public NodeMapDTO() {

    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer iD) {
        ID = iD;
    }

    public Double getDisplayX() {
        return displayX;
    }

    public void setDisplayX(Double displayX) {
        this.displayX = displayX;
    }

    public Double getDisplayY() {
        return displayY;
    }

    public void setDisplayY(Double displayY) {
        this.displayY = displayY;
    }

    public Double getRelativeX() {
        return relativeX;
    }

    public void setRelativeX(Double relativeX) {
        this.relativeX = relativeX;
    }

    public Double getRelativeY() {
        return relativeY;
    }

    public void setRelativeY(Double relativeY) {
        this.relativeY = relativeY;
    }

    public List<NeighborPropDTO> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<NeighborPropDTO> neighbors) {
        this.neighbors = neighbors;
    }
}
