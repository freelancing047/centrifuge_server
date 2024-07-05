package csi.client.gwt.viz.graph.plunk.edit;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;

import csi.client.gwt.widget.boot.Dialog;

/**
 * @author Centrifuge Systems, Inc.
 */
public class EditPlunkedItemDialog {

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    @UiField
    Dialog dialog;

    public EditPlunkedItemDialog(IsWidget view) {
        uiBinder.createAndBindUi(this);
        dialog.add(view);
        setupButtons();
    }

    private void setupButtons() {
        dialog.getActionButton().setText(Dialog.txtSaveButton);
        dialog.getActionButton().setType(ButtonType.SUCCESS);
        dialog.getCancelButton().setText(Dialog.txtCloseButton);
        dialog.hideOnCancel();
    }

    public void show() {
        dialog.show();
    }

    public void hide() {
        dialog.hide();
    }

    public void addSaveClickHandler(ClickHandler clickHandler){
        dialog.getActionButton().addClickHandler(clickHandler);
    }

    interface SpecificUiBinder extends UiBinder<Dialog, EditPlunkedItemDialog> {
    }

    public void remove() {
        dialog.destroy();
    }
}
