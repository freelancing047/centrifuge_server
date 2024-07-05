package csi.server.common.dto.graph.search;



import com.google.gwt.user.client.rpc.IsSerializable;


public class EdgeInfo implements IsSerializable {

    public String sourceLabel;
    public String targetLabel;
    public Number id;

}
