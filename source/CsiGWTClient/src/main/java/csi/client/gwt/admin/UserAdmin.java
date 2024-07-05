package csi.client.gwt.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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
import csi.server.common.dto.Response;
import csi.server.common.dto.UserDisplay;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.enumerations.GroupType;
import csi.server.common.service.api.UserAdministrationServiceProtocol;


public class UserAdmin {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtDeleteDialogTitle = _constants.deleteItems_Title();
    private static final String _txtDeleteDialogDescription = _constants.deleteItems_Description(Dialog.txtContinueButton, Dialog.txtCancelButton);

    private UserTab _tab;
    private SharedItems _shared;
    private boolean _inputEnabled = false;
    private boolean _editButtonEnabled = false;
    private boolean _otherButtonsEnabled = false;
    private boolean _groupButtonsEnabled = false;
    private boolean _requestSent = false;
    
    private UserInfo _info;
    private Grid<UserDisplay> _grid;
    private Map<String, UserDisplay> _userMap = null;

    private String _sharingSelection = null;
    private String _securitySelection = null;
    private int _groupMenuId = 0;
    private String _sharingFilterSelection = null;
    private String _securityFilterSelection = null;
    private String _priorSharingFilter = null;
    private String _priorSecurityFilter = null;
    private boolean _groupSelectionMade = false;
    private int _selectionCount = 0;
    private int _filterMenuId = 0;
    private int _userSlots = 0;
    private int _requestedSlots = 0;
    private List<String> _requestList = null;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler handleDoDeleteClick
    = new ClickHandler() {
        
        public void onClick(ClickEvent eventIn) {
            
            deleteUsers();
        }
    };
    
    protected VortexEventHandler<Response<String, UserDisplay>> handleCreateOrUpdateResponse
    = new AbstractVortexEventHandler<Response<String, UserDisplay>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {
            // Display error message.
            Dialog.showException(exceptionIn);
            return false;
        }
        
