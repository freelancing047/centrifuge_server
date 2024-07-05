package csi.client.gwt.viz.graph.window.legend;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.etc.BaseCsiEvent;

public class DropCompleteEvent extends BaseCsiEvent<DropCompleteEventHandler> {

    private int position = -1;
    private Widget widget = null;
    public static final GwtEvent.Type<DropCompleteEventHandler> type = new GwtEvent.Type<DropCompleteEventHandler>();
    
    public DropCompleteEvent(Widget widget, int index){
        setPosition(index);
        setWidget(widget);
    }
    
    @Override
    public GwtEvent.Type<DropCompleteEventHandler> getAssociatedType() {
        return type;
    }
    
    public static GwtEvent.Type<DropCompleteEventHandler> getType() {
        return type;
    }

    @Override
    protected void dispatch(DropCompleteEventHandler handler) {
        
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

}
