package csi.client.gwt.events;

import java.util.Map;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.server.common.dto.Response;
import csi.server.common.model.dataview.DataView;

public class OpenDirectedViewEvent extends BaseCsiEvent<OpenDirectedViewEventHandler> {

    public static final Type<OpenDirectedViewEventHandler> type = new Type<>();

    private String dataViewUUID;
    private LayoutType layoutType;
    private Map<String, Integer> vizParams;

    public OpenDirectedViewEvent(String dataViewUUID, LayoutType layoutType, Map<String, Integer> params) {
        this.dataViewUUID = dataViewUUID;
        this.layoutType = layoutType;
        this.setVizParams(params);
    }

    private Response<String, DataView> response;

    public OpenDirectedViewEvent(Response<String, DataView> response, LayoutType layoutType, Map<String, Integer> params) {
        this.response = response;
        this.layoutType = layoutType;
        setVizParams(params);
    }

    public String getDataViewUUID() {
        return dataViewUUID;
    }

    public LayoutType getLayoutType(){
    	return this.layoutType;
    }

    @Override
    public Type<OpenDirectedViewEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(OpenDirectedViewEventHandler handler) {
        handler.onDataViewOpen(this);
    }

	public Map<String, Integer> getVizParams() {
		return vizParams;
	}

	public void setVizParams(Map<String, Integer> vizParams) {
		this.vizParams = vizParams;
	}

    public Response<String, DataView> getResponse() {
        return response;
    }
}