        @Override
        public void onSuccess(Response<String, UserDisplay> responseIn) {

            if (ResponseHandler.isSuccess(responseIn)) {
                
                UserDisplay myUserInfo = responseIn.getResult();
                
                if (null != myUserInfo) {

                    ((ListStore<UserDisplay>)_grid.getStore()).update(replaceInfo(myUserInfo));

                } else {
                    
                    Display.error("No information found for user \"" + responseIn.getKey() + "\"");
                }
                
                enableSelectionControls(responseIn.getCount());
            }
        }
    };
    
    protected VortexEventHandler<Response<String, List<UserDisplay>>> handleUserSearchResponse
    = new AbstractVortexEventHandler<Response<String, List<UserDisplay>>>() {
        
        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);
            return true;
        }
        
        @Override
        public void onSuccess(Response<String, List<UserDisplay>> responseIn) {

            if (ResponseHandler.isSuccess(responseIn)) {
                
                List<UserDisplay> myNewList = responseIn.getResult();
                ListStore<UserDisplay> myGridStore = (ListStore<UserDisplay>)_grid.getStore();

                _tab.itemsReturnedLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);
                _tab.itemsReturnedLabel.setText(_constants.returnCount(myNewList.size()));
                _tab.itemsReturnedLabel.setVisible(true);

                if (_tab.replaceButton.getValue()) {

                    myGridStore.clear();
                    myGridStore.addAll(myNewList);
                    _userMap.clear();

                    for (UserDisplay myUser : myNewList) {

                        _userMap.put(myUser.getName().toLowerCase(), myUser);
                    }

                } else {

                    for (UserDisplay myUser : myNewList) {

                        String myKey = myUser.getName().toLowerCase();

                        if (_userMap.containsKey(myKey)) {

                            updateUser(_userMap.get(myKey), myUser);

                        } else {

                            myGridStore.add(myUser);
                            _userMap.put(myKey, myUser);
                        }
                    }
                }
                _grid.getView().refresh(false);
                enableSelectionControls(responseIn.getCount());
            }
        }
    };
    
    protected VortexEventHandler<Response<String, List<UserDisplay>>> handleUserRefreshResponse
    = new AbstractVortexEventHandler<Response<String, List<UserDisplay>>>() {
        
        @Override
        public boolean onError(Throwable myException) {
            
            _requestList = null;
            Dialog.showException(myException);
            return true;
        }
        
        @Override
        public void onSuccess(Response<String, List<UserDisplay>> responseIn) {
            
            _requestList = null;

            if (ResponseHandler.isSuccess(responseIn)) {
                
                ListStore<UserDisplay> myGridStore = (ListStore<UserDisplay>)_grid.getStore();
                List<UserDisplay> myNewList = responseIn.getResult();

                for (UserDisplay myUser : myNewList) {
                    
                    updateUser(_userMap.get(myUser.getName().toLowerCase()), myUser);
                }
                _grid.getView().refresh(false);
                
                enableSelectionControls(responseIn.getCount());
            }
        }
    };

    protected VortexEventHandler<Response<String, List<String>>> handleUserDeleteResponse
    = new AbstractVortexEventHandler<Response<String, List<String>>>() {
        
        @Override
        public boolean onError(Throwable myException) {
            
            Dialog.showException(myException);
            return true;
        }
        
        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {
            
            if (ResponseHandler.isSuccess(responseIn)) {
                
                List<String> myList = responseIn.getResult();
                
                //TODO: 
                
                _requestList = null;
                
                enableSelectionControls(responseIn.getCount());
                
            } else {
            
                refreshUsers();
            }
        }
    };

    protected VortexEventHandler<Response<String, List<String>>> handleUserActivateResponse
    = new AbstractVortexEventHandler<Response<String, List<String>>>() {
        
        @Override
        public boolean onError(Throwable myException) {
            
            Dialog.showException(myException);
            return true;
        }
        
        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {
            
            if (ResponseHandler.isSuccess(responseIn)) {

                List<String> myList = responseIn.getResult();
                
                //TODO: 
                
                _requestList = null;

                enableSelectionControls(responseIn.getCount());

            } else {
            
                refreshUsers();
            }
        }
    };

    protected VortexEventHandler<Response<String, List<String>>> handleUserDeactivateResponse
    = new AbstractVortexEventHandler<Response<String, List<String>>>() {
        
        @Override
        public boolean onError(Throwable myException) {
            
            Dialog.showException(myException);
            return true;
        }
        
        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {
            
            if (ResponseHandler.isSuccess(responseIn)) {

                List<String> myList = responseIn.getResult();
                
                //TODO: 
                
                _requestList = null;

                enableSelectionControls(responseIn.getCount());

            } else {
            
                refreshUsers();
            }
        }
    };

    protected VortexEventHandler<Response<String, List<String>>> handleUserRemovalResponse
    = new AbstractVortexEventHandler<Response<String, List<String>>>() {
        
        @Override
        public boolean onError(Throwable myException) {
            
            Dialog.showException(myException);
            return true;
        }
        
        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {
            
            if (ResponseHandler.isSuccess(responseIn)) {

                List<String> myList = responseIn.getResult();
                
                //TODO: 
                
                _requestList = null;

                enableSelectionControls(responseIn.getCount());

            } else {
            
                refreshUsers();
            }
        }
    };

    protected VortexEventHandler<Response<String, List<String>>> handleUserIncludeResponse
    = new AbstractVortexEventHandler<Response<String, List<String>>>() {
        
        @Override
        public boolean onError(Throwable myException) {
            
            Dialog.showException(myException);
            return true;
        }
        
        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {
            
            if (ResponseHandler.isSuccess(responseIn)) {

                List<String> myList = responseIn.getResult();
                
                //TODO: 
                
                _requestList = null;

                enableSelectionControls(responseIn.getCount());

            } else {
            
                refreshUsers();
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

    public UserAdmin(UserTab tabIn, SharedItems sharedIn) {
                
        _tab = tabIn;
        _shared = sharedIn;
        
        disableSelectionControls();
        
        _userMap = new HashMap<String, UserDisplay>();

        _shared.setUserControl(this);

        _info = new UserInfo(_shared.provideSecurity());
        _grid = _info.createGrid();
        _tab.gridContainer.setGrid(_grid);
        CsiTabPanel.fixRadioButtons(new RadioButton[]{_tab.searchCheckBox, _tab.allCheckBox});
        CsiTabPanel.fixRadioButtons(new RadioButton[]{_tab.augmentButton, _tab.replaceButton});
        _tab.augmentButton.setValue(true);
        
        _shared.addDataChangeEventHandler(new DataChangeEventHandler() {
            public void onDataChange(DataChangeEvent eventIn) {

                replaceSharingGroupLists();
                
                if (_shared.provideSecurity()) {
                    
                    replaceSecurityGroupLists();
                }
            }
        });

        _grid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedHandler<UserDisplay>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<UserDisplay> event) {
                enableSelectionControls();
            }
        });

        _tab.editButton.addClickHandler(handleAnyButton(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                List<UserDisplay> myUserList = _grid.getSelectionModel().getSelection();
                UserDisplay myUser = (null != myUserList) ? myUserList.get(0) : null;

                if (null != myUser) {

                   UserInfoPopup myDialog = new UserInfoPopup(_grid.getSelectionModel().getSelection().get(0),
                            _shared, _userSlots);
    
                    myDialog.addDataChangeEventHandler(new DataChangeEventHandler() {
                        public void onDataChange(DataChangeEvent eventIn) {
    
                            updateUser(eventIn.getData());
                        }
                    });
    
                    myDialog.show();
                }
            }
        }));

        _tab.clearButton.addClickHandler(handleAnyButton(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {

                _grid.getStore().clear();
                _userMap.clear();
            }
        }));

        if (_shared.provideSharing()) {
            
            _tab.newButton.addClickHandler(handleAnyButton(new ClickHandler() {
                public void onClick(ClickEvent eventIn) {
                    UserInfoPopup myDialog = new UserInfoPopup(null, _shared, _userSlots);
                    
                    myDialog.addDataChangeEventHandler(new DataChangeEventHandler() {
                        public void onDataChange(DataChangeEvent eventIn) {

                            addUser(eventIn.getData());
                         }
                    });

                    myDialog.show();
                }
            }));
            
            _tab.deleteButton.addClickHandler(handleAnyButton(new ClickHandler() {
                public void onClick(ClickEvent eventIn) {
                    Dialog.showYesNoDialog(_txtDeleteDialogTitle, _txtDeleteDialogDescription, handleDoDeleteClick);
                }
            }));
            
            _tab.activateButton.addClickHandler(handleAnyButton(new ClickHandler() {
                public void onClick(ClickEvent eventIn) {
                    activateUsers();
                }
            }));
            
            _tab.deactivateButton.addClickHandler(handleAnyButton(new ClickHandler() {
                public void onClick(ClickEvent eventIn) {
                    deactivateUsers();
                }
            }));
            
        } else {
            
            _tab.deleteButton.setVisible(false);
            _tab.newButton.setVisible(false);
            _tab.activateButton.setVisible(false);
            _tab.deactivateButton.setVisible(false);
        }
        
        _tab.groupRemoveButton.addClickHandler(handleAnyButton(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                removeUsersFromGroup(_tab.sharingCheckBox.getValue() ? GroupType.SHARING : GroupType.SECURITY);
            }
        }));
        
        _tab.groupAddButton.addClickHandler(handleAnyButton(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                addUsersToGroup(_tab.sharingCheckBox.getValue() ? GroupType.SHARING : GroupType.SECURITY);
            }
        }));

        _tab.sharingListBox.addSelectionChangedHandler(new SelectionChangedHandler<String>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                _groupMenuId = _tab.sharingListBox.getSelectedIndex();
                if (0 < _groupMenuId) {
                    
                    _sharingSelection = _tab.sharingListBox.getItemText(_groupMenuId);
                }
                else {
                    
                    _sharingSelection = null;
                }
                enableSelectionControls();
            }
        });
        
        _tab.sharingFilterListBox.addSelectionChangedHandler(new SelectionChangedHandler<String>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                _filterMenuId = _tab.sharingFilterListBox.getSelectedIndex();
                if (0 < _filterMenuId) {
                    
                    _sharingFilterSelection = _tab.sharingFilterListBox.getItemText(_filterMenuId);
                }
                else {
                    
                    _sharingFilterSelection = null;
                }
                
            }
        });
        
        if (_shared.provideSecurity()) {
            
            _tab.securityListBox.addSelectionChangedHandler(new SelectionChangedHandler<String>() {

                @Override
                public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                    _groupMenuId = _tab.securityListBox.getSelectedIndex();
                    if (0 < _groupMenuId) {
                        
                        _securitySelection = _tab.securityListBox.getItemText(_groupMenuId);
                    }
                    else {
                        
                        _securitySelection = null;
                    }
                    enableSelectionControls();
                }
            });
            
            _tab.securityFilterListBox.addSelectionChangedHandler(new SelectionChangedHandler<String>() {

                @Override
                public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

                    _filterMenuId = _tab.securityFilterListBox.getSelectedIndex();
                    if (0 < _filterMenuId) {
                        
                        _securityFilterSelection = _tab.securityFilterListBox.getItemText(_filterMenuId);
                    }
                    else {
                        
                        _securityFilterSelection = null;
                    }
                    
                }
            });
        }
    }
    
    private void refreshUsers() {
 
        if (null != _requestList) {
            
            // Request data from the server
            
            try {
                VortexFuture<Response<String, List<UserDisplay>>> vortexFuture = WebMain.injector.getVortex().createFuture();
                
                vortexFuture.execute(UserAdministrationServiceProtocol.class).retrieveUsers(_requestList);
                vortexFuture.addEventHandler(handleUserRefreshResponse);

            } catch (Exception myException) {
                
                Dialog.showException(myException);
            }
        }
    }
    
    public void enableSelectionControls(long slotsIn) {
        
        _userSlots = (int)slotsIn;
        
        enableSelectionControls();
    }

    public void enableSelectionControls() {

        int mySelectionCount = countInactive();

        if ((mySelectionCount != _selectionCount) || (groupSelectionMade() != _groupSelectionMade)) {

            _selectionCount = mySelectionCount;
            _groupSelectionMade = groupSelectionMade();

            if (0 == _selectionCount) {

                _editButtonEnabled = false;
                _otherButtonsEnabled = false;
                _groupButtonsEnabled = false;
            }
            else if (1 == _selectionCount) {

                _editButtonEnabled = true;
                _otherButtonsEnabled = true;
                _groupButtonsEnabled = _groupSelectionMade;
            }
            else {

                _editButtonEnabled = false;
                _otherButtonsEnabled = true;
                _groupButtonsEnabled = _groupSelectionMade;
            }
            enableControls();
        }
        /*
        if ((1 == mySelectionCount) || (null != _sharingSelection) || (null != _securitySelection)) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    enableSelectionControls();
                }
            });
        }
        */
    }
    
    public void disableSelectionControls() {
        _inputEnabled = false;
        _tab.newButton.setEnabled(false);
        _tab.editButton.setEnabled(false);
        _tab.deleteButton.setEnabled(false);
        _tab.activateButton.setEnabled(false);
        _tab.deactivateButton.setEnabled(false);
        _tab.groupAddButton.setEnabled(false);
        _tab.groupRemoveButton.setEnabled(false);
        _tab.disableControls();
    }

    private void addUser(Object dataIn) {
        
        UserDisplay myUser = (UserDisplay)dataIn;
        
        if (null != myUser) {
            
            VortexFuture<Response<String, UserDisplay>> myVortexFuture = WebMain.injector.getVortex().createFuture();
            
            _shared._roleMap.put(myUser.getName().toLowerCase(), false);
            myUser.setId(_shared.getBogusId());
            ((ListStore<UserDisplay>)_grid.getStore()).add(myUser);
            _userMap.put(myUser.getName().toLowerCase(), myUser);
            
            try {

                myVortexFuture.execute(UserAdministrationServiceProtocol.class).createUser(myUser);
                myVortexFuture.addEventHandler(handleCreateOrUpdateResponse);

            } catch (Exception myException) {
                
                Dialog.showException(myException);
            }
        }
    }
    
    private void updateUser(Object dataIn) {
        
        UserDisplay myUser = (UserDisplay)dataIn;
        
        if (null != myUser) {
            
            VortexFuture<Response<String, UserDisplay>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            ((ListStore<UserDisplay>)_grid.getStore()).update(myUser);

            try {
                myVortexFuture.execute(UserAdministrationServiceProtocol.class).updateUser(myUser);
                myVortexFuture.addEventHandler(handleCreateOrUpdateResponse);

            } catch (Exception myException) {
                
                Dialog.showException(myException);
            }
        }
    }
    
    private void deleteUsers() {
        
        List<String> myList = new ArrayList<String>();
        List<UserDisplay> myUserList = _grid.getSelectionModel().getSelection();
        
        if ((null != myUserList) && (0 < myUserList.size())) {
            
            for (UserDisplay myUser : myUserList) {
                
                String myUsername = myUser.getName();
                
                if ((myUsername.equalsIgnoreCase(_shared._adminUser))
                        || (myUsername.equalsIgnoreCase(_shared._securityUser))
                        || (myUsername.equalsIgnoreCase(_shared._currentUser))) {
                    
                    if (1 < myUserList.size()) {
                        
                        Dialog.showProblem(_constants.administrationDialogs_CannotDeleteUser2(myUsername));
                        
                    } else {
                        
                        Dialog.showProblem(_constants.administrationDialogs_CannotDeleteUser1(myUsername));
                    }
                    
                } else {
                    
                    _shared._roleMap.remove(myUsername);
                        
                    ((ListStore<UserDisplay>)_grid.getStore()).remove(myUser);
                    _userMap.remove(myUser.getName().toLowerCase());
                    
                    myList.add(myUsername);
                }
            }

            if (0 < myList.size()) {
                
                _requestList = myList;

                try {
                    VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.execute(UserAdministrationServiceProtocol.class).deleteUsers(myList);
                    
                    vortexFuture.addEventHandler(handleUserDeleteResponse);

                } catch (Exception myException) {
                    
                    Dialog.showException(myException);
                }
            }
        }
    }
    
    private void activateUsers() {
        
        List<String> myList = new ArrayList<String>();
        List<UserDisplay> myUserList = _grid.getSelectionModel().getSelection();

        if ((null != myUserList) && (0 < myUserList.size())) {
            
            for (UserDisplay myUser : myUserList) {
                
                myUser.setDisabled(false);
                ((ListStore<UserDisplay>)_grid.getStore()).update(myUser);
                
                myList.add(myUser.getName());
            }

            if (0 < myList.size()) {
                
                _requestList = myList;

                try {
                    VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.execute(UserAdministrationServiceProtocol.class).activateUsers(myList);
                    
                    vortexFuture.addEventHandler(handleUserActivateResponse);

                } catch (Exception myException) {
                    
                    Dialog.showException(myException);
                }
            }
        }
    }
    
    private void deactivateUsers() {
        
        List<String> myList = new ArrayList<String>();
        List<UserDisplay> myUserList = _grid.getSelectionModel().getSelection();

        if ((null != myUserList) && (0 < myUserList.size())) {
            
            for (UserDisplay myUser : myUserList) {
                
                String myUsername = myUser.getName();
                
                if (myUsername.equalsIgnoreCase(_shared._adminUser)
                        || myUsername.equalsIgnoreCase(_shared._securityUser)
                        || myUsername.equalsIgnoreCase(_shared._currentUser)) {
                    
                    if (1 < myUserList.size()) {
                        
                        Dialog.showProblem(_constants.administrationDialogs_CannotDeactivateUser2(myUsername));
                        
                    } else {
                        
                        Dialog.showProblem(_constants.administrationDialogs_CannotDeactivateUser1(myUsername));
                    }
                
                } else {
                    
                    myUser.setDisabled(true);
                    ((ListStore<UserDisplay>)_grid.getStore()).update(myUser);
                    
                    myList.add(myUser.getName());
                }
            }

            if (0 < myList.size()) {
                
                _requestList = myList;

                try {
                    VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.execute(UserAdministrationServiceProtocol.class).deactivateUsers(myList);
                    
                    vortexFuture.addEventHandler(handleUserDeactivateResponse);

                } catch (Exception myException) {
                    
                    Dialog.showException(myException);
                }
            }
        }
    }
    
    private void removeUsersFromGroup(GroupType typeIn) {
        
        List<String> myList = new ArrayList<String>();
        List<UserDisplay> myUserList = _grid.getSelectionModel().getSelection();

        if ((null != myUserList) && (0 < myUserList.size())) {
            
            ListStore<UserDisplay> myListStore = (ListStore<UserDisplay>)_grid.getStore();
            String myGroupSelection = GroupType.SECURITY.equals(typeIn) ? _securitySelection : _sharingSelection;
            boolean myCheckAdmin = myGroupSelection.equalsIgnoreCase(_shared._adminGroup);
            boolean myCheckSecurity = myGroupSelection.equalsIgnoreCase(_shared._securityGroup);

            for (UserDisplay myUser : myUserList) {
                
                String myUsername = myUser.getName();
                
                if ((myCheckAdmin && myUsername.equalsIgnoreCase(_shared._adminUser))
                        || (myCheckSecurity && myUsername.equalsIgnoreCase(_shared._securityUser))
                        || myUsername.equalsIgnoreCase(_shared._currentUser)) {
                    
                    if (1 < myUserList.size()) {
                        
                        Dialog.showProblem(_constants.administrationDialogs_CannotRemoveUserFromGroup2(myUsername, myGroupSelection));
                        
                    } else {
                        
                        Dialog.showProblem(_constants.administrationDialogs_CannotRemoveUserFromGroup1(myUsername, myGroupSelection));
                    }
                    
                } else {

                    if (removeGroupFromRowItem(typeIn, myUser, myGroupSelection)) {
                        
                        myListStore.update(myUser);
                        myList.add(myUser.getName());
                    }
                }
            }

            if (0 < myList.size()) {
                
                _requestList = myList;

                try {
                    VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.execute(UserAdministrationServiceProtocol.class).removeUsersFromGroup(myGroupSelection, myList);
                    
                    vortexFuture.addEventHandler(handleUserRemovalResponse);

                } catch (Exception myException) {
                    
                    Dialog.showException(myException);
                }
            }
        }
        resetSelection();
    }
    
    private void addUsersToGroup(GroupType typeIn) {
        
        List<String> myList = new ArrayList<String>();
        List<UserDisplay> myUserList = _grid.getSelectionModel().getSelection();

        if ((null != myUserList) && (0 < myUserList.size())) {
            
            ListStore<UserDisplay> myListStore = (ListStore<UserDisplay>)_grid.getStore();
            String myGroupSelection = GroupType.SECURITY.equals(typeIn) ? _securitySelection : _sharingSelection;
            boolean myCheckAdmin = !myGroupSelection.equalsIgnoreCase(_shared._adminGroup);
            boolean myCheckSecurity = !myGroupSelection.equalsIgnoreCase(_shared._securityGroup);

            for (UserDisplay myUser : myUserList) {
                
                String myUsername = myUser.getName();
                
                if ((myCheckAdmin && myUsername.equalsIgnoreCase(_shared._adminUser))
                        || (myCheckSecurity && myUsername.equalsIgnoreCase(_shared._securityUser))) {
                    
                    if (1 < myUserList.size()) {
                        
                        Dialog.showProblem(_constants.administrationDialogs_CannotAddUserToGroup2(myUsername, myGroupSelection));
                        
                    } else {
                        
                        Dialog.showProblem(_constants.administrationDialogs_CannotAddUserToGroup1(myUsername, myGroupSelection));
                    }
                    
                } else {

                    if (addGroupToRowItem(typeIn, myUser, myGroupSelection)) {
                        
                        myListStore.update(myUser);
                        myList.add(myUser.getName());
                    }
                }
            }

            if (0 < myList.size()) {
                
                _requestList = myList;

                try {
                    VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.execute(UserAdministrationServiceProtocol.class).addUsersToGroup(myGroupSelection, myList);
                    
                    vortexFuture.addEventHandler(handleUserIncludeResponse);

                } catch (Exception myException) {
                    
                    Dialog.showException(myException);
                }
            }
        }
        resetSelection();
    }
    
    void requestData(String searchStringIn, String sharingFilterIn, String securityFilterIn) {

        _requestSent = true;

        try {

            List<Boolean> myUserFlags = (null != _tab) ? _tab.getUserFlags() : null;
            VortexFuture<Response<String, List<UserDisplay>>> vortexFuture = WebMain.injector.getVortex().createFuture();
            if (((null != sharingFilterIn) && (0 < sharingFilterIn.length()))
                    || ((null != securityFilterIn) && (0 < securityFilterIn.length()))) {

                vortexFuture.execute(UserAdministrationServiceProtocol.class).searchGroupUsers(searchStringIn,
                                                                        sharingFilterIn, securityFilterIn, myUserFlags);
            }
            else {

                vortexFuture.execute(UserAdministrationServiceProtocol.class).searchUsers(searchStringIn, myUserFlags);
            }
            
            vortexFuture.addEventHandler(handleUserSearchResponse);

        } catch (Exception myException) {
            
            Dialog.showException(myException);
        }
    }
