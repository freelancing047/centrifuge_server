package csi.client.gwt.dataview.fieldlist.editor.scripted;

import csi.server.common.model.functions.ScriptFunction;

/**
 * @author Centrifuge Systems, Inc.
 * Model that represents an abritrary javascript function
 */
public class JavascriptFunction extends ScriptFunction {

    private String script;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
