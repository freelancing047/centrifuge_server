package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class RenderFooterEvent extends BaseCsiEvent<RenderFooterEventHandler> {

    public static final GwtEvent.Type<RenderFooterEventHandler> type = new GwtEvent.Type<RenderFooterEventHandler>();
    
    public RenderFooterEvent(){
    	super();
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<RenderFooterEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(RenderFooterEventHandler handler) {
		handler.onRender(this);
	}



}
