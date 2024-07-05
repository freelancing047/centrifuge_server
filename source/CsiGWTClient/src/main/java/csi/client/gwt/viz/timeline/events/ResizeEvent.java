package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class ResizeEvent extends BaseCsiEvent<ResizeEventHandler> {

    public static final GwtEvent.Type<ResizeEventHandler> type = new GwtEvent.Type<ResizeEventHandler>();
    
    public ResizeEvent(){
    	super();
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ResizeEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(ResizeEventHandler handler) {
		handler.onResize(this);
	}



}
