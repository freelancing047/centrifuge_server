package csi.client.gwt.widget.labels;

import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by centrifuge on 9/2/2016.
 */
public class WarningLabel extends Label {

    public WarningLabel() {

        super();

        getElement().getStyle().setColor(Dialog.txtWarningColor);
    }
}
