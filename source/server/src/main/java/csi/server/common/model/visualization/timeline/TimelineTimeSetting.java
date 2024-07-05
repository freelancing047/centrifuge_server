package csi.server.common.model.visualization.timeline;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.graph.TimePlayerUnit;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TimelineTimeSetting extends ModelObject implements Serializable{

	//Date or a numerical duration only
	@ManyToOne
	private FieldDef fieldDef;
	
	@Enumerated(EnumType.STRING)
	private TimeType type = TimeType.DATE; //Default to date for now

    @Enumerated(EnumType.STRING)
    public TimePlayerUnit durationUnit = TimePlayerUnit.MILLISECOND;

	public FieldDef getFieldDef() {
		return fieldDef;
	}


	public void setFieldDef(FieldDef fieldDef) {
		this.fieldDef = fieldDef;
	}



	public TimePlayerUnit getDurationUnit() {
		return durationUnit;
	}


	public void setDurationUnit(TimePlayerUnit durationUnit) {
		this.durationUnit = durationUnit;
	}


    public TimeType getType() {
        return type;
    }


    public void setType(TimeType type) {
        this.type = type;
    }


    public TimelineTimeSetting copy() {
        TimelineTimeSetting copy = new TimelineTimeSetting();
        copy.setFieldDef(getFieldDef());
        copy.setType(getType());
        copy.setDurationUnit(getDurationUnit());
        return copy;
    }
	
}
