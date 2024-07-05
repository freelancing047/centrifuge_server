package csi.client.gwt.dataview.fieldlist.editor.scripted.widget;

import java.util.List;

import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctions;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditorModel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.feedback.ValueBoxErrorLabelValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidator;
import csi.client.gwt.validation.multi.MultiValidatorShowingFirstFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.PositiveIntegerValidator;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.functions.SubstringFunction;

/**
 * @author Centrifuge Systems, Inc.
 *         Substring of a field
 */
public class SubstringWidget extends Composite implements ScriptedFunctionsWidget {

    private static final String _140PX = "140px";
	private static final String _50PX = "50px";
	private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static final int GRID_COLUMNS = 4;
    private static final int GRID_ROWS = 2;
    private final FieldList fieldList;

    private final Grid grid = new Grid(GRID_ROWS, GRID_COLUMNS);
    private final TextBox beginTextBox = new TextBox();
    private final TextBox endTextBox = new TextBox();
    private final CsiStringListBox fieldListBox = new CsiStringListBox();

    private final Label errorLabel = new Label();
    private final MultiValidator multiValidator = new MultiValidatorShowingFirstFeedback();

    public SubstringWidget(FieldList fieldListIn, String currentModelName) {
        this.fieldList = fieldListIn;

        buildGrid();

        SimplePanel mainPanel = new SimplePanel();
        mainPanel.add(grid);
        initWidget(mainPanel);

        setSpacing();
        populateDropdown(currentModelName);
        initValidator();
        addHandlers();
    }

    @Override
    public void setUIFromModel(ScriptedFunctionsEditorModel dynamicFunctionsModel) {
        SubstringFunction substringFunction = (SubstringFunction) dynamicFunctionsModel.getScriptFunction();

        if (null != substringFunction) {

            setSelectedValue(fieldListBox, getFieldName(substringFunction.getField(fieldList.getModelProxy())));

            if (substringFunction.getStartIndex() >= 0) {
                beginTextBox.setText(substringFunction.getStartIndex() + ""); //$NON-NLS-1$
            }
            if (substringFunction.getEndIndex() >= 0) {
                endTextBox.setText(substringFunction.getEndIndex() + ""); //$NON-NLS-1$
            }

        } else {

            Display.error(i18n.substringWidgetProcessErrorTitle(), //$NON-NLS-1$
                    i18n.substringWidgetProcessErrorMessage()); //$NON-NLS-1$

            dynamicFunctionsModel.setScriptFunction(createSubstringFunction());
        }
    }

