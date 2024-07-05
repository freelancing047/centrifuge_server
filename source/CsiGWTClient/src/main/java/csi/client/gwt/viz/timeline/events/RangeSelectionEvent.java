package csi.client.gwt.viz.timeline.events;

import csi.client.gwt.etc.BaseCsiEvent;

public class RangeSelectionEvent extends BaseCsiEvent<RangeSelectionEventHandler> {

    public static final Type<RangeSelectionEventHandler> type = new Type<RangeSelectionEventHandler>();
    
    private double startX;
    private double endX;
    private boolean select;
	private String trackName;

	/**
	 *  @param startX
	 * @param endX
	 * @param select true to select the items, false to deselect the items
	 * @param trackName
	 */
    public RangeSelectionEvent(double startX, double endX, boolean select, String trackName){
    	super();
    	this.startX = startX;
    	this.endX = endX;
		this.select = select;
		this.trackName = trackName;
	}
    
	@Override
	public Type<RangeSelectionEventHandler> getAssociatedType() {
		return type;
	}

	@Override
	protected void dispatch(RangeSelectionEventHandler handler) {
		handler.onRangeSelection(this);
	}

	public double getStartX() {
		return startX;
	}

	public void setStartX(double startX) {
		this.startX = startX;
	}

	public double getEndX() {
		return endX;
	}

	public void setEndX(double endX) {
		this.endX = endX;
	}

	public boolean isSelect() {
		return select;
	}

	public void setSelect(boolean select) {
		this.select = select;
	}

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
}
