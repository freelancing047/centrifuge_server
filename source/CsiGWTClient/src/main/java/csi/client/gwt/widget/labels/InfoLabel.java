package csi.client.gwt.widget.labels;

import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by centrifuge on 9/2/2016.
 */
public class InfoLabel extends Label {

    public InfoLabel() {

        super();

        getElement().getStyle().setColor(Dialog.txtInfoColor);
    }
}
