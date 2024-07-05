package csi.client.gwt.viz.graph.tab.player;

import com.google.gwt.event.shared.GwtEvent;

public class StopEvent  extends GwtEvent<StopEventHandler> {
    public static final Type<StopEventHandler> TYPE = new Type<StopEventHandler>();

    public StopEvent( ) {}

    @Override
    public Type<StopEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(StopEventHandler handler) {
        handler.onStopEvent(this);
    }


}
