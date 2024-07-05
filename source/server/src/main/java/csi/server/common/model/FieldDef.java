package csi.server.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import csi.server.common.dto.CsiMap;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.interfaces.ColumnKeys;
import csi.server.common.interfaces.MapByDataType;
import csi.server.common.model.functions.ScriptFunction;
import csi.server.common.util.StringUtil;
import csi.server.common.util.Update;

@Entity
@DynamicUpdate
@DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(csi.server.dao.jpa.FieldDefEntityListener.class)
public class FieldDef extends ModelObject implements DeepCopiable<FieldDef>, InPlaceUpdate<FieldDef>, MapByDataType, ColumnKeys, Serializable {
    private static final boolean CASELESS_NAMES = false;

    public static String defaultNumberFormat = "#0.########";

    @Enumerated(value = EnumType.STRING)
    protected FieldType fieldType;

    @Column(columnDefinition = "TEXT")
    protected String fieldName;

    /**
     * This is an immutable id that is used to associate FieldReference objects to the correct FieldDef by VizDefs that
     * derive from AbstractFieldReferencingViewDefSetting. The value for this is setup an FieldDef construction time.
     * The FieldDefEntityListener is used to ensure that legacy FieldDefs that don't have the localId are given a
     * localId when they are loaded the first time around.
     */
    @Column(nullable = true)
    private String localId;

    protected String dsLocalId;
    protected String tableLocalId;
    protected String abandonedId;
    protected String columnLocalId;

    protected int ordinal;

