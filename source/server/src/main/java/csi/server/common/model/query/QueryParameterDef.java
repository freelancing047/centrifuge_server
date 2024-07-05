package csi.server.common.model.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.ParameterType;
import csi.server.common.interfaces.ParameterListAccess;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.InPlaceUpdate;
import csi.server.common.model.ModelObject;
import csi.server.common.model.dataview.DataViewDef;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class QueryParameterDef extends ModelObject implements InPlaceUpdate<QueryParameterDef> {

    protected String localId;
    protected String name;
    @Enumerated(value = EnumType.STRING)
    protected CsiDataType type;
    @Enumerated(value = EnumType.STRING)
    protected ParameterType useType;
    protected String description;
    protected String prompt;
    protected boolean neverPrompt;
    protected boolean alwaysPrompt;
    protected boolean alwaysFill;
    protected Boolean trimValues = true;
    protected boolean systemParam;
    protected boolean requiredParam;
    protected Integer sourceCount = 0;
    protected Integer fieldCount = 0;
    boolean listParameter = false;
    @Column(length = 2147483647)
    @Lob
    ArrayList<String> values;
    @Column(length = 2147483647)
    @Lob
    ArrayList<String> defaultValues;
    @Transient
    ParameterListAccess listAccess = null;
    @ManyToOne
    private DataViewDef parent;
    private int ordinal;

    public QueryParameterDef() {
        super();
        setLocalId(CsiUUID.randomUUID());
    }

    // Exclusively for setting Transient parameters such as @USER
    public QueryParameterDef(String localIdIn, String nameIn, CsiDataType typeIn, String valueIn, String descriptionIn) {
        super();
        setLocalId(localIdIn);
        setName(nameIn);
        setType(typeIn);
        setSingleValue(valueIn);
        setDefaultValue(valueIn);
        setSystemParam(true);
        setDescription(descriptionIn);
    }

    public void setListAccess(ParameterListAccess listAccessIn) {

        listAccess = listAccessIn;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinalIn) {
        ordinal = ordinalIn;
    }

    public DataViewDef getParent() {
        return parent;
    }

    public void setParent(DataViewDef parentIn) {
        parent = parentIn;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public boolean isSystemParam() {
        return systemParam;
    }

    public void setSystemParam(boolean systemParamIn) {
        this.systemParam = systemParamIn;
    }

    public boolean isRequiredParam() {
        return requiredParam;
    }

    public void setRequiredParam(boolean requiredParamIn) {
        this.requiredParam = requiredParamIn;
    }

    public void lockInUse() {

        if (null == sourceCount) {

            sourceCount = 0;
        }
        if (null == fieldCount) {

            fieldCount = 0;
        }
        requiredParam = 0 < (sourceCount + fieldCount);
    }

    public void clearCounts() {

        sourceCount = 0;
        fieldCount = 0;
        requiredParam = false;
    }

    public void clearFieldCount() {

        fieldCount = 0;
        requiredParam = false;
    }

    public void clearSourceCount() {

        sourceCount = 0;
        requiredParam = false;
    }

    public boolean isInUse() {

        return requiredParam || neededByField() || neededBySource();
    }

    public Integer getSourceCount() {
        return sourceCount;
    }

    public void setSourceCount(Integer sourceCountIn) {
        sourceCount = sourceCountIn;
    }

    public boolean addSourceItem() {

        if ((null != sourceCount) || initializeListAccess()) {

            sourceCount = (null != sourceCount) ? sourceCount + 1 : 1;
            return true;
        }
        return false;
    }

    public boolean removeSourceItem() {

        if ((null != sourceCount) || initializeListAccess()) {

            if ((null != sourceCount) && (0 < sourceCount)) {

                sourceCount--;

            } else {

                sourceCount = 0;
            }
            return true;
        }
        return false;
    }

    public Integer getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(Integer fieldCountIn) {
        fieldCount = fieldCountIn;
    }

    public boolean addFieldItem() {

        if ((null != fieldCount) || initializeListAccess()) {

            fieldCount = (null != fieldCount) ? fieldCount + 1 : 1;
            return true;
        }
        return false;
    }

    public boolean removeFieldItem() {

        if ((null != fieldCount) || initializeListAccess()) {

            if ((null != fieldCount) && (0 < fieldCount)) {

                fieldCount--;

            } else {

                fieldCount = 0;
            }
            return true;
        }
        return false;
    }

    public boolean getNeverPrompt() {
        return neverPrompt;
    }

    public void setNeverPrompt(boolean neverPromptIn) {
        this.neverPrompt = neverPromptIn;
    }

    public boolean needsPrompt() {

        return ((null == getValues()) || getValues().isEmpty())
                && ((null == getValue()) || (0 == getValue().length()))
                && ((null == getDefaultValues()) || getDefaultValues().isEmpty())
                && ((null == getDefaultValue()) || (0 == getDefaultValue().length()));
    }

    private boolean initializeListAccess() {

        if (null == listAccess) {

            listAccess = parent;
        }
        if (null != listAccess) {

            return (null != listAccess.initializeParameterUse());
        }
        return false;
    }

    public boolean neededBySource() {

        if ((null == sourceCount) && (!initializeListAccess())) {

            return true;
        }
        return (null != sourceCount) && (0 < sourceCount);
    }

    public boolean neededByField() {

        if ((null == fieldCount) && (!initializeListAccess())) {

            return true;
        }
        return (null != fieldCount) && (0 < fieldCount);
    }

    public boolean getAlwaysPrompt() {
        return alwaysPrompt;
    }

    public void setAlwaysPrompt(boolean alwaysPromptIn) {
        this.alwaysPrompt = alwaysPromptIn;
    }

    public boolean getAlwaysFill() {
        return alwaysFill;
    }

    public void setAlwaysFill(boolean alwaysFillIn) {
        alwaysFill = alwaysFillIn;
    }

   public Boolean getTrimValues() {
      return Boolean.valueOf((trimValues == null) || trimValues);
   }

    public void setTrimValues(Boolean trimValuesIn) {
        trimValues = trimValuesIn;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public ParameterType getUseType() {
        return this.useType;
    }

    public void setUseType(ParameterType value) {
        this.useType = value;
    }

    public CsiDataType getType() {
        return this.type;
    }

    public void setType(CsiDataType value) {
        this.type = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public String getValue(int indexIn) {
        if ((values != null) && (values.size() > indexIn)) {
            return values.get(indexIn);
        }
        return null;
    }

    public String getValue() {
        return getValue(0);
    }

    public void setSingleValue(String value) {
        if (values == null) {
            values = new ArrayList<String>();
        }

        values.clear();
        values.add(value);
    }

   public String getDefaultValue() {
      String result = null;

      if ((defaultValues != null) && !defaultValues.isEmpty()) {
         result = defaultValues.get(0);
      }
      return result;
   }

    public void setDefaultValue(String value) {
        if (defaultValues == null) {
            defaultValues = new ArrayList<String>();
        }

        defaultValues.clear();
        defaultValues.add(value);
    }

    public boolean getListParameter() {
        return listParameter;
    }

    public void setListParameter(boolean flag) {
        this.listParameter = flag;
    }

    public ArrayList<String> getValues() {
        if (values == null) {
            values = new ArrayList<String>();
        }
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    public ArrayList<String> getDefaultValues() {
        if (defaultValues == null) {
            defaultValues = new ArrayList<String>();
        }
        return defaultValues;
    }

    public void setDefaultValues(ArrayList<String> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public void addValue(String valueIn) {

        if (values == null) {
            values = new ArrayList<String>();
        }
        values.add(valueIn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(localId);
    }

    @Override
    public boolean equals(Object obj) {
       return (this == obj) ||
              ((obj != null) &&
               (obj instanceof QueryParameterDef) &&
               getLocalId().equals(((QueryParameterDef) obj).getLocalId()));
    }

    @Override
    public QueryParameterDef clone() {

        QueryParameterDef myClone = new QueryParameterDef();

        super.cloneComponents(myClone);

        return cloneValues(myClone);
    }

    @Override
    public QueryParameterDef fullClone() {

        QueryParameterDef myClone = new QueryParameterDef();

        super.fullCloneComponents(myClone);

        return cloneValues(myClone);
    }

    public void updateInPlace(QueryParameterDef cloneIn) {

        cloneIn.cloneValues(this);
    }

    public QueryParameterDef cloneValues(QueryParameterDef cloneIn) {

        cloneIn.setLocalId(getLocalId());
        cloneIn.setName(getName());
        cloneIn.setType(getType());
        cloneIn.setDescription(getDescription());
        cloneIn.setListParameter(getListParameter());
        cloneIn.setPrompt(getPrompt());
        cloneIn.setNeverPrompt(getNeverPrompt());
        cloneIn.setAlwaysPrompt(getAlwaysPrompt());
        cloneIn.setAlwaysFill(getAlwaysFill());
        cloneIn.setSystemParam(isSystemParam());
        cloneIn.setRequiredParam(isRequiredParam());
        cloneIn.setFieldCount(getFieldCount());
        cloneIn.setSourceCount(getSourceCount());
        cloneIn.setValues(cloneStringList(getValues()));
        cloneIn.setDefaultValues(cloneStringList(getDefaultValues()));
        cloneIn.setTrimValues(getTrimValues());

        return cloneIn;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, localId, indentIn, "localId");
        debugObject(bufferIn, name, indentIn, "name");
        debugObject(bufferIn, type, indentIn, "type");
        debugObject(bufferIn, description, indentIn, "description");
        debugObject(bufferIn, listParameter, indentIn, "listParameter");
        debugObject(bufferIn, prompt, indentIn, "prompt");
        debugObject(bufferIn, neverPrompt, indentIn, "neverPrompt");
        debugObject(bufferIn, alwaysPrompt, indentIn, "alwaysPrompt");
        debugObject(bufferIn, alwaysFill, indentIn, "alwaysFill");
        debugObject(bufferIn, systemParam, indentIn, "transientParam");
        debugObject(bufferIn, requiredParam, indentIn, "requiredParam");
        debugObject(bufferIn, fieldCount, indentIn, "fieldCount");
        debugObject(bufferIn, sourceCount, indentIn, "sourceCount");
        debugObject(bufferIn, values, indentIn, "values");
        debugObject(bufferIn, defaultValues, indentIn, "defaultValues");
    }

    private ArrayList<String> cloneStringList(List<String> listIn) {

        if (null != listIn) {

            ArrayList<String> myList = new ArrayList<String>();

            for (String myItem : listIn) {

                myList.add(myItem);
            }

            return myList;

        } else {

            return null;
        }
    }
}
