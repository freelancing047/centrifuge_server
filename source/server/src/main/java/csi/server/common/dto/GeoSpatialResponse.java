package csi.server.common.dto;



import com.google.gwt.user.client.rpc.IsSerializable;


public class GeoSpatialResponse implements IsSerializable {

    public long rows = 0;
    public long points = 0;
    public long rejects = 0;
    public String tempFile;
    public String tempPath;
}
