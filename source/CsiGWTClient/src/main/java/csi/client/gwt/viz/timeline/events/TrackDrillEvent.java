package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class TrackDrillEvent extends BaseCsiEvent<TrackDrillEventHandler> {

    public static final GwtEvent.Type<TrackDrillEventHandler> type = new GwtEvent.Type<TrackDrillEventHandler>();
    
    private String trackName = null;
    
    
    public TrackDrillEvent(){
        super();
    }
    
    public TrackDrillEvent(String trackName){
        super();
        setTrackName(trackName);
       
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<TrackDrillEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(TrackDrillEventHandler handler) {
		handler.onDrill(this);
	}

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }



}
