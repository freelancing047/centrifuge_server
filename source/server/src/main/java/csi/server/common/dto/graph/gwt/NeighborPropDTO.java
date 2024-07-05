package csi.server.common.dto.graph.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NeighborPropDTO implements IsSerializable {

    public Integer ID;
    public Double displayX;
    public Double displayY;
    public Boolean isInSelection;
    public Double relativeX;
    public Double relativeY;

}
