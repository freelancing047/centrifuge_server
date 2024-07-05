package csi.server.business.visualization.viewer.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

public class ViewerGridConfig implements IsSerializable{

    private List<ViewerGridHeader> headers;

    public List<ViewerGridHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<ViewerGridHeader> headers) {
        this.headers = headers;
    }
}
