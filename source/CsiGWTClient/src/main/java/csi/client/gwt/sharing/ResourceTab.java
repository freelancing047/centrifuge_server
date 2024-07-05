package csi.client.gwt.sharing;

import java.util.*;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.RenameResourceDialog;
import csi.client.gwt.csi_resource.filters.ResourceFilterDialog;
import csi.client.gwt.csiwizard.wizards.ClassificationWizard;
import csi.client.gwt.csiwizard.wizards.ClassificationWizard.ClassificationCallback;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.events.TransferCompleteEvent;
import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.csi_resource.ExportDialog;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.AbstractCsiTab;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.AmberButton;
import csi.client.gwt.widget.buttons.GreenButton;
import csi.client.gwt.widget.buttons.BlueButton;
import csi.client.gwt.widget.buttons.CyanButton;
import csi.client.gwt.widget.buttons.RedButton;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.client.gwt.widget.list_boxes.ResourceFilterListBox;
import csi.server.common.dto.ClientStartupInfo;
import csi.server.common.dto.DataPair;
import csi.server.common.dto.Response;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.service.api.ModelActionsServiceProtocol;
import csi.server.common.service.api.UserAdministrationServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.StringUtil;
import csi.server.common.util.ValuePair;

public abstract class ResourceTab extends AbstractCsiTab {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface ResourceTabUiBinder extends UiBinder<Widget, ResourceTab> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    LayoutPanel container;

    // TOP WIDGETS

    @UiField
    ResourceFilterListBox filterDropDown;
    @UiField
    TextBox pattern;
    @UiField
    CsiStringListBox userFilter;
    @UiField
    CyanButton getButton;
    @UiField
    BlueButton newButton;
    @UiField
    BlueButton alternateButton;
    @UiField
    HorizontalPanel adminOnly;
    @UiField
    CyanButton clearButton;
    @UiField
    RadioButton replaceButton;
    @UiField
    RadioButton augmentButton;
    @UiField
    Label itemsReturnedLabel;
    @UiField
    CheckBox resetFilterCheckBox;

    // CENTER WIDGETS

    @UiField
    GridContainer gridContainer;

    // BOTTOM WIDGETS

    @UiField
    HorizontalPanel sharingPanel;
    @UiField
    GreenButton grantButton;
    @UiField
    RadioButton groupRadioButton;
    @UiField
    CsiStringListBox groupList;
    @UiField
    RadioButton userRadioButton;
    @UiField
    CsiStringListBox userList;
    @UiField
    CheckBox readCheckBox;
    @UiField
    CheckBox editCheckBox;
    @UiField
    CheckBox deleteCheckBox;
    @UiField
    VerticalPanel showAdmin;
    @UiField
    CheckBox ownerCheckBox;
    @UiField
    Label deleteSpacer;
    @UiField
    RedButton deleteButton;
    @UiField
    Label renameSpacer;
    @UiField
    BlueButton renameButton;
    @UiField
    Label editSpacer;
    @UiField
    BlueButton editButton;
    @UiField
    Label exportSpacer;
    @UiField
    BlueButton exportButton;
    @UiField
    Label shareSpacer;
    @UiField
    Label resourceOnly;
    @UiField
    Label privileges;
    @UiField
    BlueButton shareButton;
    @UiField
    Label classifySpacer;
    @UiField
    AmberButton classifyButton;
    @UiField
    Label launchSpacer;
    @UiField
    AmberButton launchButton;
    @UiField
    HorizontalPanel topContainer;
    @UiField
    HorizontalPanel bottomContainer;

    private com.github.gwtbootstrap.client.ui.Icon spinnerIcon = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final int _updateRequestSize = 20;
    private final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final String _augmentKey = "augmentDisplay";
    private static final String _resetFilterKey = "resetFilters";

    private static ResourceTabUiBinder uiBinder = GWT.create(ResourceTabUiBinder.class);

    private boolean _radioState;
    private ResourceFilter _resourceFilter = null;
    private ResourceFilter _priorFilter = null;
    private String _sharingSelection = null;
    private SharingGrid _info;
    protected Grid<SharingDisplay> _grid;
    private ResourceFilterDialog _filterEditDialog = null;
    private int _priorFilterChoice = 0;
    protected ResourceSharingView parent;
    protected String _classKey;

    protected Map<String, SharingDisplay> _selectionMap = null;

    private CheckBox[] _permissionCheckBoxes;
    private AclControlType[] _permissionTypes = {AclControlType.READ, AclControlType.EDIT, AclControlType.DELETE};
    private List<String> _partialDisplayList = new ArrayList<String>();
    private Set<String> _partialDisplaySet = new HashSet<String>();
    private boolean _busyFlag = false;
    private boolean _clearFlag = false;

    protected UserSecurityInfo _userInfo;
    protected ClientStartupInfo _startupInfo;
    protected MainPresenter _mainPresenter = null;

    protected abstract AclResourceType getResourceType();

    protected abstract ClickHandler getExportClickHandler();

    protected abstract ClickHandler getRenameClickHandler();

    protected abstract ClickHandler getEditClickHandler();

    protected abstract ClickHandler getClassificationHandler();

    protected abstract ClickHandler getLaunchClickHandler();

    protected abstract ClickHandler getCreateClickHandler();

    protected abstract ClickHandler getDeleteClickHandler();

    protected abstract String getEditButtonLabel();

    protected abstract String getCreateButtonLabel();

    protected abstract String getResourceTypeString();

    protected abstract String getResourceTypePluralString();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected VortexEventHandler<ValuePair<String, Boolean>> handleEditAuthorizationResponse
            = new AbstractVortexEventHandler<ValuePair<String, Boolean>>() {


        @Override
        public boolean onError(Throwable myException) {

            AclResourceType myType = getResourceType();
            String myTypeLabel = myType.getLabel();
            String myTitle = AclResourceType.DATAVIEW.equals(myType)
                    ? _constants.openResourceResponse_Title(myTypeLabel)
                    : _constants.editResourceResponse_Title(myTypeLabel);

            Display.error(myTitle, myException);
            return true;
        }

        @Override
        public void onSuccess(ValuePair<String, Boolean> responseIn) {

            if ((null != responseIn) && (null != responseIn.getValue1()
                    && (null != responseIn.getValue2() && responseIn.getValue2().booleanValue()))) {

                getEditClickHandler().onClick(null);

            } else {

                AclResourceType myType = getResourceType();
                String myTypeLabel = myType.getLabel();
                String myTitle = AclResourceType.DATAVIEW.equals(myType)
                        ? _constants.openResourceResponse_Title(myTypeLabel)
                        : _constants.editResourceResponse_Title(myTypeLabel);

                Display.error(myTitle, _constants.serverMessage_UserNotAuthorized());
            }
        }
    };

