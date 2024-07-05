package csi.server.common.model.visualization.timeline;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="timelinetrackstate")
public class TimelineTrackState extends ModelObject implements Serializable, Comparable<TimelineTrackState> {

    private String trackName;

    private Boolean collapse = false;
    private Boolean visible = true;

    
    public TimelineTrackState copy(){
        TimelineTrackState myCopy = new TimelineTrackState();

        myCopy.setTrackName(getTrackName());
        myCopy.setCollapse(getCollapse());
        myCopy.setVisible(getVisible());
        
        return myCopy;
    }

    public Boolean getCollapse() {
        return collapse;
    }



    public void setCollapse(Boolean collapse) {
        this.collapse = collapse;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
    

    @Override
    public int compareTo(TimelineTrackState timelineTrackState) {
        return timelineTrackState.getTrackName().compareTo(getTrackName());
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    
}