    @Enumerated(value = EnumType.STRING)
    protected FunctionType functionType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "parent_uuid")
    @Fetch(FetchMode.SELECT)
    protected List<ScriptFunction> functions;

    protected String scriptType;

    @Column(columnDefinition = "TEXT")
    protected String scriptText;

    protected String scriptSeparator;

    @Column(columnDefinition = "TEXT")
    protected String staticText;

    /**
     * TODO: Not sure if we need this or if we should coerce the values to
     * strings. Also, we may just want to have a separate attribute to indicate
     * that it's a "classification" type.
     * <p/>
     * Indicates the field's expected value type String, int, boolean, etc. Set
     * to FieldDef.CLASSIFICATION to indicate that the value of this field is
     * used for classification purposes.
     */
    @Enumerated(value = EnumType.STRING)
    protected CsiDataType valueType;
    protected CsiDataType storageType;

    protected String displayFormat;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    protected SqlTokenTreeItemList sqlExpression;
    protected boolean preCalculated = false;
    protected boolean dirty = false;
    protected int finalSort;

    @Transient
    private int _level = 0;

    public FieldDef() {
		this(FieldType.COLUMN_REF);
    }

   public FieldDef(Boolean trueFieldIn) {
      super();

      if (trueFieldIn.booleanValue()) {
         localId = UUID.randomUUID();
         functions = new ArrayList<ScriptFunction>();
      }
   }

    public FieldDef(FieldType typeIn) {
        super();
        fieldType = typeIn;
        localId = UUID.randomUUID();
        functions = new ArrayList<ScriptFunction>();
    }

    public FieldDef(String nameIn, FieldType typeIn, CsiDataType dataTypeIn) {
        super();
        fieldName = nameIn;
        fieldType = typeIn;
        valueType = dataTypeIn;
        localId = UUID.randomUUID();
        functions = new ArrayList<ScriptFunction>();

        if (FieldType.LINKUP_REF == typeIn) {

            this.columnLocalId = UUID.randomUUID();
            forceStorageType(valueType);
        }
    }

    public FieldDef(int ordinalIn, String nameIn, FieldType typeIn, CsiDataType dataTypeIn,
                    String dsLocalIdIn, String tableLocalIdIn, String localIdIn) {
        super();
        ordinal = ordinalIn;
        fieldName = nameIn;
        fieldType = typeIn;
        valueType = dataTypeIn;
        localId = localIdIn;
        dsLocalId = dsLocalIdIn;
        tableLocalId = tableLocalIdIn;
        columnLocalId = null;
        functions = new ArrayList<ScriptFunction>();
    }

    public String getColumnKey() {

        return columnLocalId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public CsiDataType getDataType() {

        return valueType;
    }

    public CsiDataType getValueType() {
        return valueType;
    }

    public void setValueType(CsiDataType value) {
        this.valueType = value;
    }

    public CsiDataType tryStorageType() {
        return (null != storageType) ? storageType : valueType;
    }

    public CsiDataType getStorageType() {
        return storageType;
    }

    public void forceStorageType(CsiDataType storageTypeIn) {

        storageType = storageTypeIn;
        promoteStorageType();
    }

    public void promoteStorageType() {

        if (null == storageType) {

            storageType = CsiDataType.Unsupported;
        }
        if ((null == valueType) || (CsiDataType.Unsupported == valueType)) {

            valueType = storageType;
        }
    }

    public void setStorageType(CsiDataType storageTypeIn) {
        this.storageType = storageTypeIn;
    }

    public String getDisplayFormat() {
        // if (displayFormat == null) {
        // return null;
        // }
        // TODO: hack, we have to do this
        // to get around ui not allowing exposing
        // display formats to users
        String format = displayFormat;

        if (valueType == null) {
            return null;
        }

        if ((CsiDataType.Integer == valueType) || (CsiDataType.Number == valueType)) {
            format = defaultNumberFormat;

        } else if (format != null) {
            if (CsiDataType.Time == valueType) {
                // remove any date portion from format str
                format = displayFormat.replaceAll("[Mdy/\\\\-]", "");
            } else if (CsiDataType.Date == valueType) {
                // remove any time portion from format
                format = displayFormat.replaceAll("[Hms:]", "");
            } else if (CsiDataType.String == valueType) {
                format = null;
            }
        }

        return format;
    }

    public void setDisplayFormat(String value) {
        this.displayFormat = value;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(String scriptText) {
        this.scriptText = scriptText;
    }

    public String getScriptSeparator() {
        return scriptSeparator;
    }

    public void setScriptSeparator(String scriptSeparator) {
        this.scriptSeparator = scriptSeparator;
    }

    public String getStaticText() {
        return staticText;
    }

    public void setStaticText(String staticText) {
        this.staticText = staticText;
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public void setFunctionType(FunctionType functionType) {
        this.functionType = functionType;
    }

    public List<ScriptFunction> getFunctions() {
        return functions;
    }

    public void setFunctions(List<ScriptFunction> functions) {
        this.functions = functions;
    }

    public boolean isRawScript() {
        return (getFunctionType() == null) || (getFunctionType() == FunctionType.NONE);
    }

   public String mapKey() {
      return (CASELESS_NAMES && (getFieldName() != null)) ? getFieldName().trim().toLowerCase() : getFieldName();
   }

   public static String makeKey(String nameIn) {
      return CASELESS_NAMES ? nameIn.toLowerCase() : nameIn;
   }

    public String getDsLocalId() {
        return dsLocalId;
    }

    public void setDsLocalId(String dsLocalId) {
        this.dsLocalId = dsLocalId;
    }

    public String getTableLocalId() {
        return tableLocalId;
    }

    public void setTableLocalId(String tableLocalId) {
        this.tableLocalId = tableLocalId;
    }

    public String getColumnLocalId() {
        return columnLocalId;
    }

    public void setColumnLocalId(String columnLocalId) {
        this.columnLocalId = columnLocalId;
    }

   public boolean isAnonymous() {
      return (this.fieldType == FieldType.STATIC) && ((this.fieldName == null) || "".equals(this.fieldName));
   }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public SqlTokenTreeItemList getSqlExpression() {

        return sqlExpression;
    }

    public void setSqlExpression(SqlTokenTreeItemList sqlExpressionIn) {

        sqlExpression = sqlExpressionIn;
    }

    public boolean isPreCalculated() {

        return preCalculated || (FieldType.SCRIPTED == fieldType);
    }

    public void setPreCalculated(boolean preCalculatedIn) {

        preCalculated = preCalculatedIn;
    }

    public boolean isDirty() {

        return dirty;
    }

    public void setDirty(boolean dirtyIn) {

        dirty = dirtyIn;
    }

    public int getFinalSort() {

        return finalSort;
    }

    public void setFinalSort(int finalSortIn) {

        finalSort = finalSortIn;
    }

    public String getAbandonedId() {

        return abandonedId;
    }

    public void setAbandonedId(String abandonedIdIn) {

        abandonedId = abandonedIdIn;
    }

    public String getCoercion(CsiDataType sourceTypeIn) {

        return valueType.getCoercion(sourceTypeIn);
    }

    public boolean isNullable() {

        return true;
    }

    public void mapDoubleMapByType(CsiDataType dataTypeIn, Map<String, String> idMapIn, Map<String, String> nameMapIn) {

        if ((getValueType() == dataTypeIn)
                && ((FieldType.COLUMN_REF == getFieldType())
                || (FieldType.LINKUP_REF == getFieldType()))) {

            nameMapIn.put(getFieldName(), getLocalId());
            idMapIn.put(getLocalId(), getFieldName());
        }
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof FieldDef)) {
            return false;
        } else {
            FieldDef typed = (FieldDef) obj;
            return Objects.equal(this.getUuid(), typed.getUuid());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("name", getFieldName()) //
                .add("type", getFieldType()) //
                .add("ordinal", getOrdinal())
                .add("uuid", getUuid()) //
                .add("finalsort", getFinalSort())
                .add("columnlocal", getColumnLocalId())
                .add("displayformat", getDisplayFormat())
                .add("dslocalid", getDsLocalId())
                .add("functions", getFunctions())
                .add("functionType", getFunctionType())
                .add("localid", getLocalId())
                .add("name", getName())
                .add("script sep", getScriptSeparator())
                .add("script text", getScriptText())
                .add("script type", getScriptType())
                .add("static text", getStaticText())
                .add("value type", getValueType())
                .add("storage type", getStorageType())
                .add("tableid", getTableLocalId())
                .toString();
    }

    @Override
    public FieldDef copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = Maps.newHashMap();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if ((copyOfThis != null) && (this.getFieldName() != null)) {
                return (FieldDef) copyOfThis;
            }
        }
        FieldDef copy = new FieldDef();
        copies.put(getUuid(), copy);
        copy.setFinalSort(getFinalSort());
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.columnLocalId = this.columnLocalId;
        copy.displayFormat = this.displayFormat;
        copy.dsLocalId = this.dsLocalId;
        copy.fieldName = this.fieldName;
        copy.fieldType = this.fieldType;
        if (this.functions != null) {
            copy.functions = Lists.newArrayList();

            for (ScriptFunction scriptFunction : this.functions) {
                ScriptFunction sfCopy = scriptFunction.copy(copies);
                copies.put(scriptFunction.getUuid(), sfCopy);
                copy.functions.add(sfCopy);
            }
        }
        copy.functionType = this.functionType;
        copy.localId = this.getLocalId();
        copy.ordinal = this.ordinal;
        copy.scriptSeparator = this.scriptSeparator;
        copy.scriptText = this.scriptText;
        copy.scriptType = this.scriptType;
        copy.staticText = this.staticText;
        copy.tableLocalId = this.tableLocalId;
        copy.valueType = this.valueType;
        copy.storageType = this.storageType;
        copy.uuid = this.uuid;
        copy.sqlExpression = this.sqlExpression;
        return copy;
    }

    public void updateInPlace(FieldDef sourceIn) {

        sourceIn.cloneValues(this);
        updateFunctionList(sourceIn.getFunctions());
        updateSqlExpression(sourceIn.getSqlExpression());
    }

    @Override
    public FieldDef fullClone() {

        FieldDef myClone = new FieldDef();

        super.fullCloneComponents(myClone);

        fullCloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FieldDef clone() {

        FieldDef myClone = new FieldDef();

        cloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cloneComponents(ModelObject cloneIn) {

        super.cloneComponents(cloneIn);

        cloneValues((FieldDef)cloneIn);
        ((FieldDef)cloneIn).setFunctions(cloneFunctions());
        ((FieldDef)cloneIn).setSqlExpression(cloneExpression());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fullCloneComponents(ModelObject cloneIn) {

        super.fullCloneComponents(cloneIn);

        cloneValues((FieldDef)cloneIn);
        ((FieldDef)cloneIn).setFunctions(fullCloneFunctions());
        ((FieldDef)cloneIn).setSqlExpression(fullCloneExpression());
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, fieldType, indentIn, "fieldType");
        debugObject(bufferIn, fieldName, indentIn, "fieldName");
        debugObject(bufferIn, localId, indentIn, "localId");
        debugObject(bufferIn, dsLocalId, indentIn, "dsLocalId");
        debugObject(bufferIn, tableLocalId, indentIn, "tableLocalId");
        debugObject(bufferIn, columnLocalId, indentIn, "columnLocalId");
        debugObject(bufferIn, ordinal, indentIn, "ordinal");
        debugObject(bufferIn, functionType, indentIn, "functionType");
        debugObject(bufferIn, scriptType, indentIn, "scriptType");
        debugObject(bufferIn, scriptText, indentIn, "scriptText");
        debugObject(bufferIn, scriptSeparator, indentIn, "scriptSeparator");
        debugObject(bufferIn, staticText, indentIn, "staticText");
        debugObject(bufferIn, valueType, indentIn, "valueType");
        debugObject(bufferIn, storageType, indentIn, "storageType");
        debugObject(bufferIn, displayFormat, indentIn, "displayFormat");
        debugList(bufferIn, functions, indentIn, "functions");
    }

   private void updateFunctionList(List<ScriptFunction> newListIn) {
      if (newListIn != null) {
         functions = Update.updateListInPlace(functions, newListIn);

         if (functions != null) {
            int nextOrdinal = 0;

            for (ScriptFunction myFunction : functions) {
               myFunction.setOrdinal(nextOrdinal++);
            }
         }
      } else {
         functions = null;
      }
   }

    private void updateSqlExpression(SqlTokenTreeItemList expressionIn) {

        if ((null != sqlExpression) && (null != expressionIn)) {

            sqlExpression.updateInPlace(expressionIn);

        } else {

            sqlExpression = expressionIn;
        }
    }

    private List<ScriptFunction> cloneFunctions() {

        if (null != getFunctions()) {

            List<ScriptFunction> myList = new ArrayList<ScriptFunction>();

            for (ScriptFunction myItem : getFunctions()) {

                myList.add(myItem.clone());
            }

            return myList;

        } else {

            return null;
        }
    }

    private List<ScriptFunction> fullCloneFunctions() {

        if (null != getFunctions()) {

            List<ScriptFunction> myList = new ArrayList<ScriptFunction>();

            for (ScriptFunction myItem : getFunctions()) {

                myList.add(myItem.fullClone());
            }

            return myList;

        } else {

            return null;
        }
    }

    @Override
    public <T extends ModelObject> FieldDef clone(Map<String, T> fieldMapIn){

        FieldDef copyOfThis = (FieldDef) fieldMapIn.get(this.getUuid());
        if ((copyOfThis != null) && (getFieldType() != FieldType.COLUMN_REF)
                && (getFieldType() != FieldType.LINKUP_REF)
                && (getFieldType() != FieldType.SCRIPTED)
                && (getFieldType() != FieldType.DERIVED)) {
            return copyOfThis;
        }
        FieldDef copy = new FieldDef();
        super.copyComponents(copy);
        copy.columnLocalId = this.columnLocalId;
        copy.displayFormat = this.displayFormat;
        copy.dsLocalId = this.dsLocalId;
        copy.fieldName = this.fieldName;
        copy.fieldType = this.fieldType;
        copy.setFinalSort(this.getFinalSort());

        if (this.functions != null) {
            copy.functions = Lists.newArrayList();

            for (ScriptFunction scriptFunction : this.functions) {
                ScriptFunction sfCopy = scriptFunction.clone(fieldMapIn);
                fieldMapIn.put(scriptFunction.getUuid(), (T) sfCopy);
                copy.functions.add(sfCopy);
            }
        }
        copy.functionType = this.functionType;
        copy.ordinal = this.ordinal;
        copy.scriptSeparator = this.scriptSeparator;
        copy.scriptText = this.scriptText;
        copy.scriptType = this.scriptType;
        copy.staticText = this.staticText;
        copy.tableLocalId = this.tableLocalId;
        copy.valueType = this.valueType;
        copy.storageType = this.storageType;
        copy.sqlExpression = this.sqlExpression;
        return copy;
    }

    public void cloneValues(FieldDef cloneIn) {

        cloneIn.setFieldType(getFieldType());
        cloneIn.setFieldName(getFieldName());
        cloneIn.setLocalId(getLocalId());
        cloneIn.setDsLocalId(getDsLocalId());
        cloneIn.setTableLocalId(getTableLocalId());
        cloneIn.setColumnLocalId(getColumnLocalId());
        cloneIn.setOrdinal(getOrdinal());
        cloneIn.setFunctionType(getFunctionType());
        cloneIn.setScriptType(getScriptType());
        cloneIn.setScriptText(getScriptText());
        cloneIn.setScriptSeparator(getScriptSeparator());
        cloneIn.setStaticText(getStaticText());
        cloneIn.setValueType(getValueType());
        cloneIn.setStorageType(getStorageType());
//        cloneIn.setDisplayFormat(getDisplayFormat());
        cloneIn.setPreCalculated(isPreCalculated());
        cloneIn.setDirty(isDirty());
        cloneIn.setFinalSort(getFinalSort());
    }

    private SqlTokenTreeItemList fullCloneExpression() {

        return (null != sqlExpression) ? sqlExpression.fullClone() : null;
    }

    private SqlTokenTreeItemList cloneExpression() {

        return (null != sqlExpression) ? sqlExpression.clone() : null;
    }

    @Override
    public String getName() {
        return getFieldName();
    }

    public String escapeStaticText() {

        return StringUtil.escapeStaticSqlText(staticText);
    }

    public boolean notListedFieldDef() {

        return (null == fieldName) || (0 == fieldName.trim().length());
    }

    public int getLevel() {

        return _level;
    }

    public void setLevel(int levelIn) {

        _level = levelIn;
    }

    public void invertLevel(int limitIn) {

        _level = limitIn - _level;
    }
}