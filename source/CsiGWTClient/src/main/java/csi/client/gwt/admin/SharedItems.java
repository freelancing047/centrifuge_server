package csi.client.gwt.admin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

import csi.client.gwt.WebMain;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.GroupType;
import csi.server.common.service.api.UserAdministrationServiceProtocol;


public class SharedItems implements HasHandlers {

    class GroupSet {
        
        Collection<StringEntry> _list;
        Map<String, StringEntry> _map;
        Map<String, StringEntry> _gone;
    }
    
    private HandlerManager _handlerManager;
    
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    String _metaViewerGroup;
    String _adminGroup;
    String _securityGroup;
    String _everyoneGroup;
    String _adminUser;
    String _securityUser;
    String _originatorGroup;
    String _currentUser;
    
    private UserSecurityInfo _userInfo;

    Map<String, Boolean> _roleMap;
    Map<String, Boolean> _portionMap;

    GroupSet[] _groupSet = new GroupSet[] {new GroupSet(), new GroupSet()};

    Long _bogusId = 0x7fffffffffffffffL;
    
    private UserAdmin _userAdmin = null;
    
    
    AbstractVortexEventHandler<Response<GroupType, List<String>>> handleListGroupsResponse
            = new AbstractVortexEventHandler<Response<GroupType, List<String>>>() {

        @Override
        public boolean onError(Throwable myException) {
            
            Dialog.showException(myException);
            return true;
        }
        @Override
        public void onSuccess(Response<GroupType, List<String>> responseIn) {
            
            int myGroupKey = responseIn.getKey().ordinal();
            List<String> myNewList = responseIn.getResult();

            identifyExistingGroups(myGroupKey, myNewList);
        }
    };


    public SharedItems(UserSecurityInfo userInfoIn) {
        
        _userInfo = userInfoIn;
        _metaViewerGroup = _userInfo.getViewerGroup();
        _adminGroup = _userInfo.getAdminGroup();
        _securityGroup = _userInfo.getCsoGroup();
        _originatorGroup = _userInfo.getOriginatorGroup();
        _everyoneGroup = _userInfo.getEveryoneGroup();
        _adminUser = _userInfo.getAdminUser();
        _securityUser = _userInfo.getCsoUser();
        _currentUser = _userInfo.getName();
        _handlerManager = new HandlerManager(this);
        
    }
    
    public Long getBogusId() {
        
        return _bogusId--;
    }
    
    public void setUserControl(UserAdmin userAdminIn) {
        
        _userAdmin = userAdminIn;
    }
    
    public void adjustButtons(long slotsIn) {
        
        _userAdmin.enableSelectionControls(slotsIn);
    }
    
    public void addGroup(GroupType typeIn, String groupNameIn) {
        
        if (null != groupNameIn) {
            
            int myOffset = typeIn.ordinal();
                
            if (!_groupSet[myOffset]._map.containsKey(groupNameIn)) {
                
                _groupSet[myOffset]._map.put(groupNameIn, new StringEntry(groupNameIn));
            }
            
            if (_groupSet[myOffset]._gone.containsKey(groupNameIn)) {
                
                _groupSet[myOffset]._gone.remove(groupNameIn);
            }
            _groupSet[myOffset]._list = _groupSet[myOffset]._map.values();
            fireEvent(new DataChangeEvent(_groupSet[myOffset]._list));
        }
    }
    
    public void deleteGroups(GroupType typeIn, List<String> listIn) {
        
        if ((null != listIn) && (0 < listIn.size())) {
            
            int myOffset = typeIn.ordinal();
            
            for (String myGroupName : listIn) {
                
                if (_groupSet[myOffset]._map.containsKey(myGroupName)) {
                    
                    _groupSet[myOffset]._map.remove(myGroupName);
                }
                
                if (! _groupSet[myOffset]._gone.containsKey(myGroupName)) {
                    
                    _groupSet[myOffset]._gone.put(myGroupName, new StringEntry(myGroupName));
                }
            }
            _groupSet[myOffset]._list = _groupSet[myOffset]._map.values();
            fireEvent(new DataChangeEvent(_groupSet[myOffset]._list));
        }
    }
    
