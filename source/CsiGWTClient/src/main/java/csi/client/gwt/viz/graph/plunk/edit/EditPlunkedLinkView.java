package csi.client.gwt.viz.graph.plunk.edit;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.Container;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlternateSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.plunk.edit.widgets.ColorSelector;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.LinkDirectionComboBox;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.client.gwt.widget.ui.form.SelectOrEnterStringComboBox;
import csi.shared.gwt.viz.graph.LinkDirection;

/**
 * A form for editing a plunked link
 * @author Centrifuge Systems, Inc.
 */
public class EditPlunkedLinkView extends Composite {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final EditPlunkedLinkPresenter presenter;
    private final Container container = new Container();

    private HTML titleHtml = new HTML("<h4>" + i18n.plunking_Edit_Link_Title() + "</h4>");
    private TextBox labelTextBox = new TextBox();
    private ComboBox<String> typeComboBox;
    private TextBox sizeTextBox = new TextBox();
    private TextBox transparencyTextBox = new TextBox();
    private LinkDirectionComboBox linkDirectionListBox = new LinkDirectionComboBox();
    private ColorSelector colorSelector = new ColorSelector();

    private final Label sizeError = new Label();
    private final Label transparencyError = new Label();

    public EditPlunkedLinkView(EditPlunkedLinkPresenter presenter){
        this.presenter = presenter;
        initWidget(container);
        initComboBox();
        initLinkDirection();
        buildUI();

        styleUI();
        setupHandlers();
    }

    private void initComboBox() {
        typeComboBox = new SelectOrEnterStringComboBox();
        presenter.addLegendItemsToListStore(typeComboBox.getStore());
    }

    private void initLinkDirection() {
        linkDirectionListBox.getStore().clear();
        for (LinkDirection direction : LinkDirection.values()) {
            linkDirectionListBox.getStore().add(direction);
        }

        linkDirectionListBox.getListView().refresh();
        
    }

    private void styleUI() {
        sizeTextBox.setAlternateSize(AlternateSize.MINI);
        transparencyTextBox.setAlternateSize(AlternateSize.MINI);
        sizeError.setText(i18n.plunking_Width_Help_Info());
        sizeError.getElement().getStyle().setColor(Dialog.txtInfoColor);
        transparencyError.setText(i18n.plunking_Transparency_Help_Info());
        transparencyError.getElement().getStyle().setColor(Dialog.txtInfoColor);

        container.setWidth("480px");//NON-NLS
        container.setHeight("310px");//NON-NLS
        typeComboBox.setWidth(214);
    }

    private void buildUI() {
        container.add(titleHtml);
        addRow(createRow(i18n.plunking_Edit_Node_Label(), labelTextBox));
        addRow(createRow(i18n.plunking_Edit_Node_Type(), typeComboBox));
        addRow(createRow(i18n.plunking_Edit_Link_Direction(), linkDirectionListBox));
        addRow(createSizeValidatedRow(i18n.plunking_Edit_Node_Size(), sizeTextBox, sizeError));
        addRow(createSizeValidatedRow(i18n.plunking_Edit_Node_Transparency(), transparencyTextBox, transparencyError));
        addRow(createRow(i18n.plunking_Edit_Node_Color(), colorSelector, 4));
    }

    private void setupHandlers() {
        sizeTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                presenter.validate();
            }
        });
        transparencyTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                presenter.validate();
            }
        });
    }

    private void addRow(Row row) {
        container.add(row);
    }

    private Row createRow(String label, Widget control) {
        return createRow(label, control, 3);
    }

    private Row createRow(String label, Widget control, int column) {
        Row row = new Row();
        Column column1 = new Column(2);
        column1.add(new Label(label));

        Column column2 = new Column(column);
        column2.add(control);
        column2.getElement().getStyle().setHeight(30, Style.Unit.PX);

        row.add(column1);
        row.add(column2);
        row.getElement().getStyle().setHeight(35, Style.Unit.PX);
        return row;
    }

    private Row createSizeValidatedRow(String label, Widget control, Label errorLabel){
        Row row = new Row();
        Column column1 = new Column(2);
        column1.add(new Label(label));

        Column column2 = new Column(1);
        column2.add(control);
        column2.getElement().getStyle().setHeight(30, Style.Unit.PX);

        Column errorColumn = new Column(3);
        errorColumn.add(errorLabel);

        row.add(column1);
        row.add(column2);
        row.add(errorColumn);
        row.getElement().getStyle().setHeight(35, Style.Unit.PX);
        return row;
    }

    public void apply(EditPlunkedLinkModel model) {
        labelTextBox.setText(model.getLabel());
        linkDirectionListBox.setValue(model.getLinkDirection());
        sizeTextBox.setText(model.getSize() + "");
        transparencyTextBox.setText(model.getTransparency() + "");
        colorSelector.setValue(model.getColor());
        typeComboBox.setValue(model.getLinkType());
    }

    private String getLinkDirectionName(EditPlunkedLinkModel model) {
        if(model.getLinkDirection() != null)
            return model.getLinkDirection().name();
        return null;
    }


    public EditPlunkedLinkModel createModelFromUI() {
        EditPlunkedLinkModel model = new EditPlunkedLinkModel();
        model.setLabel(labelTextBox.getValue());
        model.setLinkType(typeComboBox.getValue());
        model.setLinkDirection(linkDirectionListBox.getValue());
        model.setSize(Integer.valueOf(sizeTextBox.getValue()));
        model.setTransparency(Integer.valueOf(transparencyTextBox.getValue()));
        model.setColor(colorSelector.getValue());
        return model;
    }

    public ValueBoxBase<String> getSizeTextBox() {
        return sizeTextBox;
    }

    public Label getSizeError() {
        return sizeError;
    }

    public ValueBoxBase<String> getTransparencyTextBox() {
        return transparencyTextBox;
    }

    public Label getTransparencyError() {
        return transparencyError;
    }
}
