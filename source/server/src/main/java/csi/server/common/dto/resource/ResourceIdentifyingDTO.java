package csi.server.common.dto.resource;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ResourceIdentifyingDTO implements Serializable {

    public static final ResourceIdentifyingDTO NULL_DTO = new ResourceIdentifyingDTO();

    private String uuid;
    private boolean isDataView;

    public ResourceIdentifyingDTO() {
    }

    public ResourceIdentifyingDTO(String uuid, boolean isDataView) {
        this.uuid = uuid;
        this.isDataView = isDataView;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isDataView() {
        return isDataView;
    }

    public void setDataView(boolean dataView) {
        this.isDataView = dataView;
    }
}
