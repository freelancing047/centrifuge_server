package csi.client.gwt.viz.graph.plunk;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.event.TriggerClickEvent;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.ui.form.SelectOrEnterStringComboBox;

/**
 * Wraps a dialog for naming and typing a new node.
 * @author Centrifuge Systems, Inc.
 */
public class PlunkNodeDialog {

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private final PlunkNodePresenter presenter;

    @UiField
    Dialog dialog;

    private HTML titleHtml = new HTML("<h4>"+ CentrifugeConstantsLocator.get().userAddNOde_DialogTitle()+"</h4>");
    private TextBox nameTextBox = new TextBox();
    private SelectOrEnterStringComboBox typeComboBox;
    private Label nameErrorLabel = new Label();
    private Label typeErrorLabel = new Label();

    public PlunkNodeDialog(PlunkNodePresenter presenter) {
        this.presenter = presenter;
        uiBinder.createAndBindUi(this);

        initComboBox();
        buildUI();
        setupButtons();
        initializeHandlers();
    }

    public void show() {
        dialog.show();
    }

    public void hide() {
        dialog.hide();
    }

    public Label getTypeErrorLabel() {
        return typeErrorLabel;
    }

    public Label getNameErrorLabel() {
        return nameErrorLabel;
    }

    public ValueBoxBase<String> getNameTextBox() {
        return nameTextBox;
    }

    public HasValue<String> getTypeComboBox() {
        return typeComboBox;
    }

    private void initComboBox() {
        typeComboBox = new SelectOrEnterStringComboBox();
        presenter.addLegendItemsToListStore(typeComboBox.getStore());
    }

    private void buildUI() {
        VerticalPanel vp = new VerticalPanel();
        vp.add(titleHtml);
        vp.add(createRow(CentrifugeConstantsLocator.get().userAddNode_nodeName(), createNameTextBoxAndErrorLabel()));
        vp.add(createRow(CentrifugeConstantsLocator.get().userAddNode_nodeType(), createTypeDropdownAndErrorLabel()));
        vp.setHeight("155px");//NON-NLS

        dialog.add(vp);

        nameErrorLabel.getElement().getStyle().setColor(Dialog.txtErrorColor);
        typeErrorLabel.getElement().getStyle().setColor(Dialog.txtErrorColor);
    }

    private Row createRow(String label, Widget control) {
        Row row = new Row();
        Column column1 = new Column(2);
        column1.add(new ControlLabel(label));

        Column column2 = new Column(4);
        column2.add(control);

        row.add(column1);
        row.add(column2);

        return row;
    }

    private HorizontalPanel createNameTextBoxAndErrorLabel() {
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(nameTextBox);
        hp.add(nameErrorLabel);

        nameTextBox.setWidth("150px");//NON-NLS
        hp.setCellWidth(nameTextBox, "180px");//NON-NLS
        return hp;
    }

    private HorizontalPanel createTypeDropdownAndErrorLabel() {
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(typeComboBox);
        hp.add(typeErrorLabel);

        typeComboBox.setWidth("170px");//NON-NLS
        hp.setCellWidth(typeComboBox, "180px");//NON-NLS
        return hp;
    }

    private void setupButtons() {
        dialog.hideOnCancel();
        Button actionButton = dialog.getActionButton();
        actionButton.setText(CentrifugeConstantsLocator.get().dialog_CreateButton());
    }

    private void initializeHandlers() {
        dialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.createPlunkedNode();
            }
        });

        typeComboBox.addTriggerClickHandler(new TriggerClickEvent.TriggerClickHandler() {
            @Override
            public void onTriggerClick(TriggerClickEvent event) {
                typeErrorLabel.setText("");
            }
        });
        nameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                presenter.validateName();
            }
        });
    }

    interface SpecificUiBinder extends UiBinder<Dialog, PlunkNodeDialog> {
    }
}
