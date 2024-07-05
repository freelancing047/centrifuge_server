package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.AclControlType;

public class DataviewRequest implements IsSerializable {

	private String dataviewName;
	private String userName;
    private AclControlType accessMode;

	public String getDataviewName() {
		return dataviewName;
	}
	public void setDataviewName(String dataviewName) {
		this.dataviewName = dataviewName;
	}
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public AclControlType getAccessMode() {
        return accessMode;
    }
    public void setAccessMode(AclControlType accessModeIn) {
        this.accessMode = accessModeIn;
    }

}
