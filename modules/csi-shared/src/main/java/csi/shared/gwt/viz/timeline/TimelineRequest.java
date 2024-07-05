package csi.shared.gwt.viz.timeline;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import csi.shared.core.util.IntCollection;

public class TimelineRequest implements Serializable{
    
    private String vizUuid;
    private String dvUuid;
    private Long startTime;
    private Long endTime;
    private int startGroupIndex = 0;
    private int groupLimit = 20;
    private String trackName;
    private boolean calculateOverview = true;
    private int vizWidth = 200;
    private TimeUnit summaryLevel = null;
    private IntCollection eventIdSelection;
    private Set<String> filteredKeys = new HashSet<String>();
    
    public Long getStartTime() {
        return startTime;
    }
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    public Long getEndTime() {
        return endTime;
    }
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
    public String getVizUuid() {
        return vizUuid;
    }
    public void setVizUuid(String vizUuid) {
        this.vizUuid = vizUuid;
    }
    public String getDvUuid() {
        return dvUuid;
    }
    public void setDvUuid(String dvUuid) {
        this.dvUuid = dvUuid;
    }
    public int getStartGroupIndex() {
        return startGroupIndex;
    }
    public void setStartGroupIndex(int startGroupIndex) {
        this.startGroupIndex = startGroupIndex;
    }
    public int getGroupLimit() {
        return groupLimit;
    }
    public void setGroupLimit(int groupLimit) {
        this.groupLimit = groupLimit;
    }
    public String getTrackName() {
        return trackName;
    }
    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
    public int getVizWidth() {
        return vizWidth;
    }
    public void setVizWidth(int vizWidth) {
        this.vizWidth = vizWidth;
    }
    public boolean isCalculateOverview() {
        return calculateOverview;
    }
    public void setCalculateOverview(boolean calculateOverview) {
        this.calculateOverview = calculateOverview;
    }
    public TimeUnit getSummaryLevel() {
        return summaryLevel;
    }
    public void setSummaryLevel(TimeUnit summaryLevel) {
        this.summaryLevel = summaryLevel;
    }
    public IntCollection getEventIdSelection() {
        return eventIdSelection;
    }
    public void setEventIdSelection(IntCollection eventIdSelection) {
        this.eventIdSelection = eventIdSelection;
    }
    public Set<String> getFilteredKeys() {
        return filteredKeys;
    }
    public void setFilteredKeys(Set<String> filteredKeys) {
        this.filteredKeys = filteredKeys;
    }

}
