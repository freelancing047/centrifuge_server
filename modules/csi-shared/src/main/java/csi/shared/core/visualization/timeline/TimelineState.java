package csi.shared.core.visualization.timeline;

import java.io.Serializable;

public class TimelineState implements Serializable{

	private String stateImage = null;
	private long timestamp;
	private boolean stale = false;
	
	public String getStateImage() {
		return stateImage;
	}
	public void setStateImage(String stateImage) {
		this.stateImage = stateImage;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public boolean isStale() {
		return stale;
	}
	public void setStale(boolean stale) {
		this.stale = stale;
	}
	
}
