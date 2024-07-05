package csi.client.gwt.csi_resource;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.wizards.ParameterWizard;
import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.events.UserInputEvent;
import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.ui.uploader.FileSelectorDialog;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.service.api.TestActionsServiceProtocol;
import csi.server.common.service.api.UploadServiceProtocol;
import csi.server.common.util.ValuePair;

import java.util.List;
import java.util.Map;

/**
 * Created by centrifuge on 4/9/2019.
 */
public class RefreshInstalledTable {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final String _txtrefreshErrorDialogTitle = _constants.refreshErrorDialogTitle();
    private static final String _txtParameterRefreshTitle = _constants.parameterRefreshTitle();
    private static final String _txtParameterRefreshHelp = _constants.parameterRefreshHelpTarget();
    private static final String _txtParameterRefreshButton = Dialog.txtRefreshButton;

    private List<QueryParameterDef> _requiredParameters;
    private List<DataSourceDef> _requiredAuthorizations;
    private List <AuthDO> _credentials;
    private List <LaunchParam> _parameters;
    private String _uuid;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private VortexEventHandler<Response<String, ValuePair<Boolean, InstalledTable>>> _externalHandler = null;

    private VortexEventHandler<Response<String, ValuePair<Boolean, InstalledTable>>> _handleUpdateResponse
            = new VortexEventHandler<Response<String, ValuePair<Boolean, InstalledTable>>>() {
        @Override
        public void onSuccess(Response<String, ValuePair<Boolean, InstalledTable>> resultIn) {

            hideWatchBox();
            if (null != _externalHandler) {

                _externalHandler.onSuccess(resultIn);
            }
        }

        @Override
        public boolean onError(Throwable exceptionIn) {

            hideWatchBox();
            if (null != _externalHandler) {

                _externalHandler.onError(exceptionIn);
            }
            return false;
        }

        @Override
        public void onUpdate(int taskProgessIn, String taskMessageIn) {

            if (null != _externalHandler) {

                _externalHandler.onUpdate(taskProgessIn, taskMessageIn);
            }
        }

        @Override
        public void onCancel() {

            hideWatchBox();
            if (null != _externalHandler) {

                _externalHandler.onCancel();
            }
        }
    };

    private UserInputEventHandler<Integer> processLogon = new UserInputEventHandler<Integer>() {

        @Override
        public void onUserInput(UserInputEvent<Integer> eventIn) {

            if (!eventIn.isCanceled()) {

                try {

                    launchRefresh();

                } catch (Exception myException) {

                    Display.error("RefreshInstalledTable", 1, myException);
                }
            }
            _requiredAuthorizations = null;
        }
    };

    private VortexEventHandler<Response<String, InstalledTable>> handlePrepareForRefreshResponse
            = new VortexEventHandler<Response<String, InstalledTable>>() {
        @Override
        public void onSuccess(Response<String, InstalledTable> resultIn) {

            hideWatchBox();
            try {

                if (ResponseHandler.isSuccess(resultIn)) {

                    InstalledTable myTable = resultIn.getResult();

                    switch (myTable.getUploadType()) {

                        case DATAVIEW:

                            Display.error("RefreshInstalledTable",
                                    "This Installed Table can only be refreshed by selecting data from an Opened DataView.");
                            break;

                        case ADHOC:

                            getRequiredParameters(myTable);
                            break;

                        case CSV:
                        case TEXT:
                        case NEW_EXCEL:

                            (new FileSelectorDialog(null, null, myTable)).show();
                            break;
                    }
                }

            } catch (Exception myException) {

            }
        }

        @Override
        public boolean onError(Throwable exceptionIn) {
            hideWatchBox();
            return false;
        }

        @Override
        public void onUpdate(int taskProgessIn, String taskMessageIn) {

        }

        @Override
        public void onCancel() {

            hideWatchBox();
        }
    };

