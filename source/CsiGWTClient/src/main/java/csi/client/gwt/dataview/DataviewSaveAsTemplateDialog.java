package csi.client.gwt.dataview;

import java.util.List;

import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.ResourceSelectorDialog;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel.SelectorMode;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;
import csi.server.common.service.api.ModelActionsServiceProtocol;


public class DataviewSaveAsTemplateDialog extends ResourceSelectorDialog {
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                             GUI Objects from the XML File                              //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String TEMPLATE = "Template"; //$NON-NLS-1$

	private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtHelpTarget = _constants.dataviewSaveAsTemplateDialog_HelpTarget();
    private static final String _txtInfoString = _constants.resourceSaveAsDialog_InfoString(TEMPLATE); //$NON-NLS-1$
    private static final String _txtFailureDialogTitle = _constants.resourceSaveAsDialog_FailureTitle(TEMPLATE); //$NON-NLS-1$
    private static final String _txtSuccessResponseTitle = _constants.resourceSaveAsDialog_SuccessTitle(TEMPLATE); //$NON-NLS-1$

    AbstractDataViewPresenter _dataViewPresenter;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private VortexEventHandler<Response<String, Resource>> handleSaveAsResponse = new AbstractVortexEventHandler<Response<String, Resource>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {
            hideWatchBox();
            // Display error message.
            Dialog.showException(_txtFailureDialogTitle, exceptionIn);
            unlockDisplay();
            return false;
        }
        
        @Override
        public void onSuccess(Response<String, Resource> responseIn) {

            hideWatchBox();
            if (ResponseHandler.isSuccess(responseIn)) {
                
                Resource myResult = responseIn.getResult();

                Dialog.showSuccess(_txtSuccessResponseTitle, _constants.resourceSaveAsDialog_SuccessMessage(TEMPLATE, myResult.getName())); //$NON-NLS-1$
                _dataViewPresenter = null;
                destroy();
            }
        }
    };
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
 
    public DataviewSaveAsTemplateDialog(AbstractDataViewPresenter dataViewPresenterIn) {

        super(SelectorMode.WRITE);
        
        _dataViewPresenter = dataViewPresenterIn;
        selectorWidget.setDefaultName(_dataViewPresenter.getDataView().getName());

        retrieveDisplayList();
    }
    
    public void show() {

        show(AclResourceType.TEMPLATE, _txtHelpTarget, _txtInfoString);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void handleSelectionMade(ResourceBasics selectionIn, boolean forceOverWriteIn) {

        if(selectButton != null && selectButton.isEnabled()){
            selectButton.setEnabled(false);
            executeRequest(selectionIn.getName(), selectionIn.getRemarks(), forceOverWriteIn);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Request data from the server to initialize the list dataviews to prevent naming conflicts
    //
    private void retrieveDisplayList() {
        VortexFuture<List<List<ResourceBasics>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            //
            // !! Not currently bringing back names of templates we cannot access !!
            //
            myVortexFuture.addEventHandler(handleTripleListRequestResponse);
            myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).getTemplateOverWriteControlLists();

        } catch (Exception myException) {
            
            Dialog.showException(_txtFailureDialogTitle, myException);
        }
    }
    
    //
    //
    //
    private void executeRequest(String nameIn, String remarksIn, boolean forceOverWriteIn) {
        DataViewDef myTemplate = _dataViewPresenter.getDataView().getMeta();
        VortexFuture<Response<String, Resource>> myVortexFuture = WebMain.injector.getVortex().createFuture();
        
//        myTemplate.setRemarks(remarksIn);
        
        try {

            showWatchBox();
            myVortexFuture.addEventHandler(handleSaveAsResponse);
            myVortexFuture.execute(ModelActionsServiceProtocol.class).saveDataviewAsTemplate( _dataViewPresenter.getUuid(), nameIn, remarksIn,  forceOverWriteIn);

        } catch (Exception myException) {

            hideWatchBox();
            Dialog.showException(_txtFailureDialogTitle, myException);
        }
    }
}
