package csi.server.common.dto.graph.path;



import com.google.gwt.user.client.rpc.IsSerializable;


public class NeighborRequest implements IsSerializable {

    public int stepsAway;

    public String selectionId;
}
