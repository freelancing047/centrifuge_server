package csi.client.gwt.viz.timeline.events;

import java.util.List;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class TrackSelectionEvent extends BaseCsiEvent<TrackSelectionEventHandler> {

    public static final GwtEvent.Type<TrackSelectionEventHandler> type = new GwtEvent.Type<TrackSelectionEventHandler>();
    private boolean select;
    private List<Integer> ids;
    

	/**
	 *
	 * @param startX
	 * @param endX
	 * @param select true to select the items, false to deselect the items
	 */
    public TrackSelectionEvent(List<Integer> ids, boolean select){
    	super();
    	this.ids = ids;
    	this.setSelect(select);
    }
    
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<TrackSelectionEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(TrackSelectionEventHandler handler) {
		handler.onTrackSelection(this);
	}

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

	
}
