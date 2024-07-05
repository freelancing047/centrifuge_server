package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;


public class ChartResponse implements IsSerializable {

    public ChartTableDTO table;
    public boolean exceededMax = false;
    public int numDataPoints = -1;
    public int maxResults = -1;
    public int numRenderSlots = -1;
}