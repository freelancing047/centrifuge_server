package csi.client.gwt.dataview;

/**
 * Created by centrifuge on 12/26/2018.
 */
public interface DataViewLoadingCallback {

    public void onCallback(long countIn, boolean moreDataIn);
    public void onCallback(boolean moreDataIn);
}
