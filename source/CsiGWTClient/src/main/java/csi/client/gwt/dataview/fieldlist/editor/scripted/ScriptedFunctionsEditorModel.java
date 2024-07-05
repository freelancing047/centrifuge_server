
package csi.client.gwt.dataview.fieldlist.editor.scripted;

import csi.server.common.model.functions.ScriptFunction;

/**
 * @author Centrifuge Systems, Inc.
 * Data for a dynamic function
 */
public class ScriptedFunctionsEditorModel {
    private ScriptedFunctions functionType;
    private ScriptFunction scriptFunction;

    public ScriptedFunctions getFunctionType() {
        return functionType;
    }

    public void setFunctionType(ScriptedFunctions functionType) {
        this.functionType = functionType;
    }

    public ScriptFunction getScriptFunction() {
        return scriptFunction;
    }

    public void setScriptFunction(ScriptFunction scriptFunction) {
        this.scriptFunction = scriptFunction;
    }
}
