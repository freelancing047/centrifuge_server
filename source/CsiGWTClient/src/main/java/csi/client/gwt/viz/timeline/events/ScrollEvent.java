package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class ScrollEvent extends BaseCsiEvent<ScrollEventHandler> {

    public static final GwtEvent.Type<ScrollEventHandler> type = new GwtEvent.Type<ScrollEventHandler>();
    
    private Boolean up = null;
    
    public ScrollEvent(){
        super();
    }
    
    public ScrollEvent(boolean up){
        super();
        this.setUp(up);
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ScrollEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(ScrollEventHandler handler) {
		handler.onScroll(this);
	}

    public Boolean isUp() {
        return up;
    }

    public void setUp(Boolean up) {
        this.up = up;
    }


}
