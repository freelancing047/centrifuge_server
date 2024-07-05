package csi.client.gwt.csiwizard.panels;

import java.util.*;

import csi.client.gwt.WebMain;
import csi.client.gwt.admin.UserListFilterDialog;
import csi.client.gwt.csiwizard.panels.WizardTabPanel;
import csi.client.gwt.csiwizard.widgets.CsiTabWidget;
import csi.client.gwt.csiwizard.widgets.DualPairedListWidget;
import csi.client.gwt.csiwizard.widgets.PairedListWidget;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.BooleanResponse;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.SharingContext;
import csi.server.common.dto.SelectionListData.SharingRequest;
import csi.server.common.dto.SelectionListData.StringEntry;
import csi.server.common.dto.system.FilteredUserRequest;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.UserAdministrationServiceProtocol;

/**
 * Created by centrifuge on 9/13/2016.
 */
public class ResourceSharingPanel extends WizardTabPanel {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private List<PairedListWidget<StringEntry>> userAclWidget;
    private List<PairedListWidget<StringEntry>> groupAclWidget;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static String _txtLeftUserHeaderDefault = _constants.leftUserHeaderDefault();
    private static String _txtRightUserHeaderDefault = _constants.rightUserHeaderDefault();
    private static String _txtLeftGroupHeaderDefault = _constants.leftGroupHeaderDefault();
    private static String _txtRightGroupHeaderDefault = _constants.rightGroupHeaderDefault();

    private int _readTab = -1;
    private int _editTab = -1;
    private int _deleteTab = -1;

    private Set<String> myLeftReadMap = new HashSet<String>();
    private Set<String> myLeftEditMap = new HashSet<String>();
    private Set<String> myLeftDeleteMap = new HashSet<String>();
    private Set<String> myRightReadMap = new HashSet<String>();
    private Set<String> myRightEditMap = new HashSet<String>();
    private Set<String> myRightDeleteMap = new HashSet<String>();

    private List<StringEntry> _fullUserList = null;
    private List<List<StringEntry>> _activeUserList = null;

    private FilteredUserRequest _filteredUserRequest = new FilteredUserRequest(null, null, null, true);
    private AclResourceType _resourceType;
    private AclControlType[] _controlArray;
    private Integer _activeKey = -1;
    private int _keyLimit = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected VortexEventHandler<Response<String, SharingContext>> handleSharingContextResponse
            = new AbstractVortexEventHandler<Response<String, SharingContext>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            hideWatchBox();

