package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.server.common.dto.SelectionListData.SelectorBasics;


public class ResourceSelectionEvent<T extends SelectorBasics> extends BaseCsiEvent<ResourceSelectionEventHandler> {

    private T _selection = null;
    private boolean _forceOverwrite = false;
    private String _name = null;
    private String _remarks = null;

    public static final GwtEvent.Type<ResourceSelectionEventHandler> type = new GwtEvent.Type<ResourceSelectionEventHandler>();
    
    public ResourceSelectionEvent(T selectionIn, String nameIn, String remarksIn, boolean forceOverwriteIn) {
        
        _selection = selectionIn;
        _name = nameIn;
        _remarks = remarksIn;
        _forceOverwrite = forceOverwriteIn;
    }

    public T getSelection() {
        
        return (null != _selection) ? _selection : null;
    }

    public boolean forceOverwrite() {
        return _forceOverwrite;
    }

    public String getName() {
        
        return (null != _name) ? _name :  (null != _selection) ? _selection.getName() : null;
    }

    public String getRemarks() {
        
        return (null != _remarks) ? _remarks : (null != _selection) ? _selection.getRemarks() : null;
    }
    
    public boolean wasFound() {
        
        return (null != _selection);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ResourceSelectionEventHandler> getAssociatedType() {
        
        return type;
    }

    @Override
    protected void dispatch(ResourceSelectionEventHandler handler) {
        
        handler.onResourceSelection(this);
    }
}
