/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.csi_resource.filters;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.RenameResourceDialog;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.DialogInfoTextArea;
import csi.client.gwt.widget.boot.KnowsParent;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.buttons.MiniBlueButton;
import csi.client.gwt.widget.buttons.MiniRedButton;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.OptionBasics;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.service.api.UserAdministrationServiceProtocol;
import csi.server.common.util.StringUtil;
import csi.server.common.util.ValuePair;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ResourceFilterListDialog extends WatchingParent {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<Dialog, ResourceFilterListDialog> {
    }

    interface ResourceFilterPropertyAccess extends PropertyAccess<OptionBasics> {

        @Editor.Path("filter.id")
        public ModelKeyProvider<OptionBasics> key();

        public ValueProvider<OptionBasics, String> name();

        public ValueProvider<OptionBasics, String> remarks();

        public ValueProvider<OptionBasics, Boolean> defaultOption();

    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Static Values                                      //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtDialogTitle = _constants.resourceFilterListDialog_Title();
    private static final String _txtHelpTarget = _constants.resourceFilterListDialog_HelpTarget();
    private static final String _txtDeleteTitle = _constants.resourceFilterListDialog_ConfirmDeleteTitle();
    private static final String _txtRenameTitle = _constants.resourceFilterListDialog_RenameResourceFilter();
    private static final String _txtResourceFilterSingular = _constants.resourceFilterListDialog_ResourceFilterSingular();
    private static final String _txtResourceFilterPlural = _constants.resourceFilterListDialog_ResourceFilterPlural();
    private static final String _txtConfirmDelete = _constants.resourceFilterListDialog_ConfirmDeletePrompt();

    private static ResourceFilterPropertyAccess propertyAccess = GWT.create(ResourceFilterPropertyAccess.class);
    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField(provided = true)
    String renameButton = Dialog.txtRenameButton; //$NON-NLS-1$
    @UiField(provided = true)
    String addButton = Dialog.txtAddButton; //$NON-NLS-1$
    @UiField(provided = true)
    String editButton = Dialog.txtEditButton; //$NON-NLS-1$
    @UiField(provided = true)
    String deleteButton = Dialog.txtDeleteButton; //$NON-NLS-1$

    @UiField
    Dialog dialog;
    @UiField
    GridContainer gridContainer;
    @UiField
    MiniBlueButton buttonRename;
    @UiField
    MiniBlueButton buttonAdd;
    @UiField
    MiniBlueButton buttonEdit;
    @UiField
    MiniRedButton buttonDelete;
    @UiField
    DialogInfoTextArea instructionTextArea;

    private Grid<OptionBasics> grid;
    private KnowsParent activeDialog = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private FilterGridInfo _info;
    private ResourceFilter _shadowResource = null;
    private ResourceFilter _activeResource = null;
    private boolean _keepHidden = false;
    private List<OptionBasics> _filterList;
    private List<OptionBasics> _selectionList;
    private OptionBasics _selection = null;
    private boolean _monitoring = false;
    private boolean _vissible = false;
    private List<StringEntry> _allUsers = null;
    private List<StringEntry> _allGroups = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ChoiceMadeEventHandler defaultSelectionHandler = new ChoiceMadeEventHandler() {
        @Override
        public void onChoiceMade(ChoiceMadeEvent eventIn) {

            final ListStore<OptionBasics> myDisplayList = grid.getStore();
            final int myLimit = (null != myDisplayList) ? myDisplayList.size() : 0;
            final int myChoice = eventIn.getChoice();
            OptionBasics myDisplay = (myLimit > myChoice) ? myDisplayList.get(myChoice) : null;
            ResourceFilter myFilter = (null != myDisplay) ? getResourceFilter(myDisplay) : null;
            Object myData = eventIn.getData();
            boolean myValue = ((null != myData) && (myData instanceof Boolean)) ? (Boolean)myData : false;

            if (null != myFilter) {

                grid.getSelectionModel().deselectAll();
//                myDisplay.setDefaultOption(myValue);
                myFilter.setDefaultFilter(myValue);
                addUpdateResourceFilter(myFilter);
/*
                if (myValue && (1 < myLimit)) {

                    for (int i = 0; myLimit > i; i++) {

                        if (myChoice != i) {

                            myDisplay = myDisplayList.get(i);

                            if ((null != myDisplay) && myDisplay.getDefaultOption()) {

//                                myDisplay.setDefaultOption(false);
                                myFilter = getResourceFilter(myDisplay);
                                if (null != myFilter) {

                                    myFilter.setDefaultFilter(false);
                                    addUpdateResourceFilter(myFilter);
                                }
                            }
                        }
                    }
                }
*/
            }
            DeferredCommand.add(new Command() {
                public void execute() {
                    grid.getView().refresh(false);
                }
            });
        }
    };

    private ClickHandler handleDialogExit = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {

            _monitoring = false;
            DeferredCommand.add(new Command() {
                public void execute() {
                    hide();
                }
            });
        }
    };

    private ClickHandler handleRenameCancel = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                displayResults();

            } catch (Exception myException) {

                Display.error("ResourceFilterListDialog", 1, myException);
            }
        }
    };

    private ClickHandler handleFilterRename = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                if ((null != activeDialog) && (null != _activeResource)) {

                    _activeResource.setName(((RenameResourceDialog)activeDialog).getName());
                    _activeResource.setRemarks(((RenameResourceDialog)activeDialog).getRemarks());
                    addUpdateResourceFilter(_activeResource);
                }

            } catch (Exception myException) {

                show();
                Display.error("ResourceFilterListDialog", 2, myException);
            }
        }
    };

    private ClickHandler handleCancelCreation = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                displayResults();

            } catch (Exception myException) {

                Display.error("ResourceFilterListDialog", 3, myException);
            }
        }
    };

    private ClickHandler handleFilterCreation = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                launchRenameDialog();

            } catch (Exception myException) {

                _keepHidden = false;
                show();
                Display.error("ResourceFilterListDialog", 4, myException);
            }
        }
    };

    private ClickHandler handleCancelUpdate = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                displayResults();

            } catch (Exception myException) {

                Display.error("ResourceFilterListDialog", 5, myException);
            }
        }
    };

    private ClickHandler handleFilterUpdate = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                if (null != _shadowResource) {

                    addUpdateResourceFilter(_shadowResource);
                }

            } catch (Exception myException) {

                show();
                Display.error("ResourceFilterListDialog", 6, myException);
            }
        }
    };

    private ClickHandler handleFilterDestruction = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {

            try {

                if ((null != _selectionList) && (0 < _selectionList.size())) {

                    deleteResourceFilters(_selectionList);
                }

            } catch (Exception myException) {

                Display.error("ResourceFilterListDialog", 7, myException);
            }
        }
    };

    private ClickHandler handleRenameButtonClick = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                _monitoring = false;
                _activeResource = getResourceFilter(grid.getSelectionModel().getSelectedItem());

                launchRenameDialog();
                _monitoring = false;

            } catch (Exception myException) {

                Display.error("ResourceFilterListDialog", 8, myException);
            }
        }
    };

    private ClickHandler handleAddButtonClick = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                _activeResource = new ResourceFilter(true);
                _keepHidden = true;

                activeDialog = new ResourceFilterDialog(_activeResource, _allUsers, _allGroups,
                                                        handleFilterCreation, handleCancelCreation, false);
                activeDialog.show(_this);
                _monitoring = false;

            } catch (Exception myException) {

                _keepHidden = false;
                Display.error("ResourceFilterListDialog", 9, myException);
            }
        }
    };

    private ClickHandler handleEditButtonClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {

            try {

                _activeResource = getResourceFilter(grid.getSelectionModel().getSelectedItem());

                if (null != _activeResource) {

                    _shadowResource = _activeResource.clone();
                    activeDialog = new ResourceFilterDialog(_shadowResource, _allUsers, _allGroups,
                                                            handleFilterUpdate, handleCancelUpdate, false);
                    activeDialog.show(_this);
                    _monitoring = false;
                }

            } catch (Exception myException) {

                Display.error("ResourceFilterListDialog", 10, myException);
            }
        }
    };

    private ClickHandler handleDeleteButtonClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                _selectionList = grid.getSelectionModel().getSelectedItems();

                if ((null != _selectionList) && (0 < _selectionList.size())) {

                    WarningDialog myDialog = new WarningDialog(_txtDeleteTitle, _txtConfirmDelete);

                    myDialog.addClickHandler(handleFilterDestruction);
                    myDialog.show();
                    _monitoring = false;
                }

            } catch (Exception myException) {

                Display.error("ResourceFilterListDialog", 11, myException);
            }
        }
    };

    private VortexEventHandler<Response<String, ValuePair<List<String>, List<String>>>> handleRoleListResponse
            = new AbstractVortexEventHandler<Response<String, ValuePair<List<String>, List<String>>>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                hideWatchBox();

                // Display error message.
                Display.error(exceptionIn);

            } catch (Exception myException) {

                Display.error("ResourceFilterListDialog", 12, myException);
            }
            return false;
        }

        @Override
        public void onSuccess(Response<String, ValuePair<List<String>, List<String>>> responseIn) {

            try {

                hideWatchBox();
                if (ResponseHandler.isSuccess(responseIn)) {

                    ValuePair<List<String>, List<String>> myResponse = responseIn.getResult();

                    if (null != myResponse) {

                        List<String> myUsers = myResponse.getValue1();
                        List<String> myGroups = myResponse.getValue2();

                        _allUsers = (List<StringEntry>) StringUtil.buildDisplayList(new ArrayList<StringEntry>(), myUsers);
                        _allGroups = (List<StringEntry>)StringUtil.buildDisplayList(new ArrayList<StringEntry>(), myGroups);

                    } else {

                        ResponseHandler.displayError(responseIn);
                    }
                }

            } catch (Exception myException) {

                Display.error("ResourceFilterListDialog", 13, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ResourceFilterListDialog() {

        super();

        WebMain.injector.getMainPresenter().getResourceFilterDisplayList(filterListCallBack);
        retrieveUsersAndGroups();

        uiBinder.createAndBindUi(this);
        dialog.defineHeader(_txtDialogTitle, _txtHelpTarget, true);
//        dialog.hideOnCancel();
//        dialog.hideOnAction();
        dialog.getActionButton().setVisible(false);
        dialog.getCancelButton().setText(Dialog.txtExitButton);
        instructionTextArea.setText("");
        initGrid();
        addHandlers();
    }

    public void show() {

        if (!_vissible) {

            if (!_keepHidden) {

                dialog.show(60);
                _vissible = true;
            }
            _keepHidden = false;
        }
    }

    public void hide() {

        if (_vissible) {

            dialog.hide();
            _vissible = false;
        }
    }

    @SuppressWarnings("unchecked")
    private void initGrid() {

        _info = new FilterGridInfo(defaultSelectionHandler);
        grid = _info.createGrid();
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.MULTI);
        grid.setBorders(true);
        gridContainer.setGrid(grid);
    }

    private void addHandlers() {

        buttonRename.addClickHandler(handleRenameButtonClick);
        buttonAdd.addClickHandler(handleAddButtonClick);
        buttonEdit.addClickHandler(handleEditButtonClick);
        buttonDelete.addClickHandler(handleDeleteButtonClick);
        dialog.getCancelButton().addClickHandler(handleDialogExit);
    }

    private void enableDisableButtons() {

        List<OptionBasics> mySelection = grid.getSelectionModel().getSelectedItems();

        if ((null != mySelection) && (0 < mySelection.size())) {

            if (1 == mySelection.size()) {

                buttonRename.setEnabled(true);
                buttonEdit.setEnabled(true);

            } else {

                buttonRename.setEnabled(false);
                buttonEdit.setEnabled(false);
            }
            buttonDelete.setEnabled(true);

        } else {

            buttonRename.setEnabled(false);
            buttonEdit.setEnabled(false);
            buttonDelete.setEnabled(false);
        }
        if (_monitoring) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    enableDisableButtons();
                }
            });
        }
    }
    private void finalizeChange() {

        _activeResource = null;
        _shadowResource = null;
        _selection = null;
        _selectionList = null;
        activeDialog = null;

        _monitoring = true;
        enableDisableButtons();
    }

    private void getResourceFilters() {

        WebMain.injector.getMainPresenter().getResourceFilterDisplayList(filterListCallBack);
    }

    private ResourceFilter getResourceFilter(OptionBasics selectionIn) {

        return WebMain.injector.getMainPresenter().getResourceFilter(selectionIn);
    }

    private void addUpdateResourceFilter(ResourceFilter resourceFilterIn) {

        WebMain.injector.getMainPresenter().addReplaceResourceFilter(resourceFilterIn, filterListCallBack);
    }

    private void deleteResourceFilters(List<OptionBasics> listIn) {

        if ((null != listIn) && (0 < listIn.size())) {

            List<Long> myIdList = new ArrayList<>();

            for (OptionBasics myItem : listIn) {

                if (null != myItem) {

                    ResourceFilter myFilter = getResourceFilter(myItem);

                    if (null != myFilter) {

                        myIdList.add(myFilter.getId());
                    }
                }
            }
            if (0 < myIdList.size()) {

                        WebMain.injector.getMainPresenter().deleteResourceFilters(myIdList, filterListCallBack);
            }
        }
    }

    private void launchRenameDialog() {

        if (null != _activeResource) {

            String myName = _activeResource.getName();

            activeDialog = new RenameResourceDialog(_activeResource.getName(), _txtRenameTitle,
                    _txtResourceFilterSingular, _txtResourceFilterPlural, handleFilterRename,
                    handleRenameCancel, _filterList);
            activeDialog.show(_this);
        }
    }

    private Callback<List<OptionBasics>> filterListCallBack = new Callback<List<OptionBasics>>() {

        @Override
        public void onSuccess(List<OptionBasics> filterListIn) {

            try {

                _filterList = filterListIn;
                displayResults();

            } catch (Exception myException) {

                Display.error("ResourceFilterListDialog", 14, myException);
            }
        }
    };

    private void loadGrid() {

        grid.getStore().clear();
        if (null != _filterList) {

            for (OptionBasics myFilter : _filterList) {
                grid.getStore().add(myFilter);
            }
        }
    }

    private void displayResults() {

        _keepHidden = false;
        show();
        loadGrid();
        finalizeChange();
        grid.getView().refresh(true);
    }

    private void retrieveUsersAndGroups() {

        showWatchBox();
        UserSecurityInfo myUserInfo = WebMain.injector.getMainPresenter().getUserInfo();
        VortexFuture<Response<String, ValuePair<List<String>, List<String>>>> myVortexFuture
                = WebMain.injector.getVortex().createFuture();
        try {

            myVortexFuture.addEventHandler(handleRoleListResponse);
            if (myUserInfo.isAdmin() || myUserInfo.isSecurity()) {

                myVortexFuture.execute(UserAdministrationServiceProtocol.class).listAllUsersAndGroups("Filter");

            } else {

                myVortexFuture.execute(UserAdministrationServiceProtocol.class).listActiveUsersAndGroups("Filter");
            }

        } catch (Exception myException) {

            hideWatchBox();
            Display.error("ResourceFilterListDialog", 15, myException);
        }
    }
}
