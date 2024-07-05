package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

/**
 * Created by centrifuge on 3/23/2016.
 */
public class CsiDropEvent extends BaseCsiEvent<CsiDropEventHandler> {

    public static final GwtEvent.Type<CsiDropEventHandler> type = new GwtEvent.Type<CsiDropEventHandler>();

    private Object _source;
    private Object _target;

    public CsiDropEvent(Object sourceIn, Object targetIn) {

        _source = sourceIn;
        _target = targetIn;
    }

    public Object getSource() {

        return _source;
    }

    public Object getTarget() {

        return _target;
    }

    @Override
    public Type<CsiDropEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(CsiDropEventHandler handler) {
        handler.onDrop(this);
    }
}
