package csi.client.gwt.dataview.fieldlist.editor.scripted.widget;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.SimplePanel;

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
import csi.server.common.model.functions.MathFunction;
import csi.server.common.model.operator.OperatorType;

/**
 * @author Centrifuge Systems, Inc.
 * Calculate a math value.
 */
public class CalculateValueWidget extends Composite implements ScriptedFunctionsWidget {

    private static final String _140PX = "140px";
	private static final String FORWARD_SLASH = "/";
	private static final String ASTERIK = "*";
	private static final String MINUS = "-";
	private static final String PLUS = "+";
	private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final FieldList fieldList;

    private final Grid grid = new Grid(2, 3);
    private final CsiStringListBox field1ListBox = new CsiStringListBox();
    private final CsiStringListBox field2ListBox = new CsiStringListBox();
    private final CsiStringListBox operatorListBox = new CsiStringListBox();

    public CalculateValueWidget(FieldList fieldListIn, String currentModelName) {
        this.fieldList = fieldListIn;

        buildGrid();

        SimplePanel mainPanel = new SimplePanel();
        mainPanel.add(grid);
        initWidget(mainPanel);

        setSpacing();
        populateDropdowns(currentModelName);
    }

    @Override
    public ScriptedFunctionsEditorModel getValue() {
        ScriptedFunctionsEditorModel model = new ScriptedFunctionsEditorModel();
        MathFunction mathFunction = createMathFunction();
        model.setFunctionType(ScriptedFunctions.CALCULATE_VALUE);
        model.setScriptFunction(mathFunction);
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
        MathFunction mathFunction = (MathFunction) dynamicFunctionsModel.getScriptFunction();

        if (null != mathFunction) {

            setSelectedValue(field1ListBox, getFieldName(mathFunction.getField1(fieldList.getModelProxy())));
            setSelectedValue(field2ListBox, getFieldName(mathFunction.getField2(fieldList.getModelProxy())));
            setOperatorListBox(mathFunction.getOperator());

        } else {

            Display.error(i18n.calculateValueWidgetErrorTitle(), //$NON-NLS-1$
                    i18n.calculateValueWidgetErrorMessage()); //$NON-NLS-1$

            dynamicFunctionsModel.setScriptFunction(createMathFunction());
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

    private String getFieldName(FieldDef fieldIn) {
        return (null != fieldIn) ? fieldIn.getFieldName() : null;
    }

    private void buildGrid() {
        grid.setWidget(0, 0, new ControlLabel(i18n.fieldList_Field()));
        grid.setWidget(0, 1, new ControlLabel(i18n.fieldList_CalculateValue_Operator()));
        grid.setWidget(0, 2, new ControlLabel(i18n.fieldList_Field()));
        grid.setWidget(1, 0, field1ListBox);
        grid.setWidget(1, 1, operatorListBox);
        grid.setWidget(1, 2, field2ListBox);
    }

    private MathFunction createMathFunction() {
        MathFunction mathFunction = new MathFunction();
        mathFunction.setOrdinal(0);
        mathFunction.setOperator(getOperatorType());
        mathFunction.setField1(fieldList.getModelProxy().findFieldDefByName(field1ListBox.getSelectedValue()));
        mathFunction.setField2(fieldList.getModelProxy().findFieldDefByName(field2ListBox.getSelectedValue()));
        return mathFunction;
    }

    private OperatorType getOperatorType() {
        if (operatorListBox.getSelectedValue().equals(PLUS)) { //$NON-NLS-1$
            return OperatorType.ADD;
        }
        if (operatorListBox.getSelectedValue().equals(MINUS)) { //$NON-NLS-1$
            return OperatorType.SUBTRACT;
        }
        if (operatorListBox.getSelectedValue().equals(ASTERIK)) { //$NON-NLS-1$
            return OperatorType.MULTIPLY;
        }
        if (operatorListBox.getSelectedValue().equals(FORWARD_SLASH)) { //$NON-NLS-1$
            return OperatorType.DIVIDE;
        }
        return null;
    }

    private void setOperatorListBox(OperatorType operator) {
        switch (operator) {
            case ADD:
                operatorListBox.setSelectedValue(PLUS); //$NON-NLS-1$
                break;
            case SUBTRACT:
                operatorListBox.setSelectedValue(MINUS); //$NON-NLS-1$
                break;
            case MULTIPLY:
                operatorListBox.setSelectedValue(ASTERIK); //$NON-NLS-1$
                break;
            case DIVIDE:
                operatorListBox.setSelectedValue(FORWARD_SLASH); //$NON-NLS-1$
                break;
        }
    }

    private void setSpacing() {
        field1ListBox.setWidth(_140PX); //$NON-NLS-1$
        field2ListBox.setWidth(_140PX); //$NON-NLS-1$
        operatorListBox.setWidth("70px"); //$NON-NLS-1$

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                grid.getCellFormatter().setHorizontalAlignment(j, i, HasAlignment.ALIGN_CENTER);
                grid.getCellFormatter().setVerticalAlignment(j, i, HasAlignment.ALIGN_MIDDLE);
            }
        }
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

        operatorListBox.addItem(PLUS); //$NON-NLS-1$
        operatorListBox.addItem(MINUS); //$NON-NLS-1$
        operatorListBox.addItem(ASTERIK); //$NON-NLS-1$
        operatorListBox.addItem(FORWARD_SLASH); //$NON-NLS-1$
    }
}