    protected ClickHandler getEditAuthorizationCheckHandler() {

        return new ClickHandler() {
            public void onClick(ClickEvent eventIn) {

                try {

                    VortexFuture<ValuePair<String, Boolean>> vortexFuture = WebMain.injector.getVortex().createFuture();
                    vortexFuture.addEventHandler(handleEditAuthorizationResponse);
                    vortexFuture.execute(ModelActionsServiceProtocol.class).isAuthorized(getSelection().getUuid(), AclControlType.EDIT);

                } catch (Exception myException) {

                    AclResourceType myType = getResourceType();
                    String myTypeLabel = myType.getLabel();
                    String myTitle = AclResourceType.DATAVIEW.equals(myType)
                            ? _constants.openResourceResponse_Title(myTypeLabel)
                            : _constants.editResourceResponse_Title(myTypeLabel);

                    Display.error(myTitle, myException);
                }
            }
        };
    }

    protected VortexEventHandler<ValuePair<String, Boolean>> handleRenameAuthorizationResponse
            = new AbstractVortexEventHandler<ValuePair<String, Boolean>>() {

        @Override
        public boolean onError(Throwable myException) {

            Display.error(_constants.renameResourceResponse_Title(getResourceType().getLabel()), myException);
            return true;
        }

        @Override
        public void onSuccess(ValuePair<String, Boolean> responseIn) {

            if ((null != responseIn) && (null != responseIn.getValue1()
                    && (null != responseIn.getValue2() && responseIn.getValue2().booleanValue()))) {

                getRenameClickHandler().onClick(null);

            } else {

                Display.error(_constants.renameResourceResponse_Title(getResourceType().getLabel()),
                        _constants.serverMessage_UserNotAuthorized());
            }
        }
    };

    protected VortexEventHandler<ValuePair<String, Boolean>> handleExportAuthorizationResponse
            = new AbstractVortexEventHandler<ValuePair<String, Boolean>>() {

        @Override
        public boolean onError(Throwable myException) {

            Display.error(_constants.renameResourceResponse_Title(getResourceType().getLabel()), myException);
            return true;
        }

        @Override
        public void onSuccess(ValuePair<String, Boolean> responseIn) {

            if ((null != responseIn) && (null != responseIn.getValue1()
                    && (null != responseIn.getValue2() && responseIn.getValue2().booleanValue()))) {

                getExportClickHandler().onClick(null);

            } else {

                Display.error(_constants.renameResourceResponse_Title(getResourceType().getLabel()),
                        _constants.serverMessage_UserNotAuthorized());
            }
        }
    };

