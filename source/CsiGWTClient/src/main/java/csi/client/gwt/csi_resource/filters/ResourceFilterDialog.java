package csi.client.gwt.csi_resource.filters;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.RenameResourceDialog;
import csi.client.gwt.csiwizard.panels.WizardTabPanel;
import csi.client.gwt.csiwizard.widgets.*;
import csi.client.gwt.csiwizard.widgets.CsiTabWidget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.*;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.input_boxes.FilteredTextBox;
import csi.client.gwt.widget.input_boxes.ValidityCheck;
import csi.client.gwt.widget.list_boxes.ResourceFilterListBox;
import csi.client.gwt.widget.list_boxes.SortListBox;
import csi.client.gwt.widget.list_boxes.SortingSet;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.OptionBasics;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.ResourceSortMode;
import csi.server.common.service.api.UserAdministrationServiceProtocol;
import csi.server.common.util.StringUtil;
import csi.server.common.util.ValuePair;

public class ResourceFilterDialog extends WatchingParent implements KnowsParent, ValidityCheck {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<ValidatingDialog, ResourceFilterDialog> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private CanBeShownParent parentDialog;
    private ValidatingDialog dialog;

    private Button selectButton;
    private Button cancelButton;
    private List<PairedListWidget<StringEntry>> aclWidgetList;
    private List<DateRangeWidget> historyWidgetList;
    @UiField
    FilteredTextBox patternMatchTextBox;
    @UiField
    FilteredTextBox patternRejectTextBox;
    @UiField
    CheckBox searchNames;
    @UiField
    CheckBox searchRemarks;
    @UiField
    WizardTabPanel historyTabPanel;
    @UiField
    WizardTabPanel aclTabPanel;
    @UiField
    SortListBox<ResourceSortMode> firstSort;
    @UiField
    SortListBox<ResourceSortMode> secondSort;
    @UiField
    SortListBox<ResourceSortMode> thirdSort;
    @UiField
    SortListBox<ResourceSortMode> fourthSort;

    CheckBox addToList = null;
    RenameResourceDialog activeDialog = null;
    ResourceFilterListBox filterListBox = null;

    SortingSet<ResourceSortMode> sortSelectionSet = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final String _txtRenameTitle = _constants.resourceFilterListDialog_RenameResourceFilter();
    private static final String _txtResourceFilterSingular = _constants.resourceFilterListDialog_ResourceFilterSingular();
    private static final String _txtResourceFilterPlural = _constants.resourceFilterListDialog_ResourceFilterPlural();
    private static final String _txtAddToPrompt = _constants.resourceFilterListDialog_AddToPrompt();
    private static final String _pairedListWidgetYes = _constants.resourceFilter_FilterDialog_PairedListWidgetYes();
    private static final String _pairedListWidgetNo = _constants.resourceFilter_FilterDialog_PairedListWidgetNo();

    private static final String[] _historyOptionArray = {

            _constants.created(), _constants.modified(), _constants.accessed()
    };
    private static final String[] _aclOptionArray = {

            _constants.ownedBy(), _constants.canRead(), _constants.canEdit(), _constants.canDelete()
    };
    private static final String[] _aclLeftHeaderArray = {

            _constants.leftUserHeaderDefault(), _constants.leftUserHeaderDefault(),
            _constants.leftUserGroupHeaderDefault(), _constants.leftUserGroupHeaderDefault()
    };
    private static final String[] _aclRightHeaderArray = {

            _constants.rightUserHeaderDefault(), _constants.rightUserHeaderDefault(),
            _constants.rightUserGroupHeaderDefault(), _constants.rightUserGroupHeaderDefault()
    };

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private List<StringEntry> _allUsers = null;
    private List<StringEntry> _allUsersAndGroups = null;

    private String _txtDialogTitleString = null;
    private Integer _buttonWidth = 60;

    private ResourceFilter _filter = null;
    private ClickHandler _updateHandler = null;
    private ClickHandler _refreshHandler = null;
    private ClickHandler _cancelHandler = null;
    private List<OptionBasics> _filterList = null;

    private boolean _isReady = false;
    private boolean _displayRequested = false;
    private boolean _isAdHoc;
    private boolean _inExit = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected VortexEventHandler<Response<String, ValuePair<List<String>, List<String>>>> handleRoleListResponse
            = new AbstractVortexEventHandler<Response<String, ValuePair<List<String>, List<String>>>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                hideWatchBox();

