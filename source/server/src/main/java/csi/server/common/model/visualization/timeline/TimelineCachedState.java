package csi.server.common.model.visualization.timeline;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import csi.server.common.model.ModelObject;


@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="timelinecachedstate")
public class TimelineCachedState extends ModelObject implements Serializable {

    private Integer scrollPosition = 0;
    private Long startPosition = Long.MIN_VALUE;
    private Long endPosition = Long.MAX_VALUE;
    
    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private Set<TimelineTrackState> trackStates;
    
    @Transient
    @XStreamOmitField
    private String focusedTrack;
    
    public long getEndPosition() {
        return endPosition;
    }


    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }


    public long getStartPosition() {
        return startPosition;
    }


    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }


    public int getScrollPosition() {
        return scrollPosition;
    }


    public void setScrollPosition(int scrollPosition) {
        this.scrollPosition = scrollPosition;
    }


    public Set<TimelineTrackState> getTrackStates() {
        return trackStates;
    }


    public void setTrackStates(Set<TimelineTrackState> trackStates) {
        this.trackStates = trackStates;
    }


    public TimelineCachedState copy() {
        TimelineCachedState myCopy = new TimelineCachedState();
        myCopy.setEndPosition(getEndPosition());
        myCopy.setScrollPosition(getScrollPosition());
        myCopy.setStartPosition(getStartPosition());
        if(trackStates != null){

            HashSet<TimelineTrackState> myStates = new HashSet<TimelineTrackState>();
            for(TimelineTrackState trackState: trackStates){
                myStates.add(trackState.copy());
            }
            myCopy.setTrackStates(myStates);
        }
        return myCopy;
    }


    public String getFocusedTrack() {
        return focusedTrack;
    }


    public void setFocusedTrack(String focusedTrack) {
        this.focusedTrack = focusedTrack;
    }

    
}
