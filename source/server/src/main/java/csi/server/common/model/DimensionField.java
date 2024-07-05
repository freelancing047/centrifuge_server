package csi.server.common.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DimensionField extends ModelObject {

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef fieldDef;

    protected Integer ordinal;

    public DimensionField() {
    	super();
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer value) {
        this.ordinal = value;
    }
}
