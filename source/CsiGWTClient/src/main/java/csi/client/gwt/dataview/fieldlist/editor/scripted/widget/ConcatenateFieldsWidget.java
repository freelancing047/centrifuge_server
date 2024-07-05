package csi.client.gwt.dataview.fieldlist.editor.scripted.widget;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctions;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditorModel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.OrderedField;
import csi.server.common.model.functions.ConcatFunction;

/**
 * @author Centrifuge Systems, Inc.
 * Append two fields
 */
public class ConcatenateFieldsWidget extends Composite implements ScriptedFunctionsWidget {

    private static final String MARGIN_BOTTOM = "marginBottom"; //$NON-NLS-1$
	private static final String _10PX = "10px"; //$NON-NLS-1$
	private static final String _125PX = "125px"; //$NON-NLS-1$
	private static final String _140PX = "140px"; //$NON-NLS-1$
	private static final String APPEND = "append"; //$NON-NLS-1$
	private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final FieldList fieldList;

    private final Grid grid = new Grid(2, 3);
    private final CsiStringListBox field1ListBox = new CsiStringListBox();
    private final CsiStringListBox field2ListBox = new CsiStringListBox();
    private final CsiStringListBox appendListBox = new CsiStringListBox();
    private final TextBox appendTextBox = new TextBox();

    private final RadioButton textRadio = new RadioButton(APPEND, i18n.fieldList_Concatenate_AppendText()); //$NON-NLS-1$
    private final RadioButton dropdownRadio = new RadioButton(APPEND, i18n.fieldList_Concatenate_AppendSpecial()); //$NON-NLS-1$

    public ConcatenateFieldsWidget(FieldList fieldListIn, String currentModelName) {
        this.fieldList = fieldListIn;

        buildGrid();

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(grid);
        mainPanel.add(createRadioButtonPanel());

        initWidget(mainPanel);

        setSpacing();
        populateDropdowns(currentModelName);
    }

    @Override
    public ScriptedFunctionsEditorModel getValue() {
        ScriptedFunctionsEditorModel model = new ScriptedFunctionsEditorModel();
        ConcatFunction concatFunction = createConcatFunction();

        model.setFunctionType(ScriptedFunctions.CONCATENATE);
        model.setScriptFunction(concatFunction);
        return model;
    }

    @Override
    public List<ValidationAndFeedbackPair> getValidators() {
        return new ArrayList<ValidationAndFeedbackPair>();
    }

    @Override
    public void clear() {

    }

    @Override
    public void setUIFromModel(ScriptedFunctionsEditorModel dynamicFunctionsModel) {
        ConcatFunction concatFunction = (ConcatFunction)dynamicFunctionsModel.getScriptFunction();

        if (null != concatFunction) {

            setSelectedValue(field1ListBox, getFieldName(concatFunction.getFields().get(0)));
            setSelectedValue(field2ListBox, getFieldName(concatFunction.getFields().get(1)));
            setSeparatorUI(concatFunction.getSeparator());

        } else {

            Display.error(i18n.concatenateFieldsWidgetErrorTitle(), //$NON-NLS-1$
                    i18n.concatenateFieldsWidgetErrorMessage()); //$NON-NLS-1$

            dynamicFunctionsModel.setScriptFunction(createConcatFunction());
        }
    }

    @Override
    public void handleNameChange(String currentModelNameIn) {

        populateDropdowns(currentModelNameIn);
    }

    private void setSelectedValue(CsiStringListBox listBoxIn, String valueIn) {

        if (null != valueIn) {

            listBoxIn.setSelectedValue(valueIn);

        } else {

            listBoxIn.setSelectedIndex(-1);
        }
    }

    private String getFieldName(OrderedField fieldIn) {

        FieldDef myField = (null != fieldIn) ? fieldIn.getFieldDef(fieldList.getModelProxy()) : null;

        return (null != myField) ? myField.getFieldName() : null;
    }

    private ConcatFunction createConcatFunction() {
        ConcatFunction concatFunction = new ConcatFunction();
        concatFunction.setOrdinal(0);

        List<OrderedField> orderedFields = createOrderedFieldList();
        concatFunction.setFields(orderedFields);

        concatFunction.setSeparator(getSeparatorValue());
        return concatFunction;
    }

    private void buildGrid() {
        grid.setWidget(0, 0, new ControlLabel(i18n.fieldList_Field()));
        grid.setWidget(0, 1, new ControlLabel(i18n.fieldList_Concatenate_With()));
        grid.setWidget(0, 2, new ControlLabel(i18n.fieldList_Field()));
        grid.setWidget(1, 0, field1ListBox);
        grid.setWidget(1, 1, appendListBox);
        grid.setWidget(1, 2, field2ListBox);
    }

