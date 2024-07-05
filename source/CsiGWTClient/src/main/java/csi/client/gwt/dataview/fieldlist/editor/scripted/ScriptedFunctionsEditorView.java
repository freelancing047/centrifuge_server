package csi.client.gwt.dataview.fieldlist.editor.scripted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.Container;
import com.github.gwtbootstrap.client.ui.Row;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DeckPanel;

import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.dataview.fieldlist.editor.FieldModel;
import csi.client.gwt.dataview.fieldlist.editor.scripted.widget.CalculateDurationWidget;
import csi.client.gwt.dataview.fieldlist.editor.scripted.widget.CalculateValueWidget;
import csi.client.gwt.dataview.fieldlist.editor.scripted.widget.ConcatenateFieldsWidget;
import csi.client.gwt.dataview.fieldlist.editor.scripted.widget.JavascriptWidget;
import csi.client.gwt.dataview.fieldlist.editor.scripted.widget.ScriptedFunctionsWidget;
import csi.client.gwt.dataview.fieldlist.editor.scripted.widget.SubstringWidget;
import csi.client.gwt.validation.multi.MultiValidator;
import csi.client.gwt.validation.multi.MultiValidatorShowingFirstFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.server.common.dto.CompilationResult;

/**
 * @author Centrifuge Systems, Inc.
 * A deck of DynamicFunctionWidgets.
 */
public class ScriptedFunctionsEditorView extends Row {

    private final DeckPanel deckPanel;
    private final ScriptedFunctionsWidget concatenateFieldsWidget;
    private final ScriptedFunctionsWidget substringWidget;
    private final ScriptedFunctionsWidget calculateValueWidget;
    private final ScriptedFunctionsWidget calculateDurationWidget;
    private final JavascriptWidget javascriptWidget;

    private ScriptedFunctions currentFunction;
    private ScriptedFunctionsWidget currentWidget;
    private MultiValidator multiValidator;

    private final Map<ScriptedFunctions, ScriptedFunctionsWidget> scriptedMap;


    public ScriptedFunctionsEditorView(FieldList fieldListIn, FieldModel model) {
        this.deckPanel = new DeckPanel();
        this.concatenateFieldsWidget = new ConcatenateFieldsWidget(fieldListIn, model.getName());
        this.substringWidget = new SubstringWidget(fieldListIn, model.getName());
        this.calculateValueWidget = new CalculateValueWidget(fieldListIn, model.getName());
        this.calculateDurationWidget = new CalculateDurationWidget(fieldListIn, model.getName());
        this.javascriptWidget = new JavascriptWidget(fieldListIn, model.getName());

        scriptedMap = new HashMap<ScriptedFunctions, ScriptedFunctionsWidget>();
        scriptedMap.put(ScriptedFunctions.CONCATENATE, concatenateFieldsWidget);
        scriptedMap.put(ScriptedFunctions.SUBSTRING, substringWidget);
        scriptedMap.put(ScriptedFunctions.CALCULATE_VALUE, calculateValueWidget);
        scriptedMap.put(ScriptedFunctions.CALCULATE_DURATION, calculateDurationWidget);
        scriptedMap.put(ScriptedFunctions.ADVANCED_FUNCTION, javascriptWidget);

        currentFunction = ScriptedFunctions.ADVANCED_FUNCTION;
        currentWidget = javascriptWidget;
        createValidator();

        addWidgetsToDeckPanel();
        putDeckPanelInRow();
        showInitalWidget(model);
    }

    public void handleNameChange(String currentModelNameIn) {

        if (null != concatenateFieldsWidget) {
            concatenateFieldsWidget.handleNameChange(currentModelNameIn);
        }
        if (null != substringWidget) {
            substringWidget.handleNameChange(currentModelNameIn);
        }
        if (null != calculateValueWidget) {
            calculateValueWidget.handleNameChange(currentModelNameIn);
        }
        if (null != calculateDurationWidget) {
            calculateDurationWidget.handleNameChange(currentModelNameIn);
        }
        if (null != javascriptWidget) {
            javascriptWidget.handleNameChange(currentModelNameIn);
        }
    }

    public void showWidget(ScriptedFunctions function) {
        clearCurrentWidgetIfInvalid();
        currentFunction = function;
        currentWidget = (null != function) ? scriptedMap.get(currentFunction) : null;
        createValidator();

        deckPanel.showWidget(ScriptedFunctions.getIndex(function));
    }

    private void clearCurrentWidgetIfInvalid() {
        if(currentWidget != null) {

            currentWidget.clear();
        }
    }

    private void createValidator() {

        multiValidator = null;

        if (null != currentWidget) {

            multiValidator = new MultiValidatorShowingFirstFeedback();
            for (ValidationAndFeedbackPair validationAndFeedbackPair : currentWidget.getValidators()) {
                multiValidator.addValidationAndFeedback(validationAndFeedbackPair);
            }
        }
    }

    public boolean isValid() {

        if (ScriptedFunctions.ADVANCED_FUNCTION.equals(currentFunction)) {

            return javascriptWidget.isValid();

        } else {

            return (null != multiValidator) ? multiValidator.validate() : false;
        }
    }

    public ScriptedFunctionsEditorModel getScriptedValues() {

        ScriptedFunctionsEditorModel myModel = null;
        if(currentWidget != null) {

            myModel = currentWidget.getValue();
        }
        return myModel;
    }

    public void setUIFromModel(ScriptedFunctionsEditorModel dynamicFunctionsModel) {
        if(currentWidget != null) {

            currentWidget.setUIFromModel(dynamicFunctionsModel);
        }
    }

    public void addTestButtonClickHandler(ClickHandler clickHandler){
        javascriptWidget.addTestButtonClickHandler(clickHandler);
    }

    public void displaySuccessOrFailure(CompilationResult result) {
        javascriptWidget.handleTestResult(result);
    }

    public List<ValidationAndFeedbackPair> collectAllValidators() {
        List<ValidationAndFeedbackPair> pairs = new ArrayList<ValidationAndFeedbackPair>();
        pairs.addAll(concatenateFieldsWidget.getValidators());
        pairs.addAll(substringWidget.getValidators());
        pairs.addAll(calculateValueWidget.getValidators());
        pairs.addAll(calculateDurationWidget.getValidators());
        pairs.addAll(javascriptWidget.getValidators());
        return pairs;
    }

    //TODO: Add new Dynamic field type code
    private void showInitalWidget(FieldModel model) {
        if (model.getScriptedFunctionsModel().getFunctionType() != null) {
            showWidget(model.getScriptedFunctionsModel().getFunctionType());
        } else {
            showWidget(ScriptedFunctions.ADVANCED_FUNCTION);
        }
    }

    private void putDeckPanelInRow() {
        Column column = new Column(6);
        Container container = new Container();
        column.add(deckPanel);
        container.add(column);
        container.setWidth("480px"); //$NON-NLS-1$
        add(container);
    }

    private void addWidgetsToDeckPanel() {
        deckPanel.add(concatenateFieldsWidget);
        deckPanel.add(substringWidget);
        deckPanel.add(calculateValueWidget);
        deckPanel.add(calculateDurationWidget);
        deckPanel.add(javascriptWidget);
    }

}
