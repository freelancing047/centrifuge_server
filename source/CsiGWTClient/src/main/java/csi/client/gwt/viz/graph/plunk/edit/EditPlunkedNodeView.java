package csi.client.gwt.viz.graph.plunk.edit;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.Container;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlternateSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.widget.core.client.event.TriggerClickEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.plunk.edit.widgets.ColorSelector;
import csi.client.gwt.viz.graph.plunk.edit.widgets.IconPicker;
import csi.client.gwt.viz.graph.plunk.edit.widgets.ShapePicker;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.ui.form.SelectOrEnterStringComboBox;

/**
 * A form for editing a plunked node.
 * @author Centrifuge Systems, Inc.
 */
public class EditPlunkedNodeView extends Composite {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final EditPlunkedNodePresenter presenter;
    private final Container container = new Container();

    private HTML titleHtml = new HTML("<h4>" + i18n.plunking_Edit_Node_Title() + "</h4>");
    private TextBox labelTextBox = new TextBox();
    private ComboBox<String> typeComboBox;
    private TextBox sizeTextBox = new TextBox();
    private TextBox transparencyTextBox = new TextBox();
    private ColorSelector colorSelector = new ColorSelector();
    private ShapePicker shapePicker = new ShapePicker();
    private IconPicker iconPicker = new IconPicker();

    private final Label labelError = new Label();
    private final Label typeError = new Label();
    private final Label sizeError = new Label();
    private final Label transparencyError = new Label();

    public EditPlunkedNodeView(EditPlunkedNodePresenter presenter){
        this.presenter = presenter;
        initComboBox();
        buildUI();

        initWidget(container);
        styleUI();
        setupHandlers();
    }

    private void createIconPicker() {
        // TODO Auto-generated method stub
        
    }

    private void setupHandlers() {
        labelTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                presenter.validate();
            }
        });

        typeComboBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                presenter.validate();
            }
        });
        typeComboBox.addTriggerClickHandler(new TriggerClickEvent.TriggerClickHandler() {
            @Override
            public void onTriggerClick(TriggerClickEvent event) {
                typeError.setText("");
            }
        });

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

    private void styleUI() {
        sizeTextBox.setAlternateSize(AlternateSize.MINI);
        transparencyTextBox.setAlternateSize(AlternateSize.MINI);
        sizeError.setText(i18n.plunking_Size_Help_Info());
        sizeError.getElement().getStyle().setColor(Dialog.txtInfoColor);
        transparencyError.setText(i18n.plunking_Transparency_Help_Info());
        transparencyError.getElement().getStyle().setColor(Dialog.txtInfoColor);
        typeComboBox.setWidth(214);
        container.setWidth("480px");//NON-NLS
        container.setHeight("310px");//NON-NLS
    }

    private void initComboBox() {
        typeComboBox = new SelectOrEnterStringComboBox();
        presenter.addLegendItemsToListStore(typeComboBox.getStore());
    }

    private void buildUI() {
        container.add(titleHtml);
        addRow(createValidatedRow(i18n.plunking_Edit_Node_Label(), labelTextBox, labelError));
        addRow(createValidatedRow(i18n.plunking_Edit_Node_Type(), typeComboBox, typeError));
        addRow(createSizeValidatedRow(i18n.plunking_Edit_Node_Size(), sizeTextBox, sizeError));
        addRow(createSizeValidatedRow(i18n.plunking_Edit_Node_Transparency(), transparencyTextBox, transparencyError));
        addRow(createRow(i18n.plunking_Edit_Node_Color(), colorSelector, 5));
        addRow(createRow(i18n.plunking_Edit_Node_Shape(), shapePicker, 5));
        addRow(createRow(i18n.plunking_Edit_Node_Icon(), iconPicker));
    }

    private void addRow(Row row) {
        container.add(row);
    }

    private Row createRow(String label, Widget control) {
        return createRow(label, control, 3);
    }

    private Row createRow(String label, Widget control, int column) {
        Row row = new Row();
        Column column1 = new Column(1);
        column1.add(new Label(label));

        Column column2 = new Column(column);
        column2.add(control);
        column2.getElement().getStyle().setHeight(30, Style.Unit.PX);

        row.add(column1);
        row.add(column2);
        row.getElement().getStyle().setHeight(35, Style.Unit.PX);
        return row;
    }

    private Row createValidatedRow(String label, Widget control, Label errorLabel){
        Row row = createRow(label, control);
        Column errorColumn = new Column(2);
        errorColumn.add(errorLabel);
        row.add(errorColumn);
        return row;
    }

    private Row createSizeValidatedRow(String label, Widget control, Label errorLabel){
        Row row = createRow(label, control, 2);
        Column errorColumn = new Column(3);
        errorColumn.add(errorLabel);

        row.add(errorColumn);
        row.getElement().getStyle().setHeight(35, Style.Unit.PX);
        return row;
    }

    public void apply(EditPlunkedNodeModel model) {
        labelTextBox.setText(model.getLabel());
        typeComboBox.setValue(model.getType());
        sizeTextBox.setText(model.getSize() + "");
        transparencyTextBox.setText(model.getTransparency() + "");
        colorSelector.setValue(model.getColor());
        shapePicker.setValue(model.getShape());
        iconPicker.setValue(model.getIcon());
    }

    public EditPlunkedNodeModel createModelFromUI(){
        EditPlunkedNodeModel model = new EditPlunkedNodeModel();
        model.setLabel(labelTextBox.getValue());
        model.setType(typeComboBox.getValue());
        model.setSize(Double.valueOf(sizeTextBox.getValue()));
        model.setTransparency(Integer.valueOf(transparencyTextBox.getValue()));
        model.setColor(colorSelector.getValue());
        model.setShape(shapePicker.getValue());
        model.setIcon(iconPicker.getValue());
        return model;
    }

    public ValueBoxBase<String> getNameTextBox() {
        return labelTextBox;
    }

    public ComboBox<String> getTypeComboBox() {
        return typeComboBox;
    }

    public TextBox getSizeTextBox() {
        return sizeTextBox;
    }

    public TextBox getTransparencyTextBox() {
        return transparencyTextBox;
    }

    public Label getLabelError() {
        return labelError;
    }

    public Label getTypeError() {
        return typeError;
    }

    public Label getSizeError() {
        return sizeError;
    }

    public Label getTransparencyError() {
        return transparencyError;
    }
}
