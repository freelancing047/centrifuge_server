package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class OverviewChangeEvent extends BaseCsiEvent<OverviewChangeEventHandler> {

    public static final GwtEvent.Type<OverviewChangeEventHandler> type = new GwtEvent.Type<OverviewChangeEventHandler>();
    
    private long start;
    private long end;
    
    public OverviewChangeEvent(){
    	super();
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<OverviewChangeEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(OverviewChangeEventHandler handler) {
		handler.onChange(this);
	}

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }


}