            // Display error message.
            Display.error("ResourceSharingPanel", 1, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, SharingContext> responseIn) {

            if (ResponseHandler.isSuccess(responseIn)) {

                SharingContext myContext = responseIn.getResult();

                if (null != myContext) {

                    createWidgets();
                    buildDisplay(myContext);

                } else {

                    ResponseHandler.displayError(responseIn);
                }
                refresh();
            }
            hideWatchBox();
            setDefaultSelectedTab(1);
        }
    };
    
    private VortexEventHandler<Response<Integer, List<StringEntry>>> handleFilteredUserResponse
    = new AbstractVortexEventHandler<Response<Integer, List<StringEntry>>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            hideWatchBox();

            // Display error message.
            Display.error("ResourceSharingPanel", 2, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<Integer, List<StringEntry>> responseIn) {

            if (ResponseHandler.isSuccess(responseIn)) {

                Integer myKey = responseIn.getKey();
                List<StringEntry> myList = responseIn.getResult();

                userAclWidget.get(myKey).noSortingLeft();
                if (null != myList) {

                    userAclWidget.get(myKey).replaceLeftData(myList);

                } else {

                    userAclWidget.get(myKey).replaceLeftData(new ArrayList<StringEntry>());
                }
                refresh();
            }
            hideWatchBox();
            setDefaultSelectedTab(1);
        }
    };

    private BooleanResponse handleDialogResponse = new csi.client.gwt.util.BooleanResponse() {

        public void onClick(boolean responseIn) {

            if (responseIn) {

                // Refresh user list using requested filter
                retrieveFilteredUserList(_activeKey, _filteredUserRequest);

            } else {
                
                // Reset button state to before
                userAclWidget.get(_activeKey).setSearchButton(false);
                refresh();
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ResourceSharingPanel(AclResourceType resourceTypeIn) {

        super(null, false, false);

        _resourceType = resourceTypeIn;
//        createWidgets();
    }

    public ResourceSharingPanel(AclResourceType resourceTypeIn, SharingContext contextIn) {

        this(resourceTypeIn);
        createWidgets();
        buildDisplay(contextIn);
    }

    public void refresh() {

        for (PairedListWidget<StringEntry> myWidget : userAclWidget) {

            myWidget.refresh();
        }
        for (PairedListWidget<StringEntry> myWidget : groupAclWidget) {

            myWidget.refresh();
        }
    }

    public void initializeDisplay(WatchingParent parentDialogIn, String uuidIn) {

        if (null != uuidIn) {

            List<String> myList = new ArrayList<String>(1);
            myList.add(uuidIn);
            initializeDisplay(parentDialogIn, myList);
        }
    }

    public void initializeDisplay(WatchingParent parentDialogIn, List<String> uuidListIn) {

        parentDialog = parentDialogIn;

        if (null != parentDialog) {

            showWatchBox(_constants.resourceSharingDialog_waitingMessage());
        }
        retrieveSharingContext(uuidListIn);
    }

    public static String getInstructions() {

        return _constants.sharingInfoPopup_Instructions(_constants.sharingInfoPopup_NewInstructions());
    }

    public SharingRequest getSharingRequest() {

        try {

            return new SharingRequest(getReadRequest(), getEditRequest(), getDeleteRequest());

        } catch (Exception myException) {

            Display.error("Caught an exception creating sharing request.", myException);
        }
        return null;
    }

    @Override
    public String getText() throws CentrifugeException {
        return null;
    }

    @Override
    public boolean isOkToLeave() {

        return true;
    }

    @Override
    public void beginMonitoring() {

//        parameterInput.beginMonitoring();
        fireEvent(new ValidityReportEvent(isOkToLeave()));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void retrieveSharingContext(List<String> resourceIdListIn) {

        VortexFuture<Response<String, SharingContext>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(handleSharingContextResponse);
            myVortexFuture.execute(UserAdministrationServiceProtocol.class).retrieveSharingContext("KEY", resourceIdListIn);

        } catch (Exception myException) {

            Display.error("ResourceSharingPanel", 3, myException);
        }
    }

    private void retrieveFilteredUserList(Integer keyIn, FilteredUserRequest requestIn) {

        VortexFuture<Response<Integer, List<StringEntry>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            showWatchBox();
            myVortexFuture.addEventHandler(handleFilteredUserResponse);
            myVortexFuture.execute(UserAdministrationServiceProtocol.class).retrieveFilteredUserList(keyIn, requestIn);

        } catch (Exception myException) {

            hideWatchBox();
            Display.error("ResourceSharingPanel", 4, myException);
        }
    }

    private void createWidgets() {

        _controlArray = (null != _resourceType) ? _resourceType.getControlArray() : null;

        if (null != _controlArray) {

            _keyLimit = _controlArray.length;

            if (0 < _keyLimit) {

                userAclWidget = new ArrayList<PairedListWidget<StringEntry>>(_keyLimit);
                groupAclWidget = new ArrayList<PairedListWidget<StringEntry>>(_keyLimit);
                _activeUserList = new ArrayList<List<StringEntry>>(_keyLimit);

                for (int i = 0; _keyLimit > i; i++) {

                    final int myKey = i;

                    DualPairedListWidget<StringEntry, StringEntry> myAclWidget
                            = new DualPairedListWidget<StringEntry, StringEntry>(

                        new csi.client.gwt.util.BooleanResponse() {

                            public void onClick(boolean responseIn) {

                                if (responseIn) {

                                    // Display dialog for creating search parameters.
                                    _activeKey = myKey;
                                    (new UserListFilterDialog(_filteredUserRequest, handleDialogResponse, true)).show();

                                } else {

                                    // Refresh user list with everyone
                                    userAclWidget.get(myKey).sortAscendingLeft();
                                    userAclWidget.get(myKey).replaceLeftData(_fullUserList);
                                    refresh();
                                }
                            }
                        }
                    );
                    CsiTabWidget myTabWidget = new CsiTabWidget(myAclWidget, _controlArray[myKey].getLabel(), null);
                    PairedListWidget<StringEntry> myUserWidget = myAclWidget.getTopWidget();
                    PairedListWidget<StringEntry> myGroupWidget = myAclWidget.getBottomWidget();

                    myUserWidget.labelLeftColumn(_txtLeftUserHeaderDefault);
                    myUserWidget.labelRightColumn(_txtRightUserHeaderDefault);
                    myGroupWidget.labelLeftColumn(_txtLeftGroupHeaderDefault);
                    myGroupWidget.labelRightColumn(_txtRightGroupHeaderDefault);

                    userAclWidget.add(myUserWidget);
                    groupAclWidget.add(myGroupWidget);

                    addTab(myTabWidget);

                    if (AclControlType.READ.equals(_controlArray[myKey])) {

                        _readTab = myKey;

                    } else if (AclControlType.EDIT.equals(_controlArray[myKey])) {

                        _editTab = myKey;
                    } else if (AclControlType.DELETE.equals(_controlArray[myKey])) {

                        _deleteTab = myKey;
                    }
                }
            }
        }
    }

    private void buildDisplay(SharingContext contextIn) {

        if (null != contextIn) {

            _fullUserList = contextIn.getAllUserList();
            _activeUserList.clear();
            for (int i = 0; _keyLimit > i; i++) {

                _activeUserList.add(_fullUserList);
            }
            if (0 <= _readTab) {

                userAclWidget.get(_readTab).loadData(_fullUserList, contextIn.getReadUserList());
                groupAclWidget.get(_readTab).loadData(contextIn.getAllGroupList(), contextIn.getReadGroupList());
            }
            if (0 <= _editTab) {

                userAclWidget.get(_editTab).loadData(_fullUserList, contextIn.getEditUserList());
                groupAclWidget.get(_editTab).loadData(contextIn.getAllGroupList(), contextIn.getEditGroupList());
            }
            if (0 <= _deleteTab) {

                userAclWidget.get(_deleteTab).loadData(_fullUserList, contextIn.getDeleteUserList());
                groupAclWidget.get(_deleteTab).loadData(contextIn.getAllGroupList(), contextIn.getDeleteGroupList());
            }
        }
    }

    private List<List<String>> getReadRequest() {

        List<String> myAddList = new ArrayList<String>();
        List<String> myRemoveList = new ArrayList<String>();
        List<List<String>> myCompositeList = new ArrayList<List<String>>();

        myCompositeList.add(myAddList);
        myCompositeList.add(myRemoveList);

        if (0 <= _readTab) {

            addValues(myRemoveList, userAclWidget.get(_readTab).getListOnLeft(), myLeftReadMap);
            addValues(myRemoveList, groupAclWidget.get(_readTab).getListOnLeft(), myLeftReadMap);
            addValues(myAddList, userAclWidget.get(_readTab).getListOnRight(), myRightReadMap);
            addValues(myAddList, groupAclWidget.get(_readTab).getListOnRight(), myRightReadMap);
        }
        return myCompositeList;
    }

    private List<List<String>> getEditRequest() {

        List<String> myAddList = new ArrayList<String>();
        List<String> myRemoveList = new ArrayList<String>();
        List<List<String>> myCompositeList = new ArrayList<List<String>>();

        myCompositeList.add(myAddList);
        myCompositeList.add(myRemoveList);

        if (0 <= _editTab) {

            addValues(myRemoveList, userAclWidget.get(_editTab).getListOnLeft(), myLeftEditMap);
            addValues(myRemoveList, groupAclWidget.get(_editTab).getListOnLeft(), myLeftEditMap);
            addValues(myAddList, userAclWidget.get(_editTab).getListOnRight(), myRightEditMap);
            addValues(myAddList, groupAclWidget.get(_editTab).getListOnRight(), myRightEditMap);
        }
        return myCompositeList;
    }

    private List<List<String>> getDeleteRequest() {

        List<String> myAddList = new ArrayList<String>();
        List<String> myRemoveList = new ArrayList<String>();
        List<List<String>> myCompositeList = new ArrayList<List<String>>();

        myCompositeList.add(myAddList);
        myCompositeList.add(myRemoveList);

        if (0 <= _deleteTab) {

            addValues(myRemoveList, userAclWidget.get(_deleteTab).getListOnLeft(), myLeftDeleteMap);
            addValues(myRemoveList, groupAclWidget.get(_deleteTab).getListOnLeft(), myLeftDeleteMap);
            addValues(myAddList, userAclWidget.get(_deleteTab).getListOnRight(), myRightDeleteMap);
            addValues(myAddList, groupAclWidget.get(_deleteTab).getListOnRight(), myRightDeleteMap);
        }
        return myCompositeList;
    }

    private void addValues(List<String> targetIn, List<StringEntry> sourceIn, Set<String> rejectionMapIn) {

        if ((null != sourceIn) && (0 < sourceIn.size())) {

            for (StringEntry myEntry : sourceIn) {

                String myKey = myEntry.getValue();

                if (DisplayMode.NORMAL.equals(myEntry.getDisplayMode()) && (!rejectionMapIn.contains(myKey))) {

                    targetIn.add(myKey);
                }
            }
        }
    }

    private Map<String, StringEntry> buildTreeMap(List<StringEntry> listIn) {
        
        Map<String, StringEntry> myMap = new TreeMap<>();

        for (StringEntry myEntry : listIn) {

            myMap.put(myEntry.getDisplayString(), myEntry);
        }
        return myMap;
    }

    private List<StringEntry> buildDisplayList(List<String> listIn, Map<String, StringEntry> mapIn) {

        List<StringEntry> myList = new ArrayList<>();

        for (String myItem : listIn) {

            StringEntry myEntry = mapIn.get(myItem);
            myList.add(myEntry);
        }
        return myList;
    }
}
