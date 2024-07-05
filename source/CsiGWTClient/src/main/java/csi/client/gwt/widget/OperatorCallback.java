package csi.client.gwt.widget;

import csi.server.common.enumerations.ComparingToken;

/**
 * Created by centrifuge on 8/24/2017.
 */
public interface OperatorCallback {

    public void onOperatorSelection(ComparingToken operatorIn, int rowIn);
}