    protected ClickHandler handleExportAuthorizationCheck = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            try {
                VortexFuture<ValuePair<String, Boolean>> vortexFuture = WebMain.injector.getVortex().createFuture();
                vortexFuture.addEventHandler(handleExportAuthorizationResponse);
                vortexFuture.execute(ModelActionsServiceProtocol.class).isAuthorized(getSelection().getUuid(), AclControlType.EXPORT);

            } catch (Exception myException) {

                Display.error(_constants.renameResourceResponse_Title(getResourceType().getLabel()), myException);
            }
        }
    };

    protected VortexEventHandler<ValuePair<String, Boolean>> handleLaunchAuthorizationResponse
            = new AbstractVortexEventHandler<ValuePair<String, Boolean>>() {

        @Override
        public boolean onError(Throwable myException) {

            Display.error(_constants.renameResourceResponse_Title(getResourceType().getLabel()), myException);
            return true;
        }

        @Override
        public void onSuccess(ValuePair<String, Boolean> responseIn) {

            if ((null != responseIn) && (null != responseIn.getValue1()
                    && (null != responseIn.getValue2() && responseIn.getValue2().booleanValue()))) {

                getLaunchClickHandler().onClick(null);

            } else {

                Display.error(_constants.launchResourceResponse_Title("Template"),
                        _constants.serverMessage_UserNotAuthorized());
            }
        }
    };

    protected ClickHandler handleLaunchAuthorizationCheck = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            try {
                VortexFuture<ValuePair<String, Boolean>> vortexFuture = WebMain.injector.getVortex().createFuture();
                vortexFuture.addEventHandler(handleLaunchAuthorizationResponse);
                vortexFuture.execute(ModelActionsServiceProtocol.class).isAuthorized(getSelection().getUuid(), AclControlType.READ);

            } catch (Exception myException) {

                Display.error(_constants.renameResourceResponse_Title(getResourceType().getLabel()), myException);
            }
        }
    };

    protected ClickHandler handleRenameAuthorizationCheck = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            try {
                VortexFuture<ValuePair<String, Boolean>> vortexFuture = WebMain.injector.getVortex().createFuture();
                vortexFuture.addEventHandler(handleRenameAuthorizationResponse);
                vortexFuture.execute(ModelActionsServiceProtocol.class).isOwner(getSelection().getUuid());

            } catch (Exception myException) {

                Display.error(_constants.renameResourceResponse_Title(getResourceType().getLabel()), myException);
            }
        }
    };

    protected ValueChangeHandler<Boolean> readCheckBoxHandler = new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {

            if (!event.getValue()) {

                editCheckBox.setValue(false);
            }
        }
    };

    protected ValueChangeHandler<Boolean> editCheckBoxHandler = new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {

            if (event.getValue()) {

                readCheckBox.setValue(true);
            }
        }
    };

    protected ClickHandler handleExportRequest = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new ExportDialog(getSelection().getUuid(), getResourceType())).show();

            } catch (Exception myException) {

                Display.error("ResourceTab", 1, myException);
            }
        }
    };

    protected ClickHandler handleRenameRequest = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            (new RenameResourceDialog(getSelection().getUuid(), getResourceType(), getSelection().getName(),
                    _constants.renamingTitle(AclResourceType.DATAVIEW.equals(getResourceType())
                            ? _constants.selectedDataView()
                            : _constants.selectedTemplate()))).show();
        }
    };

    protected ClickHandler handleCancelEditResponse = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            filterDropDown.setSelectedIndex(_priorFilterChoice);
            setEnableDisable();
        }
    };

    protected ClickHandler handleEditFilterResponse = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            ResourceFilter myFilter = _filterEditDialog.getFilter();

            if (null != myFilter) {

                _resourceFilter = myFilter;
                _priorFilter = _resourceFilter;
                _priorFilterChoice = 1;
            }
            filterDropDown.setSelectedIndex(_priorFilterChoice);
            setEnableDisable();
        }
    };

    protected ClickHandler handleCreateFilterResponse = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            ResourceFilter myFilter = _filterEditDialog.getFilter();

            if (null != myFilter) {

                _resourceFilter = myFilter;
                _priorFilter = _resourceFilter;
                _priorFilterChoice = 1;
            }
            setEnableDisable();
        }
    };

    protected SelectionChangedHandler handleFilterChange = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            int myFilterChoice = filterDropDown.getSelectedIndex();

            switch (myFilterChoice) {

                case 0:

                    _priorFilterChoice = 0;
                    _resourceFilter = null;
                    break;

                case 1:

                    _filterEditDialog = new ResourceFilterDialog(_priorFilter, handleEditFilterResponse,
                                                                handleCancelEditResponse, handleCreateFilterResponse,
                                                                filterDropDown);
                    _filterEditDialog.show(null);
                    break;

                default:

                    ResourceFilter myFilter = filterDropDown.getSelectedItem();
                    if (null != myFilter) {

                        _resourceFilter = myFilter;
                    }
                    _priorFilterChoice = myFilterChoice;
                    break;
            }
            setEnableDisable();
        }
    };

    protected SelectionChangedHandler<SharingDisplay> handleGridSelectionChange
            = new SelectionChangedHandler<SharingDisplay>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<SharingDisplay> event) {
            setEnableDisable();
        }
    };

    protected SelectionChangedHandler<String> handleDropDownSelectionChange = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            setEnableDisable();
        }
    };

    protected KeyUpHandler handleKeyPress = new KeyUpHandler() {
        public void onKeyUp(KeyUpEvent eventIn) {
            setEnableDisable();
        }
    };

    protected DropHandler handleDataDrop = new DropHandler() {
        public void onDrop(DropEvent eventIn) {
            setEnableDisable();
        }
    };

    protected ClickHandler handleMouseClick = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {
            setEnableDisable();
        }
    };

    protected ClickHandler handleSharingInformationRequest = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            MainPresenter myMainPresenter = getMainPresenter();

            myMainPresenter.setDialogPreference(_classKey, _augmentKey, augmentButton.getValue());
            myMainPresenter.setDialogPreference(_classKey, _resetFilterKey, resetFilterCheckBox.getValue());
            requestSharingInformation();
        }
    };

    protected ClickHandler buildDeleteDialog() {

        return buildDeleteDialog(null);
    }

    private ClickHandler handleDeleteConfirmed = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            List<String> myItemList = new ArrayList<String>();
            List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();

            _selectionMap = new HashMap<String, SharingDisplay>();

            if ((null != mySelectedGridList) && (0 < mySelectedGridList.size())) {

                for (int i = 0; mySelectedGridList.size() > i; i++) {

                    SharingDisplay myItem = mySelectedGridList.get(i);

                    _selectionMap.put(myItem.getUuid(), myItem);
                    myItemList.add(myItem.getUuid());
                }
                requestDeleteResources(myItemList);
            }
        }
    };

    protected ClickHandler buildDeleteDialog(final ClickHandler handlerIn) {

        return new ClickHandler() {

            final ClickHandler myHandler = (null != handlerIn) ? handlerIn : handleDeleteConfirmed;
            public void onClick(ClickEvent eventIn) {

                List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();
                int myCount = (null != mySelectedGridList) ? mySelectedGridList.size() : 0;

                if (0 < myCount) {

                    String myResourceType = (1 < myCount) ? getResourceTypePluralString() : getResourceTypeString();
                    String myMessage = _constants.warningDialog_DeleteResourceMessage(myCount, myResourceType);

                    Dialog.showYesNoDialog(_constants.warningDialog_DeleteResourceTitle(myResourceType),
                            myMessage, myHandler);
                }
            }
        };
    }

    protected ClickHandler handleEditSharingRequest = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();

            if ((null != mySelectedGridList) && (0 < mySelectedGridList.size())) {

                List<String> myItemList = new ArrayList<String>();

                for (int i = 0; mySelectedGridList.size() > i; i++) {

                    SharingDisplay myItem = mySelectedGridList.get(i);

                    myItemList.add(myItem.getUuid());
                }
                (new ResourceSharingDialog(getResourceType(), myItemList, handleUpdateInformationResponse)).show();
            }
        }
    };

    protected ClickHandler handleUpdateSharingRequest = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            List<String> myItemList = new ArrayList<String>();
            List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();

            _selectionMap = new HashMap<String, SharingDisplay>();

            if ((null != mySelectedGridList) && (0 < mySelectedGridList.size())) {

                for (int i = 0; mySelectedGridList.size() > i; i++) {

                    SharingDisplay myItem = mySelectedGridList.get(i);

                    _selectionMap.put(myItem.getUuid(), myItem);
                    myItemList.add(myItem.getUuid());
                }
                requestSharingUpdate(myItemList, _sharingSelection, getPriviledgeRequest(), ownerCheckBox.getValue());
            }
        }
    };

    private VortexEventHandler<List<List<String>>> handleDeleteResponse
            = new AbstractVortexEventHandler<List<List<String>>>() {
        @Override
        public boolean onError(Throwable myException) {

            watchBox.hide();
            Display.error("ResourceTab", 2, myException);
            return true;
        }

        @Override
        public void onSuccess(List<List<String>> responseIn) {

            if ((null != _selectionMap) && (0 < _selectionMap.size())) {

                List<String> myDeletedList = (0 < responseIn.size()) ? responseIn.get(0) : null;
                List<String> mySkippedList = (1 < responseIn.size()) ? responseIn.get(1) : null;
                ListStore<SharingDisplay> myGridStore = _grid.getStore();

                watchBox.hide();

                if ((null != myDeletedList) && (0 < myDeletedList.size())) {

                    AbstractDataViewPresenter myPresenter
                            = AclResourceType.DATAVIEW.equals(getResourceType())
                            ? WebMain.injector.getMainPresenter().getDataViewPresenter(true) : null;
                    String myUuid = (null != myPresenter) ? myPresenter.getUuid() : null;

                    if (null != myUuid) {

                        for (String myItem : myDeletedList) {

                            SharingDisplay myInfo = _selectionMap.get(myItem);

                            if (null != myInfo) {

                                if (myUuid.equals(myInfo.getUuid())) {

                                    myPresenter.markDeleted();
                                }
                                myGridStore.remove(myInfo);
                            }
                        }

                    } else {

                        for (String myItem : myDeletedList) {

                            SharingDisplay myInfo = _selectionMap.get(myItem);

                            if (null != myInfo) {

                                myGridStore.remove(myInfo);
                            }
                        }
                    }
                    _grid.getView().refresh(false);
                }

                if ((null != mySkippedList) && (0 < mySkippedList.size())) {

                    StringBuilder myBuffer = new StringBuilder();

                    for (String myItem : mySkippedList) {

                        SharingDisplay myInfo = _selectionMap.get(myItem);

                        if (null != myInfo) {

                            myBuffer.append(' ');
                            myBuffer.append(myInfo.getName());
                            myBuffer.append(',');
                        }
                    }

                    Display.error(_constants.resourceTab_itemsCantBeDeleted()
                            + myBuffer.toString().substring(0, myBuffer.length() - 1) + ".");
                }
            }
        }
    };
    // TODO:
    // Response does NOT include ACL information
    protected VortexEventHandler<Response<String, List<SharingDisplay>>> handleRequestInformationResponse
            = new AbstractVortexEventHandler<Response<String, List<SharingDisplay>>>() {

        @Override
        public boolean onError(Throwable myException) {

            watchBox.hide();
            Display.error("ResourceTab", 3, myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<SharingDisplay>> responseIn) {

            watchBox.hide();

            if (ResponseHandler.isSuccess(responseIn)) {

                List<SharingDisplay> myNewList = responseIn.getResult();
                ListStore<SharingDisplay> myGridStore = _grid.getStore();
                int myCount = (null != myNewList) ? myNewList.size() : 0;
                List<String> myChangeList = null;

                if (replaceButton.getValue()) {

                    myGridStore.clear();
                    _partialDisplayList.clear();
                    _partialDisplaySet.clear();
                }
                if (0 < myCount) {

                    myChangeList = mergeGridInfo(myGridStore, myNewList, true, false);
                    recordPartial(myChangeList);
                }
                _grid.getView().refresh(false);
                if (resetFilterCheckBox.getValue()) {

                    filterDropDown.setSelectedIndex(0);
                    pattern.setText(null);
                    _priorFilterChoice = 0;
                    _resourceFilter = null;
                }
                itemsReturnedLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);
                itemsReturnedLabel.setText(_constants.returnCount(myNewList.size()));
                itemsReturnedLabel.setVisible(true);
            }
        }
    };
    //TODO:
    // Response includes ACL information
    protected VortexEventHandler<Response<String, List<SharingDisplay>>> handleAclInformationResponse
            = new AbstractVortexEventHandler<Response<String, List<SharingDisplay>>>() {

        @Override
        public boolean onError(Throwable myException) {

//            hideSpinner();
            Display.error("ResourceTab", 4, myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<SharingDisplay>> responseIn) {

//            hideSpinner();

            if (ResponseHandler.isSuccess(responseIn)) {

                List<SharingDisplay> myNewList = responseIn.getResult();
                ListStore<SharingDisplay> myGridStore = _grid.getStore();
                List<String> myChangeList = null;

                myChangeList = mergeGridInfo(myGridStore, myNewList, false, true);
                recordUpdate(myChangeList);
                _grid.getView().refresh(false);
            }
        }
    };
    //TODO:
    // Response includes ACL information
    protected VortexEventHandler<Response<String, List<SharingDisplay>>> handleUpdateInformationResponse
            = new AbstractVortexEventHandler<Response<String, List<SharingDisplay>>>() {

        @Override
        public boolean onError(Throwable myException) {

            watchBox.hide();
            Display.error("ResourceTab", 5, myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<SharingDisplay>> responseIn) {

            watchBox.hide();

            if (ResponseHandler.isSuccess(responseIn)) {

                List<SharingDisplay> myNewList = responseIn.getResult();
                ListStore<SharingDisplay> myGridStore = _grid.getStore();
                List<String> myChangeList = null;

                myChangeList = mergeGridInfo(myGridStore, myNewList, true, true);
                recordFull(myChangeList);
                _grid.getView().refresh(false);
                if (resetFilterCheckBox.getValue()) {

                    filterDropDown.setSelectedIndex(0);
                    pattern.setText(null);
                    _priorFilterChoice = 0;
                    _resourceFilter = null;
                }
            }
        }
    };
    //TODO:
    // Response includes ACL information
    protected VortexEventHandler<Response<String, DataPair<List<SharingDisplay>, List<DataPair<String, String>>>>>
            handleSharingUpdateResponse = new AbstractVortexEventHandler<Response<String, DataPair<List<SharingDisplay>, List<DataPair<String, String>>>>>() {
        @Override
        public boolean onError(Throwable myException) {

            watchBox.hide();
            Display.error("ResourceTab", 6, myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, DataPair<List<SharingDisplay>, List<DataPair<String, String>>>> responseIn) {

            watchBox.hide();

            if (ResponseHandler.isSuccess(responseIn)) {

                DataPair<List<SharingDisplay>, List<DataPair<String, String>>> myDoubleList = responseIn.getResult();
                List<SharingDisplay> myUpdateList = myDoubleList.getObjectOne();
                List<DataPair<String, String>> myReplacements = myDoubleList.getObjectTwo();
                ListStore<SharingDisplay> myGridStore = _grid.getStore();
                List<String> myChangeList = null;

                if ((null != myUpdateList) && (0 < myUpdateList.size())) {

                    myChangeList = mergeGridInfo(myGridStore, myUpdateList, true, true);
                    recordFull(myChangeList);
                    _grid.getView().refresh(false);
                }
                if ((null != myReplacements) && (0 < myReplacements.size())) {

                    StringBuilder myBuffer = new StringBuilder();

                    myBuffer.append(_constants.Sharing_renameToAvoidConclict(getResourceTypePluralString()));
                    for (DataPair<String, String> myPair : myReplacements) {

                        myBuffer.append("\n");
                        myBuffer.append(_constants.Sharing_renameTo(Format.value(myPair.getObjectOne()),
                                                                    Format.value(myPair.getObjectTwo())));
                    }
                    Dialog.showWarning(_constants.serverRequest_SuccessDialogTitle(), myBuffer.toString(), true);

                } else {

                    Dialog.showInfo(_constants.serverRequest_SuccessDialogTitle(), _constants.sharing_Success());
                }
            }
        }
    };

    protected TransferCompleteEventHandler handleInstallComplete
            = new TransferCompleteEventHandler() {

        @Override
        public void onTransferComplete(TransferCompleteEvent eventIn) {

            List<String> myResourceList = eventIn.getItemList();

            if (null != myResourceList) {

                if (1 == myResourceList.size()) {

                    requestSingleItem(myResourceList.get(0));

                } else if (1 < myResourceList.size()) {

                    for (int i = 0; myResourceList.size() > i; i++) {

                        requestSingleItem(myResourceList.get(i));
                    }
                }
            }
            if (null != parent) {

                parent.restoreState();
            }
        }
    };

    protected VortexEventHandler<Response<String, SharingDisplay>> handleSingleItemResponse
            = new AbstractVortexEventHandler<Response<String, SharingDisplay>>() {
        @Override
        public boolean onError(Throwable myException) {

            watchBox.hide();
            Display.error("ResourceTab", 7, myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, SharingDisplay> responseIn) {

            watchBox.hide();

            if (ResponseHandler.isSuccess(responseIn)) {

                SharingDisplay myItem = responseIn.getResult();

                if (null != myItem) {

                    ListStore<SharingDisplay> myGridStore = _grid.getStore();

                    myGridStore.add(myItem);
                }
            }
        }
    };

    protected ClassificationCallback securityCallback = new ClassificationCallback() {

        public void onClassification(String uuidIn, CapcoInfo capcoIn,
                                     SecurityTagsInfo securityTagsIn, boolean isAuthorizedIn) {

            WebMain.injector.getMainPresenter().updateClassification(uuidIn, capcoIn, securityTagsIn, isAuthorizedIn);
        }
    };

    private ClickHandler handleAnyButton(ClickHandler handlerIn) {

        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent eventIn) {

                _clearFlag = false;
                itemsReturnedLabel.setVisible(false);
                handlerIn.onClick(eventIn);
            }
        };
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    ResourceTab(ResourceSharingView parentIn, String classKeyIn, boolean extendedColumnsIn) {

        super();
        parent = parentIn;
        _classKey = classKeyIn;
        initialize(extendedColumnsIn);
    }


    ResourceTab(ResourceSharingView parentIn, String classKeyIn) {

        super();
        parent = parentIn;
        _classKey = classKeyIn;
        initialize(false);
    }

    public ResourceFilter getResourceFilter() {

        return _resourceFilter;
    }

    public ResourceFilter getRefreshFilter() {

        return _resourceFilter;
    }

    public void disableControls() {

        filterDropDown.setEnabled(false);
        getButton.setEnabled(false);
    }

    public void enableControls() {

        filterDropDown.setEnabled(true);
        getEnableDisable();
    }

    public void replaceGroupLists(List<String> groupsIn) {

        groupList.clear();
        groupList.addItem(_constants.resourceTab_no_groups_select(), DisplayMode.COMPONENT);
        groupList.addItem("- - - - - -", DisplayMode.DISABLED);

        if (null != groupsIn) {

            for (String myGroup : groupsIn) {

                groupList.addItem(myGroup);
            }
        }
    }

    public void replaceUserLists(List<String> usersIn) {

        userList.clear();
        userFilter.clear();

        userList.addItem(_constants.resourceTab_no_users_selected(), DisplayMode.COMPONENT);
        userFilter.addItem(_constants.resourceTab_all_users());
        userList.addItem("- - - - - -", DisplayMode.DISABLED);

        if (null != usersIn) {

            for (String myUser : usersIn) {

                userList.addItem(myUser);
                userFilter.addItem(myUser);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void initialize(boolean extendedColumnsIn) {

        _userInfo = WebMain.injector.getMainPresenter().getUserInfo();
        _startupInfo = WebMain.getClientStartupInfo();

        if ((null != _userInfo) && (null != _startupInfo)) {

            boolean myShowOthers = _userInfo.isAdmin();
            MainPresenter myMainPresenter = getMainPresenter();
            boolean myAugmentFlag = myMainPresenter.getDialogPreference(_classKey, _augmentKey, false);
            boolean myResetFlag = myMainPresenter.getDialogPreference(_classKey, _resetFilterKey, false);

            add(uiBinder.createAndBindUi(this));

            _permissionCheckBoxes = new CheckBox[] {readCheckBox, editCheckBox, deleteCheckBox};
            resetFilterCheckBox.setValue(myResetFlag);

            _info = new SharingGrid(extendedColumnsIn);
            _grid = _info.createGrid();

            CsiTabPanel.fixRadioButtons(new RadioButton[]{groupRadioButton, userRadioButton});
            CsiTabPanel.fixRadioButtons(new RadioButton[]{augmentButton, replaceButton});
            augmentButton.setValue(myAugmentFlag);
            replaceButton.setValue(!myAugmentFlag);

            container.setWidgetTopHeight(topContainer, 0, Unit.PX, 75, Unit.PX);
            container.setWidgetTopBottom(gridContainer, 75, Unit.PX, 60, Unit.PX);
            container.setWidgetBottomHeight(bottomContainer, 0, Unit.PX, 60, Unit.PX);
            gridContainer.setGrid(_grid);

            filterDropDown.addSelectionChangedHandler(handleFilterChange);

            showAdmin.setVisible(myShowOthers);
            adminOnly.setVisible(myShowOthers);
            _radioState = false;
            newButton.setVisible(isCreateAvailable(_userInfo));
            alternateButton.setVisible(isAlternateAvailable(_userInfo));
            deleteSpacer.setVisible(isDeleteAvailable(_userInfo));
            deleteButton.setVisible(isDeleteAvailable(_userInfo));
            editSpacer.setVisible(isOpenAvailable(_userInfo));
            editButton.setVisible(isOpenAvailable(_userInfo));
            exportSpacer.setVisible(isExportAvailable(_userInfo));
            exportButton.setVisible(isExportAvailable(_userInfo));
            renameSpacer.setVisible(isRenameAvailable(_userInfo));
            renameButton.setVisible(isRenameAvailable(_userInfo));
            shareSpacer.setVisible(isShareAvailable(_userInfo));
            resourceOnly.setVisible(true);
            privileges.setVisible(true);
            if(!WebMain.getClientStartupInfo().getFeatureConfigGWT().isUseNewLogoutPage()) {
                shareButton.setVisible(isShareAvailable(_userInfo));
            } else {
                shareButton.setVisible(false);
                readCheckBox.setVisible(false);
                editCheckBox.setVisible(false);
                deleteCheckBox.setVisible(false);
                ownerCheckBox.setVisible(false);
                groupRadioButton.setVisible(false);
                groupList.setVisible(false);
                userRadioButton.setVisible(false);
                userList.setVisible(false);
                resourceOnly.setVisible(false);
                privileges.setVisible(false);
            }
            classifySpacer.setVisible(isSecurityAvailable());
            classifyButton.setVisible(isSecurityAvailable());
            launchSpacer.setVisible(isLaunchAvailable(_userInfo));
            launchButton.setVisible(isLaunchAvailable(_userInfo));
            sharingPanel.setVisible(isGrantAvailable(_userInfo));
//        createSpinner();
        }
    }

    protected void wireInHandlers() {

        clearButton.addClickHandler(handleAnyButton(handleClearRequest));

        if (deleteButton.isVisible()) {

            if (null != getDeleteClickHandler()) {

                deleteButton.addClickHandler(handleAnyButton(getDeleteClickHandler()));

            } else {

                deleteButton.setVisible(false);
            }
        }
        deleteSpacer.setVisible(deleteButton.isVisible());

        if (newButton.isVisible()) {

            ClickHandler myCreateRequestHandler = getCreateClickHandler();

            if (null != myCreateRequestHandler) {

                newButton.addClickHandler(handleAnyButton(myCreateRequestHandler));
                newButton.setText(getCreateButtonLabel());

            } else {

                newButton.setVisible(false);
            }
        }

        if (alternateButton.isVisible()) {

            ClickHandler myAlternateRequestHandler = getAlternateClickHandler();

            if (null != myAlternateRequestHandler) {

                alternateButton.addClickHandler(handleAnyButton(myAlternateRequestHandler));
                alternateButton.setText(getAlternateButtonLabel());

            } else {

                alternateButton.setVisible(false);
            }
        }

        if (editButton.isVisible()) {

            ClickHandler myEditRequestHandler = getEditClickHandler();

            if (null != myEditRequestHandler) {

                editButton.addClickHandler(getEditAuthorizationCheckHandler());
                editButton.setText(getEditButtonLabel());

            } else {

                editButton.setVisible(false);
            }
        }
        editSpacer.setVisible(editButton.isVisible());

        if (exportButton.isVisible()) {

            ClickHandler myExportRequestHandler = getExportClickHandler();

            if (null != myExportRequestHandler) {

                exportButton.addClickHandler((handleExportAuthorizationCheck));
                exportButton.setText(Dialog.txtExportButton);

            } else {

                exportButton.setVisible(false);
            }
        }
        exportSpacer.setVisible(exportButton.isVisible());

        if (renameButton.isVisible()) {

            ClickHandler myRenameRequestHandler = getRenameClickHandler();

            if (null != myRenameRequestHandler) {

                renameButton.addClickHandler(handleAnyButton(handleRenameAuthorizationCheck));
                renameButton.setText(_constants.rename());

            } else {

                renameButton.setVisible(false);
            }
        }
        renameSpacer.setVisible(renameButton.isVisible());

        if (shareButton.isVisible()) {

            if (null != handleEditSharingRequest) {

                shareButton.addClickHandler(handleAnyButton(handleEditSharingRequest));

            } else {

                shareButton.setVisible(false);
            }
        }
        shareSpacer.setVisible(shareButton.isVisible());

        if (classifyButton.isVisible()) {

            ClickHandler myClassificationHandler = getClassificationHandler();

            if (null != myClassificationHandler) {

                classifyButton.addClickHandler(handleAnyButton(myClassificationHandler));

            } else {

                classifyButton.setVisible(false);
            }
        }
        classifySpacer.setVisible(classifyButton.isVisible());

        if (launchButton.isVisible()) {

            ClickHandler myLaunchRequestHandler = getLaunchClickHandler();

            if (null != myLaunchRequestHandler) {

                launchButton.addClickHandler(handleAnyButton(handleLaunchAuthorizationCheck));

            } else {

                launchButton.setVisible(false);
            }
        }
        launchSpacer.setVisible(launchButton.isVisible());
        if (sharingPanel.isVisible()) {

            if (null != handleUpdateSharingRequest) {

                grantButton.addClickHandler(handleAnyButton(handleUpdateSharingRequest));

            } else {

                sharingPanel.setVisible(false);
            }
        }
        readCheckBox.addValueChangeHandler(readCheckBoxHandler);
        editCheckBox.addValueChangeHandler(editCheckBoxHandler);
        groupRadioButton.addClickHandler(handleAnyButton(handleMouseClick));
        userRadioButton.addClickHandler(handleAnyButton(handleMouseClick));
        groupList.addSelectionChangedHandler(handleDropDownSelectionChange);
        userList.addSelectionChangedHandler(handleDropDownSelectionChange);
        getButton.addClickHandler(handleAnyButton(handleSharingInformationRequest));
        _grid.getSelectionModel().addSelectionChangedHandler(handleGridSelectionChange);

        enableControls();
    }

    protected String getAlternateButtonLabel() {

        return null;
    }

    protected ClickHandler getAlternateClickHandler() {

        return null;
    }

    protected List<SharingDisplay> getSelectionList() {

        List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();

        return mySelectedGridList;
    }

    protected SharingDisplay getSelection() {

        SharingDisplay myItem = null;
        List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();

        if ((null != mySelectedGridList) && (0 < mySelectedGridList.size())) {

            myItem = mySelectedGridList.get(0);
        }
        return myItem;
    }

    protected void renameLocalResourceEntry(String uuidIn, String nameIn, String remarksIn) {

        if (null != uuidIn) {

            SharingDisplay myItem = _grid.getStore().findModelWithKey(uuidIn);
            if (null != myItem) {

                myItem.setName(nameIn);
                myItem.setRemarks(remarksIn);
                _grid.getView().refresh(false);
            }
        }
    }

    protected ClickHandler handleClearRequest = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            _clearFlag = true;
            _busyFlag = false;
            _resourceFilter = null;
            _sharingSelection = null;
            _selectionMap = null;
            _partialDisplayList.clear();
            _partialDisplaySet.clear();
            _grid.getStore().clear();
        }
    };

    protected ClickHandler handleEditClassificationRequest = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            (new ClassificationWizard("Add / Update Security Information", "Classify.html",
                    getSelection().getUuid(), getSelection().getName(), getResourceType(), securityCallback)).show();
        }
    };

    protected void requestSingleItem(String itemNameIn) {

        String myUser = _userInfo.getName();

        try {
            watchBox.show(_constants.resourceTab_retrieveShareInfo());

            VortexFuture<Response<String, SharingDisplay>> vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.execute(UserAdministrationServiceProtocol.class).getSingleSharingName(getResourceType(), itemNameIn, myUser);

            vortexFuture.addEventHandler(handleSingleItemResponse);

        } catch (Exception myException) {

            watchBox.hide();
            Display.error("ResourceTab", 8, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void setEnableDisable() {

        List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();

        saveRadioButtons();
        checkSharingRequest();
        filterDropDown.setEnabled(true);
        userList.setEnabled(!_radioState);
        groupList.setEnabled(_radioState);
        ownerCheckBox.setEnabled(true);

        if (_radioState) {

            checkSharingRequest();

            if (null != _sharingSelection) {

                if (!((_userInfo.isAdmin() && _sharingSelection.equals(_userInfo.getAdminGroup()))
                        || (_userInfo.isAdmin() && _sharingSelection.equals(_userInfo.getAdminGroup())))) {

                    ownerCheckBox.setEnabled(false);
                }

            } else {

                ownerCheckBox.setEnabled(false);
            }
        }

        if (!ownerCheckBox.isEnabled()) {

            ownerCheckBox.setValue(false);
        }

        if(!WebMain.getClientStartupInfo().getFeatureConfigGWT().isUseNewLogoutPage()) {
            grantButton.setEnabled((null != _sharingSelection)
                    && (null != mySelectedGridList)
                    && (0 < mySelectedGridList.size()));
        } else {
            grantButton.setVisible(false);
        }

        deleteButton.setEnabled(deleteButton.isVisible()
                && (null != mySelectedGridList)
                && (0 < mySelectedGridList.size()));

        classifyButton.setEnabled(classifyButton.isVisible()
                && (null != mySelectedGridList)
                && (1 == mySelectedGridList.size()));

        shareButton.setEnabled(shareButton.isVisible()
                && (null != mySelectedGridList)
                && (0 < mySelectedGridList.size()));

        launchButton.setEnabled(launchButton.isVisible()
                && (null != mySelectedGridList)
                && (1 == mySelectedGridList.size()));

        editButton.setEnabled(editButton.isVisible()
                && (null != mySelectedGridList)
                && (1 == mySelectedGridList.size()));

        exportButton.setEnabled(exportButton.isVisible()
                && (null != mySelectedGridList)
                && (1 == mySelectedGridList.size()));

        renameButton.setEnabled(renameButton.isVisible()
                && (null != mySelectedGridList)
                && (1 == mySelectedGridList.size()));

        newButton.setEnabled(newButton.isVisible());
        getEnableDisable();
    }

    private void getEnableDisable() {

        getButton.setEnabled(true);
    }

    private void initializeRadioButtons() {

        groupRadioButton.setValue(_radioState);
        userRadioButton.setValue(!_radioState);
        setEnableDisable();
    }

    private void saveRadioButtons() {

        _radioState = groupRadioButton.getValue();
    }

    private void checkSharingRequest() {

        _sharingSelection = null;

        if (groupRadioButton.getValue()) {

            int mySelectionId = groupList.getSelectedIndex();

            if (1 < mySelectionId) {

                _sharingSelection = groupList.getItemText(mySelectionId);
            }

        } else if (userRadioButton.getValue()) {

            int mySelectionId = userList.getSelectedIndex();

            if (1 < mySelectionId) {

                _sharingSelection = userList.getItemText(mySelectionId);
            }
        }
    }

    @Override
    public void onShow() {

        initializeRadioButtons();
    }

    @Override
    public void onHide() {

        saveRadioButtons();
    }

    @Override
    public String getHeadingText() {

        return getResourceType().getPlural();
    }

    private void requestSharingInformation() {

        ResourceFilter myFilter = getResourceFilter();
        String myOwner = getOwnerValue();
        String myPattern = ((null != pattern.getValue()) && (0 < pattern.getValue().length()))
                                ? StringUtil.patternToSql(pattern.getValue())
                                : null;

        try {
            watchBox.show(_constants.resourceTab_retrieveShareInfo());

            VortexFuture<Response<String, List<SharingDisplay>>> vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.addEventHandler(handleRequestInformationResponse);
            vortexFuture.execute(UserAdministrationServiceProtocol.class).getSharingNames(getResourceType(),
                                                                                            myFilter, myPattern, myOwner);
        } catch (Exception myException) {

            watchBox.hide();
            Display.error("ResourceTab", 9, myException);
        }
    }

    private void requestDeleteResources(List<String> resourcesIn) {

        try {
            watchBox.show(_constants.resourceTab_deleteResources());

            VortexFuture<List<List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.addEventHandler(handleDeleteResponse);
            vortexFuture.execute(ModelActionsServiceProtocol.class).delete(getResourceType(), resourcesIn);

        } catch (Exception myException) {

            watchBox.hide();
            Display.error("ResourceTab", 10, myException);
        }
    }

    protected void requestSharingUpdate(List<String> resourcesIn, String roleIn, List<AclControlType> permissionsIn, boolean setOwnerIn) {

        try {
            watchBox.show(_constants.resourceTab_updateShareInfo());

            VortexFuture<Response<String, DataPair<List<SharingDisplay>, List<DataPair<String, String>>>>>
                    vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.execute(UserAdministrationServiceProtocol.class).defineSharing(getResourceType(), resourcesIn,
                    roleIn, permissionsIn, setOwnerIn);

            vortexFuture.addEventHandler(handleSharingUpdateResponse);

        } catch (Exception myException) {

            watchBox.hide();
            Display.error("ResourceTab", 11, myException);
        }
    }

    private List<AclControlType> getPriviledgeRequest() {

        Set<AclControlType> myList = new HashSet<AclControlType>();

        for (int i = 0; _permissionCheckBoxes.length > i; i++) {

            if (_permissionCheckBoxes[i].getValue()) {

                myList.add(_permissionTypes[i]);

                if (0 == i) {

                    myList.add(AclControlType.READ);
                }
            }
        }

        return new ArrayList<AclControlType>(myList);
    }

    private String getOwnerValue() {

        String myOwner = null;

        if (_userInfo.isAdmin()) {

            int myIndex = userFilter.getSelectedIndex();

            if (0 < myIndex) {

                myOwner = userFilter.getItemText(myIndex);
            }
        }
        return (null != myOwner) ? myOwner.toLowerCase() : null;
    }

    private boolean isCreateAvailable(UserSecurityInfo userInfoIn) {

        return !userInfoIn.isRestricted();
    }

    protected boolean isAlternateAvailable(UserSecurityInfo userInfoIn) {

        return !userInfoIn.isRestricted();
    }

    private boolean isOpenAvailable(UserSecurityInfo userInfoIn) {

        return !userInfoIn.isRestricted();
    }

    private boolean isExportAvailable(UserSecurityInfo userInfoIn) {

        return (!userInfoIn.isRestricted());
    }

    private boolean isRenameAvailable(UserSecurityInfo userInfoIn) {

        return (!userInfoIn.isRestricted());
    }

    private boolean isShareAvailable(UserSecurityInfo userInfoIn) {

        return (!userInfoIn.isRestricted()) || userInfoIn.isAdmin();
    }

    private boolean isSecurityAvailable() {

        return (isCapcoAvailable() || isSecurityTagsAvailable());
    }

    private boolean isCapcoAvailable() {

        return (_startupInfo.isEnforceCapcoRestrictions()
                || _startupInfo.isProvideCapcoBanners()) && _userInfo.getCanSetSecurity();
    }

    private boolean isSecurityTagsAvailable() {

        return (_startupInfo.isEnforceSecurityTags()
                || _startupInfo.isProvideTagBanners()) && _userInfo.getCanSetSecurity();
    }

    private boolean isLaunchAvailable(UserSecurityInfo userInfoIn) {

        return (!userInfoIn.isRestricted());
    }

    private boolean isDeleteAvailable(UserSecurityInfo userInfoIn) {

        return (!userInfoIn.isRestricted()) || userInfoIn.isAdmin();
    }

    private boolean isGrantAvailable(UserSecurityInfo userInfoIn) {

        return (!userInfoIn.isRestricted()) || userInfoIn.isAdmin();
//        return false;
    }

    private List<String> mergeGridInfo(ListStore<SharingDisplay> gridStoreIn, List<SharingDisplay> newDataIn,
                                       boolean hasBaseIn, boolean hasAclIn) {

        List<String> myList = new ArrayList<>();

        if (null != newDataIn) {

            for (SharingDisplay myNewData : newDataIn) {

                SharingDisplay myOldData = gridStoreIn.findModelWithKey(myNewData.getUuid());

                if (null != myOldData) {

                    myOldData.copy(myNewData, hasBaseIn, hasAclIn);
                    myList.add(myNewData.getUuid());

                } else if (hasBaseIn) {

                    gridStoreIn.add(myNewData);
                    myList.add(myNewData.getUuid());
                }
            }
        }
        return myList;
    }

    private void recordPartial(List<String> listIn) {

        requestUpdate(finalizeDataRequest(listIn, true, false));
    }

    private void recordFull(List<String> listIn) {

        requestUpdate(finalizeDataRequest(listIn, true, true));
    }

    private void recordUpdate(List<String> listIn) {

        if (!requestUpdate(finalizeDataRequest(listIn, false, true))) {

//            hideSpinner();
        }
    }

    private boolean requestUpdate(List<String> listIn) {

        boolean myRequestSent = false;

        if ((null != listIn) && (0 < listIn.size())) {

            try {

                VortexFuture<Response<String, List<SharingDisplay>>>
                        vortexFuture = WebMain.injector.getVortex().createFuture();
                vortexFuture.addEventHandler(handleAclInformationResponse);
                vortexFuture.execute(UserAdministrationServiceProtocol.class).getAclInfo(listIn);

                myRequestSent = true;
//            showSpinner();

            } catch (Exception myException) {

                Display.error("ResourceTab", 12, myException);
            }
        }
        return myRequestSent;
    }

    private synchronized List<String> finalizeDataRequest(List<String> listIn, boolean hasBaseIn, boolean hasAclIn) {

        int myInitialSize = _partialDisplayList.size();
        List<String> myToDoList = null;

        if (hasAclIn && (!hasBaseIn)) {

            _busyFlag = false;
        }
        if (null != listIn) {

            if (hasAclIn) {

                _partialDisplaySet.removeAll(listIn);
            }
            if (0 == _partialDisplaySet.size()) {

                _partialDisplayList.clear();

            } else {

                int myCount = 0;
                while (_partialDisplayList.size() > myCount) {
                    if (_partialDisplaySet.contains(_partialDisplayList.get(myCount))) {

                        break;
                    }
                    myCount++;
                }
                while (0 < myCount--) {

                    _partialDisplayList.remove(myCount);
                }
            }
            if (hasBaseIn && (!hasAclIn)) {

                _partialDisplayList.addAll(listIn);
                _partialDisplaySet.addAll(listIn);
            }
        }
        if ((!_busyFlag) && (0 < _partialDisplayList.size())) {

            Set<String> mySet = new HashSet<String>();
            int myCount = 0;

            for (String myId : _partialDisplayList) {

                if (_partialDisplaySet.contains(myId) && (!mySet.contains(myId))) {

                    mySet.add(myId);
                    if (_updateRequestSize == ++myCount) {

                        break;
                    }
                }
            }
            if (0 < myCount) {

                myToDoList = new ArrayList<String>(mySet);
                _busyFlag = true;
            }
        }
        return myToDoList;
    }

    private void showSpinner() {

        if (null != spinnerIcon) {

            spinnerIcon.setVisible(true);
        }
    }

    private void hideSpinner() {

        if (null != spinnerIcon) {

            spinnerIcon.setVisible(false);
        }
    }

    private void createSpinner() {
        spinnerIcon = new com.github.gwtbootstrap.client.ui.Icon(IconType.SPINNER);
        spinnerIcon.setIconSize(IconSize.FOUR_TIMES);
        spinnerIcon.setSpin(true);
        spinnerIcon.addStyleName("csi-icon-spinner"); //$NON-NLS-1$
        container.add(spinnerIcon);
        positionSpinner();
        spinnerIcon.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
        spinnerIcon.setVisible(false);
    }

    private void positionSpinner() {
        int height = spinnerIcon.getOffsetHeight();
        int width = spinnerIcon.getOffsetWidth();

        Style style = spinnerIcon.getElement().getStyle();
        DOM.setStyleAttribute(container.getElement(), "marginLeft", (-width / 2) + "px");
        DOM.setStyleAttribute(container.getElement(), "marginTop", (-height / 2) + "px");
        style.setPosition(Style.Position.ABSOLUTE);
    }

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }
}