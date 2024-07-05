package csi.client.gwt.csiwizard.panels;


import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.filters.ResourceFilterDialog;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.events.*;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.CsiListView;
import csi.client.gwt.widget.boot.CsiListViewReloaded;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.CsiBrowseButton;
import csi.client.gwt.widget.list_boxes.ResourceFilterListBox;
import csi.client.gwt.widget.ui.uploader.UploadWidget;
import csi.server.common.dto.SelectionListData.SelectorBasics;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.service.api.ModelActionsServiceProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                        //
//                   Selector Used for DataView and Template Selection                    //
//                                                                                        //
////////////////////////////////////////////////////////////////////////////////////////////

public class ResourceSelectorPanel<T extends SelectorBasics> extends AbstractWizardPanel implements HasHandlers {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public enum SelectorMode {

        //                          write   save    browse  simple  exact   single  reject_all

        READ_ONLY(                  false,  false,  false,  false,  true,   true,   false ),
        SIMPLE_READ_ONLY(           false,  false,  false,  true,   true,   true,   false ),
        SEARCH(                     true,   false,  false,  false,  true,   true,   false ),
        SIMPLE_SEARCH(              true,   false,  false,  true,   true,   true,   false ),
        SIMPLE_COMBO(               true,   false,  false,  true,   false,  true,   false ),
        LOCAL_BROWSE(               false,  false,  true,   false,  true,   true,   false ),
        SIMPLE_LOCAL_BROWSE(        false,  false,  true,   true,   true,   true,   false ),
        SEARCH_LOCAL_BROWSE(        true,   false,  true,   false,  true,   true,   false ),
        SEARCH_SIMPLE_LOCAL_BROWSE( true,   false,  true,   true,   true,   true,   false ),
        WRITE(                      true,   true,   false,  false,  false,  true,   false ),
        SIMPLE_WRITE(               true,   true,   false,  true,   false,  true,   false ),
        NEW(                        true,   true,   false,  false,  false,  true,   true  ),
        SIMPLE_NEW(                 true,   true,   false,  true,   false,  true,   true  );

        boolean _isSave;
        boolean _isWrite;
        boolean _needsBrowse;
        boolean _hideRemarks;
        boolean _mustMatch;
        boolean _isSingle;
        boolean _rejectAll;

        private SelectorMode(boolean isWriteIn, boolean isSaveIn, boolean needsBrowseIn, boolean hideRemarksIn,
                             boolean mustMatchIn, boolean isSingleIn, boolean rejectAllIn) {

            _isWrite = isWriteIn;
            _isSave = isSaveIn;
            _needsBrowse = needsBrowseIn;
            _hideRemarks = hideRemarksIn;
            _mustMatch = mustMatchIn;
            _isSingle = isSingleIn;
            _rejectAll = rejectAllIn;
        }

        public boolean isWrite() {

            return _isWrite;
        }

        public boolean isSave() {

            return _isSave;
        }

        public boolean needsBrowse() {

            return _needsBrowse;
        }

        public boolean hideRemarks() {

            return _hideRemarks;
        }

        public boolean mustMatch() {

            return _mustMatch;
        }

        public boolean isSingleSelection() {

            return _isSingle;
        }

