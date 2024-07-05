package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.client.gwt.viz.timeline.model.TimelineTrackModel;

public class TrackingCompleteEvent extends BaseCsiEvent<TrackingCompleteEventHandler> {

    public static final GwtEvent.Type<TrackingCompleteEventHandler> type = new GwtEvent.Type<TrackingCompleteEventHandler>();
    
    private TimelineTrackModel track;
    
    public TrackingCompleteEvent(TimelineTrackModel track){
    	super();
    	this.setTrack(track);
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<TrackingCompleteEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(TrackingCompleteEventHandler handler) {
		handler.onComplete(this);
	}

    public TimelineTrackModel getTrack() {
        return track;
    }

    public void setTrack(TimelineTrackModel track) {
        this.track = track;
    }



}
