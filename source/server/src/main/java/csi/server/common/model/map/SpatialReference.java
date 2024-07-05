package csi.server.common.model.map;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SpatialReference implements IsSerializable, Serializable {
    private int wkid;
    public SpatialReference() {
        wkid = 4326;
    }
	public int getWkid() {
		return wkid;
	}
	public void setWkid(int wkid) {
		this.wkid = wkid;
	}
}
