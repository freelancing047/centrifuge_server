package csi.client.gwt.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.GroupDisplay;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.enumerations.GroupType;
import csi.server.common.service.api.UserAdministrationServiceProtocol;
import csi.server.common.util.Format;


public class SecurityAdmin {

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    static final String NO_GROUP_SELECTED = _constants.securityAdmin_SelectSecurity();
    static final String ALL_GROUPS = _constants.securityAdmin_AllSecurity();

    private GroupTab _tab;
    private SharedItems _shared;

    private GroupInfo _info;
    private Grid<GroupDisplay> _grid;
    private List<GroupDisplay> _selectedGroupList;
    private Map<String, GroupDisplay> _groupMap = null;

    private boolean _editButtonEnabled = false;
    private boolean _otherButtonsEnabled = false;
    private boolean _groupButtonsEnabled = false;
    private boolean _requestSent = false;

    private int _groupMenuId = 0;
    private String _groupSelection = null;
    private String _filterSelection = null;
    private String _priorFilter = null;
    private int _filterMenuId = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private VortexEventHandler<Response<String, GroupDisplay>> handleCreateOrUpdateResponse
            = new AbstractVortexEventHandler<Response<String, GroupDisplay>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {
            // Display error message.
            Dialog.showException(exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, GroupDisplay> responseIn) {

            try {

                if (ResponseHandler.isSuccess(responseIn)) {

                    GroupDisplay myGroupInfo = responseIn.getResult();

                    if (null != myGroupInfo) {

                        ((ListStore<GroupDisplay>)_grid.getStore()).update(replaceInfo(myGroupInfo));

                    } else {

                        Display.error(_constants.securityAdmin_NoInfoFound() + Format.value(responseIn.getKey()));
                    }

                    _shared.adjustButtons(responseIn.getCount());

                } else {

                    refreshGroups();
                }

            } catch (Exception myException) {

                GWT.getUncaughtExceptionHandler().onUncaughtException(myException);
                Dialog.showException(myException);
            }
        }
    };

