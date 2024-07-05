package csi.server.common.model.functions;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.annotations.GwtIncompatible;

import csi.server.business.cachedb.script.IDataRow;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.ModelObject;
import csi.server.common.model.DurationUnit;
import csi.server.common.model.FieldDef;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DurationFunction extends ScriptFunction {

    private static final String DURATION_TEMPLATE = "var csiResult = _LIB.duration(%1$s, %2$s, '%3$s');";
    /*
     * At least one of the fields must be specified. If one is null, the time to
     * be used is "now".
     */
    protected String startFieldId;

    protected String endFieldId;

    @Enumerated(EnumType.STRING)
    protected DurationUnit unit;

    public DurationFunction() {
        super();
    }

    public DurationUnit getUnit() {
        return unit;
    }

    public void setUnit(DurationUnit unit) {
        this.unit = unit;
    }

    public String getStartFieldId() {
        return startFieldId;
    }

    public void setStartFieldId(String startFieldIdIn) {
        this.startFieldId = startFieldIdIn;
    }

    public String getEndFieldId() {
        return endFieldId;
    }

    public void setEndFieldId(String endFieldIdIn) {
        this.endFieldId = endFieldIdIn;
    }

    public FieldDef getStartField(FieldListAccess modelIn) {
        return (null != startFieldId) ? modelIn.getFieldDefByLocalId(startFieldId) : null;
    }

    public void setStartField(FieldDef startFieldIn) {
        this.startFieldId = (null != startFieldIn) ? startFieldIn.getLocalId() : null;
    }

    public FieldDef getEndField(FieldListAccess modelIn) {
        return (null != endFieldId) ? modelIn.getFieldDefByLocalId(endFieldId) : null;
    }

    public void setEndField(FieldDef endFieldIn) {
        this.endFieldId = (null != endFieldIn) ? endFieldIn.getLocalId() : null;
    }

    @GwtIncompatible("String.format")
    public String generateScript(FieldListAccess modelIn) {
        return String.format(DURATION_TEMPLATE, getStartField(modelIn) == null ? "'now'" : "csiRow.get('"
                + getStartField(modelIn).getFieldName() + "')", getEndField(modelIn) == null ? "'now'" : "csiRow.get('"
                + getEndField(modelIn).getFieldName() + "')", getUnit());
    }

    public String execute(IDataRow rowSet) {
        return "";
    }

    @Override
    public DurationFunction copy(Map<String,Object> copies) {
        if (copies == null) {
            copies = new HashMap<String,Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (DurationFunction) copyOfThis;
            }
        }
        DurationFunction copy = new DurationFunction();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.name = this.name;
        copy.ordinal = this.ordinal;
        copy.uuid = this.uuid;
        copy.endFieldId = this.endFieldId;
        copy.startFieldId = this.startFieldId;
        copy.unit = getUnit();
        return copy;
    }


    @Override
    public <T extends ModelObject> DurationFunction clone(Map<String,T> fieldMapIn){
        if (fieldMapIn == null) {
            fieldMapIn = new HashMap<String,T>();
        }
        {
            Object copyOfThis = fieldMapIn.get(this.getUuid());
            if (copyOfThis != null) {
                return (DurationFunction) copyOfThis;
            }
        }
        DurationFunction copy = new DurationFunction();
        fieldMapIn.put(getUuid(), (T) copy);
        super.copyComponents(copy);
        copy.name = this.name;
        copy.ordinal = this.ordinal;
        copy.endFieldId = this.endFieldId;
        copy.startFieldId = this.startFieldId;
        copy.unit = getUnit();
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DurationFunction fullClone() {

        DurationFunction myClone = new DurationFunction();

        fullCloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DurationFunction clone() {

        DurationFunction myClone = new DurationFunction();

        cloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cloneComponents(ModelObject cloneIn) {

        super.cloneComponents(cloneIn);

        cloneContents((DurationFunction)cloneIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fullCloneComponents(ModelObject cloneIn) {

        super.fullCloneComponents(cloneIn);

        cloneContents((DurationFunction)cloneIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateInPlace(ScriptFunction sourceIn) {

        super.updateInPlace(sourceIn);

        setStartFieldId(((DurationFunction)sourceIn).getStartFieldId());
        setEndFieldId(((DurationFunction)sourceIn).getEndFieldId());
        setUnit(((DurationFunction)sourceIn).getUnit());
    }

    private void cloneContents(DurationFunction cloneIn) {

        cloneIn.setStartFieldId(getStartFieldId());
        cloneIn.setEndFieldId(getEndFieldId());
        cloneIn.setUnit(getUnit());
    }
}
