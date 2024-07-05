package csi.server.common.dto;



import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;


public class ChartTableDTO implements IsSerializable {

	public ChartMetaDTO meta;
	public ArrayList<ChartRowDTO> data;
	
	public ChartMetaDTO getMeta() {
		return meta;
	}
	public void setMeta(ChartMetaDTO meta) {
		this.meta = meta;
	}
	public ArrayList<ChartRowDTO> getData() {
		return data;
	}
	public void setData(ArrayList<ChartRowDTO> data) {
		this.data = data;
	}
}
