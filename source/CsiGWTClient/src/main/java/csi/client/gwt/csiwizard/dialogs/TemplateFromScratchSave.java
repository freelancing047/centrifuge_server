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
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;

import java.util.List;

/**
 * Created by centrifuge on 6/22/2017.
 */
public class TemplateFromScratchSave extends DataViewSave {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String _txtFailureDialogTitle = _constants.serverRequest_FailureDialogTitle();

    private String _templateName = null;
    private String _templateRemarks = null;
    private boolean _overWrite = false;
    private SharingInitializationRequest _sharingRequest = null;


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
    private VortexEventHandler<Response<String, DataViewDef>> handleCreateTemplateResponse
            = new AbstractVortexEventHandler<Response<String, DataViewDef>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            hideWatchBox();

            // Display error message.
            Display.error("TemplateFromScratchSave", 1, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, DataViewDef> responseIn) {

            hideWatchBox();

            if (ResponseHandler.isSuccess(responseIn)) {

                DataViewDef myResponse = responseIn.getResult();

                if (null != myResponse) {

                    Display.success(_constants.createdResource_SuccessBoxInfo(AclResourceType.TEMPLATE.getLabel(), myResponse.getName()));

                    if (null != _handler) {

                        _handler.onTransferComplete(new TransferCompleteEvent(myResponse.getName()));
                    }

                } else {

                    ResponseHandler.displayError(responseIn);
                }
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public TemplateFromScratchSave(AdHocDataSource containerIn, WizardInterface priorDialogIn,
                                   String titleIn, String helpIn, TransferCompleteEventHandler handlerIn)
            throws CentrifugeException {

        super(containerIn, priorDialogIn, titleIn, helpIn, null,
                ResourceSelectorPanel.SelectorMode.WRITE, handlerIn, null);
    }

    @Override
    protected void createPanel() {

    }

    @Override
    protected void executeRequest(String nameIn, String remarksIn, boolean overWriteIn,
                                  SharingInitializationRequest sharingRequestIn) {

        VortexFuture<Response<String, DataViewDef>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(handleCreateTemplateResponse);

            myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).createTemplate(CsiUUID.randomUUID(),
                                                                                nameIn, remarksIn, _container,
                                                                                overWriteIn, sharingRequestIn);

            showWatchBox(_constants.creatingResource_WatchBoxInfo(AclResourceType.TEMPLATE.getLabel(), nameIn));

        } catch (Exception myException) {

            Display.error(_txtFailureDialogTitle, myException.getMessage());
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void finalizeInitialization() {

        retrieveTemplateList();
        panel.initializeDisplay(AclResourceType.TEMPLATE, _txtDataViewSelectionInfo);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Request the list templates from the server to prevent naming conflicts
    //
    private void retrieveTemplateList() {

        VortexFuture<List<List<ResourceBasics>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(panel.handleTripleListRequestResponse);
            myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).getTemplateOverWriteControlLists();
            showWatchBox();

        } catch (Exception myException) {

            Display.error("TemplateFromScratchSave", 2, myException);
        }
    }
}