/*
    private void refreshData() {
        
        if (_requestSent) {
            
            requestData(_tab.getRefreshString(), _priorSharingFilter, _priorSecurityFilter);
        }
        _shared.refreshGroups();
    }
*/
    void requestData(String searchStringIn) {
        
        _priorSharingFilter = _sharingFilterSelection;
        _priorSecurityFilter = _securityFilterSelection;
        requestData(searchStringIn, _sharingFilterSelection, _securityFilterSelection);
    }

    private void replaceSharingGroupLists() {

        Collection<StringEntry> mySharingList = _shared.getGroupList(GroupType.SHARING);
        Map<String, StringEntry> myGoneSharingGroups = _shared.getDeletedMap(GroupType.SHARING);
        
        _tab.sharingListBox.clear();
        _tab.sharingFilterListBox.clear();
        
        _tab.sharingListBox.addItem(SharingAdmin.NO_GROUP_SELECTED, DisplayMode.COMPONENT);
        _tab.sharingFilterListBox.addItem(SharingAdmin.ALL_GROUPS);
        
        if ((null != mySharingList) && (0 < mySharingList.size())) {
        
            if (_shared.provideSharing()) {
            
                for (StringEntry myGroup : mySharingList) {
                    
                    String myGroupName = myGroup.getDisplayString();
                    
                    if (_shared.provideSecurity()
                            || ((!_shared._securityGroup.equalsIgnoreCase(myGroupName))
                                && (!_shared._originatorGroup.equalsIgnoreCase(myGroupName)))) {
                        
                        _tab.sharingListBox.addItem(myGroupName);
                    }
                    _tab.sharingFilterListBox.addItem(myGroupName);
                }
                
            } else if (_shared.provideSecurity()) {
                
                for (StringEntry myGroup : mySharingList) {
                    
                    String myGroupName = myGroup.getDisplayString();
                    
                    _tab.sharingFilterListBox.addItem(myGroupName);
                }

                _tab.sharingListBox.addItem(_shared._securityGroup);
                _tab.sharingListBox.addItem(_shared._originatorGroup);
            }
        }
        
        if ((null != myGoneSharingGroups) && (0 < myGoneSharingGroups.size())) {
            
            ListStore<UserDisplay> myListStore = _grid.getStore();
            
            for (int i = 0; myListStore.size() > i; i++) {
                
                boolean myChange = false;
                UserDisplay myRowData = myListStore.get(i);
                String myGroupString = myRowData.getGroups();
                String[] myGroupNames = myGroupString.split(", ");
                
                for (int j = 0; myGroupNames.length > j; j++) {
                    
                    String myGroupName = myGroupNames[j];
                    
                    if ((null != myGroupName) && (0 < myGroupName.length())) {
                        
                        if (myGoneSharingGroups.containsKey(myGroupName)) {
                            
                            myChange = true;
                            myGroupNames[j] = null;
                        }
                    }
                }
                if (myChange) {
                    
                    myRowData.setGroups(_shared.formatList(myGroupNames, null, null));
                    myListStore.update(myRowData);
                }
            }
        }
    }

    private void replaceSecurityGroupLists() {

        Collection<StringEntry> mySecurityList = _shared.getGroupList(GroupType.SECURITY);
        Map<String, StringEntry> myGoneSecurityGroups = _shared.getDeletedMap(GroupType.SECURITY);
        
        _tab.securityListBox.clear();
        _tab.securityFilterListBox.clear();
        
        _tab.securityListBox.addItem(SecurityAdmin.NO_GROUP_SELECTED, DisplayMode.COMPONENT);
        _tab.securityFilterListBox.addItem(SecurityAdmin.ALL_GROUPS);
        
        if ((null != mySecurityList) && (0 < mySecurityList.size())) {
            
            for (StringEntry myGroup : mySecurityList) {
                
                _tab.securityListBox.addItem(myGroup.getDisplayString());
                _tab.securityFilterListBox.addItem(myGroup.getDisplayString());
            }
        }
        
        if ((null != myGoneSecurityGroups) && (0 < myGoneSecurityGroups.size())) {
            
            ListStore<UserDisplay> myListStore = _grid.getStore();
            
            for (int i = 0; myListStore.size() > i; i++) {
                
                boolean myChange = false;
                UserDisplay myRowData = myListStore.get(i);
                String myGroupString = myRowData.getGroups();
                String[] myGroupNames = myGroupString.split(", ");
                
                for (int j = 0; myGroupNames.length > j; j++) {
                    
                    String myGroupName = myGroupNames[j];
                    
                    if ((null != myGroupName) && (0 < myGroupName.length())) {
                        
                        if (myGoneSecurityGroups.containsKey(myGroupName)) {
                            
                            myChange = true;
                            myGroupNames[j] = null;
                        }
                    }
                }
                if (myChange) {
                    
                    myRowData.setGroups(_shared.formatList(myGroupNames, null, null));
                    myListStore.update(myRowData);
                }
            }
        }
    }
    
    private boolean removeGroupFromRowItem(GroupType typeIn, UserDisplay myRowData, String groupNameIn) {
        
        boolean myChange = false;
        String myGroupString = GroupType.SECURITY.equals(typeIn) ? myRowData.getClearance() : myRowData.getGroups();
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
            
            if (GroupType.SECURITY.equals(typeIn)) {
                
                myRowData.setClearance(_shared.formatList(myGroupNames, null, null));
                
            } else {
                
                myRowData.setGroups(_shared.formatList(myGroupNames, null, null));
            }
            myChange = true;
        }
        return myChange;
    }
    
    private boolean addGroupToRowItem(GroupType typeIn, UserDisplay myRowData, String groupNameIn) {
        
        boolean myChange = false;
        String myGroupString = GroupType.SECURITY.equals(typeIn) ? myRowData.getClearance() : myRowData.getGroups();
        String[] myGroupNames = myGroupString.split(", ");

        int i;
        
        for (i = 0; myGroupNames.length > i; i++) {
            
            String myGroupName = myGroupNames[i];
            
            if ((null != myGroupName) && (0 < myGroupName.length())) {
                
                if (groupNameIn.equals(myGroupName)) {
                    
                    break;
                }
            }
        }
        if (myGroupNames.length == i) {
            
            if (GroupType.SECURITY.equals(typeIn)) {
                
                myRowData.setClearance(_shared.formatList(myGroupNames, null, groupNameIn));
                
            } else {
                
                myRowData.setGroups(_shared.formatList(myGroupNames, null, groupNameIn));
            }
            myChange = true;
        }
        return myChange;
    }

    private void enableControls() {

        countInactive();

        _inputEnabled = true;
        _tab.newButton.setEnabled((0 < _userSlots));
        _tab.editButton.setEnabled(_editButtonEnabled);
        _tab.deleteButton.setEnabled(_otherButtonsEnabled);
        _tab.activateButton.setEnabled(_otherButtonsEnabled && (0 < _userSlots) && (_requestedSlots <= _userSlots));
        _tab.deactivateButton.setEnabled(_otherButtonsEnabled);
        _tab.groupAddButton.setEnabled(_groupButtonsEnabled);
        _tab.groupRemoveButton.setEnabled(_groupButtonsEnabled);
        _tab.enableControls();
    }
    
    private UserDisplay replaceInfo(UserDisplay newInfoIn) {
        
        UserDisplay myUser = _userMap.get(newInfoIn.getName().toLowerCase());
        
        return myUser.copyFrom(newInfoIn);
    }
    
    private int countInactive() {

        List<UserDisplay> myUserList = _grid.getSelectionModel().getSelection();
        int mySelectionCount = (null != myUserList) ? myUserList.size() : 0;

        _requestedSlots = 0;
        mySelectionCount = 0;
        
        if (null != myUserList) {
            
            mySelectionCount = myUserList.size();;
            
            for (UserDisplay myUser : myUserList) {
                
                if (myUser.getDisabled()) {
                    
                    _requestedSlots++;
                }
            }
        }
        return mySelectionCount;
    }
    
    private void updateUser(UserDisplay oldUserIn, UserDisplay newUserIn) {
        
        if ((null != oldUserIn) && (null != newUserIn)) {
            
            oldUserIn.copyFrom(newUserIn);
        }
    }
    
    private boolean groupSelectionMade() {
        
        return ((null != _sharingSelection) && _tab.sharingCheckBox.getValue())
                || ((null != _securitySelection) && _tab.securityCheckBox.getValue());
    }

    private void resetSelection() {

        resetSharingSelection();
        resetSecuritySelection();
    }

    private void resetSharingSelection() {

        _sharingSelection = null;
        _tab.resetSharingList();
    }

    private void resetSecuritySelection() {

        _securitySelection = null;
        _tab.resetSecurityList();
        enableSelectionControls();
    }
}
