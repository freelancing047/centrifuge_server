package csi.client.gwt.events;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class TransferCompleteEvent extends BaseCsiEvent<TransferCompleteEventHandler> {

    public static final GwtEvent.Type<TransferCompleteEventHandler> type = new GwtEvent.Type<TransferCompleteEventHandler>();
    
    private List<String> _list = null;

    public TransferCompleteEvent() {
        
    }

    public TransferCompleteEvent(String nameIn) {

        if (null != nameIn) {

            _list = new ArrayList<String>();

            _list.add(nameIn);
        }
    }

    public TransferCompleteEvent(List<String> listIn) {
        
        _list = new ArrayList<String>(listIn);
    }
    
    public String getItemName() {
        
        return ((null != _list) && (0 < _list.size())) ? _list.get(0) : null;
    }
    
    public List<String> getItemList() {
        
        return _list;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<TransferCompleteEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(TransferCompleteEventHandler handler) {
        handler.onTransferComplete(this);
    }
}
