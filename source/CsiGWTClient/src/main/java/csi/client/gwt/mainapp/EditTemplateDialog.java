package csi.client.gwt.mainapp;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.ResourceSelectorDialog;
import csi.client.gwt.csiwizard.dialogs.SourceEditDialog;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel.SelectorMode;
import csi.client.gwt.edit_sources.presenters.EditTemplatePresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.linkup.TemplateResponse;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;
import csi.server.common.service.api.ModelActionsServiceProtocol;


public class EditTemplateDialog extends ResourceSelectorDialog {
    
    
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

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtHelpTarget = _constants.editTemplateDialog_HelpTarget();
    private static final String _txtInfoString = _constants.editTemplateDialog_InfoString(Dialog.txtOpenButton);
    
    private ResourceBasics _selection = null;
    private MainView.Mode _priorMode;
    private Widget _priorWidget;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected VortexEventHandler<Response<String, TemplateResponse>> handleGetTemplateResponse
            = new AbstractVortexEventHandler<Response<String, TemplateResponse>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                hideWatchBox();
                // Display error message.
                Dialog.showException(exceptionIn);

            } catch (Exception myException) {

                Dialog.showException("EditTemplateDialog", 0, myException);
            }
            return false;
        }

        @Override
        public void onSuccess(Response<String, TemplateResponse> responseIn) {

            try {

                hideWatchBox();
                if (ResponseHandler.isSuccess(responseIn)) {

                    TemplateResponse myResponse = responseIn.getResult();

                    if (null != myResponse) {

                        DataViewDef myTemplate = myResponse.getTemplate();

                        if (null != myTemplate) {

                            SourceEditDialog myDialog = null;
                            EditTemplatePresenter myPresenter = new EditTemplatePresenter(myTemplate);

                            try {

                                myDialog = WebMain.injector.getMainPresenter().createSourceEditDialog(myPresenter, null, myPresenter.getName(), null, myResponse.getTemplate().getUuid());
                                myDialog.show();

                            } catch (Exception myException) {

                                if (null != myDialog) {

                                    myDialog.abort(myException);

                                } else {

                                    Display.error("EditTemplateDialog", 1, myException);
                                }
                            }
                            destroy();
                        }

                    } else {

                        ResponseHandler.displayError(responseIn);
                    }
                }

            } catch (Exception myException) {

                Dialog.showException("EditTemplateDialog", 2, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public EditTemplateDialog() {

        super(SelectorMode.READ_ONLY);

        try {

            retrieveDisplayList();

        } catch (Exception myException) {

            Dialog.showException("EditTemplateDialog", myException);
        }
    }
    
    public void show() {

        try {

            show(AclResourceType.TEMPLATE, _txtHelpTarget, _txtInfoString, _constants.dialog_OpenButton());

        } catch (Exception myException) {

            Dialog.showException("EditTemplateDialog", myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void handleSelectionMade(ResourceBasics selectionIn, boolean forceOverwriteIn) {

        if (null != selectionIn) {

            String myUuid = selectionIn.getUuid();

            if (null != myUuid) {

                retrieveTemplate(myUuid);
            }
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

        VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            ResourceFilter myFilter = WebMain.injector.getMainPresenter().getDefaultResourceFilter();

            myVortexFuture.addEventHandler(handleFilteredListRequestResponse);
            myVortexFuture.execute(ModelActionsServiceProtocol.class).getFilteredResourceList(
                    AclResourceType.TEMPLATE, myFilter, AclControlType.EDIT);
            showWatchBox();

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    private void retrieveTemplate(String uuidIn) {

        VortexFuture<Response<String, TemplateResponse>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            //
            // !! Not currently bringing back names of templates we cannot access !!
            //
            myVortexFuture.addEventHandler(handleGetTemplateResponse);
            myVortexFuture.execute(DataViewDefActionsServiceProtocol.class, uuidIn).getTemplate(uuidIn, AclControlType.EDIT);
            showWatchBox();

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }
}
