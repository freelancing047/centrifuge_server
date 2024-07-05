package csi.server.common.model.visualization.table;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.SortOrder;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TableViewSortField extends ModelObject {

    private String fieldId;

    private int listPosition;

    @Enumerated(value = EnumType.STRING)
    protected SortOrder sortOrder;

    @Transient
    private FieldDef fieldDef = null;

    public TableViewSortField() {
        super();
    }

    public TableViewSortField(FieldDef def) {
       this();
	   setFieldDef(def);
       fieldId = def.getLocalId();
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public void setFieldDef(FieldDef fieldDefIn) {

        fieldDef = fieldDefIn;
        fieldId = (null != fieldDefIn) ? fieldDefIn.getLocalId() : null;
    }

    public FieldDef getFieldDef() {

        return fieldDef;
    }

    public FieldDef getFieldDef(DataModelDef modelIn) {

        return (null != modelIn) ? modelIn.getFieldListAccess().getFieldDefByLocalId(fieldId) : null;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public SortOrder getSortOrder() {
        if (sortOrder == null) {
            return SortOrder.ASC;
        } else {
            return sortOrder;
        }
    }

    public SortOrder getOrder() {
        if (sortOrder == null) {
            return SortOrder.ASC;
        }
        return SortOrder.DESC;
    }

    public void setSortOrder(SortOrder value) {
        this.sortOrder = value;
    }

    public boolean equals(DataModelDef modelIn, Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != TableViewSortField.class) {
            return false;
        } else {
            return getFieldDef(modelIn).getUuid().equals(((TableViewSortField) obj).getUuid());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public TableViewSortField clone() {

        TableViewSortField myClone = new TableViewSortField();

        super.cloneComponents(myClone);

        myClone.setFieldId(getFieldId());
        myClone.setListPosition(getListPosition());

        return myClone;
    }

    @SuppressWarnings("unchecked")
    public TableViewSortField copy() {

        TableViewSortField myCopy = new TableViewSortField();

        super.cloneComponents(myCopy);

        myCopy.setFieldId(getFieldId());
        myCopy.setListPosition(getListPosition());

        return myCopy;
    }
}
