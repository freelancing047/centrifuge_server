package csi.server.common.model.functions;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.annotations.GwtIncompatible;

import csi.server.common.dto.CsiMap;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.ModelObject;
import csi.server.common.model.FieldDef;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SubstringFunction extends ScriptFunction {

    private static final String SUBSTR_TEMPLATE = "var csiResult = _LIB.substringOneBased(csiRow.getString('%1$s'), %2$d, %3$d);";
    /*
     * A user must specify at least one of the indices (though concievably
     * having neither supplied could just return the entire value). If
     * startIndex == -1, it's assumed to be 0. If endIndex == -1, it's assumed
     * to be the end of the String.
     */
    protected String fieldId;
    protected int startIndex = -1;
    protected int endIndex = -1;

    public SubstringFunction() {
        super();
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldIdIn) {
        this.fieldId = fieldIdIn;
    }

    public FieldDef getField(FieldListAccess modelIn) {
        return (null != fieldId) ? modelIn.getFieldDefByLocalId(fieldId) : null;
    }

    public void setField(FieldDef fieldIn) {
        this.fieldId = (null != fieldIn) ? fieldIn.getLocalId() : null;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    @GwtIncompatible("String.format")
    public String generateScript(FieldListAccess modelIn) {
        return String.format(SUBSTR_TEMPLATE, getField(modelIn).getFieldName(), getStartIndex(), getEndIndex());
    }

    @Override
    public SubstringFunction copy(Map<String,Object> copies) {
        if (copies == null) {
            copies = new HashMap<String,Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (SubstringFunction) copyOfThis;
            }
        }
        SubstringFunction copy = new SubstringFunction();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.name = this.name;
        copy.ordinal = this.ordinal;
        copy.uuid = this.uuid;
        copy.endIndex = getEndIndex();
        copy.fieldId = fieldId;
        copy.startIndex = getStartIndex();
        return copy;
    }


    @Override
    public <T extends ModelObject> SubstringFunction clone(Map<String,T> fieldMapIn){
        if (fieldMapIn == null) {
            fieldMapIn = new HashMap<String,T>();
        }
        {
            Object copyOfThis = fieldMapIn.get(this.getUuid());
            if (copyOfThis != null) {
                return (SubstringFunction) copyOfThis;
            }
        }
        SubstringFunction copy = new SubstringFunction();
        fieldMapIn.put(getUuid(), (T) copy);
        super.copyComponents(copy);
        copy.name = this.name;
        copy.ordinal = this.ordinal;
        copy.endIndex = getEndIndex();
        copy.fieldId = fieldId;
        copy.startIndex = getStartIndex();
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SubstringFunction clone() {

        SubstringFunction myClone = new SubstringFunction();

        cloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SubstringFunction fullClone() {

        SubstringFunction myClone = new SubstringFunction();

        fullCloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cloneComponents(ModelObject cloneIn) {

        super.cloneComponents(cloneIn);

        cloneContents((SubstringFunction)cloneIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fullCloneComponents(ModelObject cloneIn) {

        super.fullCloneComponents(cloneIn);

        cloneContents((SubstringFunction)cloneIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateInPlace(ScriptFunction sourceIn) {

        super.updateInPlace(sourceIn);

        setStartIndex(((SubstringFunction)sourceIn).getStartIndex());
        setEndIndex(((SubstringFunction)sourceIn).getEndIndex());
        setFieldId(((SubstringFunction)sourceIn).getFieldId());
    }

    private void cloneContents(SubstringFunction cloneIn) {

        cloneIn.setStartIndex(getStartIndex());
        cloneIn.setEndIndex(getEndIndex());
        cloneIn.setFieldId(getFieldId());
    }
}
