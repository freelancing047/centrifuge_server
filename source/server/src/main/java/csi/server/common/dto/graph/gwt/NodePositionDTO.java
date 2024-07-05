package csi.server.common.dto.graph.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NodePositionDTO implements IsSerializable {

    public String x;
    public String y;
    public String displayX;
    public String displayY;

    public NodePositionDTO() {

    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getdisplayX() {
        return displayX;
    }

    public void setdisplayX(String displayX) {
        this.displayX = displayX;
    }

    public String getdisplayY() {
        return displayY;
    }

    public void setdisplayY(String displayY) {
        this.displayY = displayY;
    }
}
