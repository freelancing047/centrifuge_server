package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.client.gwt.etc.DataPairDisplay;
import csi.server.common.dto.SelectionListData.ExtendedInfo;


public class MappingChangeEvent<S extends ExtendedInfo, T extends ExtendedInfo> extends BaseCsiEvent<MappingChangeEventHandler<S, T>> {

    private boolean _isMapping;
    private DataPairDisplay<S, T> _dataPair;
    
    public MappingChangeEvent(DataPairDisplay<S, T> dataPairIn, boolean isMappingIn) {
        
        _dataPair = dataPairIn;
        _isMapping = isMappingIn;
    }
    
    public boolean isMapping() {
        
        return _isMapping;
    }
    
    public S getItemOne() {
        
        return _dataPair.getItemOne();
    }
    
    public T getItemTwo() {
        
        return _dataPair.getItemTwo();
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<MappingChangeEventHandler<S, T>> getAssociatedType() {
        return new GwtEvent.Type<MappingChangeEventHandler<S, T>>();
    }

    @Override
    protected void dispatch(MappingChangeEventHandler<S, T> handler) {
        handler.onMappingChange(this);
    }
}
