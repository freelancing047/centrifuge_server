package csi.client.gwt.viz.graph.tab.player;

import java.util.Date;

import com.google.gwt.event.shared.GwtEvent;

public class StepEvent extends GwtEvent<StepEventHandler> {
    public static final Type<StepEventHandler> TYPE = new Type<StepEventHandler>();
    private final Date currentTime;

    public StepEvent(Date currentTime) {

        this.currentTime = currentTime;
    }

    @Override
    public Type<StepEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(StepEventHandler handler) {
        handler.onStepEvent(this);
    }

    public Date getCurrentTime() {
        return currentTime;
    }
}