    public Collection<StringEntry> getGroupList(GroupType typeIn) {
        
        int myOffset = typeIn.ordinal();
        
        return _groupSet[myOffset]._list;
    }
    
    public Map<String, StringEntry> getGroupMap(GroupType typeIn) {
        
        int myOffset = typeIn.ordinal();
        
        return _groupSet[myOffset]._map;
    }
    
    public Map<String, StringEntry> getDeletedMap(GroupType typeIn) {
        
        int myOffset = typeIn.ordinal();
        
        return _groupSet[myOffset]._gone;
    }

    public void refreshGroups() {

        refreshGroups(GroupType.SHARING);
        
        if (provideSecurity()) {
            
            refreshGroups(GroupType.SECURITY);
        }
    }

    public void refreshGroups(final GroupType typeIn) {
         
        try {
            
            VortexFuture<Response<GroupType, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.execute(UserAdministrationServiceProtocol.class).getGroupNames(typeIn);
            
            vortexFuture.addEventHandler(handleListGroupsResponse);
        } catch (Exception myException) {
            
            Dialog.showException(myException);
        }
    }
    
    public String formatList(Object[] arrayIn, List<?> listIn, Object itemIn) {
        
        StringBuilder myBuffer = new StringBuilder();
        
        if ((null != arrayIn) && (0 < arrayIn.length)) {
            
            for (Object myObject : arrayIn) {
                
                if ((null != myObject) && (0 < myObject.toString().length())) {
                    
                    if (0 != myBuffer.length()) {
                        
                        myBuffer.append(", ");
                    }
                    myBuffer.append(myObject.toString());
                }
            }
        }
        
        if ((null != listIn) && (0 < listIn.size())) {
            
            for (Object myObject : listIn) {
                
                if ((null != myObject) && (0 < myObject.toString().length())) {
                    
                    if (0 != myBuffer.length()) {
                        
                        myBuffer.append(", ");
                    }
                    myBuffer.append(myObject.toString());
                }
            }
        }
        
        if ((null != itemIn) && (0 < itemIn.toString().length())) {
            
            if (0 != myBuffer.length()) {
                
                myBuffer.append(", ");
            }
            myBuffer.append(itemIn.toString());
        }
        return myBuffer.toString();
    }

    @Override
    public void fireEvent(GwtEvent<?> eventIn) {
        _handlerManager.fireEvent(eventIn);
    }

    public HandlerRegistration addDataChangeEventHandler(
            DataChangeEventHandler handler) {
        return _handlerManager.addHandler(DataChangeEvent.type, handler);
    }
    
    public boolean provideSharing() {
        
        return _userInfo.isAdmin();
    }

    public boolean provideSecurity() {

        return _userInfo.isSecurity();
    }

    public boolean doCapco() {

        return _userInfo.getDoCapco();
    }

    public void buildRoleMap(List<String> listIn) {

        _roleMap = new HashMap<String, Boolean>();

        for (String myRoleName : listIn) {

            if ((null != myRoleName) && (0 < myRoleName.length())){

                _roleMap.put(myRoleName.toLowerCase(),  true);
            }
        }
    }

    public void identifyExistingGroups(int groupKeyIn, List<String> newListIn) {


        _groupSet[groupKeyIn]._map = new TreeMap<String, StringEntry>();
        _groupSet[groupKeyIn]._gone = new TreeMap<String, StringEntry>();

        if ((null != newListIn) && (0 < newListIn.size())) {

            for (String myGroup : newListIn) {

                if ((null != myGroup) && (0 < myGroup.length())) {

                    _groupSet[groupKeyIn]._map.put(myGroup, new StringEntry(myGroup));
                }
            }
        }
        // Remove the "Everyone" group from consideration
        // All normal users must belong to it and
        // all restricted users must not.
        if (_groupSet[groupKeyIn]._map.containsKey(_everyoneGroup)) {

            _groupSet[groupKeyIn]._map.remove(_everyoneGroup);
        }
        _groupSet[groupKeyIn]._list = _groupSet[groupKeyIn]._map.values();
        fireEvent(new DataChangeEvent(_groupSet[groupKeyIn]._list));
    }
}
