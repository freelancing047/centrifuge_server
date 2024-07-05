package csi.client.gwt.csi_resource;

import java.util.List;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel.SelectorMode;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.SuccessDialog;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.Resource;
import csi.server.common.service.api.ModelActionsServiceProtocol;
import csi.server.common.util.Format;


public class ResourceSaveAsDialog<T extends Resource> extends ResourceSelectorDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtHelpTarget = _constants.templateSaveAsDialog_HelpTarget();
    private static final String _txtSaveSuccessTitle = _constants.applicationToolbar_SaveSuccess_DialogTitle();

    private String _txtInfoString;
    private String _txtFailureDialogTitle;
    private String _txtSuccessResponseTitle;

    private AclResourceType _resourceType;
    private ResourcePresenter<T> _resourcePresenter;
    private ResourceSaveCallback _callback = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private VortexEventHandler<Response<String, Resource>> handleSaveAsResponse
    = new AbstractVortexEventHandler<Response<String, Resource>>() {
        
        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                hideWatchBox();

                // Display error message.
                Display.error("ResourceSaveAsDialog", 1, exceptionIn);
                unlockDisplay();

            } catch (Exception myException) {

                Display.error("ResourceSaveAsDialog", 2, myException);
            }
            return false;
        }
        
        @Override
        public void onSuccess(Response<String, Resource> responseIn) {

            try {

                hideWatchBox();

                if (ResponseHandler.isSuccess(responseIn)) {

                    Resource myResult = responseIn.getResult();

                    Dialog.showSuccess(_txtSuccessResponseTitle,
                            _constants.resourceSaveAsDialog_SuccessMessage(_resourceType.getLabel(), myResult.getName()));

                    if (null != _callback) {

                        _callback.onSave();
                    }
                    _resourcePresenter = null;
                    destroy();
                }

            } catch (Exception myException) {

                Display.error("ResourceSaveAsDialog", 3, myException);
            }
        }
    };
    
    ResourceSaveCallback saveSelfCallback = new ResourceSaveCallback() {

        public void onSave() {

            try {

                (new SuccessDialog(_txtSaveSuccessTitle, _constants.applicationToolbar_SaveSuccess_InfoString(_resourcePresenter.getName()))).show();
                _resourcePresenter = null;
                destroy();

            } catch (Exception myException) {

                Display.error("ResourceSaveAsDialog", 4, myException);
            }
        }
    };
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    
    public ResourceSaveAsDialog(ResourcePresenter<T> resourcePresenterIn, ResourceSaveCallback callbackIn) {

        super(SelectorMode.WRITE);

        try {

            _resourcePresenter = resourcePresenterIn;
            _callback = callbackIn;
            _resourceType = _resourcePresenter.getResource().getResourceType();
            selectorWidget.setDefaultName(_resourcePresenter.getResource().getName());
            initializeStrings();

            retrieveDisplayList();

        } catch (Exception myException) {

            Display.error("ResourceSaveAsDialog", 5, myException);
        }
    }
    
    public ResourceSaveAsDialog(ResourcePresenter<T> resourcePresenterIn) {

        this(resourcePresenterIn, null);
    }
    
    public void show() {

        try {

            show(_resourceType, _txtHelpTarget, _txtInfoString);

        } catch (Exception myException) {

            Display.error("ResourceSaveAsDialog", 6, myException);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void handleSelectionMade(ResourceBasics selectionIn, boolean forceOverwriteIn) {

        if ((AclResourceType.DATAVIEW.equals(_resourceType))&& (_resourcePresenter.getResource().getUuid().equals(selectionIn.getKey()))) {
            // Save the dataview as itself.
            _resourcePresenter.setName(selectionIn.getName());
            _resourcePresenter.setRemarks(selectionIn.getRemarks());
            _resourcePresenter.save(saveSelfCallback);
            
        } else {

            executeRequest(selectionIn.getName(), selectionIn.getRemarks(), forceOverwriteIn);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Initialize strings for use in dialogs
    //
    private void initializeStrings() {

        _txtInfoString = _constants.resourceSaveAsDialog_InfoString(_resourceType.getLabel());
        _txtFailureDialogTitle = _constants.resourceSaveAsDialog_FailureTitle(_resourceType.getLabel());
        _txtSuccessResponseTitle = _constants.resourceSaveAsDialog_SuccessTitle(_resourceType.getLabel());
    }
    
    //
    // Request the list dataviews from the server to prevent naming conflicts
    //
    private void retrieveDisplayList() {
        VortexFuture<List<List<ResourceBasics>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            //
            // !! Not currently bringing back names of templates we cannot access !!
            //
            myVortexFuture.execute(ModelActionsServiceProtocol.class).getOverWriteControlLists(_resourceType);
            myVortexFuture.addEventHandler(handleTripleListRequestResponse);

        } catch (Exception myException) {
            
            Display.error(_constants.serverRequest_FailureDialogTitle(), myException);
        }
    }
    
    //
    //
    //
    private void executeRequest(String nameIn, String remarksIn, boolean forceOverwriteIn) {
        
//        T myTemplate = _resourcePresenter.getResource();
        VortexFuture<Response<String, Resource>> vortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            showWatchBox();
            vortexFuture.execute(ModelActionsServiceProtocol.class).saveDataviewAsDataview(_resourcePresenter.getUuid(), nameIn, remarksIn, forceOverwriteIn);
            vortexFuture.addEventHandler(handleSaveAsResponse);

        } catch (Exception myException) {

            hideWatchBox();
            Display.error(_txtFailureDialogTitle, myException);
        }
    }
}
