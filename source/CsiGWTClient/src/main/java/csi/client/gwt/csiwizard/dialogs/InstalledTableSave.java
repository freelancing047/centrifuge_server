package csi.client.gwt.csiwizard.dialogs;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.events.TransferCompleteEvent;
import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.service.api.ModelActionsServiceProtocol;
import csi.server.common.service.api.UploadServiceProtocol;

import java.util.List;

/**
 * Created by centrifuge on 6/22/2017.
 */
public class InstalledTableSave extends DataViewSave {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String _txtSelectionPrompt
            = _constants.restrictedResourceSaveAsDialog_InfoString(AclResourceType.DATA_TABLE.getLabel());
    protected static final String _txtFailureDialogTitle = _constants.serverRequest_FailureDialogTitle();

    private String _tableName = null;
    private String _tableRemarks = null;
    private boolean _overWrite = false;
    private SharingInitializationRequest _sharingRequest = null;
    private List<LaunchParam> _parameterData = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    private VortexEventHandler<Response<String, InstalledTable>> handleCreateTableResponse
            = new AbstractVortexEventHandler<Response<String, InstalledTable>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            hideWatchBox();

            // Display error message.
            Display.error("InstalledTableSave", 1, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, InstalledTable> responseIn) {

            hideWatchBox();

            if (ResponseHandler.isSuccess(responseIn)) {

                WizardInterface myPrior = getPriorDialog();
                InstalledTable myResponse = responseIn.getResult();

                if (null != _handler) {

                    _handler.onTransferComplete(new TransferCompleteEvent(myResponse.getName()));
                }
                myPrior.clickPrior();
                destroy();
/*
                if (null != myResponse) {

                    Display.success(_constants.createdResource_SuccessBoxInfo(AclResourceType.DATA_TABLE.getLabel(), myResponse.getName()));

                } else {

                    ResponseHandler.displayError(responseIn);
                }
*/
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public InstalledTableSave(AdHocDataSource containerIn, WizardInterface priorDialogIn,
                              String titleIn, String helpIn, TransferCompleteEventHandler handlerIn,
                              List<LaunchParam> parameterDataIn, List<DataSourceDef> requiredAuthorizationsIn)
            throws CentrifugeException {

        super(containerIn, priorDialogIn, titleIn, helpIn, null, ResourceSelectorPanel.SelectorMode.NEW, handlerIn,
                WebMain.injector.getMainPresenter().getUserInfo().getName() + "." + CsiFileType.ADHOC.getExtension() + ".",
                requiredAuthorizationsIn);

        _parameterData = parameterDataIn;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void finalizeInitialization() {

        retrieveInstalledTableLists();
        panel.initializeDisplay(AclResourceType.DATA_TABLE, _txtSelectionPrompt);
    }

    @Override
    protected void createPanel() {

    }

    @Override
    protected void executeRequest(String nameIn, String remarksIn, boolean overWriteIn,
                                  SharingInitializationRequest sharingRequestIn) {

        VortexFuture<Response<String, InstalledTable>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            CapcoInfo myCapco = _container.getSecurityAccess().getCapcoInfo();
            SecurityTagsInfo myTags = _container.getSecurityAccess().getSecurityTagsInfo();

            myVortexFuture.addEventHandler(handleCreateTableResponse);

            myVortexFuture.execute(ModelActionsServiceProtocol.class).createDataResource(CsiUUID.randomUUID(), nameIn,
                                                            remarksIn, AclResourceType.DATA_TABLE, _container, _parameterData,
                                                            WebMain.injector.getMainPresenter().getAuthorizationList(),
                                                            myCapco, myTags, overWriteIn, sharingRequestIn);

            showWatchBox(myVortexFuture, cancelCallback, _constants.creatingResource_WatchBoxInfo(AclResourceType.DATA_TABLE.getLabel(), nameIn));

        } catch (Exception myException) {

            Display.error(_txtFailureDialogTitle, myException.getMessage());
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Request the list installed tables from the server to prevent naming conflicts
    //
    private void retrieveInstalledTableLists() {

        VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(panel.handleSingleListRequestResponse);
            myVortexFuture.execute(UploadServiceProtocol.class).getTableOverWriteControlList(CsiFileType.ADHOC);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }
}
