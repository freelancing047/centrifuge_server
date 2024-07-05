package csi.client.gwt.dataview.fieldlist.editor.scripted.widget;

import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.IsWidget;

import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditorModel;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;

/**
 * @author Centrifuge Systems, Inc.
 * Widgets that save and edit dynamic functions.
 */
public interface ScriptedFunctionsWidget extends IsWidget {

    public void setUIFromModel(ScriptedFunctionsEditorModel dynamicFunctionsModel);

    public ScriptedFunctionsEditorModel getValue();

    public List<ValidationAndFeedbackPair> getValidators();

    public void clear();

    public void handleNameChange(String currentModelNameIn);
}
