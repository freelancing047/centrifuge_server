package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.client.gwt.widget.drawing.BaseRenderable;

public class SelectionChangeEvent extends BaseCsiEvent<SelectionChangeEventHandler> {

    public static final GwtEvent.Type<SelectionChangeEventHandler> type = new GwtEvent.Type<SelectionChangeEventHandler>();
    
    private BaseRenderable event;
    
    public SelectionChangeEvent(BaseRenderable renderable){
    	super();
    	this.setEvent(renderable);
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SelectionChangeEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(SelectionChangeEventHandler handler) {
		handler.onSelect(this);
	}

    public BaseRenderable getEvent() {
        return event;
    }

    public void setEvent(BaseRenderable event) {
        this.event = event;
    }



}
