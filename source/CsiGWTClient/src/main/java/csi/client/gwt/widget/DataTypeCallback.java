package csi.client.gwt.widget;

import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 8/23/2017.
 */
public interface DataTypeCallback {

    public void onTypeSelection(CsiDataType dataTypeIn, int rowIn);
}
