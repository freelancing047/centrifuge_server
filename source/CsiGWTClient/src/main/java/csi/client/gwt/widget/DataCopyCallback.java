package csi.client.gwt.widget;

import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 1/24/2018.
 */
public interface DataCopyCallback<T> {

    public void onCellSelection(T dataIn);
    public void onRowSelection(int rowIn);
}
