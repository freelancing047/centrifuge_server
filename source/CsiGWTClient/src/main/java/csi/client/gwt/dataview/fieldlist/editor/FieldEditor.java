package csi.client.gwt.dataview.fieldlist.editor;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.dataview.fieldlist.editor.scripted.JavascriptFunction;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctions;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditor;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditorModel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.multi.MultiValidator;
import csi.client.gwt.validation.multi.MultiValidatorShowingFirstFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.FunctionType;
import csi.server.common.model.functions.ScriptFunction;

/**
 * Handles logic for operations on a field.
 *
 * @author Centrifuge Systems, Inc.
 */
public class FieldEditor {
    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private FieldList _fieldList;
    private FieldModel _model;
    private FieldEditorView _view;
    private ScriptedFunctionsEditor _scriptedFunctionsEditor;
    private MultiValidator _validator = new MultiValidatorShowingFirstFeedback();

    public FieldEditor(FieldList fieldListIn, FieldModel modelIn, boolean isNewIn) {
        _fieldList = fieldListIn;
        _model = modelIn;
        _scriptedFunctionsEditor = new ScriptedFunctionsEditor(_model, _fieldList);
        _view = new FieldEditorView(_model, _fieldList.getMetaProxy(),
                                    _scriptedFunctionsEditor.getView(), _fieldList.inUse(_model.getUuid()), isNewIn, _fieldList.inUseViz(_model.getUuid()));

        addHandlers();
        initValidator();
    }

    private void initValidator() {
        /*
        Validator notBlankValidator = new NotBlankValidator(getView().getNameTextBox());
        Validator nonDupeValidator = new NonDuplicateFieldValidator(_fieldList.getModel().getFieldDefs(), currentUuid, getView().getNameTextBox());
        ValidationFeedback notBlankFeedback = new ValueBoxControlLabelValidationFeedback(getView().getNameTextBox(), getView().getNameErrorLabel(), i18n.validator_RequiredName());
        ValidationFeedback nonDupeFeedback = new ValueBoxControlLabelValidationFeedback(getView().getNameTextBox(), getView().getNameErrorLabel(), i18n.validator_DuplicateName());
        _validator.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator, notBlankFeedback));
        _validator.addValidationAndFeedback(new ValidationAndFeedbackPair(nonDupeValidator, nonDupeFeedback));
        */
        for (ValidationAndFeedbackPair pair : _scriptedFunctionsEditor.getDynamicValidators()) {
            _validator.addValidationAndFeedback(pair);
        }
    }

    public void checkValidity() {

        _view.checkValidity();
    }

    public void save() {
        final FieldDef fieldDef = createFieldDefFromViewData();
        _fieldList.addOrUpdateFieldDef(fieldDef);
    }

    public void delete() {
        final FieldDef fieldDef = createFieldDefFromViewData();
        _fieldList.deleteFieldDef(fieldDef.getUuid());
    }

    public FieldEditorView getView() {
        return _view;
    }

    public boolean validate() {
        return _validator.validate();
    }

    private FieldDef createFieldDefFromViewData() {
        FieldModel model = _view.storeViewIntoModel();
        return convertFromFieldModel(model);
    }

    //TODO: Add dynamic field type code
    private void addHandlers() {
        _view.addScriptedFunctionsDropdownChangeHandler(new SelectionChangedHandler<String>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {
                ScriptedFunctions functions = ScriptedFunctions.fromTitle(_view.getScriptedFunctionsDropdownValue());
                _scriptedFunctionsEditor.showWidget(functions);
            }
        });

        _scriptedFunctionsEditor.addTestButtonClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final FieldDef fieldDef = createFieldDefFromViewData();
                _fieldList.testScriptedField(fieldDef);
            }
        });
    }

    private static FieldDef convertFromFieldModel(FieldModel model) {
        
        FieldDef fieldDef = model.returnBasicFieldDef();

        if (FieldType.SCRIPTED.equals(fieldDef.getFieldType())) {

            convertScriptedFunction(model, fieldDef);

        } else {

            clearScriptedInfo(fieldDef);
        }
        fieldDef.setDirty(true);
        return fieldDef;
    }

    //TODO: Add dynamic field type code
    private static void convertScriptedFunction(FieldModel model, FieldDef fieldDef) {
        ScriptedFunctionsEditorModel dynamicFunctionsModel = model.getScriptedFunctionsModel();

        if (null != dynamicFunctionsModel) {

            if (dynamicFunctionsModel.getFunctionType() == ScriptedFunctions.ADVANCED_FUNCTION) {
                JavascriptFunction javascriptFunction = (JavascriptFunction) dynamicFunctionsModel.getScriptFunction();
                fieldDef.setScriptText(javascriptFunction.getScript());
            } else {
                fieldDef.setFunctionType(ScriptedFunctions.toFunctionType(dynamicFunctionsModel.getFunctionType()));
                if (dynamicFunctionsModel.getScriptFunction() != null) {
                    fieldDef.setFunctions(Lists.newArrayList(dynamicFunctionsModel.getScriptFunction()));
                }
                fieldDef.setScriptText(null);
            }
        }
    }

    private static void clearScriptedInfo(FieldDef fieldDefIn) {

        fieldDefIn.setFunctions(new ArrayList<ScriptFunction>());
        fieldDefIn.setFunctionType(FunctionType.NONE);
        fieldDefIn.setScriptSeparator(null);
        fieldDefIn.setScriptType(null);
    }
}
