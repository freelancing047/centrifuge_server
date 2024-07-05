package csi.client.gwt.viz.timeline.view.summary;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class SummaryCompleteEvent extends BaseCsiEvent<SummaryCompleteEventHandler> {

    public static final GwtEvent.Type<SummaryCompleteEventHandler> type = new GwtEvent.Type<SummaryCompleteEventHandler>();
    
    int height;
    
    public SummaryCompleteEvent(int height){
        super();
        this.height = height;
    }
    
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<SummaryCompleteEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(SummaryCompleteEventHandler handler) {
        handler.onComplete(this);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }



}