    private void populateDropdowns(String currentModelNameIn) {

        List<FieldDef> myFieldList = fieldList.getModelProxy().getAlphaOrderedNonDependentFieldDefs();

        field1ListBox.clear();
        field2ListBox.clear();
        myFieldList.addAll(fieldList.getModelProxy().getAlphaOrderedScriptedFieldDefs());
        for (FieldDef myField : myFieldList) {
            String myFieldName = myField.getFieldName();
            if ((null == currentModelNameIn) || (0 == currentModelNameIn.length())
                    || ((!myFieldName.equals(currentModelNameIn))
                        && ((!FieldType.SCRIPTED.equals(myField.getFieldType()))
                            || (0 > myFieldName.compareTo(currentModelNameIn))))) {
                field1ListBox.addItem(myFieldName);
                field2ListBox.addItem(myFieldName);
            }
        }
        field1ListBox.setSelectedIndex(0);
        field2ListBox.setSelectedIndex(0);

        appendListBox.addItem(i18n.concatenateFieldsNoValue(), ""); //$NON-NLS-1$ //$NON-NLS-2$
        appendListBox.addItem(i18n.concatenateFieldsSpace(), " "); //$NON-NLS-1$ //$NON-NLS-2$
        appendListBox.addItem(i18n.concatenateFieldsTab(), "\t"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private HorizontalPanel createRadioButtonPanel() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(dropdownRadio);
        panel.add(textRadio);
        panel.setCellWidth(dropdownRadio, "195px"); //$NON-NLS-1$
        dropdownRadio.setValue(Boolean.TRUE);

        dropdownRadio.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                grid.setWidget(1, 1, appendListBox);
            }
        });

        textRadio.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                grid.setWidget(1, 1, appendTextBox);
            }
        });

        return panel;
    }

    private void setSpacing() {
        field1ListBox.setWidth(_140PX); //$NON-NLS-1$
        field2ListBox.setWidth(_140PX); //$NON-NLS-1$
        appendTextBox.setWidth(_125PX); //$NON-NLS-1$
        appendListBox.setWidth(_140PX); //$NON-NLS-1$

        field1ListBox.getElement().getStyle().setProperty(MARGIN_BOTTOM, _10PX); //$NON-NLS-1$ //$NON-NLS-2$
        field2ListBox.getElement().getStyle().setProperty(MARGIN_BOTTOM, _10PX); //$NON-NLS-1$ //$NON-NLS-2$
        appendListBox.getElement().getStyle().setProperty(MARGIN_BOTTOM, _10PX); //$NON-NLS-1$ //$NON-NLS-2$

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                grid.getCellFormatter().setHorizontalAlignment(j, i, HasAlignment.ALIGN_CENTER);
                grid.getCellFormatter().setVerticalAlignment(j, i, HasAlignment.ALIGN_MIDDLE);
            }
        }
    }

    private void setSeparatorUI(String separator) {
        if(separator.equals("") || separator.equals(" ") || separator.equals("\t")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            appendListBox.setSelectedValue(separator);
            dropdownRadio.setValue(Boolean.TRUE);
            grid.setWidget(1, 1, appendListBox);
        }else{
            appendTextBox.setText(separator);
            textRadio.setValue(Boolean.TRUE);
            grid.setWidget(1, 1, appendTextBox);
        }
    }

    private String getSeparatorValue() {
        if(textRadio.getValue()){
            return appendTextBox.getValue();
        }
        else{
            return appendListBox.getSelectedValue();
        }
    }

    private List<OrderedField> createOrderedFieldList() {
        OrderedField orderedField1 = createOrderedField(field1ListBox.getSelectedValue(), 0);
        OrderedField orderedField2 = createOrderedField(field2ListBox.getSelectedValue(), 1);

        List<OrderedField> orderedFields = new ArrayList<OrderedField>(2);
        orderedFields.add(orderedField1);
        orderedFields.add(orderedField2);
        return orderedFields;
    }

    private OrderedField createOrderedField(String fieldName, int ordinal) {
        OrderedField orderedField = new OrderedField();
        FieldDef def = fieldList.getModelProxy().findFieldDefByName(fieldName);
        orderedField.setOrdinal(ordinal);
        orderedField.setFieldDef(def);
        return orderedField;
    }

}
