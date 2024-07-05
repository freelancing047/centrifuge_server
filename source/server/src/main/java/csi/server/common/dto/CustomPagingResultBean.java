package csi.server.common.dto;

import java.util.List;

import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class CustomPagingResultBean<Data> extends PagingLoadResultBean<Data> {

    private List<TableDataHeader> headers;
    
    public void setHeaders(List<TableDataHeader> headers) {
        this.headers = headers;
    }

    public List<TableDataHeader> getHeaders(){
        return this.headers;
    }
}