    @Override
    public void handleNameChange(String currentModelNameIn) {

        populateDropdown(currentModelNameIn);
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

    @Override
    public ScriptedFunctionsEditorModel getValue() {
        ScriptedFunctionsEditorModel model = new ScriptedFunctionsEditorModel();
        SubstringFunction substringFunction = createSubstringFunction();

        model.setFunctionType(ScriptedFunctions.SUBSTRING);
        model.setScriptFunction(substringFunction);
        return model;
    }

    @Override
    public List<ValidationAndFeedbackPair> getValidators() {
        return multiValidator.getValidationAndFeedbackPairs();
    }

    @Override
    public void clear() {
        beginTextBox.setText(""); //$NON-NLS-1$
        endTextBox.setText(""); //$NON-NLS-1$
        multiValidator.validate();
    }

    private SubstringFunction createSubstringFunction() {
        SubstringFunction substringFunction = new SubstringFunction();
        substringFunction.setOrdinal(0);
        substringFunction.setStartIndex(getStartIndexValue());
        substringFunction.setEndIndex(getEndIndexValue());
        substringFunction.setField(getField());
        return substringFunction;
    }

    private void buildGrid() {
        grid.setWidget(0, 0, new ControlLabel(i18n.fieldList_Substring_Begin()));
        grid.setWidget(0, 1, new ControlLabel(i18n.fieldList_Field()));
        grid.setWidget(0, 2, new ControlLabel(i18n.fieldList_Substring_End()));
        grid.setWidget(1, 0, beginTextBox);
        grid.setWidget(1, 1, fieldListBox);
        grid.setWidget(1, 2, endTextBox);
        grid.setWidget(1, 3, errorLabel);
    }

    private FieldDef getField() {
        String fieldName = fieldListBox.getSelectedValue();
        return fieldList.getModelProxy().findFieldDefByName(fieldName);
    }

    private int getEndIndexValue() {
        try {
            return Integer.parseInt(endTextBox.getValue());
        } catch (Exception e) {
            return -1;
        }
    }

    private int getStartIndexValue() {
        try {
            return Integer.parseInt(beginTextBox.getValue());
        } catch (Exception e) {
            return -1;
        }
    }

    private void addHandlers() {
        beginTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                multiValidator.validate();
            }
        });
        endTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                multiValidator.validate();
            }
        });
    }

    private void initValidator() {
        ValidationFeedback beginBoxFeedback = new ValueBoxErrorLabelValidationFeedback(beginTextBox, errorLabel, i18n.substringWidgetFirstCharacterErrorMessage()); //$NON-NLS-1$
        ValidationFeedback endBoxFeedback = new ValueBoxErrorLabelValidationFeedback(endTextBox, errorLabel, i18n.substringWidgetLastCharacterErrorMessage()); //$NON-NLS-1$
        ValidationFeedback valueLessThanFeedback = new ValueBoxErrorLabelValidationFeedback(endTextBox, errorLabel, i18n.substringWidgetIllegalIndexMessage()); //$NON-NLS-1$
        Validator valueLessThanValidator = new Validator(){

            @Override
            public boolean isValid() {
                if("".equals(beginTextBox.getValue()) || "".equals(endTextBox.getValue())) //$NON-NLS-1$ //$NON-NLS-2$
                    return true;

                Integer begin = Integer.valueOf(beginTextBox.getValue());
                Integer end = Integer.valueOf(endTextBox.getValue());
                return begin <= end;
            }
        };

        final ValidationAndFeedbackPair beginValidator = new ValidationAndFeedbackPair(new PositiveIntegerValidator(beginTextBox), beginBoxFeedback);
        final ValidationAndFeedbackPair endValidator = new ValidationAndFeedbackPair(new PositiveIntegerValidator(endTextBox), endBoxFeedback);
        final ValidationAndFeedbackPair valueValidator = new ValidationAndFeedbackPair(valueLessThanValidator, valueLessThanFeedback);

        multiValidator.addValidationAndFeedback(beginValidator);
        multiValidator.addValidationAndFeedback(endValidator);
        multiValidator.addValidationAndFeedback(valueValidator);
    }


    private void setSpacing() {
        fieldListBox.setWidth(_140PX); //$NON-NLS-1$
        beginTextBox.setWidth(_50PX); //$NON-NLS-1$
        endTextBox.setWidth(_50PX); //$NON-NLS-1$
        fieldListBox.getElement().getStyle().setProperty("marginBottom", "10px"); //$NON-NLS-1$ //$NON-NLS-2$

        for (int i = 0; i < GRID_COLUMNS; i++) {
            for (int j = 0; j < GRID_ROWS; j++) {
                grid.getCellFormatter().setHorizontalAlignment(j, i, HasAlignment.ALIGN_CENTER);
                grid.getCellFormatter().setVerticalAlignment(j, i, HasAlignment.ALIGN_MIDDLE);
            }
        }
        grid.setWidth("500px"); //$NON-NLS-1$
        grid.getColumnFormatter().setWidth(3, "250px"); //$NON-NLS-1$
    }

    private void populateDropdown(String currentModelNameIn) {
        List<FieldDef> myFieldList = fieldList.getModelProxy().getAlphaOrderedNonDependentFieldDefs();

        fieldListBox.clear();
        myFieldList.addAll(fieldList.getModelProxy().getAlphaOrderedScriptedFieldDefs());
        for (FieldDef myField : myFieldList) {
            String myFieldName = myField.getFieldName();
            if ((null == currentModelNameIn) || (0 == currentModelNameIn.length())
                    || ((!myFieldName.equals(currentModelNameIn))
                        && ((!FieldType.SCRIPTED.equals(myField.getFieldType()))
                            || (0 > myFieldName.compareTo(currentModelNameIn))))) {
                fieldListBox.addItem(myFieldName);
            }
        }
        fieldListBox.setSelectedIndex(0);
    }
}
