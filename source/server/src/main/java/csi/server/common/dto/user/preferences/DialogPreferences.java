package csi.server.common.dto.user.preferences;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 5/20/2016.
 */
public class DialogPreferences implements IsSerializable {

    double dseLeftPanelWidth;
    double dseRightPanelWidth;

    public DialogPreferences() {
    }

    public void setDseLeftPanelWidth(double dseLeftPanelWidthIn) {

        dseLeftPanelWidth = dseLeftPanelWidthIn;
    }

    public double getDseLeftPanelWidth() {

        return dseLeftPanelWidth;
    }

    public void setDseRightPanelWidth(double dseRightPanelWidthIn) {

        dseRightPanelWidth = dseRightPanelWidthIn;
    }

    public double getDseRightPanelWidth() {

        return dseRightPanelWidth;
    }
}
