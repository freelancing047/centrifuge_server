package csi.client.gwt.csiwizard.dialogs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewInNewTab;
import csi.client.gwt.events.*;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.dto.SelectionListData.SharingRequest;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.service.api.DataViewActionServiceProtocol;

import java.util.Arrays;
import java.util.List;

/**
 * Created by centrifuge on 6/14/2017.
 */
public abstract class DataViewSave extends WizardDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String _defaultName = _constants.dataviewCreationWizardDefaultName(); //$NON-NLS-1$
    protected static final String _txtFailureDialogTitle = _constants.serverRequest_FailureDialogTitle();
    protected static final String _txtChoiceDialogNewButtonString = _constants.openDataviewDialog_Choice_NewButtonString();
    protected static final String _txtChoiceDialogCurrentButtonString = _constants.openDataviewDialog_Choice_CurrentButtonString();
    protected static final String _txtChoiceDialogTitle = _constants.openDataviewDialog_Choice_DialogTitle();
    protected static final String _txtChoiceInfoString = _constants.openDataviewDialog_Choice_InfoString(_txtChoiceDialogNewButtonString, _txtChoiceDialogCurrentButtonString);
    protected static final String _txtCreateButton = _constants.dialog_CreateButton();
    protected static final String _txtInstructions = _constants.dataviewFromTemplateWizard_DataviewPanel(_txtCreateButton);
    protected static final String _txtDataViewSelectionInfo = _constants.resourceSaveAsDialog_InfoString(AclResourceType.DATAVIEW.getLabel()); //$NON-NLS-1$

    private AbstractDataViewPresenter _presenter = null;
    private List<DataSourceDef> _requiredAuthorizations = null;
    private String _dataViewUuid = null;
    private String _dataViewName = null;
    private String _dataViewRemarks = null;
    private long _rowCount = 0L;
    private boolean _moreData = false;
    private boolean _overWrite = false;
    private SharingInitializationRequest _sharingRequest = null;
    protected TransferCompleteEventHandler _handler = null;
    protected AdHocDataSource _container = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected ResourceSelectorPanel<ResourceBasics> panel = null;

    private static final List<ButtonDef> _buttonList = Arrays.asList(

            new ButtonDef(_txtChoiceDialogCurrentButtonString),
            new ButtonDef(_txtChoiceDialogNewButtonString)
    );


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    AbstractMethods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void executeRequest(String nameIn, String remarksIn,
                                           boolean overWrite, SharingInitializationRequest sharingIn);


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected AsyncCallback<Void> cancelCallback = new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caughtIn) {

            WebMain.injector.getMainPresenter().conditionalAbort();
            Display.error("Caught error canceling request.", caughtIn);
        }

        @Override
        public void onSuccess(Void result) {

            WebMain.injector.getMainPresenter().conditionalAbort();
            Display.success("Successfully canceled request.");
        }
    };

    //
    // Handle selection made from resource list
    //
    protected ResourceSelectionEventHandler<ResourceBasics> handleResourceSelection
            = new ResourceSelectionEventHandler<ResourceBasics>() {
        @Override
        public void onResourceSelection(ResourceSelectionEvent<ResourceBasics> eventIn) {

            try {

                _dataViewName = eventIn.getName();
                _dataViewRemarks = eventIn.getRemarks();
                _overWrite = eventIn.forceOverwrite();

                handleSelectionMade();

            } catch (Exception myException) {

                Dialog.showException("DataViewSave", 1, myException);
            }
        }
    };

    protected void handleSelectionMade() {

        if ((null != _requiredAuthorizations) && (0 < _requiredAuthorizations.size())) {

            try {

                Dialog.showCredentialDialogs(WebMain.injector.getMainPresenter().getAuthorizationMap(),
                                                _requiredAuthorizations, processLogon);

            } catch (Exception myException) {

                Dialog.showException("DataViewSave", 4, myException);
            }

        } else {

            //
            // Create new resource
            //
            executeRequest(_dataViewName, _dataViewRemarks, _overWrite, _sharingRequest);
        }
    }

    //
    //
    //
    protected VortexEventHandler<Response<String, DataView>> handleCreateDataViewResponse
            = new AbstractVortexEventHandler<Response<String, DataView>>() {

        @Override
        public void onUpdate(int taskProgessIn, String taskMessageIn) {

            if (0 < taskProgessIn) {

                if (null != taskMessageIn) {

                    updateWatchBox(taskMessageIn + ": Processed " + Integer.toString(taskProgessIn) + ",000 rows.");

                } else {

                    updateWatchBox("Processed " + Integer.toString(taskProgessIn) + ",000 rows.");
                }
            }
        }

        @Override
        public boolean onError(Throwable exceptionIn) {

            hideWatchBox();

            // Display error message.
            Dialog.showException("DataViewSave", 1, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, DataView> responseIn) {

            hideWatchBox();

            if (responseIn.isAuthorizationRequired()) {

                try {

                    Dialog.showCredentialDialogs(WebMain.injector.getMainPresenter().getAuthorizationMap(),
                                                    responseIn.getAuthorizationList(), processLogon);

                } catch (Exception myException) {

                    Dialog.showException("DataViewSave", 2, myException);
                }

            } else if (ResponseHandler.isSuccess(responseIn)) {

                DataView myResponse = responseIn.getResult();

                if (null != myResponse) {

                    _dataViewUuid = myResponse.getUuid();
                    _dataViewName = myResponse.getName();
                    _rowCount = responseIn.getCount();
                    _moreData = responseIn.getLimitedData();
                    WebMain.injector.getMainPresenter().beginOpenDataView(_dataViewUuid, _rowCount, _moreData);

                    if (null != _presenter) {
                        //
                        // Hide but do not destroy
                        //
                        hide();

                    } else {
                        //
                        // free resources and leave
                        //
                        destroy();
                    }

                } else {

                    ResponseHandler.displayError(responseIn);
                }
            }
        }
    };

    private UserInputEventHandler<Integer> processLogon
            = new UserInputEventHandler<Integer>() {

        @Override
        public void onUserInput(UserInputEvent<Integer> eventIn) {

            if (!eventIn.isCanceled()) {

                try {

                    //
                    // Create new dataview
                    //
                    executeRequest(_dataViewName, _dataViewRemarks, _overWrite, _sharingRequest);

                } catch (Exception myException) {

                    Dialog.showException("DataViewSave", 3, myException);
                }
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    public DataViewSave(WizardInterface priorDialogIn, String titleIn, String helpIn,
                        String defaultNameInn, List<DataSourceDef> requiredAuthorizationsIn) {

        this(null, priorDialogIn, titleIn, helpIn, defaultNameInn,
                ResourceSelectorPanel.SelectorMode.WRITE, null, requiredAuthorizationsIn);
    }

    public DataViewSave(AdHocDataSource containerIn, WizardInterface priorDialogIn, String titleIn,
                        String helpIn, String defaultNameIn, ResourceSelectorPanel.SelectorMode modeIn,
                        TransferCompleteEventHandler handlerIn, List<DataSourceDef> requiredAuthorizationsIn) {

        this(containerIn, priorDialogIn, titleIn, helpIn, defaultNameIn,
                modeIn, handlerIn, null, requiredAuthorizationsIn);
    }

    public DataViewSave(AdHocDataSource containerIn, WizardInterface priorDialogIn, String titleIn,
                        String helpIn, String defaultNameIn, ResourceSelectorPanel.SelectorMode modeIn,
                        TransferCompleteEventHandler handlerIn, String prefixIn,
                        List<DataSourceDef> requiredAuthorizationsIn) {

        super(priorDialogIn, new ResourceSelectorPanel(null, modeIn, prefixIn), titleIn, helpIn, _txtInstructions);

        setAsFinal();
        _container = containerIn;
        _handler = handlerIn;
        panel = (ResourceSelectorPanel<ResourceBasics>)getCurrentPanel();
        panel.setDefaultName((null != defaultNameIn) ? defaultNameIn : _defaultName);
        panel.addResourceSelectionEventHandler(handleResourceSelection);
        panel.addValidityReportEventHandler(handleValidityReportEvent);
        _requiredAuthorizations = requiredAuthorizationsIn;
        DeferredCommand.add(new Command() {
            public void execute() {
                finalizeInitialization();
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void finalizeInitialization() {

        retrieveDataviewList();
        panel.initializeDisplay(AclResourceType.DATAVIEW, _txtDataViewSelectionInfo);
    }

    protected void execute() {

        if (null != panel) {

            panel.handleSelectButtonClick.onClick(null);
        }
    }

    protected void execute2() {

        _dataViewName = panel.getName();
        _dataViewRemarks = panel.getRemarks();

        if ((null != _requiredAuthorizations)
                && (0 < _requiredAuthorizations.size())) {

            try {

                Dialog.showCredentialDialogs(WebMain.injector.getMainPresenter().getAuthorizationMap(), _requiredAuthorizations, processLogon);

            } catch (Exception myException) {

                Dialog.showException("DataViewSave", 4, myException);
            }

        } else {

            //
            // Create new dataview
            //
            executeRequest(_dataViewName, _dataViewRemarks, _overWrite, _sharingRequest);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Request the list dataviews from the server to prevent naming conflicts
    //
    private void retrieveDataviewList() {

        VortexFuture<List<List<ResourceBasics>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(panel.handleTripleListRequestResponse);
            myVortexFuture.execute(DataViewActionServiceProtocol.class).getDataViewOverWriteControlLists();
            showWatchBox();

        } catch (Exception myException) {

            Dialog.showException("DataViewSave", 5, myException);
        }
    }
}
