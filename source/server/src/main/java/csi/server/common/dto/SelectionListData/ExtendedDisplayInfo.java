package csi.server.common.dto.SelectionListData;


/**
 * Created by centrifuge on 9/16/2016.
 */
public interface ExtendedDisplayInfo extends ExtendedInfo {

    public void clearComponent();
    public void setComponent();
    public void clearSpecial();
    public void setSpecial();
    public void clearError();
    public void setError();
}
