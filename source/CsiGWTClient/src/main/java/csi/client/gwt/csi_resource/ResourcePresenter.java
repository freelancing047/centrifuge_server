package csi.client.gwt.csi_resource;

import csi.client.gwt.dataview.DataViewLoadingCallback;
import csi.server.common.model.Resource;


public interface ResourcePresenter<T extends Resource> {

    public void save(ResourceSaveCallback callbackIn);
    public void onLoad(final DataViewLoadingCallback callbackIn);
    public T getResource();
    public String getUuid();
    public String getName();
    public String getDisplayName();
    public void setName(String nameIn);
    public void setRemarks(String remarksIn);
}
