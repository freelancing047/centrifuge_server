package csi.server.common.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.dto.CsiMap;
import csi.server.common.dto.FieldListAccess;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class OrderedField extends ModelObject implements InPlaceUpdate<OrderedField> {

    protected int ordinal;

    protected String fieldDefId;

    public OrderedField() {
        super();
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getFieldDefId() {
        return fieldDefId;
    }

    public void setFieldDefId(String fieldDefIdIn) {
        this.fieldDefId = fieldDefIdIn;
    }

    public void setFieldDef(FieldDef fieldDefIn) {

        fieldDefId = (null != fieldDefIn) ? fieldDefIn.getLocalId() : null;
    }

    public FieldDef getFieldDef(FieldListAccess modelIn) {

        return (null != fieldDefId) ? modelIn.getFieldDefByLocalId(fieldDefId) : null;
    }

    public OrderedField copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = new HashMap<String, Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (OrderedField) copyOfThis;
            }
        }
        OrderedField copy = new OrderedField();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.ordinal = this.ordinal;
        copy.fieldDefId = this.fieldDefId;
        copy.uuid = this.uuid;
        return copy;
    }

    @Override
    public <T extends ModelObject> OrderedField clone(Map<String, T> copies) {
        if (copies == null) {
            copies = new HashMap<String, T>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (OrderedField) copyOfThis;
            }
        }
        OrderedField copy = new OrderedField();
        copies.put(getUuid(), (T) copy);
        copy.clientProperties = new CsiMap<String, String>();
        super.copyComponents(copy);

        copy.ordinal = this.ordinal;
        copy.fieldDefId = this.fieldDefId;
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public OrderedField clone() {

        OrderedField myClone = new OrderedField();

        cloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public OrderedField fullClone() {

        OrderedField myClone = new OrderedField();

        fullCloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cloneComponents(ModelObject cloneIn) {

        super.cloneComponents(cloneIn);

        cloneContents((OrderedField)cloneIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fullCloneComponents(ModelObject cloneIn) {

        super.fullCloneComponents(cloneIn);

        cloneContents((OrderedField)cloneIn);
    }

    private void cloneContents(OrderedField cloneIn) {

        cloneIn.setOrdinal(getOrdinal());
        cloneIn.setFieldDefId(getFieldDefId());
    }

    @Override
    public void updateInPlace(OrderedField sourceIn) {

        setOrdinal(sourceIn.getOrdinal());
        setFieldDefId(sourceIn.getFieldDefId());
    }
}
