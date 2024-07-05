package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.shared.core.visualization.timeline.CommonTrack;

public class TrackChangeEvent extends BaseCsiEvent<TrackChangeEventHandler> {

    public static final GwtEvent.Type<TrackChangeEventHandler> type = new GwtEvent.Type<TrackChangeEventHandler>();
    
    private CommonTrack trackModel;
    
    public TrackChangeEvent(CommonTrack trackModel2){
    	super();
    	this.trackModel = trackModel2;
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<TrackChangeEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(TrackChangeEventHandler handler) {
		handler.onChange(this);
	}

    public CommonTrack getTrackModel() {
        return trackModel;
    }



}
