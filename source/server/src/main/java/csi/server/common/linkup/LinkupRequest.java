package csi.server.common.linkup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.AuthDO;
import csi.server.common.dto.LaunchParam;
import csi.server.common.enumerations.LinkupMode;
import csi.server.common.model.visualization.selection.Selection;


public class LinkupRequest implements IsSerializable {

    private LinkupMode _mode;
    private String _sessionId;

    // uuid of visualization doing the linkup
    private String _visualizationId;

    // name of the linkup definition
    private String _linkupUuid;
    private String _newDataViewName;
    private Selection _selection;
    private boolean _discardNulls = true;

    private List<LinkupHelper> _parameterBuilderList;
    private List<LaunchParam> _parameterValues;
    private List<AuthDO> _authorizationList = null;
    
    @Transient
    private Map<String, AuthDO> _authorizationMap = null;

    public boolean isDiscardNulls() {

        return _discardNulls;
    }

    public void setDiscardNulls(boolean discardNullsIn) {

        _discardNulls = discardNullsIn;
    }

    public LinkupMode getMode() {
        
        return _mode;
    }
    
    public void setMode(LinkupMode modeIn) {
        
        _mode = modeIn;
    }
    
    public String getSessionId() {
        
        return _sessionId;
    }
    
    public void setSessionId(String sessionIdIn) {
        
        _sessionId = sessionIdIn;
    }
    
    public String getVisualizationId() {
        
        return _visualizationId;
    }
    
    public void setVisualizationId(String visualizationIdIn) {
        
        _visualizationId = visualizationIdIn;
    }

    public String getLinkupUuid() {
        
        return _linkupUuid;
    }
    
    public void setLinkupUuid(String linkupUuidIn) {
        
        _linkupUuid = linkupUuidIn;
    }
    
    public String getNewDataViewName() {
        
        return _newDataViewName;
    }
    
    public void setNewDataViewName(String newDataViewNameIn) {
        
        _newDataViewName = newDataViewNameIn;
    }
    
    public Selection getSelection() {
        
        return _selection;
    }
    
    public void setSelection(Selection selectionIn) {
        
        _selection = selectionIn;
    }
    
    public List<LinkupHelper> getParameterBuilderList() {
        
        return _parameterBuilderList;
    }
    
    public void setParameterBuilderList(List<LinkupHelper> parameterBuilderListIn) {
        
        _parameterBuilderList = parameterBuilderListIn;
    }

    public List<LaunchParam> getParameterValues() {

        return _parameterValues;
    }

    public void setParameterValues(List<LaunchParam> parameterValuesIn) {

        _parameterValues = parameterValuesIn;
    }

    public List<AuthDO> getAuthorizationList() {
        
        return _authorizationList;
    }
    
    public void setAuthorizationList(List<AuthDO> authorizationListIn) {
        
        _authorizationList = authorizationListIn;
        _authorizationMap = null;
    }
    
    public Map<String, AuthDO> getAuthorizationMap() {
        
        if (null == _authorizationMap) {
            
            _authorizationMap = new HashMap<String, AuthDO>();
            
            if (null != _authorizationList) {
                
                for (AuthDO myAuthorization : _authorizationList) {
                    
                    _authorizationMap.put(myAuthorization.getDsLocalId(), myAuthorization);
                }
            }
        }
        return _authorizationMap;
    }
    
    public void refreshAuthorizationList() {
        
        _authorizationList = new ArrayList<AuthDO>(_authorizationMap.values());
    }
}
