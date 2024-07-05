package csi.client.gwt.dataview;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.RenameResourceDialog;
import csi.client.gwt.csi_resource.ResourcePresenter;
import csi.client.gwt.csi_resource.ResourceSaveCallback;
import csi.client.gwt.csiwizard.dialogs.SourceEditDialog;
import csi.client.gwt.csiwizard.wizards.ParameterWizard;
import csi.client.gwt.dataview.broadcast.BroadcastManager;
import csi.client.gwt.dataview.directed.DirectedPresenter;
import csi.client.gwt.edit_sources.presenters.EditDataViewPresenter;
import csi.client.gwt.events.*;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.ApplicationToolbar;
import csi.client.gwt.mainapp.ApplicationToolbarLocator;
import csi.client.gwt.mainapp.CsiDisplay;
import csi.client.gwt.mainapp.SecurityBanner;
import csi.client.gwt.sharing.ResourceSharingDialog;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.util.VortexUtil;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.viewer.Viewer;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.client.gwt.widget.ui.CsiBanner;
import csi.server.common.dto.*;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.DataOperation;
import csi.server.common.linkup.LinkupDataTransfer;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;
import csi.server.common.service.api.ModelActionsServiceProtocol;
import csi.server.common.service.api.TestActionsServiceProtocol;
import csi.server.common.util.ConnectorSupport;
import csi.server.common.util.Format;
import csi.server.common.util.SynchronizeChanges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class AbstractDataViewPresenter extends WatchingParent implements ResourcePresenter<DataView>, CsiDisplay {

    protected DataView dataView;
    protected DataViewDef metaData;
    protected DataModelDef dataModel;
    protected FieldListAccess fieldAcces;
    protected String dataViewUuid;
    protected List<QueryParameterDef> _requiredParameters;
    protected List<DataSourceDef> _requiredAuthorizations;

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final static String _txtrefreshErrorDialogTitle = i18n.refreshErrorDialogTitle();
    private final static String _txtParameterRefreshTitle = i18n.parameterRefreshTitle();
    private final static String _txtParameterRefreshHelp = i18n.parameterRefreshHelpTarget();
    private final static String _txtParameterRefreshButton = Dialog.txtRefreshButton;
    private final static int _nameMax = 60;
    private boolean _bannerInitialized = false;
    private CsiBanner _banner = null;
    private boolean _isDeleted = false;
    private boolean _isOwner = false;
    private boolean _readOnly = false;
    private boolean _removeable = false;
    private boolean _blockViewing = false;
    private boolean _aborting = false;

    private UserInputEventHandler<Integer> _processLogon = null;
    private VortexEventHandler<Response<String, DataView>> handleRefreshResponse = null;

    private List<LaunchParam> _parameterData = null;
    private DataViewLoadingCallback callback = null;

    private EventBus eventBus = new ResettableEventBus(new SimpleEventBus());

    private BroadcastManager broadcastManager;

    public AbstractDataViewPresenter() {

        broadcastManager = new BroadcastManager(this);
    }

    public String getOwner() {

        return (null != dataView) ? dataView.getOwner() : null;
    }

    public void launchParameterDisplay() {

        new DisplayParametersDialog(this).show();
    }

    public boolean checkDataView(String nameIn, String ownerIn) {

        return ((null != nameIn) && (null != ownerIn) && nameIn.equals(getName()) && ownerIn.equals(getOwner()));
    }

    public void save(final ResourceSaveCallback callbackIn) {

        try {

            List<Visualization> myVisualizations = getVisualizations();

            if ((null != myVisualizations) && (0 < myVisualizations.size())) {

                Collection<VortexFuture<Void>> futures = new ArrayList<VortexFuture<Void>>();

                showWatchBox();
                for (Visualization visualization : myVisualizations) {
                    if (visualization instanceof MapPresenter) {
                        futures.add(((MapPresenter) visualization).saveSettingsOnly(false));
                    } else {
                        futures.add(visualization.saveSettings(false));
                    }
                }

                VortexEventHandler<Collection<Void>> handler = new AbstractVortexEventHandler<Collection<Void>>() {
                    @Override
                    public boolean onError(Throwable exceptionIn) {
                        hideWatchBox();
                        // Display error message.
                        Display.error("AbstractDataViewPresenter", 1, exceptionIn);
                        return false;
                    }

                    @Override
                    public void onSuccess(Collection<Void> result) {
                        hideWatchBox();
                        callbackIn.onSave();
                    }
                };
                VortexUtil.afterAllFutures(futures, handler);

            } else {

                callbackIn.onSave();
            }

        } catch (Exception myException) {

            hideWatchBox();
            Display.error("AbstractDataViewPresenter", 2, myException);
        }
    }

    public void saveState() {}

    public void restoreState() {

        DataViewDef myBannerResource = (null != dataView) ? dataView.getMeta() : null;

        SecurityBanner.displayBanner(myBannerResource);
    }

    public void forceExit() {}

    public boolean isBlocked() {

        return _blockViewing;
    }

    public Widget asWidget() {

        if (_blockViewing) {

            if (!_aborting) {

                Display.error("Blocked For Security", "User not auhorized to access this DataView.", handleSecurityAbort);
            }
            _aborting = true;
        }
        return getView();
    }

    private ClickHandler handleSecurityAbort = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            WebMain.injector.getMainPresenter().abortDataView();
        }
    };

    public void updateClassification(String uuidIn, CapcoInfo capcoIn, SecurityTagsInfo securityTagsIn, boolean isAuthorizedIn) {

        if (isUuid(uuidIn)) {

            if (null != dataView) {

                _blockViewing = !isAuthorizedIn;
                SynchronizeChanges.updateSecurity(dataView, capcoIn, securityTagsIn);
                SecurityBanner.displayBanner(dataView.getMeta());
            }
        }
    }

    private ClickHandler handleDataViewAbort = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            hideWatchBox();
            WebMain.injector.getMainPresenter().abortDataView();
        }
    };

    private UserInputEventHandler<List<LaunchParam>> handleParameterLoadingComplete
            = new UserInputEventHandler<List<LaunchParam>>() {

        @Override
        public void onUserInput(UserInputEvent<List<LaunchParam>> eventIn) {

            if (eventIn.isCanceled()) {

                hideWatchBox();
                callback = null;
                WebMain.injector.getMainPresenter().conditionalAbort();

            } else {

                _parameterData = eventIn.getKey();

                try {

                    if ((null != _requiredAuthorizations) && (0 < _requiredAuthorizations.size())) {

                        hideWatchBox();
                        Dialog.showCredentialDialogs(getAuthorizationMap(), _requiredAuthorizations, processLogon()); // TODO

                    } else {

                        refreshDataView();
                    }

                } catch (Exception myException) {

                    hideWatchBox();
                    Display.error("AbstractDataViewPresenter", 3, myException);
                    WebMain.injector.getMainPresenter().conditionalAbort();
                }
            }
        }
    };

    private VortexEventHandler<Response<Object, List<QueryParameterDef>>> handleParameterListResponse
            = new AbstractVortexEventHandler<Response<Object, List<QueryParameterDef>>>() {

        @Override
        public boolean onError(Throwable t) {
            hideWatchBox();
            WebMain.injector.getMainPresenter().conditionalAbort();
            return true;
        }

        @Override
        public void onSuccess(Response<Object, List<QueryParameterDef>> responseIn) {

            if (ResponseHandler.isSuccess(responseIn)) {

                _requiredParameters = responseIn.getResult();

                if (responseIn.isAuthorizationRequired()) {

                    _requiredAuthorizations = responseIn.getAuthorizationList();
                }

                try {

                    if ((null != _requiredParameters) && (0 < _requiredParameters.size())) {
                        hideWatchBox();
                        (new ParameterWizard(_txtParameterRefreshTitle, _txtParameterRefreshHelp, _txtParameterRefreshButton, _requiredParameters, handleParameterLoadingComplete)).show();

                    } else {

                        handleParameterLoadingComplete.onUserInput(new UserInputEvent<List<LaunchParam>>(false)); // TODO
                    }

                } catch (Exception myException) {
                    hideWatchBox();
                    Display.error("AbstractDataViewPresenter", 4, myException);
                    WebMain.injector.getMainPresenter().conditionalAbort();
                }

            } else {

                callback = null;
                hideWatchBox();
                WebMain.injector.getMainPresenter().conditionalAbort();
            }
        }
    };

    public <H extends EventHandler> HandlerRegistration addHandler(final H handler, GwtEvent.Type<H> type) {
        return eventBus.addHandler(type, handler);
    }

    protected AsyncCallback<Void> cancelCallback = new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caughtIn) {

            WebMain.injector.getMainPresenter().conditionalAbort();
            Display.error(i18n.abstractDataViewPresenter_CancelError(), caughtIn);
        }

        @Override
        public void onSuccess(Void result) {

            WebMain.injector.getMainPresenter().conditionalAbort();
            Display.success(i18n.abstractDataViewPresenter_CancelSuccess());
        }
    };

    protected VortexEventHandler<Response<String, DataView>> handleRefreshResponse() {
        if (handleRefreshResponse == null) {
            handleRefreshResponse = new AbstractVortexEventHandler<Response<String, DataView>>() {

                @Override
                public void onUpdate(int taskProgessIn, String taskMessageIn) {

                    if (0 < taskProgessIn) {

                        if (null != taskMessageIn) {

                            updateWatchBox(i18n.abstractDataViewPresenter_ProcessedRowsMessage(taskMessageIn, taskProgessIn));

                        } else {

                            updateWatchBox(i18n.abstractDataViewPresenter_ProcessedRows(taskProgessIn));
                        }
                    }
                }

                @Override
                public boolean onError(Throwable myException) {
                    hideWatchBox();
                    Display.error("AbstractDataViewPresenter", 5, myException);
                    callback = null;
                    WebMain.injector.getMainPresenter().conditionalAbort();
                    return true;
                }

                @Override
                public void onSuccess(Response<String, DataView> responseIn) {
                    hideWatchBox();

                    if (responseIn.isAuthorizationRequired()) {

                        try {

                            Dialog.showCredentialDialogs(getAuthorizationMap(), responseIn.getAuthorizationList(), processLogon());

                        } catch (Exception myException) {

                            Display.error("AbstractDataViewPresenter", 6, myException);
                            callback = null;
                            WebMain.injector.getMainPresenter().conditionalAbort();
                        }

                    } else if (ResponseHandler.isSuccess(responseIn, _txtrefreshErrorDialogTitle)) {

                        WebMain.injector.getMainPresenter().closeExistingDataViewAndOpenNewOne(dataView, responseIn.getCount(), responseIn.getLimitedData());

                        if (null != callback) {

                            callback.onCallback(responseIn.getCount(), responseIn.getLimitedData());
                            callback = null;
                        }

                    } else {

                        callback = null;
                        ApplicationToolbarLocator.getInstance().abortDataView();
                    }
                }
            };
        }
        return handleRefreshResponse;
    }

    protected ClickHandler continueOpen = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            openDataview(dataView);
        }
    };

    protected ClickHandler cancelOpen = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            openDataview(dataView);
        }
    };

    private VortexEventHandler<LinkupDataTransfer> handleNewLinkup
            = new AbstractVortexEventHandler<LinkupDataTransfer>() {

        @Override
        public boolean onError(Throwable errorIn) {
            hideWatchBox();
            fireEvent(new DataChangeEvent(DataOperation.CREATE, errorIn));
            return true;
        }

        @Override
        public void onSuccess(LinkupDataTransfer linkupDataIn) {
            hideWatchBox();
            Exception myError = SynchronizeChanges.addToLinkupList(getDataView().getMeta(),
                    linkupDataIn.getObjectOfInterest(), linkupDataIn.getNewFields());
            if (null == myError) {
                fireEvent(new DataChangeEvent(linkupDataIn, DataOperation.CREATE));
            } else {
                fireEvent(new DataChangeEvent(DataOperation.CREATE, myError));
            }
        }
    };

    private VortexEventHandler<LinkupDataTransfer> handleUpdatedLinkup
            = new AbstractVortexEventHandler<LinkupDataTransfer>() {

        @Override
        public boolean onError(Throwable errorIn) {
            hideWatchBox();
            fireEvent(new DataChangeEvent(DataOperation.UPDATE, errorIn));
            return true;
        }

        @Override
        public void onSuccess(LinkupDataTransfer linkupDataIn) {
            hideWatchBox();
            Exception myError = SynchronizeChanges.removeFromLinkupList(getDataView().getMeta(),
                    linkupDataIn.getObjectOfInterest(), linkupDataIn.getDeletedFields());
            if (null == myError) {
                myError = SynchronizeChanges.addToLinkupList(getDataView().getMeta(),
                        linkupDataIn.getObjectOfInterest(), linkupDataIn.getNewFields());
            }
            if (null == myError) {
                fireEvent(new DataChangeEvent(linkupDataIn, DataOperation.UPDATE));
            } else {
                fireEvent(new DataChangeEvent(DataOperation.UPDATE, myError));
            }
        }
    };

    private VortexEventHandler<AccessRights> handleOwnershipResponse
            = new AbstractVortexEventHandler<AccessRights>() {

        @Override
        public boolean onError(Throwable myException) {

            _isOwner = false;
            _readOnly = false;
            _removeable = false;
            ApplicationToolbarLocator.getInstance().enableDataMenus(getDataView());
            return true;
        }

        @Override
        public void onSuccess(AccessRights responseIn) {

//            Dialog.showInfo("Return user authorizations.");

            if (null != responseIn) {

                _isOwner = responseIn.getIsOwner();
                _readOnly = !responseIn.getCanWrite();
                _removeable = responseIn.getCanDelete();

            } else {

                _isOwner = false;
                _readOnly = false;
                _removeable = false;
            }
            ensureDataviewData();
        }
    };

    protected UserInputEventHandler<Integer> processLogon() {
        if (_processLogon == null) {
            _processLogon = new UserInputEventHandler<Integer>() {

                @Override
                public void onUserInput(UserInputEvent<Integer> eventIn) {

                    if (eventIn.isCanceled()) {

                        callback = null;
                        WebMain.injector.getMainPresenter().conditionalAbort();

                    } else {

                        try {

                            refreshDataView();

                        } catch (Exception myException) {

                            Display.error("AbstractDataViewPresenter", 7, myException);
                            callback = null;
                            WebMain.injector.getMainPresenter().conditionalAbort();
                        }
                    }
                    _requiredAuthorizations = null;
                }
            };
        }
        return _processLogon;
    }

    public VortexEventHandler<Response<String, DataView>> getHandlerOpenDataViewResponse() {
        return handlerOpenDataViewResponse;
    }

    public void prepareDataView(Response<String, DataView> responseIn,final DataViewLoadingCallback callbackIn) {
        callback = callbackIn;
        handlerOpenDataViewResponse.onSuccess(responseIn);
    }

    private VortexEventHandler<Response<String, DataView>> handlerOpenDataViewResponse
            = new AbstractVortexEventHandler<Response<String, DataView>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            WebMain.injector.getMainPresenter().hideMask();

            // Display error message.
            Display.error("AbstractDataViewPresenter", 8, exceptionIn);
            callback = null;
            ApplicationToolbarLocator.getInstance().abortDataView();
            return false;
        }

        @Override
        public void onSuccess(Response<String, DataView> responseIn) {

            WebMain.injector.getMainPresenter().hideMask();

            if (responseIn.isAuthorizationRequired()) {

                try {

                    Dialog.showCredentialDialogs(WebMain.injector.getMainPresenter().getAuthorizationMap(), responseIn.getAuthorizationList(), processLogon());

                } catch (Exception myException) {

                    Display.error("AbstractDataViewPresenter", 9, myException);
                    callback = null;
                    ApplicationToolbarLocator.getInstance().abortDataView();
                }

            } else if (ResponseHandler.isSuccess(responseIn)) {

                dataView = responseIn.getResult();
                dataViewUuid = (null != dataView) ? dataView.getUuid() : null;

                if (responseIn.getLimitedData()) {

                    long myRowCount = responseIn.getCount();

                    Display.continueDialog(i18n.abstractDataViewPresenter_MoreDataTitle(),
                                                i18n.abstractDataViewPresenter_MoreDataMessage(myRowCount),
                                                continueOpen, cancelOpen);

                } else {

                    openDataview(dataView);
                }

            } else {

                callback = null;
                ApplicationToolbarLocator.getInstance().abortDataView();
            }
        }
    };

    public boolean getOwnership() {

        return _isOwner;
    }

    public void openDataview(DataView dataViewIn) {

        if (null != dataViewIn) {

            initDataViewAccess(dataViewIn);
        }
    }

    private boolean editIsAuthorized() {

        boolean myOk = true;

        if (isReadOnly()) {

            myOk = false;
            Display.error(i18n.refreshErrorDialogTitle(), i18n.serverMessage_DataViewEditError(), handleDataViewAbort);
        }
        return myOk;
    }


    public boolean ensureDataviewData() {

        boolean myOk = false;

        hideWatchBox();
        if (null != dataView) {

            DataViewDef myMeta = dataView.getMeta();

            if (dataView.getNeedsSource() && ConnectorSupport.getInstance().canEnterSourceEditor(myMeta)) {

                if (editIsAuthorized()) {

                    launchDataSourceEditor();
                }

            } else if (dataView.getNeedsRefresh()) {

                if (editIsAuthorized() && !(this instanceof DirectedPresenter)) {

                    WebMain.injector.getMainPresenter().hideMask();
                    RefreshDataViewDialog refresh = new RefreshDataViewDialog(this);
                    refresh.show();
                }

            } else {

//                Dialog.showInfo("Open DataView");

                setup();
                if (null != callback) {
                    callback.onCallback(false);
                    Logger.getLogger("csi.server.business.service.DataViewActionsService").info
                            ("Dataview successfully opened.");
                }
                callback = null;
                onDataChange();
                ApplicationToolbarLocator.getInstance().enableMenus();
                ApplicationToolbarLocator.getInstance().enableDataMenus(dataView);
                adjustVisualizations();
                myOk = true;
            }
        }
        return myOk;
    }

    protected void setup() {
    }

    public void launchSharingDialog() {

        (new ResourceSharingDialog(AclResourceType.DATAVIEW, getUuid())).show();
    }

    public void launchRenamingDialog() {

        (new RenameResourceDialog(getUuid(), AclResourceType.DATAVIEW, getName(),
                i18n.renamingTitle(i18n.currentDataView()))).show();
    }

    public Map<String, AuthDO> getAuthorizationMap() {

        return WebMain.injector.getMainPresenter().getAuthorizationMap(); // TODO
    }

    public List<AuthDO> getAuthorizationList() {

        return WebMain.injector.getMainPresenter().getAuthorizationList();
    }

    /**
     * Loads the data view for this and calls the callback when it is ready.
     *
     * @param callbackIn Callback to invoke onLoad
     */
    public void onLoad(final DataViewLoadingCallback callbackIn) {
        callback = callbackIn;

        VortexFuture<Response<String, DataView>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(handlerOpenDataViewResponse);
            myVortexFuture.execute(DataViewActionServiceProtocol.class, dataViewUuid).openDataView(dataViewUuid);
        } catch (Exception myException) {

            Display.error("AbstractDataViewPresenter", 10, myException);
            WebMain.injector.getMainPresenter().hideMask();
            callback = null;
            ApplicationToolbarLocator.getInstance().abortDataView();
        }
    }

    public void refreshDataView() {

        try {
            final DataView myDataView = getResource();

            resetTableCaching();
            getBroadcastManager().invalidateDataviewBroadcast(myDataView.getUuid());
            VortexFuture<Response<String, DataView>> vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.addEventHandler(handleRefreshResponse());
            showRefreshWatchBox(vortexFuture);
            vortexFuture.execute(DataViewActionServiceProtocol.class).relaunch(myDataView.getUuid(), _parameterData, getAuthorizationList());

        } catch (Exception myException) {
            hideWatchBox();
            Display.error("AbstractDataViewPresenter", 11, myException);
            callback = null;
            WebMain.injector.getMainPresenter().conditionalAbort();
        }
    }

    public List<DataSourceDef> getDataSources() {

        return metaData.getDataSources();
    }

    public void renameDataView(String uuidIn, String nameIn, String remarksIn) {

        if ((null != dataView) && (null != dataViewUuid) && dataViewUuid.equals(uuidIn)) {

            setName(nameIn);
            setRemarks(remarksIn);
        }
    }

    public void getRequiredParameters() {

        final DataView myDataView = getResource();

        VortexFuture<Response<Object, List<QueryParameterDef>>> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            showWatchBox();

            vortexFuture.execute(TestActionsServiceProtocol.class).getLaunchRequirements(myDataView, getAuthorizationList());
            vortexFuture.addEventHandler(handleParameterListResponse);

        } catch (Exception myException) {
            hideWatchBox();
            WebMain.injector.getMainPresenter().conditionalAbort();
            Display.error("AbstractDataViewPresenter", 12, myException);
        }
    }

    public void launchRefreshData() {

        getRequiredParameters();
    }

    public void launchDataSourceEditor() {

        if (ConnectorSupport.getInstance().canEnterSourceEditor(getDataSources())) {

            SourceEditDialog myDialog = null;
            EditDataViewPresenter myPresenter = new EditDataViewPresenter(dataView);
//            WebMain.injector.getEventBus().fireEvent( new csi.client.gwt.events.EnterSourceEditModeEvent(myPresenter));
            try {

                myDialog = WebMain.injector.getMainPresenter().createSourceEditDialog(myPresenter, null, myPresenter.getName(), null, dataView.getUuid());
                myDialog.show();

            } catch (Exception myException) {

                if (null != myDialog) {

                    myDialog.abort(myException);

                } else {

                    Display.error("AbstractDataViewPresenter", 13, myException);
                }
            }

        } else {

            Display.error(i18n.security_NotAuthorized());
        }
    }

    public void markDeleted() {

        _isDeleted = true;
    }

    public void checkStatus() {

        if (_isDeleted) {

            List<ButtonDef> myButtonList = new ArrayList<ButtonDef>(1);

            myButtonList.add(new ButtonDef(Dialog.txtOkayButton));

            (new DecisionDialog(i18n.deleteResource_DisplayOutOfDate(),
                    i18n.deleteResource_ResourceDeleted(AclResourceType.DATAVIEW.getLabel(),
                            Format.value(getDisplayName())), myButtonList, new ChoiceMadeEventHandler() {
                @Override
                public void onChoiceMade(ChoiceMadeEvent event) {

                    WebMain.injector.getMainPresenter().clearDataViewDisplay();
                }
            })).show(60);
        }
    }

    public void addLinkup(LinkupDataTransfer linkupDataIn) {
        showWatchBox(i18n.abstractDataViewPresenterAddMessage()); //$NON-NLS-1$
        VortexFuture<LinkupDataTransfer> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(DataViewActionServiceProtocol.class).addLinkupInformation(linkupDataIn);
            vortexFuture.addEventHandler(handleNewLinkup);

        } catch (Exception myException) {

            hideWatchBox();
            Display.error(myException.getMessage());
        }
    }

    public void updateLinkup(LinkupDataTransfer linkupDataIn) {
        showWatchBox(i18n.abstractDataViewPresenterUpdateMessage()); //$NON-NLS-1$
        VortexFuture<LinkupDataTransfer> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(DataViewActionServiceProtocol.class).updateLinkupInformation(linkupDataIn);
            vortexFuture.addEventHandler(handleUpdatedLinkup);

        } catch (Exception myException) {

            hideWatchBox();
            Display.error(myException.getMessage());
        }
    }

    public void removeLinkup(LinkupDataTransfer linkupDataIn) {
        showWatchBox(i18n.abstractDataViewPresenterDeleteMessage()); //$NON-NLS-1$
        VortexFuture<Void> vortexFuture2 = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture2.execute(DataViewActionServiceProtocol.class).removeLinkup(linkupDataIn);
            vortexFuture2.addEventHandler(new AbstractVortexEventHandler<Void>() {
                @Override
                public boolean onError(Throwable errorIn) {
                    hideWatchBox();
                    fireEvent(new DataChangeEvent(DataOperation.DELETE, errorIn));
                    return true;
                }

                @Override
                public void onSuccess(Void result) {
                    SynchronizeChanges.removeLinkup(getDataView().getMeta(), linkupDataIn.getObjectOfInterest());
                    VortexFuture<List<FieldDef>> vortexFuture1 = WebMain.injector.getVortex().createFuture();
                    try {
                        vortexFuture1.execute(DataViewActionServiceProtocol.class).getLinkupDiscardedFields(linkupDataIn.dataViewUuid);
                        vortexFuture1.addEventHandler(new AbstractVortexEventHandler<List<FieldDef>>() {
                            @Override
                            public boolean onError(Throwable errorIn) {
                                hideWatchBox();
                                fireEvent(new DataChangeEvent(DataOperation.DELETE, errorIn));
                                return true;
                            }

                            @Override
                            public void onSuccess(List<FieldDef> deletedFields) {
                                VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
                                try {
                                    linkupDataIn.setDeletedFields(deletedFields);
                                    vortexFuture.execute(DataViewActionServiceProtocol.class).removeLinkupDiscardedFields(linkupDataIn);
                                    vortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {
                                        @Override
                                        public boolean onError(Throwable errorIn) {
                                            hideWatchBox();
                                            fireEvent(new DataChangeEvent(DataOperation.DELETE, errorIn));
                                            return true;
                                        }

                                        @Override
                                        public void onSuccess(Void result) {
                                            hideWatchBox();
                                            SynchronizeChanges.removeLinkupDiscardedFields(getDataView().getMeta(), linkupDataIn.getDeletedFields());
                                        }
                                    });
                                } catch (Exception myException) {
                                    hideWatchBox();
                                    Display.error(myException.getMessage());
                                }
                            }
                        });
                    } catch (Exception myException) {
                        hideWatchBox();
                        Display.error(myException.getMessage());
                    }
                }
            });
        } catch (Exception myException) {
            hideWatchBox();
            Display.error(myException.getMessage());
        }
    }

    public List<LinkupMapDef> getLinkupMappings() {
        List<LinkupMapDef> myMappings = null;
        try {
            myMappings = metaData.getLinkupDefinitions();

        } catch (Exception myException) {

            Display.error(myException.getMessage());
        }
        return myMappings;
    }

    public String generateFieldName(String baseNameIn) {

        String myName = baseNameIn;

        if (fieldAcces.containsFieldName(myName)) {
            myName += "_L(" + Integer.toString(getLinkupList().size()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return myName;
    }

    public boolean containsFieldWithName(String nameIn) {

        return fieldAcces.containsFieldName(nameIn);
    }

    public boolean containsFieldWithLocalId(String localIdIn) {

        return fieldAcces.containsFieldLocalId(localIdIn);
    }

    protected List<LinkupMapDef> getLinkupList() {

        if (null == metaData.getLinkupDefinitions()) {
            metaData.setLinkupDefinitions(new ArrayList<LinkupMapDef>());
        }
        return metaData.getLinkupDefinitions();
    }

    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    public BroadcastManager getBroadcastManager() {
        return broadcastManager;
    }

    public String getName() {
        return dataView.getName();
    }

    public String getDisplayName() {
        String myTemp = getName();
        String myName = (myTemp.length() > _nameMax) ? myTemp.substring(0, _nameMax) + " . . ." : myTemp;
        return myName;
    }

    public String getRemarks() {
        return dataView.getRemarks();
    }

    public String getUuid() {
        return dataViewUuid;
    }

    public boolean isUuid(String uuidIn) {

        return (null != uuidIn) && uuidIn.equals(dataViewUuid);
    }

    public FieldDef getFieldByLocalId(String localIdIn) {
        return fieldAcces.getFieldDefByLocalId(localIdIn);
    }

    public DataView getResource() {
        return getDataView();
    }

    public DataView getDataView() {
        return dataView;
    }

    public void setName(String nameIn) {
        dataView.setName(nameIn);
    }

    public void setRemarks(String remarksIn) {
        dataView.setRemarks(remarksIn);
    }

    public List<VisualizationDef> getVizDefs() {
        return getResource().getMeta().getModelDef().getVisualizations();
    }

    public abstract List<Visualization> getVisualizations();

    public abstract Widget getView();

    public EventBus getEventBus() {
        return eventBus;
    }

    public DataViewLoadingCallback getCallback() {
        return callback;
    }

    public void setCallback(DataViewLoadingCallback callback) {
        this.callback = callback;
    }


    public boolean isRemovable() {

        return _removeable;
    }

    public boolean isReadOnly() {

        return _readOnly;
    }

    public boolean canUpdate() {

        if (_readOnly) {

            Display.error(i18n.readOnlyError(Format.value(getDisplayName())));
        }
        return !_readOnly;
    }

    public void displaySecurityBanner() {

        SecurityBanner.displayBanner(metaData);
    }

    protected void initDataViewAccess(DataView dataViewIn) {

        dataView = dataViewIn;
        dataViewUuid = dataView.getUuid();
        metaData = dataView.getMeta();
        dataModel = metaData.getModelDef();
        fieldAcces = dataModel.getFieldListAccess();

        verifyOwnership();
        displaySecurityBanner();
    }

    private void verifyOwnership() {

        try {

            VortexFuture<AccessRights> vortexFuture = WebMain.injector.getVortex().createFuture();

            _isOwner = false;
            vortexFuture.addEventHandler(handleOwnershipResponse);
            vortexFuture.execute(ModelActionsServiceProtocol.class).verifyAccess(getUuid());

        } catch (Exception myException) {

            Display.error("AbstractDataViewPresenter", 14, myException);
        }
    }

    private void adjustVisualizations() {

        if (_readOnly) {

            Widget myDisplayWidget = getView();

            for (Visualization myVisualization : getVisualizations()) {

                if (null != myVisualization) {

                    try {

                        myVisualization.setReadOnly();

                    } catch (Exception myException) {

                        Display.error("AbstractDataViewPresenter", 15, myException);
                    }
                }
            }

            for (VisualizationDef myVizDef : getDataView().getMeta().getModelDef().getVisualizations()) {

                if (null != myVizDef) {

                    try {

                        myVizDef.setReadOnly();

                    } catch (Exception myException) {

                        Display.error("AbstractDataViewPresenter", 16, myException);
                    }
                }
            }

            if ((null != myDisplayWidget) && (myDisplayWidget instanceof DataViewWidget)) {

                ((DataViewWidget) myDisplayWidget).setReadOnly();
            }
        }
    }

    public void resetTableCaching() {

        for (Visualization myVisualization : getVisualizations()) {

            if (myVisualization instanceof TableViewDef) {
                ((TableViewDef) myVisualization).resetCache();
            }
        }
    }

    public void onDataChange() {

        displaySecurityBanner();
        resetTableCaching();
    }

    public void show() {

        // DO NOTHING -- "show" is a meaningless concept for a non visual object
    }

    public void hide() {

        // DO NOTHING -- "show" is a meaningless concept for a non visual object
    }

    //
    // Request the list of required fields from the server
    //
    public void retrieveRequiredFieldList(VortexEventHandler<List<String>> handlerIn) {

        String myUuid = (null != metaData) ? metaData.getUuid() : null;

        if (null != myUuid) {

            VortexFuture<List<String>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.addEventHandler(handlerIn);
                myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).testFieldReferences(myUuid);

            } catch (Exception myException) {

                Display.error("AbstractDataViewPresenter", 17, myException);
            }
        }
    }

    public void retrieveVisualizationsUsingField(VortexEventHandler<List<String>> handlerIn, String fieldUuid) {

        String myUuid = (null != metaData) ? metaData.getUuid() : null;

        if (null != myUuid) {

            VortexFuture<List<String>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.addEventHandler(handlerIn);
                myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).testFieldReferenceAndReturnViz(myUuid, fieldUuid);

            } catch (Exception myException) {

                Display.error("AbstractDataViewPresenter", 17, myException);
            }
        }
    }

    //
    // Request the list of required fields from the server
    //
    public void retrieveRequiredCoreFieldList(VortexEventHandler<List<String>> handlerIn) {

        String myUuid = (null != metaData) ? metaData.getUuid() : null;

        if (null != myUuid) {

            VortexFuture<List<String>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.addEventHandler(handlerIn);
                myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).testCoreFieldReferences(myUuid);

            } catch (Exception myException) {

                Display.error("AbstractDataViewPresenter", 18, myException);
            }
        }
    }

    private void showRefreshWatchBox(VortexFuture futureIn) {

        showWatchBox(futureIn, cancelCallback, i18n.abstractDataViewPresenterRefreshMessage());
    }

    public abstract Viewer getViewer();

}
