package csi.server.common.dto.graph.gwt;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

public class DragStartDTO implements IsSerializable {

    public int imageX;
    public int imageY;
    public List<NodeMapDTO> dragNodes;

    public DragStartDTO() {

    }

    public int getImageX() {
        return imageX;
    }

    public void setImageX(Integer imageX) {
        this.imageX = imageX;
    }

    public int getImageY() {
        return imageY;
    }

    public void setImageY(Integer imageY) {
        this.imageY = imageY;
    }

    public List<NodeMapDTO> getDragNodes() {
        if(dragNodes == null) {
            return Lists.newArrayList();
        }
        return dragNodes;
    }

    public void setDragNodes(List<NodeMapDTO> dragNodes) {
        this.dragNodes = dragNodes;
    }
}
