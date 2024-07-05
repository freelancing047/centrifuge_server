package csi.shared.core.visualization.timeline;

import java.util.ArrayList;
import java.util.List;

public class MeasuredTimelineResult extends TimelineResult{
    
    private List<MeasuredTrack> measuredTracks = new ArrayList<MeasuredTrack>();
    
    private int startGroup;

    private int groupCount;

    public List<MeasuredTrack> getMeasuredTracks() {
        return measuredTracks;
    }
    public void setMeasuredTracks(List<MeasuredTrack> measuredTracks) {
        this.measuredTracks = measuredTracks;
    }
    public int getStartGroup() {
        return startGroup;
    }
    public void setStartGroup(int startGroup) {
        this.startGroup = startGroup;
    }
    public void setGroupCount(int size) {
        this.groupCount = size;
    }
    public int getGroupCount() {
        return groupCount;
    }

}
