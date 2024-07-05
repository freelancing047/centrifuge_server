package csi.server.common.model.visualization.timeline;

import java.io.Serializable;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TimelineLegendDefinition extends ModelObject implements Serializable {

    private String value;

    private Integer color;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }
    
    public TimelineLegendDefinition copy(){
        TimelineLegendDefinition myCopy = new TimelineLegendDefinition();
        
        myCopy.setValue(getValue());
        myCopy.setColor(getColor());
        
        return myCopy;
    }
    
}
