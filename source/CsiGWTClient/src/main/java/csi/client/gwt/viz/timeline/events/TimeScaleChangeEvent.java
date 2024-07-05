package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class TimeScaleChangeEvent extends BaseCsiEvent<TimeScaleChangeEventHandler> {

    public static final GwtEvent.Type<TimeScaleChangeEventHandler> type = new GwtEvent.Type<TimeScaleChangeEventHandler>();
    
    private double startX;
    private double endX;
        
	public TimeScaleChangeEvent(double startX, double endX) {
        super();
        this.startX = startX;
        this.endX = endX;
    }

    @Override
	public com.google.gwt.event.shared.GwtEvent.Type<TimeScaleChangeEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(TimeScaleChangeEventHandler handler) {
		handler.onTimeScaleChange(this);
	}

	public double getStartX() {
		return startX;
	}

	public void setStartX(double startX) {
		this.startX = startX;
	}

	public double getEndX() {
		return endX;
	}

	public void setEndX(int endX) {
		this.endX = endX;
	}


}
