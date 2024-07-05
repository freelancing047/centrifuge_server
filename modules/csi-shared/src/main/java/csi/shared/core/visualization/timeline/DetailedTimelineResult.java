package csi.shared.core.visualization.timeline;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("serial")
public class DetailedTimelineResult extends TimelineResult<SingularTimelineEvent>{

	
	private double min = 0;
	private double max = 0;
	private List<TimelineTrack> tracks = new ArrayList<TimelineTrack>();
	private int eventMax = 0;
	private boolean groupLimit = false;
	private boolean colorLimit = false;
			
    public List<TimelineTrack> getTracks() {
        return tracks;
    }
    public void setTracks(List<TimelineTrack> tracks) {
        this.tracks = tracks;
    }
	
    public void addTrack(TimelineTrack track){
        this.tracks.add(track);
    }
    
    public double getMin() {
        return min;
    }
    public void setMin(double min) {
        this.min = min;
    }
    public double getMax() {
        return max;
    }
    public void setMax(double max) {
        this.max = max;
    }
    public int getEventMax() {
        return eventMax;
    }
    public void setEventMax(int eventMax) {
        this.eventMax = eventMax;
    }
    public boolean isGroupLimit() {
        return groupLimit;
    }
    public void setGroupLimit(boolean groupLimit) {
        this.groupLimit = groupLimit;
    }
    public boolean isColorLimit() {
        return colorLimit;
    }
    public void setColorLimit(boolean colorLimit) {
        this.colorLimit = colorLimit;
    }
	
}