                // Display error message.
                Display.error("ResourceFilterDialog", 1, exceptionIn);

            } catch (Exception myException) {

                Display.error("ResourceFilterDialog", 2, myException);
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

                        _allUsers = (List<StringEntry>)StringUtil.buildDisplayList(new ArrayList<StringEntry>(), myUsers);
                        _allUsersAndGroups = (List<StringEntry>)StringUtil.buildDisplayList(new ArrayList<StringEntry>(), myGroups);
                        _allUsersAndGroups.addAll(_allUsers);

                        extractFilter();

                    } else {

                        ResponseHandler.displayError(responseIn);
                    }
                }

            } catch (Exception myException) {

                Display.error("ResourceFilterDialog", 3, myException);
            }
        }
    };

    //
    // Handle clicking the Cancel button
    //
    private ClickHandler handleCancelButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                stopMonitoring();
                _filter = null;
                performCleanUp(eventIn, _cancelHandler);

            } catch (Exception myException) {

                Display.error("ResourceFilterDialog", 4, myException);
            }
        }
    };

    private ClickHandler handleListUpdate
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                _inExit = true;
                performCleanUp(eventIn, _updateHandler);

            } catch (Exception myException) {

                Display.error("ResourceFilterDialog", 5, myException);
            }
        }
    };

    private ClickHandler handleFilterRename = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                if ((null != activeDialog) && (null != _filter)) {

                    _filter.setName(((RenameResourceDialog) activeDialog).getName());
                    _filter.setRemarks(((RenameResourceDialog) activeDialog).getRemarks());
                    filterListBox.updatetList(_filter, handleListUpdate);
                }

            } catch (Exception myException) {

                show();
                Dialog.showException("ResourceFilterListDialog", 2, myException);
            }
        }
    };

    private ClickHandler handleRenameCancel = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

        }
    };

    //
    // Handle clicking the Apply button
    //
    private ClickHandler handleApplyButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                stopMonitoring();
                updateFilter();
                if ((null != addToList) && addToList.getValue()) {

                    addToList.setValue(false);
                    activeDialog = new RenameResourceDialog(null, _txtRenameTitle, _txtResourceFilterSingular,
                                                            _txtResourceFilterPlural, handleFilterRename,
                                                            handleRenameCancel, _filterList);
                    activeDialog.show(_this);

                } else {

                    performCleanUp(eventIn, _refreshHandler);
                }

            } catch (Exception myException) {

                Display.error("ResourceFilterDialog", 6, myException);
            }
        }
    };



    public void disableACL(){
        aclTabPanel.setVisible(false);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ResourceFilterDialog(ResourceFilter filterIn, ClickHandler refreshIn, ClickHandler cancelIn,
                                ClickHandler updateHandlerIn, ResourceFilterListBox listBoxIn) {

        this(filterIn, null, null, refreshIn, cancelIn, true);
        _updateHandler = updateHandlerIn;
        addToList = new CheckBox(_txtAddToPrompt);
        addToList.setValue(false);
        filterListBox = listBoxIn;
        _filterList = (null != filterListBox) ? filterListBox.getFilterList() : null;
        if (null != _filterList) {

            dialog.addLeftControl(addToList);
        }
    }

    public ResourceFilterDialog(ResourceFilter filterIn, List<StringEntry> usersIn, List<StringEntry> usersAndGroupsIn,
                                ClickHandler refreshIn, ClickHandler cancelIn, boolean isAdHocIn) {

        try {

            _filter = (null != filterIn) ? filterIn : new ResourceFilter(true);
            _allUsers = usersIn;
            _allUsersAndGroups = usersAndGroupsIn;
            _refreshHandler = refreshIn;
            _cancelHandler = cancelIn;
            _isAdHoc = isAdHocIn;

            //
            // Initialize the display objects
            //
            initializeObject();
            initializeDisplay();

        } catch (Exception myException) {

            Display.error("ResourceFilterDialog", 7, myException);
        }
    }

    public void checkValidity() {

        String myMatch = patternMatchTextBox.getText();
        String myReject = patternRejectTextBox.getText();
        boolean myPatternSet = ((null != myMatch) && (0 < myMatch.length()))
                                || ((null != myReject) && (0 < myReject.length()));
        boolean myTargetSet = searchNames.getValue() || searchRemarks.getValue();

        selectButton.setEnabled(myPatternSet ? myTargetSet : true);
    }

    public void setButtonWidth(Integer widthIn) {

        _buttonWidth = widthIn;
    }

    public void show() {

        show(parentDialog);
    }

    public void show(CanBeShownParent parentDialogIn) {

        if (!_inExit) {

            try {

                parentDialog = parentDialogIn;

                if (_isReady) {

                    dialog.show(_buttonWidth);
                    if (null != parentDialog) {

                        parentDialog.hide();
                    }
                    beginMonitoring();

                } else {

                    _displayRequested = true;
                }

            } catch (Exception myException) {

                Display.error("ResourceFilterDialog", 8, myException);
            }
        }
    }

    public ResourceFilter getFilter() {

        return _filter;
    }

    public void unlockDisplay() {
        if(selectButton != null)
            selectButton.setEnabled(true);
    }

    public void hide() {

        stopMonitoring();
        dialog.hide();
    }

    public void destroy() {

        try {

            stopMonitoring();
            dialog.destroy();

            dialog = null;
            selectButton = null;
            cancelButton = null;

            _txtDialogTitleString = null;

        } catch (Exception myException) {

            Display.error("ResourceFilterDialog", 9, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    private void initializeObject() {

        //
        // Link UI XML code to this file and all GWT to create remaining components
        //
        dialog = uiBinder.createAndBindUi(this);
        //
        // Set up the dialog cancel button
        //
        cancelButton = dialog.getCancelButton();
        cancelButton.addClickHandler(handleCancelButtonClick);
        //
        // Set up the dialog save button
        //
        selectButton = dialog.getActionButton();
        selectButton.addClickHandler(handleApplyButtonClick);
    }

    //
    //
    //
    private void initializeDisplay() {

        _txtDialogTitleString = _constants.resourceFilter_FilterDialog_Title();

        // Format the dialog title bar
        //
        dialog.defineHeader(_txtDialogTitleString, (String) null, true);

        cancelButton.setText(Dialog.txtCancelButton);
        cancelButton.setVisible(true);
        cancelButton.setEnabled(true);

        selectButton.setText(Dialog.txtApplyButton);
        selectButton.setVisible(true);
        selectButton.setEnabled(false);

        if (null != _allUsers) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    extractFilter();
                }
            });

        } else {

            retrieveUsersAndGroups();
        }
    }

    private void extractFilter() {

        if (null != _filter) {

            patternMatchTextBox.setText(_filter.getMatchDisplayPattern());
            patternRejectTextBox.setText(_filter.getRejectDisplayPattern());
            searchNames.setText(_constants.resourceFilter_FilterDialog_SearchNames());
            searchRemarks.setText(_constants.resourceFilter_FilterDialog_SearchRemarks());
            searchNames.setValue(_filter.getTestName());
            searchRemarks.setValue(_filter.getTestRemarks());

            createTabWidgets();
            createSortWidgets();
            _isReady = true;
            if (_displayRequested) {

                show(parentDialog);
            }
        }
    }

    private void createTabWidgets() {

        Date[] myTemporalAbsolutes = (null != _filter)
                ? _filter.getTemporalAbsolutes()
                : new Date[_historyOptionArray.length];
        Integer[] myTemporalDeltas = (null != _filter)
                ? _filter.getTemporalDeltas()
                : new Integer[_historyOptionArray.length];
        List<List<Collection<StringEntry>>> myAclList = (null != _filter)
                ? _filter.getListOfAclDisplayLists()
                : createEmptyAclList();

        if (null != _aclOptionArray) {

            int myTabCount = _aclOptionArray.length;

            if (0 < myTabCount) {

                aclWidgetList = new ArrayList<PairedListWidget<StringEntry>>(myTabCount);

                for (int i = 0; myTabCount > i; i++) {

                    PairedListWidget<StringEntry> myAclWidget = new PairedListWidget<StringEntry>(new String[]{_pairedListWidgetYes, _pairedListWidgetNo});
                    CsiTabWidget myTabWidget = new CsiTabWidget(myAclWidget, _aclOptionArray[i], null);

                    myAclWidget.labelLeftColumn(_aclLeftHeaderArray[i]);
                    myAclWidget.labelRightColumn(_aclRightHeaderArray[i]);
                    if (null != myAclList) {

                        if (0 < i) {

                            myAclWidget.loadDataSet(_allUsersAndGroups, myAclList.get(i));

                        } else {

                            myAclWidget.loadDataSet(_allUsers, myAclList.get(i));
                        }
                    }
                    aclWidgetList.add(myAclWidget);
                    aclTabPanel.addTab(myTabWidget);
                }
            }
        }
        if (null != _historyOptionArray) {

            int myTabCount = _historyOptionArray.length;

            if (0 < myTabCount) {

                historyWidgetList = new ArrayList<DateRangeWidget>(myTabCount);

                for (int i = 0; myTabCount > i; i++) {

                    try {

                        int myLeftIndex = i * 2;
                        int myRightIndex = myLeftIndex + 1;
                        boolean myAbsoluteFlag = _isAdHoc || (null != myTemporalAbsolutes[myLeftIndex])
                                                    || (null != myTemporalAbsolutes[myRightIndex]);
                        String myRadioGroup = "Tab" + Integer.toString(i + 1);
                        DateRangeWidget myHistoryWidget
                                        = new DateRangeWidget(myAbsoluteFlag, myTemporalAbsolutes[myLeftIndex],
                                                    myTemporalAbsolutes[myRightIndex], myTemporalDeltas[myLeftIndex],
                                                    myTemporalDeltas[myRightIndex], myRadioGroup);
                        CsiTabWidget myTabWidget = new CsiTabWidget(myHistoryWidget, _historyOptionArray[i], null);

                        historyWidgetList.add(myHistoryWidget);
                        historyTabPanel.addTab(myTabWidget);

                    } catch (Exception myException) {

                        Display.error("ResourceFilterDialog", 10, myException);
                    }
                }
            }
        }
    }

    private void createSortWidgets() {


        ResourceSortMode[] mySelections = (null != _filter)
                ? new ResourceSortMode[]{_filter.getFirstSort(), _filter.getSecondSort(),
                _filter.getThirdSort(), _filter.getFourthSort()}
                : null;
        sortSelectionSet
                = new SortingSet<ResourceSortMode>(new SortListBox[]{firstSort, secondSort, thirdSort, fourthSort},
                ResourceSortMode.list(), mySelections);
    }

    private List<List<Collection<StringEntry>>> createEmptyAclList() {

        List<List<Collection<StringEntry>>> myList = new ArrayList<List<Collection<StringEntry>>>();

        for (int i = 0; _aclOptionArray.length > i; i++) {

            List<Collection<StringEntry>> myPair = new ArrayList<Collection<StringEntry>>();

            myPair.add(new ArrayList<StringEntry>());
            myPair.add(new ArrayList<StringEntry>());
            myList.add(myPair);
        }
        return myList;
    }

    private void updateFilter() {

        if (null != _filter) {

            _filter.setMatchDisplayPattern(patternMatchTextBox.getText());
            _filter.setRejectDisplayPattern(patternRejectTextBox.getText());
            _filter.setTestName(searchNames.getValue());
            _filter.setTestRemarks(searchRemarks.getValue());
            _filter.setFirstSort(firstSort.getSelectedValue());
            _filter.setSecondSort(secondSort.getSelectedValue());
            _filter.setThirdSort(thirdSort.getSelectedValue());
            _filter.setFourthSort(fourthSort.getSelectedValue());
            updateFilterTemporalData();
            updateFilterAclData();
        }
    }

    private void updateFilterTemporalData() {

        Date[] myAbsoluteValues = null;
        Integer[] myDeltaValues = null;

        _filter.clearTemporalAbsoluteList();
        _filter.clearTemporalDeltaList();

        if (null != historyWidgetList) {

            int myCount = historyWidgetList.size();

            if (0 < myCount) {

                myAbsoluteValues = new Date[myCount * 2];
                myDeltaValues = new Integer[myCount * 2];

                for (int i = 0; myCount > i; i++) {

                    try {

                        int myBase = i * 2;
                        DateRangeWidget myHistoryWidget = historyWidgetList.get(i);

                        if (myHistoryWidget.isAbsolute()) {

                            myAbsoluteValues[myBase] = myHistoryWidget.getLeftAbsolute();
                            myDeltaValues[myBase++] = null;
                            myAbsoluteValues[myBase] = myHistoryWidget.getRightAbsolute();
                            myDeltaValues[myBase] = null;

                        } else {

                            myAbsoluteValues[myBase] = null;
                            myDeltaValues[myBase++] = myHistoryWidget.getLeftDelta();
                            myAbsoluteValues[myBase] = null;
                            myDeltaValues[myBase] = myHistoryWidget.getRightDelta();
                        }

                    } catch (Exception myException) {

                        Display.error("ResourceFilterDialog", 11, myException);
                    }
                }
                if (null != _filter) {

                    _filter.setTemporalAbsolutes(myAbsoluteValues);
                    _filter.setTemporalDeltas(myDeltaValues);
                }

            } else if (null != _filter) {

                _filter.clearTemporalAbsoluteList();
                _filter.clearTemporalDeltaList();
            }
        }
    }

    private void updateFilterAclData() {

        List<List<List<StringEntry>>> myListOfLists = null;

        if (null != aclWidgetList) {

            int myCount = aclWidgetList.size();

            if (0 < myCount) {

                myListOfLists = new ArrayList<List<List<StringEntry>>>(myCount);

                for (int i = 0; myCount > i; i++) {

                    PairedListWidget<StringEntry> myAclWidget = aclWidgetList.get(i);

                    myListOfLists.add(myAclWidget.getAllListsOnRight());
                }
            }
        }
        if (null != _filter) {

            _filter.setListOfAclLists(myListOfLists);
        }
    }

    private void performCleanUp(ClickEvent eventIn, ClickHandler handlerIn) {

        if (null != handlerIn) {

            handlerIn.onClick(eventIn);
        }

        if (null != parentDialog) {

            parentDialog.show();
        }
        //
        // Hide dialog
        //
        destroy();
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
            Display.error("ResourceFilterDialog", 12, myException);
        }
    }
    
    private void beginMonitoring() {

        dialog.setCallBack(this);
        dialog.beginMonitoring();
    }
    
    private void stopMonitoring() {
        
        dialog.suspendMonitoring();
    }
}
