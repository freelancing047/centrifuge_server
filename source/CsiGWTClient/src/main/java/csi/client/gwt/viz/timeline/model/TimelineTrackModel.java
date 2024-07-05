package csi.client.gwt.viz.timeline.model;

import java.util.ArrayList;
import java.util.List;

import csi.shared.core.visualization.timeline.CommonTrack;


public class TimelineTrackModel implements Comparable<TimelineTrackModel>, CommonTrack{
	private double trackHeight;
	private double trackTop;
	private double collapsedHeight;
	private List<DetailedEventProxy> events;
	private int startY;
	private int endY;
	private String label;
	private boolean shadow = false;
	private boolean highlight = false;

	private boolean visible = true;
    private boolean collapsed;
    private boolean summary;
    private boolean groupSpace;
    private boolean allowCollapse = true;
    

	public static final int CELL_HEIGHT = 18;
	
	public TimelineTrackModel(){
	}

	public void setTop(double topRatio) {
		this.trackTop = topRatio;
	}

	public void setHeight(double heightRatio) {
		this.trackHeight = heightRatio;
	}

	public List<DetailedEventProxy> getEvents() {
        if(events == null){
            events = new ArrayList<DetailedEventProxy>();
        }
		return events;
	}

	public void setEvents(List<DetailedEventProxy> events) {
		this.events = events;
	}

	public int getStartY() {
		return startY;
	}

	public void setStartY(int startY) {
		this.startY = startY;
	}

	public int getEndY() {
		return endY;
	}

	public void setEndY(int endY) {
		this.endY = endY;
	}

	public double getHeight() {
		return trackHeight;
	}

	public double getTop() {
		return trackTop;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void addEvent(DetailedEventProxy eventProxy) {
		if(events == null){
			events = new ArrayList<DetailedEventProxy>();
		}
		

		events.add(eventProxy);
	}

	public double size() {
		return getEvents().size();
	}

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    @Override
    public int compareTo(TimelineTrackModel track) {
        if(label == null){
            return -1;
        }
        if(track.getLabel() == null){
            return label.compareTo("");
        }
        return label.compareTo(track.getLabel());
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public boolean hasSummary() {
        return summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    public double getCollapsedHeight() {
        return collapsedHeight;
    }

    public void setCollapsedHeight(double collapsedHeightRatio) {
        this.collapsedHeight = collapsedHeightRatio;
    }

    public void setHighlight(boolean b) {
        highlight = b;
    }
    
    public boolean isHighlight(){
        return highlight;
    }

    public boolean isGroupSpace() {
        return groupSpace;
    }

    public void setGroupSpace(boolean groupSpace) {
        this.groupSpace = groupSpace;
    }

    public boolean isAllowCollapse() {
        return allowCollapse;
    }

    public void setAllowCollapse(boolean allowCollapse) {
        this.allowCollapse = allowCollapse;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