        public boolean mustRejectAll() {

            return _rejectAll;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private Label selectionListLabel = null;
    private Label selectionNameLabel = null;
    private Label selectionRemarksLabel = null;
    private Label prefixLabel = null;

    private CsiListView<T> selectionList = null;
    private HorizontalPanel outerPanel = null;
    private FlowPanel innerPanel = null;
    private TextBox selectionName = null;
    private TextArea selectionRemarks = null;
    private ResourceFilterListBox filterDropDown = null;
    private CsiBrowseButton<T> browseButton = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtFailureDialogTitle = _constants.serverRequest_ErrorDialogTitle();

    protected final String _txtSelectionListLabel = _constants.resourceSelector_ListLabel();
    protected final String _txtSelectionNameLabel = _constants.resourceSelector_NameLabel();
    protected final String _txtSelectionRemarksLabel = _constants.resourceSelector_RemarksLabel();
    protected String _txtDialogTitleString = null;
    protected String _txtOverwriteDialogTitleString = null;
    protected String _txtOverwriteDialogInfoString = null;
    private ClickHandler _reloadClickHandler;
    private static final String _txtBrowseLocally = _constants.resourceSelector_BrowseLocally();

    private List<T> _completeSelectionList = null;
    private ListStore<T> _resourceListStore = null;
    private Map<String, Integer> _selectionMap = null;
    private Map<String, T> _rejectionMap = null;
    private Map<String, T> _conflictMap = null;
    private Map<String, T> _existenceMap = null;
    private Map<String, String> _xferMap = null;
    private List<Map<String, T>> _rejectionList = null;
    private AclResourceType _resourceType = null;
    private boolean _nameIsValid = false;
    private boolean _isSave = false;
    private boolean _isWrite = false;
    private boolean _needsBrowse = false;
    private boolean _processingRequest = false;
    private boolean _hideRemarks = false;
    private boolean _mustMatch = false;
    private boolean _isSingle = true;
    private boolean _rejectAll = false;
    private boolean _nameInUse = false;
    private T _selection = null;
    private T _currentResource = null;
    private String _fileTypes = null;
    private String _default = null;
    private boolean _monitoring = false;
    private boolean _provideName = true;
    private String _baseName = "unnamed";
    private String _currentName = null;
    private SelectionChangedHandler<T> _selectionMonitor = null;
    private ResourceFilter _resourceFilter = null;
    private ResourceFilter _priorFilter = null;
    private ResourceFilterDialog _filterEditDialog = null;
    private AclControlType _requiredPermission = null;
    private int _priorFilterChoice = 0;
    private String _preSelect = null;
    private boolean _provideSourceName = WebMain.injector.getMainPresenter().provideSourceName();
    private boolean _incrementImmediately = WebMain.injector.getMainPresenter().incrementImmediately();
    private boolean _bracketDefault = WebMain.injector.getMainPresenter().bracketDefault();
    private boolean _rejectAllConflicts = false;
    private boolean _suppressFilter = false;
    private boolean _currentInQuestion = false;
    private boolean _suppressNameModification = false;
    private List<T> _shadowList = null;
    private int _selectionWidth = 0;
    private int _prefixSize = 0;
    private String _prefixText = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler handleCancelEditResponse = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {
                if(filterDropDown instanceof  ResourceFilterListBox) {
                    ((ResourceFilterListBox) filterDropDown).setSelectedIndex(_priorFilterChoice);
                }

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 1, myException);
            }
        }
    };

    private ClickHandler handleEditFilterResponse = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                ResourceFilter myFilter = _filterEditDialog.getFilter();

                // TODO: Add support for stored filters
                if (null != myFilter) {

                    _priorFilterChoice = 1;
                    _resourceFilter = myFilter;
                    _priorFilter = _resourceFilter;
                    requestFilteredResourceList(_resourceFilter);
                }
                if(filterDropDown instanceof ResourceFilterListBox) {
                    ((ResourceFilterListBox)filterDropDown).setSelectedIndex(_priorFilterChoice);
                }

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 1, myException);
            }
        }
    };

    private ClickHandler handleCreateFilterResponse = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                ResourceFilter myFilter = _filterEditDialog.getFilter();

                // TODO: Add support for stored filters
                if (null != myFilter) {

                    _priorFilterChoice = 1;
                    _resourceFilter = myFilter;
                    _priorFilter = _resourceFilter;
                    requestFilteredResourceList(_resourceFilter);
                }

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 1, myException);
            }
        }
    };

    private SelectionChangedHandler handleFilterChange = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            try {

                int myFilterChoice = filterDropDown.getSelectedIndex();

                switch (myFilterChoice) {

                    case 0:

                        _resourceFilter = null;
                        if (0 != _priorFilterChoice) {

                            _priorFilterChoice = 0;
                            requestFilteredResourceList(_resourceFilter);

                        } else {

                            handleFilteredListRequestResponse.onSuccess(_completeSelectionList);
                        }
                        break;

                    case 1:

                        _filterEditDialog = new ResourceFilterDialog(_priorFilter, handleEditFilterResponse,
                                                            handleCancelEditResponse, handleCreateFilterResponse, filterDropDown);
                        _filterEditDialog.show(parentDialog);
                        break;

                    default:

                        ResourceFilter myFilter = filterDropDown.getSelectedItem();
                        if (null != myFilter) {

                            _resourceFilter = myFilter;
                            requestFilteredResourceList(_resourceFilter);
                        }
                        _priorFilterChoice = myFilterChoice;
                        break;
                }

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 2, myException);
            }

        }
    };

    //
    // Handle response to request for filtered resource list
    //
    public VortexEventHandler<List<T>> handleFilteredListRequestResponse
            = new AbstractVortexEventHandler<List<T>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            Dialog.showException(_txtFailureDialogTitle, myException);

            return false;
        }

        @Override
        public void onSuccess(List<T> listIn) {

            try {

                hideWatchBox();
                _selection = null;
                _resourceListStore.clear();
                _selectionMap = new HashMap<String, Integer>();
                selectionName.setText(null);
                checkSelectionName(getTrimmedSelectionName());

                if (null != listIn) {

                    _resourceListStore.addAll(listIn);
                    for (int i = 0; _resourceListStore.size() > i; i++) {

                        T myItem = _resourceListStore.get(i);

                        _selectionMap.put(myItem.getDisplayString(_prefixSize), i);
                    }
                }

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 3, myException);
            }
        }
    };

    //
    // Handle response to request for overwrite resource list
    //
    public VortexEventHandler<List<List<T>>> handleTripleListRequestResponse
            = new AbstractVortexEventHandler<List<List<T>>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            Dialog.showException(_txtFailureDialogTitle, myException);

            return false;
        }

        @Override
        public void onSuccess(List<List<T>> listPairIn) {

            try {

                hideWatchBox();
                if (null != listPairIn) {

                    List<T> mySelectionList = (0 < listPairIn.size()) ? listPairIn.get(0) : null;
                    List<T> myRejectionList = (1 < listPairIn.size()) ? listPairIn.get(1) : null;
                    List<T> myConflictList = (2 < listPairIn.size()) ? listPairIn.get(2) : null;

                    _rejectionMap = new HashMap<String, T>();
                    _conflictMap = new HashMap<String, T>();
                    _completeSelectionList = myConflictList;

                    if (null != myRejectionList) {

                        for (T myResource : myRejectionList) {

                            _rejectionMap.put(myResource.getDisplayString(_prefixSize), myResource);
                        }
                    }
                    if (_rejectAll) {

                        if (null != myConflictList) {

                            for (T myResource : myConflictList) {

                                _rejectionMap.put(myResource.getDisplayString(_prefixSize), myResource);
                            }
                        }
                        if (null != mySelectionList) {

                            for (T myResource : mySelectionList) {

                                _rejectionMap.put(myResource.getDisplayString(_prefixSize), myResource);
                            }
                        }

                    } else {

                        if (null != myConflictList) {

                            for (T myResource : myConflictList) {

                                _conflictMap.put(myResource.getDisplayString(_prefixSize), myResource);
                            }
                        }
                        if (!_currentInQuestion) {

                            adjustConflicts(_currentName);
                        }
                    }
                    loadSelectionList(mySelectionList, mySelectionList);
                }

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 4, myException);
            }
        }
    };

    //
    // Handle response to request for open resource list
    //
    public VortexEventHandler<List<T>> handleSingleListRequestResponse
            = new AbstractVortexEventHandler<List<T>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            Dialog.showException(_txtFailureDialogTitle, myException);

            return false;
        }

        @Override
        public void onSuccess(List<T> listIn) {

            try {

                hideWatchBox();
                loadSelectionList(listIn, listIn);

                if (_rejectAll || _rejectAllConflicts) {

                    _rejectionMap = new HashMap<String, T>();

                    if (null != listIn) {

                        for (T myResource : listIn) {

                            _rejectionMap.put(myResource.getDisplayString(_prefixSize), myResource);
                        }
                    }
                }
                if (!_rejectAll) {

                    adjustConflicts(_currentName);
                }

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 5, myException);
            }
        }
    };

    public boolean requestConfirmed() {

        return true;
    }

    public void setTripleList(List<List<T>> listIn, boolean currentInQuestionIn) {

        try {

            _currentInQuestion = currentInQuestionIn;
            handleTripleListRequestResponse.onSuccess(listIn);

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 6, myException);
        }
    }

    public void setSingleList(List<T> listIn) {

        try {

            handleSingleListRequestResponse.onSuccess(listIn);

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 7, myException);
        }
    }

    public void selectInput() {

        selectionName.setFocus(true);
        if ((null != selectionName.getText()) && (0 < selectionName.getText().length())) {

            selectionName.selectAll();
        }
    }

    //
    // Handle selection change
    //
    protected SelectionChangedHandler<T> selectionListChangeHandler
            = new SelectionChangedHandler<T>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<T> event) {

            try {

                T mySelection = selectionList.getSelectionModel().getSelectedItem();

                if (null != mySelection) {

                    String mySelectionName = mySelection.getDisplayString(_prefixSize).trim();

                    selectionName.setText(mySelectionName);
                    if (!_hideRemarks) {

                        selectionRemarks.setText(mySelection.getRemarks());
                    }
                    checkSelectionName(mySelectionName);
                    if (null != _selectionMonitor) {

                        _selectionMonitor.onSelectionChanged(event);
                    }
                    checkValidity();
                    fireEvent(new ValidityReportEvent(isOkToLeave()));
                }

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 8, myException);
            }
        }
    };

    protected void forceSelect(List<T> listIn) {

        selectionList.getSelectionModel().select(listIn, true);
    }

    //
    // Monitor typing into the "selectionName" text field
    // and continually check for name conflicts
    //
    private KeyUpHandler handleNewTextBoxKeyUp
            = new KeyUpHandler() {

        @Override
        public void onKeyUp(KeyUpEvent eventIn) {

            try {

                _selection = null;
                checkSelectionName(getTrimmedSelectionName());

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 9, myException);
            }
        }
    };

    //
    // Check for name conflicts whenever a text drag object
    // is dropped onto the "selectionName" text field
    //
    private DropHandler handleTextBoxDrop
            = new DropHandler() {
        @Override
        public void onDrop(DropEvent eventIn) {

            try {

                String myDataIn = eventIn.getData("text/plain");
                String myDataOut = filterDrop(myDataIn);

                if (null != myDataOut) {

                    eventIn.setData(myDataOut, "text/plain");
                }
                _selection = null;
                checkSelectionName((null != myDataOut) ? myDataOut : myDataIn);

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 10, myException);
            }
        }
    };

    //
    // Monitor any change to the "selectionName" text field
    // and continually check for name conflicts
    //
    private ChangeHandler handleTextBoxChange
            = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent eventIn) {

            try {

                _selection = null;
                checkSelectionName(getTrimmedSelectionName());

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 11, myException);
            }
        }
    };

    //
    // Monitor mouse clicks to catch the box being clear
    // IE does not consider clearing the text a change event!
    //
    private ClickHandler handleTextBoxClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                checkSelectionName(getTrimmedSelectionName());

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 12, myException);
            }
        }
    };

    //
    // Handle a positive response from the overwrite dialog
    //
    protected ClickHandler handleOverwriteRequest
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                performRequest(true);

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 13, myException);
            }
        }
    };

    //
    // Handle a negative response from the overwrite dialog
    //
    protected ClickHandler handleCancelRequest
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                // Force validity event to toggle action button back to enabled
                _nameIsValid = false;

                enableInput();

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 14, myException);
            }
        }
    };

    //
    // Handle clicking the Select button from a parent dialog!!
    //
    public ClickHandler handleSelectButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            try {

                executeRequested();

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 15, myException);
            }
        }
    };

    //
    // Handle completed transfer of files to the server
    //
    public TransferCompleteEventHandler handleUploadComplete
            = new TransferCompleteEventHandler() {
        @Override
        public void onTransferComplete(TransferCompleteEvent eventIn) {

            try {

                List<String> myXferList = eventIn.getItemList();

                if ((null != myXferList) && (0 < myXferList.size())) {

                    _xferMap = new HashMap<String, String>();

                    for (String myItem : myXferList) {

                        _xferMap.put(myItem, myItem);
                    }
                }

                disableAll();
                fireEvent(new RefreshRequiredEvent());

            } catch (Exception myException) {

                Dialog.showException("ResourceSelectorPanel", 16, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ResourceSelectorPanel(CanBeShownParent parentDialogIn, SelectorMode modeIn, boolean isRequiredIn) {

        this(parentDialogIn, modeIn, isRequiredIn, null);
        _suppressNameModification = true;
    }

    public ResourceSelectorPanel(CanBeShownParent parentDialogIn, SelectorMode modeIn, boolean isRequiredIn, String prefixIn) {

        super(parentDialogIn, isRequiredIn);
        _prefixText = prefixIn;
        _prefixSize = (null != _prefixText) ? prefixIn.length() : 0;

        try {

            //
            // Initialize control values
            //
            setControlValues(modeIn);

            //
            // Initialize the display objects
            //
            initializeObject();

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 17, myException);
        }
    }

    public ResourceSelectorPanel(CanBeShownParent parentDialogIn, CsiBrowseButton browseButtonIn, boolean isRequiredIn) {

        this(parentDialogIn, browseButtonIn, isRequiredIn, null);
    }

    public ResourceSelectorPanel(CanBeShownParent parentDialogIn, CsiBrowseButton browseButtonIn, boolean isRequiredIn, String prefixIn) {

        super(parentDialogIn, isRequiredIn);
        _prefixText = prefixIn;
        _prefixSize = (null != _prefixText) ? prefixIn.length() : 0;

        try {

            //
            // Initialize control values
            //
            setControlValues(SelectorMode.SIMPLE_LOCAL_BROWSE);

            //
            // Identify browsing object.
            //
            browseButton = browseButtonIn;

            //
            // Initialize the display objects
            //
            initializeObject();

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 18, myException);
        }
    }

    public ResourceSelectorPanel(CanBeShownParent parentDialogIn, SelectorMode modeIn) {

        this(parentDialogIn, modeIn, true);
    }

    public ResourceSelectorPanel(CanBeShownParent parentDialogIn, SelectorMode modeIn, String prefixIn) {

        this(parentDialogIn, modeIn, true, prefixIn);
    }

    public ResourceSelectorPanel(CanBeShownParent parentDialogIn, SelectorMode modeIn, int widthIn, int heightIn, boolean isRequiredIn) {

        this(parentDialogIn, modeIn, widthIn, heightIn, isRequiredIn, null);
    }

    public ResourceSelectorPanel(CanBeShownParent parentDialogIn, SelectorMode modeIn, int widthIn, int heightIn, boolean isRequiredIn, String prefixIn) {

        this(parentDialogIn, modeIn, isRequiredIn, prefixIn);

        try {

            //
            // Set dimensions for the widget
            //
            setPixelSize(widthIn, heightIn);

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 19, myException);
        }
    }

    public ResourceSelectorPanel(CanBeShownParent parentDialogIn, SelectorMode modeIn, int widthIn, int heightIn) {

        this(parentDialogIn, modeIn, widthIn, heightIn, true);
    }

    public ResourceSelectorPanel(CanBeShownParent parentDialogIn, SelectorMode modeIn, int widthIn, int heightIn, String prefixIn) {

        this(parentDialogIn, modeIn, widthIn, heightIn, true, prefixIn);
    }

    //
    //
    //
    public void resetDisplay(AclResourceType resourceTypeIn, String infoStringIn, List<T> listIn) {

        resetDisplay(resourceTypeIn, infoStringIn, null, listIn);
    }
    
    //
    //
    //
    public void resetDisplay(AclResourceType resourceTypeIn, String infoStringIn, String defaultIn, List<T> listIn) {

        try {

            initializeDisplay(resourceTypeIn, infoStringIn, defaultIn);
            loadSelectionList(listIn, listIn);

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 20, myException);
        }
    }

    //
    //
    //
    public void initializeDisplay(AclResourceType resourceTypeIn, String infoStringIn, String defaultIn) {

        //
        // Setup the selection list info label
        //
        String myText = (null != infoStringIn) ? infoStringIn.trim() : _txtSelectionListLabel;

        selectionListLabel.setText(myText);
        selectionListLabel.setVisible(0 < myText.length());

        _default = (null != defaultIn) ? defaultIn : null;

        //
        //
        //
        _resourceType = resourceTypeIn;

        filterDropDown.setVisible(false);

        if (null != _resourceType) {

            _txtOverwriteDialogTitleString = _constants.resourceSelector_OverwriteDialogTitle(_resourceType.getLabel());
            _txtOverwriteDialogInfoString = _constants.resourceSelector_OverwriteInfoString(_resourceType.getLabel(),
                    Dialog.txtContinueButton, Dialog.txtCancelButton);

            if (AclResourceType.DATAVIEW.equals(_resourceType)
                    || AclResourceType.TEMPLATE.equals(_resourceType)
                    || AclResourceType.SAMPLE.equals(_resourceType)
                    || AclResourceType.GRAPH_THEME.equals(_resourceType)
                    || AclResourceType.MAP_THEME.equals(_resourceType)
                    || AclResourceType.DATA_TABLE.equals(_resourceType)) {

                filterDropDown.setVisible(!_suppressFilter);
            }
        }

        //
        // Setup the selection name info label
        //

        selectionNameLabel.setText(_txtSelectionNameLabel);

        if (!_hideRemarks) {

            //
            // Setup selection remarks info label
            //
            selectionRemarksLabel.setText(_txtSelectionRemarksLabel);

            //
            // Make remarks text area not resizeable
            //
            selectionRemarks.getElement().getStyle().setProperty("resize", "none");
        }

        //
        // Organize and resize the display
        //
        layoutDisplay();

        //
        // Enable components
        //
        enableInput();
    }

    //
    //
    //
    public void initializeDisplay(AclResourceType resourceTypeIn, String infoStringIn) {

        try {

            initializeDisplay(resourceTypeIn, infoStringIn, null);

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 21, myException);
        }
    }

    public boolean hasSelectionList() {

        return (null != _resourceListStore) && (0 < _resourceListStore.size());
    }

    public void replaceSelectionMonitor(SelectionChangedHandler<T> selectionMonitorIn) {

        _selectionMonitor = selectionMonitorIn;
    }

    public void setPreSelect(String keyIn) {

        try {

            boolean myPreSelectFlag = ((null != keyIn) && (0 < keyIn.trim().length()));

            _preSelect = myPreSelectFlag ? keyIn.trim() : null;

            if (myPreSelectFlag) {

                selectionName.setText(_preSelect);

                if ((null != selectionList) && (null != _resourceListStore)) {

                    for (int i = 0; _resourceListStore.size() > i; i++) {

                        T myItem = _resourceListStore.get(i);
                        String myLabel = myItem.getDisplayString(_prefixSize);

                        if (myPreSelectFlag && _preSelect.equals(myItem.getKey())) {

//                            if (_isSingle) {

                                selectionList.scrollToAndSelect(myLabel);
                                _selection = selectionList.getSelectionModel().getSelectedItem();
                                selectionName.setText(myLabel);
//                            }
                            myPreSelectFlag = false;
                        }
                        getSelectionMap().put(myLabel, i);
                    }
                }
                selectionName.setFocus(true);
                selectionName.selectAll();

            } else if ((null != selectionList) && (null != _resourceListStore)) {

                for (int i = 0; _resourceListStore.size() > i; i++) {

                    T myItem = _resourceListStore.get(i);
                    getSelectionMap().put(myItem.getDisplayString(_prefixSize), i);
                }
            }

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 22, myException);
        }
    }

    public void setCurrentName(String nameIn) {

        if ((null != nameIn) && (0 < nameIn.trim().length())) {

            _currentName = nameIn.trim();
            adjustConflicts(_currentName);
            if (null == _default) {

                setDefaultName(_currentName);
            }
            if (null == _preSelect) {

                setPreSelect(_currentName);
            }
        }
    }

    public void setDefaultName(String nameIn) {

        _default = nameIn.trim();

        if ((null != _default) && (0 < _default.length())) {

            _baseName = _default;
        }
    }

    public void rejectAllConflicts() {

        _rejectAllConflicts = true;
    }

    public void suppressFilter() {

        _suppressFilter = true;
    }



    public ClickHandler addReloadButtonClickHandler(ClickHandler handlerIn) {
        _reloadClickHandler = handlerIn;
        return handlerIn;
    }

    public ClickHandler getReloadClickHandler() {
            return _reloadClickHandler;
    }

    public HandlerRegistration addResourceSelectionEventHandler(ResourceSelectionEventHandler handlerIn) {

        try {

            return getEventManager().addHandler(ResourceSelectionEvent.type, handlerIn);

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 24, myException);
        }
        return null;
    }
    
    public HandlerRegistration addRefreshRequiredEventHandler(RefreshRequiredEventHandler handlerIn) {
        return getEventManager().addHandler(RefreshRequiredEvent.type, handlerIn);
    }

    public void clearSelection() {

        try {

            selectionList.getSelectionModel().deselectAll();

            selectionName.setText(null);
            if (!_hideRemarks) {

                selectionRemarks.setText(null);
            }
            checkSelectionName(null);

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 25, myException);
        }
    }
    
    public String getText() {

        try {

            String myResult = getTrimmedSelectionName();

            T mySelection = getSelection();

            if (null != mySelection) {

                if (null != mySelection.getKey()) {

                    myResult = mySelection.getKey();
                }
            }

            return myResult;

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 28, myException);
        }
        return null;
    }

    public void enableInput() {

        try {

            selectionList.setEnabled(true);

            //
            // Tag the text boxes as either read-only (if open mode) or editable (if save mode)
            //
            selectionName.setEnabled(_isWrite);
            selectionName.setReadOnly(!_isWrite);

            if (!_hideRemarks) {

                selectionRemarks.setEnabled(_isSave);
                selectionRemarks.setReadOnly(!_isSave);
            }

            if ((null != browseButton) && (null == _rejectionList)) {

                _rejectionList = new ArrayList<Map<String, T>>();

                if ((null != _existenceMap) && (0 < _existenceMap.size())) {
                    _rejectionList.add(_existenceMap);
                }

                if ((null != _rejectionMap) && (0 < _rejectionMap.size())) {
                    _rejectionList.add(_rejectionMap);
                }

                browseButton.initialize((0 < _rejectionList.size()) ? _rejectionList : null, _fileTypes);
            }

            beginMonitoring();

            DeferredCommand.add(new Command() {
                public void execute() {
                    grabFocus();
                }
            });

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 29, myException);
        }
    }
    
    public void setFileTypes(List<String> fileTypesIn) {

        try {

            _fileTypes = UploadWidget.formatFileTypes(fileTypesIn);

            if (selectionList.isEnabled()) {

                if (null != browseButton) {

                    browseButton.initialize(((null != _rejectionList) && (0 < _rejectionList.size()))
                            ? _rejectionList : null, _fileTypes);
                }
            }

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 30, myException);
        }
    }
    
    public void grabFocus() {

        try {

            if (_isWrite) {

                if (null != selectionName) {

                    selectionName.setFocus(true);
                }

            } else if ((null != selectionList) && (0 < selectionList.getItemCount())) {

                if (null == _selection) {

                    selectionList.setFocus(0);
                    selectionList.getSelectionModel().select(0, false);
                }

            } else {

                DeferredCommand.add(new Command() {
                    public void execute() {
                        grabFocus();
                    }
                });
            }

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 31, myException);
        }
    }

    public T getSelection() {

        try {

            T myDefault = _selection;

            String myKey = getTrimmedSelectionName();

            if ((null != _selectionMap) && (_selectionMap.containsKey(myKey))) {

                Integer myIndex = _selectionMap.get(myKey);

                _selection = (null != myIndex) ? _resourceListStore.get(myIndex) : myDefault;
            }
            return _selection;

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 32, myException);
        }
        return null;
    }

    public List<T> getSelectionList() {

        return selectionList.getSelectionModel().getSelectedItems();
    }

    public String getName() {

        try {

            return _isWrite ? getSelectionName() : (null != _selection) ? _selection.getName() : null;

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 33, myException);
        }
        return null;
    }
    
    public String getRemarks() {

        try {

            return _isWrite ? getSelectionRemarks() : (null != _selection) ? _selection.getRemarks() : null;

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 34, myException);
        }
        return null;
    }
    
    public String getKey() {

        try {

            return (null != getSelection()) ? _selection.getKey() : null;

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 35, myException);
        }
        return null;
    }
    
    public String getSelectionKey() {

        try {

            return (null != getSelection()) ? _selection.getKey() : null;

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 36, myException);
        }
        return null;
    }

    public boolean isOkToLeave() {

        try {

            return ((!isRequired()) || isSelectionNameValid(getTrimmedSelectionName()));

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 37, myException);
        }
        return false;
    }
    
    public void destroy() {

        try {

            selectionListLabel = null;
            selectionNameLabel = null;
            selectionRemarksLabel = null;
            selectionList = null;
            selectionName = null;
            selectionRemarks = null;
            browseButton = null;

            _txtDialogTitleString = null;
            _txtOverwriteDialogTitleString = null;
            _txtOverwriteDialogInfoString = null;

            _resourceListStore = null;
            _selectionMap = null;
            _rejectionMap = null;
            _resourceType = null;
            _nameIsValid = false;
            _isWrite = false;
            _isSave = false;
            _processingRequest = false;
            _hideRemarks = false;
            _selection = null;

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 38, myException);
        }
    }
    
    @Override
    public void suspendMonitoring() {

        _monitoring = false;
    }

    @Override
    public void beginMonitoring() {

        try {

            if (! _monitoring) {

                _monitoring = true;
            }
            checkValidity();
            fireEvent(new ValidityReportEvent(isOkToLeave()));

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 39, myException);
        }
    }

    public void handleCarriageReturn() {

        try {

            if (!(_isSave || _isWrite)) {

                if (isSelectionNameValid(getTrimmedSelectionName())) {

                    super.handleCarriageReturn();
                }
            }

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 40, myException);
        }
    }

    public void setMultiSelection(boolean multiFlagIn) {

        clearSelection();
        _isSingle = !multiFlagIn;
        selectionList.getSelectionModel().setSelectionMode(_isSingle ? SelectionMode.SINGLE : SelectionMode.MULTI);
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    protected void initializeObject() {

        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets();
        
        //
        // Wire in the handlers
        //
        wireInHandlers();
    }
    
    protected void wireInHandlers() {
        
        //
        // Set up handlers to capture changes in the resource name
        // in order to recognize conflicts with existing resources
        // when naming a new resource
        //
        selectionName.addKeyUpHandler(handleNewTextBoxKeyUp);
        selectionName.addDropHandler(handleTextBoxDrop);
        selectionName.addChangeHandler(handleTextBoxChange);
        selectionName.addClickHandler(handleTextBoxClick);

        //
        //Attach an event handler to the selection list for updating text boxes
        //
        selectionList.getSelectionModel().addSelectionChangedHandler(selectionListChangeHandler);
    }
    
    protected void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn) {
        
        executeRequested();
    }

    protected void layoutDisplay() {
        
        int myWidth = _width - (2 * Dialog.intMargin);
        int mySelectionLabelTop = (null != browseButton) ? Dialog.intMargin + (Dialog.intMargin / 2) : Dialog.intMargin;
        int mySelectionLabelRightMargin = (null != browseButton) ? ((2 * Dialog.intMargin) + _buttonWidth) : Dialog.intMargin;
        int myListLabelHeight = (0 < selectionListLabel.getText().length()) ? Dialog.intLabelHeight : 0;
        int myListTop = mySelectionLabelTop + myListLabelHeight;
        int myDropDownWidth = _width / 3;
        int myDropDownHeight = Dialog.intTextBoxHeight + (Dialog.intMargin / 2);
        int myRemarksHeight = _hideRemarks ? 0 : (360 > _height) ? 50 : 70;
        int myRemarksLabelHeight = _hideRemarks ? 0 : Dialog.intLabelHeight;
        int myListHeight = _height - myListTop - myDropDownHeight - Dialog.intMargin - Dialog.intTextBoxHeight - Dialog.intMargin - myRemarksLabelHeight - myRemarksHeight;
        int myDropDownTop = myListTop + myListHeight + (Dialog.intMargin / 2);
        int myNameLabelTop = myDropDownTop + Dialog.intTextBoxHeight + (Dialog.intMargin / 2) - Dialog.intLabelHeight;
        int myNameEntryTop = myNameLabelTop + Dialog.intLabelHeight;
        int myRemarksLabelTop = myNameEntryTop + Dialog.intTextBoxHeight + Dialog.intMargin;
        int myRemarksEntryTop = myRemarksLabelTop + myRemarksLabelHeight;
        int myDeltaHeight = (Dialog.intTextBoxHeight - Dialog.intLabelHeight) / 2;
        /*
        (3 * Dialog.intMargin) - Dialog.intTextBoxHeight -
         */
        _selectionWidth = myWidth - 14;

        if (0 < myListLabelHeight) {

            setWidgetTopHeight(selectionListLabel, mySelectionLabelTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetLeftRight(selectionListLabel, Dialog.intMargin, Unit.PX, mySelectionLabelRightMargin, Unit.PX);
        }

        selectionList.setPixelSize(myWidth - 2, myListHeight - 2);
        setWidgetTopHeight(selectionList, myListTop, Unit.PX, myListHeight, Unit.PX);
        setWidgetLeftRight(selectionList, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);

        setWidgetTopHeight(selectionNameLabel, myNameLabelTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
        setWidgetLeftRight(selectionNameLabel, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);

        if (filterDropDown instanceof Component) {
            Component component = (Component) filterDropDown;
            component.setWidth(myDropDownWidth);
        }else{
            filterDropDown.asWidget().setWidth(myDropDownWidth+"px");
        }

        setWidgetTopHeight(filterDropDown, myDropDownTop, Unit.PX, myDropDownHeight, Unit.PX);
        setWidgetRightWidth(filterDropDown, 0, Unit.PX, myDropDownWidth, Unit.PX);

        outerPanel.setPixelSize(_selectionWidth + Dialog.intMargin, Dialog.intTextBoxHeight);
        setWidgetTopHeight(outerPanel, myNameEntryTop, Unit.PX, Dialog.intTextBoxHeight, Unit.PX);
        setWidgetLeftRight(outerPanel, Dialog.intMargin, Unit.PX, -3, Unit.PX);

        if (0 < _prefixSize) {

            selectionName.setWidth(Integer.toString(((2 * _selectionWidth) /3))+ "px");
            selectionName.getElement().getStyle().setMarginLeft(5, Unit.PX);
            selectionName.getElement().getStyle().setMarginRight(-3, Unit.PX);

        } else {
            selectionName.setWidth(Integer.toString(_selectionWidth) + "px");
            selectionName.getElement().getStyle().setMargin(0, Unit.PX);
        }

        if (!_hideRemarks) {
            
            setWidgetTopHeight(selectionRemarksLabel, myRemarksLabelTop, Unit.PX, Dialog.intLabelHeight, Unit.PX);
            setWidgetLeftRight(selectionRemarksLabel, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);
            
            selectionRemarks.setPixelSize((myWidth - 14), (myRemarksHeight - Dialog.intMargin));
            setWidgetTopHeight(selectionRemarks, myRemarksEntryTop, Unit.PX, myRemarksHeight, Unit.PX);
            setWidgetLeftRight(selectionRemarks, Dialog.intMargin, Unit.PX, Dialog.intMargin, Unit.PX);
        }
        
        if (null != browseButton) {
            
            browseButton.setPixelSize(_buttonWidth, Dialog.intButtonHeight);
            setWidgetTopHeight(browseButton.asWidget(), 0, Unit.PX, Dialog.intButtonHeight, Unit.PX);
            setWidgetRightWidth(browseButton.asWidget(), Dialog.intMargin, Unit.PX, _buttonWidth, Unit.PX);
        }
    }
    
    protected String filterDrop(String dataIn) {
        
        StringBuilder myBuffer = new StringBuilder();
        
        for (char myCharacter : dataIn.toCharArray()) {
            
            if (31 < myCharacter) {
                
                myBuffer.append(myCharacter);
                
            } else {
                
                myBuffer.append(' ');
            }
        }
        return myBuffer.toString();
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void loadSelectionList(List<T> selectionListIn, List<T> existenceListIn) {

        try {

            if ((null != selectionList) && (null != _resourceListStore)) {

                T myAlternate = null;

                _selection = null;
                _resourceListStore.clear();
                if ((null != selectionListIn) && (0 < selectionListIn.size())) {

                    _resourceListStore.addAll(selectionListIn);
                }
                _selectionMap = new HashMap<String, Integer>();
                _rejectionList = null;
                selectionName.setText(_default);

                _existenceMap = new HashMap<String, T>();

                if (null != existenceListIn) {

                    for (T myResource : existenceListIn) {

                        String myKey = myResource.getDisplayString(_prefixSize);

                        if (myResource.getKey().equals(_preSelect)) {

                            _selection = myResource;
                            break;
                        }

                        if ((null != _xferMap) && (_xferMap.containsKey(myKey))) {

                            myResource.setSpecial();

                            if (null == _selection) {

                                _selection = myResource;
                            }
                        }

                        _existenceMap.put(myKey, myResource);

                        if ((null == _selection) && (myKey.equals(_default))) {

                            myAlternate = myResource;
                        }
                    }
                }
                if (null == _selection) {

                    _selection = myAlternate;
                }
                if (null != _selection) {

                    int mySelection = _resourceListStore.indexOf(_selection);

                    selectionList.getSelectionModel().select(_selection, false);
                    selectionName.setText(_selection.getDisplayString(_prefixSize));
                    selectionList.setFocus(mySelection);
                }
                if ((!_suppressNameModification) && _isWrite && _provideName) {

                    String myDefaultName = getTrimmedSelectionName();

                    if ((null == myDefaultName) || (0 == myDefaultName.length())
                            || ((null != _existenceMap) && _existenceMap.containsKey(myDefaultName))
                            || ((null != _rejectionMap) && _rejectionMap.containsKey(myDefaultName))) {

                        if (_provideSourceName) {

                            int myDefaultCount = (_incrementImmediately) ? 1 : 0;

                            if ((null != myDefaultName) && (0 < myDefaultName.length())) {

                                _baseName = myDefaultName;
                            }

                            if (_incrementImmediately) {

                                myDefaultName = _bracketDefault ?  "<" + _baseName + " 1>" : _baseName + " 1";

                            } else {

                                myDefaultName = _baseName;
                            }

                            while (((null != _existenceMap) && _existenceMap.containsKey(myDefaultName))
                                    || ((null != _rejectionMap) && _rejectionMap.containsKey(myDefaultName))) {
//                        while (((null != _rejectionMap) && _rejectionMap.containsKey(myDefaultName))) {

                                if (_bracketDefault) {

                                    myDefaultName = "<" + _baseName + " " + Integer.toString(++myDefaultCount) + ">";

                                } else {

                                    myDefaultName = _baseName + " " + Integer.toString(++myDefaultCount);
                                }
                            }
                        }
                    }
                    selectionName.setText(myDefaultName);
                    selectionName.setFocus(true);
                    selectionName.selectAll();
                }
                setPreSelect(_preSelect);
                enableInput();
                checkSelectionName(getTrimmedSelectionName());

            } else {

                _shadowList = selectionListIn;
            }

        } catch (Exception myException) {

            Dialog.showException("ResourceSelectorPanel", 23, myException);
        }
    }

    private void setControlValues(SelectorMode modeIn) {

        _requiredPermission = (_isSave) ? AclControlType.DELETE : AclControlType.READ;

        _isSave = modeIn.isSave();
        _isWrite = modeIn.isWrite();
        _needsBrowse = modeIn.needsBrowse();
        _hideRemarks = modeIn.hideRemarks();
        _mustMatch = modeIn.mustMatch();
        _isSingle = modeIn.isSingleSelection();
        _rejectAll = modeIn.mustRejectAll();
    }

    private void createList() {

        if(null != _prefixText) {
            selectionList = new CsiListViewReloaded(this);
        } else {
            selectionList = new CsiListView<>();
        }

        _resourceListStore = selectionList.getStore();
        selectionList.getSelectionModel().setSelectionMode((_isSingle) ? SelectionMode.SINGLE : SelectionMode.MULTI);
        if (null != _shadowList) {

            loadSelectionList(_shadowList, _shadowList);
            _shadowList = null;
        }
    }

    private String getSelectionRemarks() {
        
        String myRemarks = (!_hideRemarks) ? selectionRemarks.getText() : null;

        return (null != myRemarks) ? myRemarks : "";
    }

    private String getSelectionName() {
        
        String myName = getTrimmedSelectionName();

        if (null != myName) {

            Integer myIndex = getSelectionMap().get(myName);

            if (null != myIndex) {

                _selection = _resourceListStore.get(myIndex);
            }
        }
        return (null != myName) ? myName : "";
    }
    
    private void disableAll() {
        
        suspendMonitoring();
        
        selectionList.setEnabled(false);
        selectionName.setEnabled(false);
        selectionName.setReadOnly(true);

        if (!_hideRemarks) {
            
            selectionRemarks.setEnabled(false);
            selectionRemarks.setReadOnly(true);
        }
        
        if (null != browseButton) {
            
            browseButton.setEnabled(false);
        }
    }

    private void createWidgets() {
        
        selectionListLabel = new Label();
        add(selectionListLabel);

        if (_needsBrowse) {

            if (null == browseButton) {

                browseButton = new UploadWidget(_txtBrowseLocally, true);
            }
            browseButton.addTransferCompleteEventHandler(handleUploadComplete);
            browseButton.showSuccessPopup(true);
            add(browseButton.asWidget());
        }

        createList();
        add(selectionList);
        
        selectionNameLabel = new Label();
        add(selectionNameLabel);

        filterDropDown = new ResourceFilterListBox();
        filterDropDown.setVisible(false);
        filterDropDown.addSelectionChangedHandler(handleFilterChange);
        add(filterDropDown);

        outerPanel = new HorizontalPanel();
        outerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        outerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        outerPanel.getElement().getStyle().setMargin(0, Unit.PX);
        outerPanel.getElement().getStyle().setPadding(0, Unit.PX);
        innerPanel = new FlowPanel();
        innerPanel.getElement().getStyle().setMargin(0, Unit.PX);
        innerPanel.getElement().getStyle().setPadding(0, Unit.PX);
        selectionName = new TextBox();
        prefixLabel = new InlineLabel();
        if (0 < _prefixSize) {

            prefixLabel.setText(_prefixText);
            prefixLabel.setVisible(true);
            prefixLabel.getElement().getStyle().setFontSize(11, Unit.PT);

        } else {
            prefixLabel.setText("");
            prefixLabel.setVisible(false);
        }
        outerPanel.getElement().getStyle().setMargin(0, Unit.PX);
        innerPanel.add(prefixLabel);
        innerPanel.add(selectionName);
        outerPanel.add(innerPanel);
        add(outerPanel);

        if (!_hideRemarks) {
            
            selectionRemarksLabel = new Label();
            add(selectionRemarksLabel);
            
            selectionRemarks = new TextArea();
            add(selectionRemarks);
        }
    }

    private void performRequest(boolean forceOverwriteIn) {
        
        String myResourceName = getSelectionName();
        String myResourceRemarks = getSelectionRemarks();

        disableAll();
        //
        // Fire selection made event
        //
        fireEvent(new ResourceSelectionEvent<T>(_selection, myResourceName, myResourceRemarks, forceOverwriteIn));
    }
    
    private void executeRequested() {

        String myName = getTrimmedSelectionName();
        
        if (isSelectionNameValid(myName) || (!isRequired() && ((null == myName) || (0 == myName.length())))) {
            
            if (_isSave) {
                
                String myResourceName = getTrimmedSelectionName();

                if (((null != _selectionMap) && _selectionMap.containsKey(myResourceName))
                    || ((null != _conflictMap) && _conflictMap.containsKey(myResourceName))) {
                    
                    Dialog.showContinueDialog(_txtOverwriteDialogTitleString, _txtOverwriteDialogInfoString,
                                                handleOverwriteRequest, handleCancelRequest);

                } else {
                    
                    performRequest(false);
                }
                
            } else {
                
                performRequest(false);
            }
            
        } else {
            
            fireEvent(new ValidityReportEvent(false));
        }
    }
    
    private void checkValidity() {
        
        checkSelectionName(getTrimmedSelectionName());

        if (_monitoring ) {
            
            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }
    
    private void checkSelectionName(String nameIn) {
        
        if (isSelectionNameValid(nameIn) != _nameIsValid) {

            _nameIsValid = !_nameIsValid;
            //
            // Fire valid/invalid event
            //
            fireEvent(new ValidityReportEvent(_nameIsValid));
        }
    }
    
    private boolean isSelectionNameValid(String nameIn) {
        _nameInUse = false;
        String myName = (null != nameIn) ? nameIn.trim() : null;
        boolean myNameIsValid = (null != myName) && (0 < myName.length());
        
        if (myNameIsValid && !selectionName.isReadOnly()) {

            if ((null != _rejectionMap) && _rejectionMap.containsKey(myName)) {
                
                myNameIsValid = false;
                selectionName.getElement().getStyle().setColor(Dialog.txtErrorColor);
                
            } else {
                selectionName.getElement().getStyle().setColor(Dialog.txtLabelColor);
            }
        }
        return myNameIsValid;
    }

    private void requestFilteredResourceList(ResourceFilter resourceFilterIn) {

        VortexFuture<List<T>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(handleFilteredListRequestResponse);
            myVortexFuture.execute(ModelActionsServiceProtocol.class).getFilteredResourceList(_resourceType,
                    resourceFilterIn, _requiredPermission);
            showWatchBox();

        } catch (Exception myException) {

            Dialog.showException(_txtFailureDialogTitle, myException);
        }
    }

    private void adjustConflicts(String currentNameIn) {

        if (null != currentNameIn) {

            if (null != _rejectionMap) {

                T myResource = _rejectionMap.get(currentNameIn);

                if (null != myResource) {

                    _currentResource = myResource;
                    _rejectionMap.remove(currentNameIn);
                }
            }

            if (null != _conflictMap) {

                T myResource = _conflictMap.get(currentNameIn);

                if (null != myResource) {

                    _currentResource = myResource;
                    _conflictMap.remove(currentNameIn);
                }
            }

            if ((null != _rejectionList) && (0 < _rejectionList.size())) {

                for (Map<String, T> myMap : _rejectionList) {

                    if (null != myMap) {

                        T myResource = myMap.get(currentNameIn);

                        if (null != myResource) {

                            _currentResource = myResource;
                            myMap.remove(currentNameIn);
                        }
                    }
                }
            }
            if (null != _currentResource) {

                selectionList.getSelectionModel().select(_currentResource, false);
            }
        }
    }
    
    private String getTrimmedSelectionName() {
        
		if (null != selectionName) {
		
			String myName = selectionName.getText();

			return (null != myName) ? myName.trim() : null;
		}
		return null;
    }

    private Map<String, Integer> getSelectionMap() {

        if (null == _selectionMap) {

            _selectionMap = new HashMap<String, Integer>();
        }
        return _selectionMap;
    }
}
