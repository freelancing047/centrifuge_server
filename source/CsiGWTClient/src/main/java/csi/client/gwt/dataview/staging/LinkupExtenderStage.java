package csi.client.gwt.dataview.staging;

import java.util.ArrayList;
import java.util.List;

import csi.server.common.dto.CsiMap;
import csi.server.common.model.ParamMapEntry;
import csi.server.common.model.linkup.LinkupExtender;


public class LinkupExtenderStage extends LinkupExtender {

    private LinkupExtender _source;
    private boolean _created = false;
    private boolean _deleted = false;

    public LinkupExtenderStage(LinkupExtenderStage sourceIn, boolean createdIn) {

        initialize(sourceIn);
        _created = createdIn;
    }

    public LinkupExtenderStage(LinkupExtender sourceIn) {
        
        initialize(sourceIn);
    }
    
    public LinkupExtenderStage(String vizDefIdIn) {
        
        initialize(new LinkupExtenderStage());
        _source.setClientProperties(new CsiMap<String,String>());
        _vizDefId = vizDefIdIn;
        _created = true;
    }
    
    public LinkupExtenderStage() {
        
        initialize(new LinkupExtender());
        _source.setClientProperties(new CsiMap<String,String>());
        _created = true;
    }
    
    public void delete() {
        
        _deleted = true;
    }

    public boolean wasCreated() {

        return _created;
    }
    
    public void finalize(List<LinkupExtender> listIn) {
        
        if (_created) {
            
            if (!_deleted) {
                
                // Add object to list
                //
                moveDataToItem();
                listIn.add(_source);
            }
            
        } else if (_deleted) {
            
            if (_source instanceof LinkupExtenderStage) {
                
                // Mark object for removal from list
                //
                ((LinkupExtenderStage) _source).delete();
                
            } else {
                
                // Remove object from list
                //
                listIn.remove(_source);
            }
            
        } else {
            
            // Update object within list
            //
            moveDataToItem();
        }
        _created = false;
    }
    
    private void initialize(LinkupExtender sourceIn) {
        
        _source = sourceIn;
        
        _vizDefId = _source.getVizDefId();
        _nodeDefId = _source.getNodeDefId();
        _linkDefId = _source.getLinkDefId();
        _name = _source.getName();
        _description = _source.getDescription();
        _isDisabled = _source.getIsDisabled();
        
        loadParameterList(_source.getParameterList());
    }
    
    private void moveDataToItem() {
        
        _source.setVizDefId(_vizDefId);
        _source.setNodeDefId(_nodeDefId);
        _source.setLinkDefId(_linkDefId);
        _source.setName(_name);
        _source.setDescription(_description);
        _source.setIsDisabled(_isDisabled);
        
        _source.setParameterList(_parameterList);
    }
    
    private void loadParameterList(List<ParamMapEntry> sourceParamsIn) {
        
        if ((null != sourceParamsIn) && (0 < sourceParamsIn.size())) {
            
            _parameterList = new ArrayList<ParamMapEntry>();
            
            for (int i = 0; sourceParamsIn.size() > i; i++) {
                
                _parameterList.add(sourceParamsIn.get(i).newCopy());
            }
        }
    }
}
