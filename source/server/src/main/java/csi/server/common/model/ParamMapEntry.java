package csi.server.common.model;

import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.query.QueryParameterDef;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ParamMapEntry extends ModelObject {

    protected String paramId;
    protected String paramName;
    protected String fieldLocalId;
    protected String targetFieldLocalId;

    protected int paramOrdinal;

    protected String value;

    public ParamMapEntry() {
    	super();
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramIdIn) {
        this.paramId = paramIdIn;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getFieldLocalId() {
        return fieldLocalId;
    }

    public void setFieldLocalId(String fieldLocalIdIn) {
        this.fieldLocalId = fieldLocalIdIn;
    }

    public String getTargetFieldLocalId() {
        return targetFieldLocalId;
    }

    public void setTargetFieldLocalId(String targetFieldLocalIdIn) {
        this.targetFieldLocalId = targetFieldLocalIdIn;
    }

    public int getParamOrdinal() {
        return paramOrdinal;
    }

    public void setParamOrdinal(int paramOrdinal) {
        this.paramOrdinal = paramOrdinal;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	public boolean isValid() {
	    
	    return (((((null != targetFieldLocalId) &&(0 < targetFieldLocalId.length()))
                || ((null != paramName) && (0 < paramName.length())))
	            && (((null != fieldLocalId) &&(0 < fieldLocalId.length()))
                || ((null != value) && (0 < value.length())))) &&
	            (((null == targetFieldLocalId) || (0 == targetFieldLocalId.length())
                        || (null == paramName) || (0 == paramName.length()))
	            && ((null == fieldLocalId) || (0 == fieldLocalId.length())
                        || (null == value) || (0 == value.length()))));
	}

    public ParamMapEntry newCopy() {
     
        ParamMapEntry myCopy = new ParamMapEntry();

        myCopy.setParamId(paramId);
        myCopy.setParamName(paramName);
        myCopy.setParamOrdinal(paramOrdinal);
        myCopy.setValue(value);
        myCopy.setFieldLocalId(fieldLocalId);
        myCopy.setTargetFieldLocalId(targetFieldLocalId);
        
        return myCopy;
    }
    public QueryParameterDef genParameter() throws CentrifugeException {
        return genParameter(UUID.randomUUID());
    }
    public QueryParameterDef genParameter(String idIn) throws CentrifugeException {
        
        QueryParameterDef myParameter = null;
        String myParamName = this.getParamName();
        if (myParamName != null) {

            myParameter = new QueryParameterDef();
            myParameter.setName(myParamName);
            myParameter.setType(null);
            myParameter.setLocalId(idIn);

        } else {
            throw new CentrifugeException("Linkup mapping is missing the parameter name.");
        }
        return myParameter;
	}

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModelObject> ParamMapEntry clone(Map<String, T> fieldMapIn) {
        
        ParamMapEntry myClone = new ParamMapEntry();
        
        super.cloneComponents(myClone);

        myClone.setParamName(getParamName());
        myClone.setParamOrdinal(getParamOrdinal());
        myClone.setValue(getValue());
        myClone.setFieldLocalId(getFieldLocalId());
        myClone.setTargetFieldLocalId(getTargetFieldLocalId());

        return myClone;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, paramName, indentIn, "paramName");
        debugObject(bufferIn, paramOrdinal, indentIn, "paramOrdinal");
        debugObject(bufferIn, value, indentIn, "value");
        debugObject(bufferIn, fieldLocalId, indentIn, "fieldDef");
        debugObject(bufferIn, targetFieldLocalId, indentIn, "targetFieldDef");
    }
}