    private VortexEventHandler<Response<GroupType, List<GroupDisplay>>> handleGroupInfoResponse
            = new AbstractVortexEventHandler<Response<GroupType, List<GroupDisplay>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);
            return true;
        }

        @Override
        public void onSuccess(Response<GroupType, List<GroupDisplay>> responseIn) {

            try {

                if (ResponseHandler.isSuccess(responseIn)) {

                    List<GroupDisplay> myNewList = responseIn.getResult();
                    ListStore<GroupDisplay> myGridStore = (ListStore<GroupDisplay>)_grid.getStore();

                    _tab.itemsReturnedLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);
                    _tab.itemsReturnedLabel.setText(_constants.returnCount(myNewList.size()));
                    _tab.itemsReturnedLabel.setVisible(true);

                    if (_tab.replaceButton.getValue()) {

                        myGridStore.clear();
                        myGridStore.addAll(myNewList);
                        _groupMap.clear();

                        for (GroupDisplay myGroup : myNewList) {

                            _groupMap.put(myGroup.getName().toLowerCase(), myGroup);
                        }

                    } else {

                        for (GroupDisplay myGroup : myNewList) {

                            String myKey = myGroup.getName().toLowerCase();

                            if (_groupMap.containsKey(myKey)) {

                                ((ListStore<GroupDisplay>)_grid.getStore()).update(replaceInfo(myGroup));

                            } else {

                                myGridStore.add(myGroup);
                                _groupMap.put(myKey, myGroup);
                            }
                        }
                    }
                    _grid.getView().refresh(false);
                    _shared.adjustButtons(responseIn.getCount());
                }

            } catch (Exception myException) {

                GWT.getUncaughtExceptionHandler().onUncaughtException(myException);
                Dialog.showException(myException);
            }
        }
    };

    private VortexEventHandler<Response<GroupType, List<GroupDisplay>>> handleGroupRefreshResponse
            = new AbstractVortexEventHandler<Response<GroupType, List<GroupDisplay>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);
            return true;
        }

        @Override
        public void onSuccess(Response<GroupType, List<GroupDisplay>> responseIn) {

            try {

                if (ResponseHandler.isSuccess(responseIn)) {

                    ListStore<GroupDisplay> myGridStore = (ListStore<GroupDisplay>)_grid.getStore();
                    List<GroupDisplay> myOldList = myGridStore.getAll();
                    List<GroupDisplay> myNewList = responseIn.getResult();

                    myGridStore.clear();
                    _groupMap.clear();

                    for (GroupDisplay myUser : myNewList) {

                        _groupMap.put(myUser.getName().toLowerCase(), myUser);
                    }

                    for (GroupDisplay myUser : myOldList) {

                        if (_groupMap.containsKey(myUser.getName().toLowerCase())) {

                            myGridStore.add(myUser);
                        }
                    }
                    _shared.adjustButtons(responseIn.getCount());
                }

            } catch (Exception myException) {

                GWT.getUncaughtExceptionHandler().onUncaughtException(myException);
                Dialog.showException(myException);
            }
        }
    };

    protected VortexEventHandler<Response<String, List<String>>> handleGroupDeleteResponse
            = new AbstractVortexEventHandler<Response<String, List<String>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {

            try {

                if (ResponseHandler.isSuccess(responseIn)) {

                    List<String> myList = responseIn.getResult();

                    _shared.buildRoleMap(myList);
                    _shared.adjustButtons(responseIn.getCount());

                } else {

                    refreshGroups();
                }

            } catch (Exception myException) {

                GWT.getUncaughtExceptionHandler().onUncaughtException(myException);
                Dialog.showException(myException);
            }
        }
    };

    protected VortexEventHandler<Response<String, List<String>>> handleGroupRemovalResponse
            = new AbstractVortexEventHandler<Response<String, List<String>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {

            try {

                if (ResponseHandler.isSuccess(responseIn)) {

                    List<String> myList = responseIn.getResult();

                    //TODO:

                    _shared.adjustButtons(responseIn.getCount());

                } else {

                    refreshGroups();
                }

            } catch (Exception myException) {

                GWT.getUncaughtExceptionHandler().onUncaughtException(myException);
                Dialog.showException(myException);
            }
        }
    };

    protected VortexEventHandler<Response<String, List<String>>> handleGroupIncludeResponse
            = new AbstractVortexEventHandler<Response<String, List<String>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {

            try {

                if (ResponseHandler.isSuccess(responseIn)) {

                    List<String> myList = responseIn.getResult();

                    //TODO:

                    _shared.adjustButtons(responseIn.getCount());

                } else {

                    refreshGroups();
                }

            } catch (Exception myException) {

                GWT.getUncaughtExceptionHandler().onUncaughtException(myException);
                Dialog.showException(myException);
            }
        }
    };

    private ClickHandler handleAnyButton(ClickHandler handlerIn) {

        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent eventIn) {

                _tab.itemsReturnedLabel.setVisible(false);
                handlerIn.onClick(eventIn);
            }
        };
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SecurityAdmin(GroupTab tabIn, SharedItems sharedIn) {

        _groupMap = new HashMap<String, GroupDisplay>();

        _tab = tabIn;
        _shared = sharedIn;

        _info = new GroupInfo(GroupType.SECURITY, _shared);
        _grid = _info.createGrid();
        _tab.gridContainer.setGrid(_grid);
        CsiTabPanel.fixRadioButtons(new RadioButton[]{_tab.searchCheckBox, _tab.allCheckBox});
        CsiTabPanel.fixRadioButtons(new RadioButton[]{_tab.augmentButton, _tab.replaceButton});
        _tab.augmentButton.setValue(true);

        _shared.addDataChangeEventHandler(new DataChangeEventHandler() {
            public void onDataChange(DataChangeEvent eventIn) {

                replaceGroupLists();
            }
        });

        _grid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedHandler<GroupDisplay>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<GroupDisplay> event) {

                _selectedGroupList = _grid.getSelectionModel().getSelection();
                int mySize = 0;
                if (null != _selectedGroupList) {
                    mySize = _selectedGroupList.size();
                }
                enableDisableControls(mySize);

            }
        });

        _tab.clearButton.addClickHandler(handleAnyButton(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {

                _grid.getStore().clear();
                _groupMap.clear();
            }
        }));

        _tab.editButton.addClickHandler(handleAnyButton(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                SecurityInfoPopup myDialog = new SecurityInfoPopup(_selectedGroupList.get(0), _shared);

                myDialog.addDataChangeEventHandler(new DataChangeEventHandler() {
                    public void onDataChange(DataChangeEvent eventIn) {

                        try {

                            updateGroup(eventIn.getData());

                        } catch (Exception myException) {

                            GWT.getUncaughtExceptionHandler().onUncaughtException(myException);
                            Dialog.showException(myException);
                        }
                    }
                });

                myDialog.show();
            }
        }));

        _tab.newButton.addClickHandler(handleAnyButton(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                SecurityInfoPopup myDialog = new SecurityInfoPopup(null, _shared);

                myDialog.addDataChangeEventHandler(new DataChangeEventHandler() {
                    public void onDataChange(DataChangeEvent eventIn) {

                        try {

                            addGroup(eventIn.getData());

                        } catch (Exception myException) {

                            GWT.getUncaughtExceptionHandler().onUncaughtException(myException);
                            Dialog.showException(myException);
                        }
                    }
                });

                myDialog.show();
            }
        }));

        _tab.deleteButton.addClickHandler(handleAnyButton(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                deleteGroups();
            }
        }));

        _tab.groupRemoveButton.addClickHandler(handleAnyButton(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                removeGroupsFromGroup();
            }
        }));

        _tab.groupAddButton.addClickHandler(handleAnyButton(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                addGroupsToGroup();
            }
        }));

        _tab.groupListBox.addSelectionChangedHandler(new SelectionChangedHandler<String>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                _groupMenuId = _tab.groupListBox.getSelectedIndex();
                if (0 < _groupMenuId) {

                    _groupSelection = _tab.groupListBox.getItemText(_groupMenuId);
//                    enableGroupSelection();
                }
                else {

                    _groupSelection = null;
//                    disableGroupSelection();
                }
                enableDisableControls((null != _selectedGroupList) ? _selectedGroupList.size() : 0);
            }
        });

        _tab.filterListBox.addSelectionChangedHandler(new SelectionChangedHandler<String>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                _filterMenuId = _tab.filterListBox.getSelectedIndex();
                if (0 < _filterMenuId) {

                    _filterSelection = _tab.filterListBox.getItemText(_filterMenuId);
                }
                else {

                    _filterSelection = null;
                }

            }
        });

        enableDisableControls(0);
    }

    public void refreshGroups() {

        // Retrieve display names for groups being displayed
        List<String> myList = new ArrayList<String>();

        // Request data from the server

        try {
            VortexFuture<Response<GroupType, List<GroupDisplay>>> vortexFuture = WebMain.injector.getVortex().createFuture();

            vortexFuture.execute(UserAdministrationServiceProtocol.class).retrieveGroups(GroupType.SECURITY, myList);
            vortexFuture.addEventHandler(handleGroupRefreshResponse);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    private void addGroup(Object dataIn) {

        if ((null != dataIn) && (dataIn instanceof GroupDisplay)) {

            GroupDisplay myGroup = (GroupDisplay)dataIn;

            if (GroupType.SECURITY.equals(myGroup.getType())) {

                VortexFuture<Response<String, GroupDisplay>> myVortexFuture = WebMain.injector.getVortex().createFuture();

                _shared._roleMap.put(myGroup.getName().toLowerCase(), false);
                myGroup.setId(_shared.getBogusId());
                ((ListStore<GroupDisplay>)_grid.getStore()).add(myGroup);
                _shared.addGroup(GroupType.SECURITY, myGroup.getName());
                _groupMap.put(myGroup.getName().toLowerCase(), myGroup);

                try {
                    myVortexFuture.execute(UserAdministrationServiceProtocol.class).createGroup(myGroup);
                    myVortexFuture.addEventHandler(handleCreateOrUpdateResponse);

                } catch (Exception myException) {

                    Dialog.showException(myException);
                }
            }
        }
    }

    private void updateGroup(Object dataIn) {

        if ((null != dataIn) && (dataIn instanceof GroupDisplay)) {

            GroupDisplay myGroup = (GroupDisplay)dataIn;

            if (GroupType.SECURITY.equals(myGroup.getType())) {

                VortexFuture<Response<String, GroupDisplay>> myVortexFuture = WebMain.injector.getVortex().createFuture();

                ((ListStore<GroupDisplay>)_grid.getStore()).update(myGroup);

                try {
                    myVortexFuture.execute(UserAdministrationServiceProtocol.class).updateGroup(myGroup);
                    myVortexFuture.addEventHandler(handleCreateOrUpdateResponse);

                } catch (Exception myException) {

                    Dialog.showException(myException);
                }
            }
        }
    }

    private void deleteGroups() {

        List<String> myList = new ArrayList<String>();

        if ((null != _selectedGroupList) && (0 < _selectedGroupList.size())) {

            for (GroupDisplay myGroup : _selectedGroupList) {

                String myGroupName = myGroup.getName();

                if (myGroupName.equalsIgnoreCase(_shared._adminGroup)
                        || myGroupName.equalsIgnoreCase(_shared._securityGroup)) {

                    if (1 < _selectedGroupList.size()) {

                        Dialog.showProblem(_constants.administrationDialogs_CannotDeleteUser2(myGroupName));

                    } else {

                        Dialog.showProblem(_constants.administrationDialogs_CannotDeleteUser1(myGroupName));
                    }

                } else {

                    String myPortionText = myGroup.getPortionText();

                    ((ListStore<GroupDisplay>)_grid.getStore()).remove(myGroup);
                    _groupMap.remove(myGroup.getName().toLowerCase());
                    _shared._roleMap.remove(myGroup.getName().toLowerCase());
                    if ((null != myPortionText) && (0 < myPortionText.length())) {
                        _shared._portionMap.remove(myPortionText.toLowerCase());
                    }

                    myList.add(myGroupName);
                }
            }

            if (0 < myList.size()) {

                try {
                    VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.execute(UserAdministrationServiceProtocol.class).deleteGroups(GroupType.SECURITY, myList);

                    vortexFuture.addEventHandler(handleGroupDeleteResponse);

                    _shared.deleteGroups(GroupType.SECURITY, myList);

                } catch (Exception myException) {

                    Dialog.showException(myException);
                }
            }
        }
    }

    private void removeGroupsFromGroup() {

        List<String> myList = new ArrayList<String>();

        if ((null != _selectedGroupList) && (0 < _selectedGroupList.size())) {

            ListStore<GroupDisplay> myListStore = (ListStore<GroupDisplay>)_grid.getStore();

            for (GroupDisplay myGroup : _selectedGroupList) {

                if (removeGroupFromRowItem(myGroup, _groupSelection)) {

                    myListStore.update(myGroup);
                    myList.add(myGroup.getName());
                }
            }

            if (0 < myList.size()) {

                try {
                    VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.execute(UserAdministrationServiceProtocol.class).removeGroupsFromGroup(_groupSelection, myList);

                    vortexFuture.addEventHandler(handleGroupRemovalResponse);

                } catch (Exception myException) {

                    Dialog.showException(myException);
                }
            }
        }
        resetGroupSelection();
    }

    private void addGroupsToGroup() {

        List<String> myList = new ArrayList<String>();

        if ((null != _selectedGroupList) && (0 < _selectedGroupList.size())) {

            ListStore<GroupDisplay> myListStore = (ListStore<GroupDisplay>)_grid.getStore();

            for (GroupDisplay myGroup : _selectedGroupList) {

                String myGroupName = myGroup.getName();

                if (myGroupName.equalsIgnoreCase(_groupSelection)) {

                    if (1 < _selectedGroupList.size()) {

                        Dialog.showProblem(_constants.administrationDialogs_CannotAddGroupToGroup2(myGroupName));

                    } else {

                        Dialog.showProblem(_constants.administrationDialogs_CannotAddGroupToGroup1(myGroupName));
                    }

                } else if (addGroupToRowItem(myGroup, _groupSelection)) {

                    myListStore.update(myGroup);
                    myList.add(myGroup.getName());
                }
            }

            if (0 < myList.size()) {

                try {
                    VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.execute(UserAdministrationServiceProtocol.class).addGroupsToGroup(_groupSelection, myList);

                    vortexFuture.addEventHandler(handleGroupIncludeResponse);

                } catch (Exception myException) {

                    Dialog.showException(myException);
                }
            }
        }
        resetGroupSelection();
    }

    void requestData(String searchStringIn) {

        requestData(searchStringIn, _filterSelection);
    }

    private void requestData(String searchStringIn, String filterSelectionIn) {

        _requestSent = true;

        try {
            VortexFuture<Response<GroupType, List<GroupDisplay>>> vortexFuture = WebMain.injector.getVortex().createFuture();

            if ((null != filterSelectionIn) && (0 < filterSelectionIn.length())) {

                vortexFuture.execute(UserAdministrationServiceProtocol.class).searchGroupGroups(GroupType.SECURITY, filterSelectionIn, searchStringIn);
            }
            else {

                vortexFuture.execute(UserAdministrationServiceProtocol.class).searchGroups(GroupType.SECURITY, searchStringIn);
            }

            vortexFuture.addEventHandler(handleGroupInfoResponse);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    public void replaceGroupLists() {

        Collection<StringEntry> myList = _shared.getGroupList(GroupType.SECURITY);

        _tab.groupListBox.clear();
        _tab.filterListBox.clear();
        _tab.groupListBox.addItem(NO_GROUP_SELECTED, DisplayMode.COMPONENT);
        _tab.filterListBox.addItem(ALL_GROUPS);

        if ((null != myList) && (0 < myList.size())) {

            if (_shared.provideSecurity()) {

                for (StringEntry myGroup : myList) {

                    _tab.groupListBox.addItem(myGroup.getDisplayString());
                    _tab.filterListBox.addItem(myGroup.getDisplayString());
                }
            }
        }

        Map<String, StringEntry> myGoneGroups = _shared.getDeletedMap(GroupType.SECURITY);

        if ((null != myGoneGroups) && (0 < myGoneGroups.size())) {

            ListStore<GroupDisplay> myListStore = _grid.getStore();

            for (int i = 0; myListStore.size() > i; i++) {

                boolean myChange = false;
                GroupDisplay myRowData = myListStore.get(i);
                String myGroupString = myRowData.getParentGroups();
                String[] myGroupNames = myGroupString.split(", ");

                for (int j = 0; myGroupNames.length > j; j++) {

                    String myGroupName = myGroupNames[j];

                    if (0 < myGroupName.length()) {

                        if (myGoneGroups.containsKey(myGroupName)) {

                            myChange = true;
                            myGroupNames[j] = null;
                        }
                    }
                }
                if (myChange) {

                    myRowData.setParentGroups(_shared.formatList(myGroupNames, null, null));
                    myListStore.update(myRowData);
                }
            }
        }
        enableDisableControls(0);
    }

    private boolean addGroupToRowItem(GroupDisplay myRowData, String groupNameIn) {

        boolean myChange = false;

        String myGroupString = myRowData.getParentGroups();
        String[] myGroupNames = myGroupString.split(", ");

        int i;

        for (i = 0; myGroupNames.length > i; i++) {

            String myTestName = myGroupNames[i];

            if ((null != myTestName) && (0 < myTestName.length())) {

                if (groupNameIn.equals(myTestName)) {

                    break;
                }
            }
        }
        if (myGroupNames.length == i) {

            myRowData.setParentGroups(_shared.formatList(myGroupNames, null, groupNameIn));
            myChange = true;
        }
        return myChange;
    }

    private boolean removeGroupFromRowItem(GroupDisplay myRowData, String groupNameIn) {

        boolean myChange = false;
        String myGroupString = myRowData.getParentGroups();
        String[] myGroupNames = myGroupString.split(", ");

        int i;

        for (i = 0; myGroupNames.length > i; i++) {

            String myGroupName = myGroupNames[i];

            if ((null != myGroupName) && (0 < myGroupName.length())) {

                if (groupNameIn.equals(myGroupName)) {

                    myGroupNames[i] = null;
                    break;
                }
            }
        }
        if (myGroupNames.length != i) {


            myRowData.setParentGroups(_shared.formatList(myGroupNames, null, null));
            myChange = true;
        }
        return myChange;
    }

    private void refreshData() {

        if (_requestSent) {

            requestData(_tab.getRefreshString(), _priorFilter);
        }
        _shared.refreshGroups(GroupType.SECURITY);
    }
/*
    public void disableGroupSelection() {

        _groupButtonsEnabled = false;
//        userGroupAddButton.setEnabled(false);
//        userGroupRemoveButton.setEnabled(false);
    }

    public void enableGroupSelection() {

        _groupButtonsEnabled = groupSelectionMade();
//        userGroupAddButton.setEnabled(_otherButtonsEnabled);
//        userGroupRemoveButton.setEnabled(_otherButtonsEnabled);
    }
*/
    private void enableDisableControls(int listCountIn) {

        if (0 == listCountIn) {

            _editButtonEnabled = false;
            _otherButtonsEnabled = false;
            _groupButtonsEnabled = false;
        }
        else if (1 == listCountIn) {

            _editButtonEnabled = true;
            _otherButtonsEnabled = true;
            _groupButtonsEnabled = groupSelectionMade();
        }
        else {

            _editButtonEnabled = false;
            _otherButtonsEnabled = true;
            _groupButtonsEnabled = groupSelectionMade();
        }
        enableControls();
    }

    private void disableControls() {
        _tab.editButton.setEnabled(false);
        _tab.deleteButton.setEnabled(false);
        _tab.groupAddButton.setEnabled(false);
        _tab.groupRemoveButton.setEnabled(false);
        _tab.disableControls();
    }

    private void enableControls() {
        _tab.editButton.setEnabled(_editButtonEnabled);
        _tab.deleteButton.setEnabled(_otherButtonsEnabled);
        _tab.groupAddButton.setEnabled(_groupButtonsEnabled);
        _tab.groupRemoveButton.setEnabled(_groupButtonsEnabled);
        _tab.enableControls();
    }

    private GroupDisplay replaceInfo(GroupDisplay newInfoIn) {

        GroupDisplay myGroup = _groupMap.get(newInfoIn.getName().toLowerCase());

        return myGroup.copyFrom(newInfoIn);
    }

    private boolean groupSelectionMade() {

        return (null != _groupSelection);
    }

    private void resetGroupSelection() {

        _groupSelection = null;
        _tab.resetGroupList();
        enableDisableControls((null != _selectedGroupList) ? _selectedGroupList.size() : 0);
    }
}
