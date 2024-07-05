package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class ZoomEvent extends BaseCsiEvent<ZoomEventHandler> {

    public static final GwtEvent.Type<ZoomEventHandler> type = new GwtEvent.Type<ZoomEventHandler>();
    
    private int epicenter;
    boolean in;
    boolean recenter = false;

    private long magnitude = 1;
    
    public ZoomEvent(int x, boolean in, boolean recenter, long magnitude){
    	super();
    	this.epicenter = x;
    	this.in = in;
    	this.recenter = recenter;
    	this.setMagnitude(magnitude);
    }
    
    public ZoomEvent(int x, boolean in){
        super();
        this.epicenter = x;
        this.in = in;
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ZoomEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(ZoomEventHandler handler) {
		handler.onZoom(this);
	}

	public int getEpicenter() {
		return epicenter;
	}

	public boolean isIn() {
		return in;
	}

	public void setIn(boolean in) {
		this.in = in;
	}

    public boolean isRecenter() {
        return recenter;
    }

    public void setRecenter(boolean recenter) {
        this.recenter = recenter;
    }

    public long getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(long magnitude) {
        this.magnitude = magnitude;
    }

}
