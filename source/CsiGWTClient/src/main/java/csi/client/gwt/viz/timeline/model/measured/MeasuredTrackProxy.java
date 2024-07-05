package csi.client.gwt.viz.timeline.model.measured;

import csi.client.gwt.viz.timeline.model.Axis;
import csi.shared.core.visualization.timeline.MeasuredTrack;
import csi.shared.gwt.viz.timeline.TimeUnit;

public class MeasuredTrackProxy implements Comparable<MeasuredTrackProxy>{

	private int x;
	private MeasuredTrack track;
    private int color;
    private Axis axis;
    private boolean banding = false;
    
	public MeasuredTrackProxy(MeasuredTrack track) {
		setTrack(track);
	}
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getIndex() {
		return track.getIndex();
	}
	
	
	
    public int getColor() {
        return color;
    }


    public void setColor(int color) {
        this.color = color;
    }


    public int compareTo(MeasuredTrackProxy measuredTrack){

        if (this==measuredTrack)
            return 0;
        if (measuredTrack==null || measuredTrack.getTrack() == null)
            return 1;
        if (this.getTrack() == null){
            return -1;
        }
        
        
        return measuredTrack.getTrack().compareTo(this.getTrack());
    }


    public MeasuredTrack getTrack() {
        return track;
    }


    public void setTrack(MeasuredTrack track) {
        this.track = track;
    }

    public boolean isVisible() {
        return this.track.isVisible();
    }

    public String getLabel() {
        return track.getName();
    }


    public TimeUnit getTimeUnit(){
        return track.getTimeUnit();
    }

    public Long getStartTime() {
        // TODO Auto-generated method stub
        return track.getStartTime();
    }

    public String getNameOverride(){
	    return track.getNameOverride();
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }

    public Axis getAxis() {
        return axis;
    }

    public boolean isBanding() {
        return banding;
    }

    public void setBanding(boolean banding) {
        this.banding = banding;
    }




}
