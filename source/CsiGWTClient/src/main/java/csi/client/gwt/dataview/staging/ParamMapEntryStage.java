package csi.client.gwt.dataview.staging;

import java.util.List;

import csi.server.common.dto.CsiMap;
import csi.server.common.model.ParamMapEntry;

public class ParamMapEntryStage extends ParamMapEntry {

    private ParamMapEntry _source;
    private boolean _created = false;
    private boolean _deleted = false;

    public ParamMapEntryStage(ParamMapEntry sourceIn, boolean createdIn) {
        
        initialize(sourceIn);
        _created = createdIn;
    }

    public ParamMapEntryStage(ParamMapEntry sourceIn) {
        
        initialize(sourceIn);
    }
    
    public ParamMapEntryStage() {
        
        initialize(new ParamMapEntry());
        _source.setClientProperties(new CsiMap<String,String>());
        _created = true;
    }
    
    public void delete() {
        
        _deleted = true;
    }
    
    public void finalize(List<ParamMapEntry> listIn) {
        
        if (_created) {
            
            if (!_deleted) {
                
                // Add object to list
                //
                moveDataToItem();
                listIn.add(_source);
            }
            
        } else if (_deleted) {
            
            // Remove object from list
            //
            listIn.remove(_source);
            
        } else {
            
            // Update object within list
            //
            moveDataToItem();
        }
    }
    
    private void initialize(ParamMapEntry sourceIn) {
        
        _source = sourceIn;
        
        fieldLocalId = _source.getFieldLocalId();
        targetFieldLocalId = _source.getTargetFieldLocalId();
        paramName = _source.getParamName();
        paramOrdinal = _source.getParamOrdinal();
        value = _source.getValue();
    }
    
    private void moveDataToItem() {
        
        _source.setFieldLocalId(fieldLocalId);
        _source.setTargetFieldLocalId(targetFieldLocalId);
        _source.setParamName(paramName);
        _source.setParamOrdinal(paramOrdinal);
        _source.setValue(value);
    }
}
