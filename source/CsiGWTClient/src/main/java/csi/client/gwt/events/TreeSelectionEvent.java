package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class TreeSelectionEvent<R, S, T> extends BaseCsiEvent<TreeSelectionEventHandler<R, S, T>> {

    private R _item;
    private S _object;
    private T _parent;
    
    public TreeSelectionEvent(R itemIn, S objectIn, T parentIn) {
        
        _item = itemIn;
        _object = objectIn;
        _parent = parentIn;
    }
    
    public R getItem() {
        return _item;
    }
    
    public S getObject() {
        return _object;
    }
    
    public T getParent() {
        return _parent;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<TreeSelectionEventHandler<R, S, T>> getAssociatedType() {
        return new GwtEvent.Type<TreeSelectionEventHandler<R, S, T>>();
    }

    @Override
    protected void dispatch(TreeSelectionEventHandler<R, S, T> handler) {
        handler.onTreeSelection(this);
    }
}
