package csi.client.gwt.viz.timeline.model;

import csi.shared.core.visualization.timeline.SingularTimelineEvent;

public class DetailedEventProxy extends AbstractEventProxy{

	private SingularTimelineEvent event;
	private int x;
	private int y;
	private int right;
	private int endX = -1;
	private TimelineTrackModel track;
    private int color;
    private boolean searchHit = false;
    private boolean searchHighlight = false;
    private double drawableSize;
    private Long rightStartTime ;
	
	public DetailedEventProxy(SingularTimelineEvent event) {
		setEvent(event);
	}
	
	
	public SingularTimelineEvent getEvent() {
		return event;
	}
	public void setEvent(SingularTimelineEvent event) {
		this.event = event;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public double getDotSize() {
		return getEvent().getDotSize();
	}
	public void setDotSize(double dotSize) {
		getEvent().setDotSize(dotSize);
	}
	
	public Long getStartTime(){
		return this.event.getStartTime();
	}
	public boolean isSelected() {
		return event.isSelected();
	}

	public void setSelected(boolean selected) {
		event.setSelected(selected);
	}
	public boolean isVisible(){
		return this.event.isVisible();
	}
	public void setSpaceToRight(int space) {
		this.right = space;
	}
	public int getSpaceToRight(){
		return this.right;
	}
	public Long getEndTime() {
		return this.event.getEndTime();
	}
	public void setEndX(int num) {
		endX = num;
	}
	public int getEndX() {
        if(endX == -1){
            return getX();
        }
        return endX;
    }
	
    public int getColor() {
        return color;
    }


    public void setColor(int color) {
        this.color = color;
    }


    public boolean isSearchHit() {
        return searchHit;
    }


    public void setSearchHit(boolean searchHit) {
        this.searchHit = searchHit;
    }


    public boolean isSearchHighlight() {
        return searchHighlight;
    }


    public void setSearchHighlight(boolean searchHighlight) {
        this.searchHighlight = searchHighlight;
    }


    public double getDrawableSize() {
        return drawableSize;
    }


    public void setDrawableSize(double drawableSize) {
        this.drawableSize = drawableSize;
    }


    public void setRight(Long startTime) {
        this.rightStartTime = startTime;
    }
    
    public Long getRight(){
        return this.rightStartTime;
    }


    public TimelineTrackModel getTrack() {
        return track;
    }


    public void setTrack(TimelineTrackModel track) {
        this.track = track;
    }


    @Override
    public String getColorValue() {
        return event.getColorValue();
    }


    @Override
    public String getLabel() {
        return event.getLabel();
    }

}
