package csi.client.gwt.dataview.fieldlist.editor;

import java.util.HashMap;
import java.util.Map;

import csi.client.gwt.dataview.fieldlist.editor.scripted.JavascriptFunction;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctions;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditorModel;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.SqlTokenTreeItemList;

/**
 * @author Centrifuge Systems, Inc.
 * Holds data for the FieldEditor
 */
public class FieldModel {

    private String name;
    private CsiDataType dataType;
    private FieldType fieldType;
    private Map<String, String> clientProperties = new HashMap<String, String>();
    private String staticValue;
    private ScriptedFunctionsEditorModel scriptedFunctionsModel;
    private SqlTokenTreeItemList sqlExpression;
    private FieldDef origin = null;
    private boolean preCalculated;
    private boolean newFieldDef;

    public FieldModel() {
        
        origin = new FieldDef(true);
        newFieldDef = true;
    }
    
    public FieldModel(FieldDef originIn) {
        
        origin = originIn;
        
        setName(originIn.getFieldName());
        setDataType(originIn.getValueType());
        setFieldType(originIn.getFieldType());
        setClientProperties(originIn.getClientProperties());
        setStaticValue(originIn.getStaticText());
        setPreCalculated(originIn.isPreCalculated());

        setScriptedFunctionsModel(createScriptedModel(originIn));

        setSqlExpression(originIn.getSqlExpression());

        newFieldDef = false;
    }

    public boolean isNew() {

        return newFieldDef;
    }

    public void setPreCalculated(boolean preCalculatedIn) {

        preCalculated = preCalculatedIn;
    }

    public boolean isPreCalculated() {

        return preCalculated;
    }

    public String getUuid() {
       return origin.getUuid();
   }
   
    public String getName() {
      return name;
  }

    public void setName(String name) {
        this.name = name;
    }

    public CsiDataType getDataType() {
        return dataType;
    }

    public void setDataType(CsiDataType dataType) {
        this.dataType = dataType;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public CsiDataType getStorageType() {
        return (null != origin) ? origin.getStorageType() : null;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public Map<String, String> getClientProperties() {
        return clientProperties;
    }

    public void setClientProperties(Map<String, String> clientProperties) {
        this.clientProperties = clientProperties;
    }

    public String getStaticValue() {
        return staticValue;
    }

    public void setStaticValue(String staticValue) {
        this.staticValue = staticValue;
    }

    public ScriptedFunctionsEditorModel getScriptedFunctionsModel() {
        return scriptedFunctionsModel;
    }

    public void setScriptedFunctionsModel(ScriptedFunctionsEditorModel scriptedFunctionsModel) {
        this.scriptedFunctionsModel = scriptedFunctionsModel;
    }

    public SqlTokenTreeItemList getSqlExpression() {
        return sqlExpression;
    }

    public void setSqlExpression(SqlTokenTreeItemList sqlExpressionIn) {
        sqlExpression = sqlExpressionIn;
    }

    public FieldDef returnBasicFieldDef() {
        
        FieldDef myFieldDef = origin;

        myFieldDef.setFieldName(getName());
        myFieldDef.setValueType(getDataType());
        myFieldDef.setFieldType(getFieldType());
        myFieldDef.setStaticText(getStaticValue());
        myFieldDef.setSqlExpression(getSqlExpression());
        myFieldDef.setPreCalculated(isPreCalculated());
        myFieldDef.setDirty(true);

        return myFieldDef;
    }

    private static ScriptedFunctionsEditorModel createScriptedModel(FieldDef def) {
        ScriptedFunctionsEditorModel scriptedModel = new ScriptedFunctionsEditorModel();
        if(def.getScriptText() != null){
            handleJavascriptFunction(def, scriptedModel);
        }
        else {
            handleOtherScriptedFunctions(def, scriptedModel);
        }
        return scriptedModel;
    }

    private static void handleOtherScriptedFunctions(FieldDef def, ScriptedFunctionsEditorModel scriptedModel) {
        scriptedModel.setFunctionType(ScriptedFunctions.fromFunctionType(def.getFunctionType()));

        if (def.getFunctions() != null && def.getFunctions().size() > 0) {

            scriptedModel.setScriptFunction(def.getFunctions().get(0));
        }
    }

    private static void handleJavascriptFunction(FieldDef def, ScriptedFunctionsEditorModel scriptedModel) {
        scriptedModel.setFunctionType(ScriptedFunctions.ADVANCED_FUNCTION);
        JavascriptFunction jsFunction = new JavascriptFunction();
        jsFunction.setScript(def.getScriptText());
        scriptedModel.setScriptFunction(jsFunction);
    }
}
