package csi.client.gwt.dataview.fieldlist.editor.scripted;

import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.dataview.fieldlist.editor.FieldModel;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;

/**
 * @author Centrifuge Systems, Inc.
 * Logic for showing the appropriate DynamicFunctionsWidget
 */
public class ScriptedFunctionsEditor {
    private ScriptedFunctionsEditorView view;

    public ScriptedFunctionsEditor(FieldModel model, FieldList fieldListIn) {
        view = new ScriptedFunctionsEditorView(fieldListIn, model);
    }

    public ScriptedFunctionsEditorView getView(){
        return this.view;
    }

    public void showWidget(ScriptedFunctions functions) {
        view.showWidget(functions);
    }

    public void addTestButtonClickHandler(ClickHandler clickHandler){
        view.addTestButtonClickHandler(clickHandler);
    }

    public List<ValidationAndFeedbackPair> getDynamicValidators() {
        return view.collectAllValidators();

    }
}
