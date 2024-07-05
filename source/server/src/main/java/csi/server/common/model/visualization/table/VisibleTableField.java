package csi.server.common.model.visualization.table;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class VisibleTableField extends ModelObject {

    private String fieldId;

    private int listPosition;

    public VisibleTableField() {
    	super();
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public void setFieldDef(FieldDef fieldDefIn) {

        fieldId = (null != fieldDefIn) ? fieldDefIn.getLocalId() : null;
    }

    public FieldDef getFieldDef(DataModelDef modelIn) {

        return (null != modelIn) ? modelIn.getFieldListAccess().getFieldDefByLocalId(fieldId) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public VisibleTableField clone() {
        
        VisibleTableField myClone = new VisibleTableField();
        
        super.cloneComponents(myClone);

        myClone.setFieldId(getFieldId());
        myClone.setListPosition(getListPosition());
        
        return myClone;
    }
    
    @SuppressWarnings("unchecked")
    public VisibleTableField copy() {
        
        VisibleTableField myCopy = new VisibleTableField();
        
        super.copyComponents(myCopy);

        myCopy.setFieldId(getFieldId());
        myCopy.setListPosition(getListPosition());
        
        return myCopy;
    }
}