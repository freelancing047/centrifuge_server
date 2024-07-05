package csi.shared.core.visualization.timeline;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.shared.core.util.IntCollection;

public abstract class BaseTimelineEvent implements Serializable, IsSerializable, Comparable<BaseTimelineEvent>{
	
	

    protected int eventDefinitionId;
    protected Long startTime;
    protected Long endTime;
    protected String trackValue = TimelineTrack.NULL_TRACK;
    private Double dotSize;

    protected String colorValue;//TimelineTrack.NULL_TRACK;

    protected boolean selected = false;
    protected boolean visible = true;

	
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

    public void setColorValue(String value) {
        this.colorValue=value;
    }
    
    public String getColorValue(){
        return this.colorValue;
    }
	


	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}


	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getTrackValue() {
		return trackValue;
	}

	public void setTrackValue(String trackValue) {
		this.trackValue = trackValue;
	}

    public int getEventDefinitionId() {
        return eventDefinitionId;
    }

    public void setEventDefinitionId(int eventDefinitionId) {
        this.eventDefinitionId = eventDefinitionId;
    }
    
    @Override
    public int compareTo(BaseTimelineEvent o1) {
        if (o1.getStartTime() == null) {
            return -1;
        }
        if (getStartTime() == null) {
            return 1;
        }
        if (o1.getStartTime().longValue() == getStartTime().longValue()) {
            try {
                                
                if (o1.getEventDefinitionId() >= getEventDefinitionId()) {
                    return 1;
                } else {
                    return -1;
                }
            } catch (Exception exception) {
                //no-op
            }
        }
        return o1.getStartTime().longValue() < getStartTime().longValue() ? 1 : -1;
    }
    
    public abstract void combine(BaseTimelineEvent event);

    public Double getDotSize() {
        return dotSize;
    }

    public void setDotSize(Double dotSize) {
        this.dotSize = dotSize;
    }    
    

}
