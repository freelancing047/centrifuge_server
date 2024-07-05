package csi.shared.core.visualization.timeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import csi.shared.gwt.viz.timeline.TimeUnit;

public abstract class TimelineResult<T extends BaseTimelineEvent> implements Serializable{

    private boolean limitReached = false;
    private List<T> events = new ArrayList<T>();
    private Long lowerTimeBound = Long.MAX_VALUE;
    private Long upperTimeBound = Long.MIN_VALUE;
    private OverviewTrack overviewData;
    private int totalEvents;
    private TimeUnit summaryLevel = null;
    private String trackName = null;
    
    public boolean isLimitReached() {
        return limitReached;
    }
    public void setLimitReached(boolean limitReached) {
        this.limitReached = limitReached;
    }
    public long getLowerTimeBound() {
        return lowerTimeBound;
    }
    public void setLowerTimeBound(long lowerTimeBound) {
        this.lowerTimeBound = lowerTimeBound;
    }
    public long getUpperTimeBound() {
        return upperTimeBound;
    }
    public void setUpperTimeBound(long upperTimeBound) {
        this.upperTimeBound = upperTimeBound;
    }
    public List<T> getEvents() {
        return events;
    }
    public void setEvents(List<T> events) {
        this.events = events;
    }
    public OverviewTrack getOverviewData() {
        return overviewData;
    }
    public void setOverviewData(OverviewTrack overviewData) {
        this.overviewData = overviewData;
    }
    public TimeUnit getSummaryLevel() {
        return summaryLevel;
    }
    public void setSummaryLevel(TimeUnit summaryLevel) {
        this.summaryLevel = summaryLevel;
    }
    public int getTotalEvents() {
        return totalEvents;
    }
    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }
    public String getTrackName() {
        return trackName;
    }
    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

}
