package csi.server.business.cachedb.script;

import javax.script.Bindings;

import csi.server.common.dto.FieldListAccess;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;

public interface CsiScriptRunner {

    public abstract Bindings createBindings();

    public abstract Bindings getGlobalBindings();

    public abstract String beautifyScript(String script) throws CentrifugeException;

    public abstract Object invokeMethod(Object object, String method, Object... args) throws CentrifugeException;

    public abstract Object evalExpression(String expr, Bindings bindings) throws CentrifugeException;

    public abstract Object evalScriptedField(FieldListAccess modelIn, FieldDef f, IDataRow rowSet) throws CentrifugeException;

    public abstract String updateFieldReferences(String scriptText, String curName, String newName) throws CentrifugeException;

    public abstract boolean referencesField(String scriptText, String fieldName);

}