    private VortexEventHandler<Response<Object, List<QueryParameterDef>>> handleParameterListResponse
            = new AbstractVortexEventHandler<Response<Object, List<QueryParameterDef>>>() {

        @Override
        public boolean onError(Throwable t) {
            hideWatchBox();
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
                        (new ParameterWizard(_txtParameterRefreshTitle, _txtParameterRefreshHelp,
                                                _txtParameterRefreshButton, _requiredParameters,
                                                handleParameterLoadingComplete)).show();

                    } else {

                        handleParameterLoadingComplete.onUserInput(new UserInputEvent<List<LaunchParam>>(false));
                    }

                } catch (Exception myException) {
                    hideWatchBox();
                    Display.error("RefreshInstalledTable", 2, myException);
                }

            } else {

                hideWatchBox();
            }
        }
    };

    private UserInputEventHandler<List<LaunchParam>> handleParameterLoadingComplete
            = new UserInputEventHandler<List<LaunchParam>>() {

        @Override
        public void onUserInput(UserInputEvent<List<LaunchParam>> eventIn) {

            if (eventIn.isCanceled()) {

                hideWatchBox();

            } else {

                _parameters = eventIn.getKey();

                try {

                    if ((null != _requiredAuthorizations) && (0 < _requiredAuthorizations.size())) {

                        hideWatchBox();
                        Dialog.showCredentialDialogs(getAuthorizationMap(), _requiredAuthorizations, processLogon);

                    } else {

                        launchRefresh();
                    }

                } catch (Exception myException) {

                    hideWatchBox();
                    Display.error("RefreshInstalledTable", 3, myException);
                    WebMain.injector.getMainPresenter().conditionalAbort();
                }
            }
        }
    };



    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public RefreshInstalledTable(String uuidIn,
                                 VortexEventHandler<Response<String, ValuePair<Boolean, InstalledTable>>>
                                         handleUpdateResponseIn) {

        _uuid = uuidIn;
        _externalHandler = handleUpdateResponseIn;
    }

    public void begin() {

        try {

            showWatchBox();
            VortexFuture<Response<String, InstalledTable>> vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.addEventHandler(handlePrepareForRefreshResponse);
            vortexFuture.execute(UploadServiceProtocol.class).prepareForRefresh(_uuid);

        } catch (Exception myException) {

            hideWatchBox();
            Display.error("RefreshInstalledTable", 4, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Private Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void launchRefresh() {

        VortexFuture<Response<String, ValuePair<Boolean, InstalledTable>>> myVortexFuture
                = WebMain.injector.getVortex().createFuture();

        try {

            showWatchBox();
            myVortexFuture.addEventHandler(_handleUpdateResponse);
            myVortexFuture.execute(UploadServiceProtocol.class).updateInstalledTable(_uuid, _credentials, _parameters);

        } catch (Exception myException) {

            hideWatchBox();
            Display.error("RefreshInstalledTable", 5, myException);
        }
    }

    private void getRequiredParameters(InstalledTable tableIn) {

        VortexFuture<Response<Object, List<QueryParameterDef>>> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {

            showWatchBox();
            vortexFuture.execute(TestActionsServiceProtocol.class).getLaunchRequirements(tableIn.getSourceDefinition(),
                    getAuthorizationList());
            vortexFuture.addEventHandler(handleParameterListResponse);

        } catch (Exception myException) {
            hideWatchBox();
            Display.error("RefreshInstalledTable", 6, myException);
        }
    }

    public Map<String, AuthDO> getAuthorizationMap() {

        return WebMain.injector.getMainPresenter().getAuthorizationMap(); // TODO
    }

    private List<AuthDO> getAuthorizationList() {

        return WebMain.injector.getMainPresenter().getAuthorizationList();
    }
    private void showWatchBox() {

        WatchBox.getInstance().show();
    }

    private void hideWatchBox() {

        WatchBox.getInstance().hide();
    }
}
