package csi.client.gwt.dataview.fieldlist;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.dataview.fieldlist.FieldList.FieldListView;
import csi.client.gwt.dataview.fieldlist.housing.FieldListHousingView;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.input_boxes.ValidityCheck;

/**
 * Dialog appears when clicking Edit Fields... from a data view menu.
 */
public class FieldListDialog implements FieldListView, ValidityCheck {

    interface MyUiBinder extends UiBinder<Widget, FieldListDialog> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    @UiField
    protected VerticalPanel basePanel;
    @UiField
    protected HorizontalPanel gridLabelPanel;
    @UiField
    protected Label gridInstructions;
    @UiField
    protected Button createNewButton;
    
    @UiField(provided = true)
	String gridInstructionsText = _constants.fieldListDialogInstruction(Dialog.txtEditButton, Dialog.txtDeleteButton); //$NON-NLS-1$
    
    @UiField(provided = true)
	String buttonText = _constants.fieldListDialogButtonText(); //$NON-NLS-1$

    private Button saveEditorButton = new Button(Dialog.txtApplyButton);
    private Button deleteEditorButton = new Button(Dialog.txtDeleteButton);
    private Button cancelEditorButton = new Button(Dialog.txtCancelButton);
    private FieldListHousingView _housingView = null;
    private ClickHandler _saveButtonClickHandler = null;
    private ClickHandler _exitButtonClickHandler = null;

    @UiField
    ValidatingDialog dialog;

    private ClickHandler saveButtonClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            enable(false);

            if (null != _saveButtonClickHandler) {

                _saveButtonClickHandler.onClick(eventIn);
            }
        }
    };

    private ClickHandler exitButtonClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            enable(false);

            if (null != _exitButtonClickHandler) {

                _exitButtonClickHandler.onClick(eventIn);
            }
        }
    };

    public void enable(boolean isEnabledIn) {

        dialog.getActionButton().setEnabled(isEnabledIn);
        _housingView.setEnabled(isEnabledIn);
    }

    public void close() {

        dialog.hide();
        dialog.removeFromParent();
    }

    public FieldListDialog(FieldListHousingView housingView, String titleIn) {

        _housingView = housingView;

        uiBinder.createAndBindUi(this);

        setTitle(titleIn);
        basePanel.add(_housingView);
        dialog.setCallBack(this);
        setupButtons();
    }

    public void setTitle(String titleIn) {

        dialog.setTitle(titleIn);
    }

    @Override
    public void addCreateButtonClickHandler(ClickHandler clickHandler) {
        createNewButton.addClickHandler(clickHandler);
    }

    @Override
    public void addSaveEditorButtonClickHandler(ClickHandler clickHandler) {
        saveEditorButton.addClickHandler(clickHandler);
    }

    @Override
    public void addCancelEditorButtonClickHandler(ClickHandler clickHandler) {
        cancelEditorButton.addClickHandler(clickHandler);
    }

    @Override
    public void addDeleteEditorButtonClickHandler(ClickHandler clickHandler) {
        deleteEditorButton.addClickHandler(clickHandler);
    }

    @Override
    public void addSaveButtonClickHandler(ClickHandler clickHandler) {

        _saveButtonClickHandler = clickHandler;
    }

    @Override
    public void addExitButtonClickHandler(ClickHandler clickHandler) {

        _exitButtonClickHandler = clickHandler;
    }

    @Override
    public void updateButtonVisibilitiesForEditorMode(boolean deletable) {
        dialog.getActionButton().setVisible(false);
        dialog.getCancelButton().setVisible(false);
        gridLabelPanel.setVisible(false);
        gridInstructions.setVisible(false);
        createNewButton.setVisible(false);
        cancelEditorButton.setVisible(true);
        saveEditorButton.setVisible(true);
        deleteEditorButton.setVisible(deletable);
    }

    @Override
    public void updateButtonVisibilitiesForGridMode() {
        dialog.getActionButton().setVisible(true);
        dialog.getCancelButton().setVisible(true);
        gridLabelPanel.setVisible(true);
        gridInstructions.setVisible(true);
        createNewButton.setVisible(true);
        cancelEditorButton.setVisible(false);
        saveEditorButton.setVisible(false);
        deleteEditorButton.setVisible(false);
    }

    @Override
    public void show() {
        dialog.show(70);
    }

    @Override
    public void checkValidity() {

        if (null != _housingView) {

            saveEditorButton.setEnabled(_housingView.checkValidity());

        } else {

            saveEditorButton.setEnabled(true);
        }
    }

    private void setupButtons() {

        dialog.getActionButton().addClickHandler(saveButtonClickHandler);
        dialog.getActionButton().setText(Dialog.txtSaveButton);

        dialog.getCancelButton().addClickHandler(exitButtonClickHandler);
        dialog.getCancelButton().setText(Dialog.txtCancelButton);
        dialog.hideOnCancel();

        gridLabelPanel.setVisible(true);
        gridInstructions.setVisible(true);
        createNewButton.setVisible(true);

        saveEditorButton.setType(ButtonType.SUCCESS);
        deleteEditorButton.setType(ButtonType.DANGER);

        dialog.addRightControl(cancelEditorButton);
        dialog.addRightControl(saveEditorButton);
        dialog.addRightControl(deleteEditorButton);

        cancelEditorButton.setVisible(false);
        saveEditorButton.setVisible(false);
        deleteEditorButton.setVisible(false);
    }
}
