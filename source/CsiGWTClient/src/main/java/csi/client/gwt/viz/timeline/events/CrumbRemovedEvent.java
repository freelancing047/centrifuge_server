package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class CrumbRemovedEvent extends BaseCsiEvent<CrumbRemovedEventHandler> {

    public static final GwtEvent.Type<CrumbRemovedEventHandler> type = new GwtEvent.Type<CrumbRemovedEventHandler>();
    
    public CrumbRemovedEvent(){
    	super();
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<CrumbRemovedEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(CrumbRemovedEventHandler handler) {
		handler.onRemove(this);
	}



}
