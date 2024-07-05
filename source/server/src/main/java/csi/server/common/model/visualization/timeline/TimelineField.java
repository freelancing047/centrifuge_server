package csi.server.common.model.visualization.timeline;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;


@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TimelineField extends ModelObject implements Serializable{


    @ManyToOne
    private FieldDef fieldDef;
	
    
    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public TimelineField copy(){
        
        TimelineField myCopy = new TimelineField();
        myCopy.setFieldDef(getFieldDef());
        return myCopy;
    }
}
