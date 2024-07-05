package csi.server.common.model.visualization.timeline;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="timelineeventdefinition")
public class TimelineEventDefinition extends ModelObject implements Serializable{
    
    private String name;
    

    @OneToOne(cascade = CascadeType.ALL)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private TimelineTimeSetting startField;

    @OneToOne(cascade = CascadeType.ALL)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private TimelineTimeSetting endField;
    
    @ManyToOne
    private FieldDef labelField;

    public TimelineTimeSetting getStartField() {
        return startField;
    }

    public void setStartField(TimelineTimeSetting startField) {
        this.startField = startField;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimelineTimeSetting getEndField() {
        return endField;
    }

    public void setEndField(TimelineTimeSetting endField) {
        this.endField = endField;
    }

    public FieldDef getLabelField() {
        return labelField;
    }

    public void setLabelField(FieldDef labelField) {
        this.labelField = labelField;
    }
    
    public TimelineEventDefinition copy(){
        TimelineEventDefinition myCopy = new TimelineEventDefinition();
        if(getEndField() != null)
            myCopy.setEndField(getEndField().copy());
        if(getStartField() != null)
            myCopy.setStartField(getStartField().copy());
        
        myCopy.setLabelField(getLabelField());
        myCopy.setName(getName());
        
        return myCopy;
    }

}
