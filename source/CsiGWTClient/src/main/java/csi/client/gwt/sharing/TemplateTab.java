package csi.client.gwt.sharing;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.AdHocEditLauncher;
import csi.client.gwt.csiwizard.dialogs.DataViewFromTemplateDialog;
import csi.client.gwt.csiwizard.dialogs.SourceEditDialog;
import csi.client.gwt.csiwizard.wizards.ResourceParameterWizard;
import csi.client.gwt.edit_sources.presenters.EditTemplatePresenter;
import csi.client.gwt.events.TemplateNameChangeEvent;
import csi.client.gwt.events.TemplateNameChangeEventHandler;
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
import csi.server.common.dto.SharingDisplay;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.linkup.TemplateResponse;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;

/**
 * Created by centrifuge on 7/8/2015.
 */
public class TemplateTab extends ResourceTab {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final String _txtWizardTitle = _constants.dataviewFromTemplateWizard_DialogTitle();
    private static final String _txtHelpTarget = _constants.dataviewFromTemplateWizard_HelpTarget();


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

                watchBox.hide();
                // Display error message.
                Display.error("handleGetTemplateResponse", exceptionIn);

            } catch (Exception myException) {

                Display.error("TemplateTab", 0, myException);
            }
            return false;
        }

        @Override
        public void onSuccess(Response<String, TemplateResponse> responseIn) {

            try {

                watchBox.hide();
                if (ResponseHandler.isSuccess(responseIn)) {

                    TemplateResponse myResponse = responseIn.getResult();

                    if (null != myResponse) {

                        DataViewDef myTemplate = myResponse.getTemplate();

                        if (null != myTemplate) {

                            SourceEditDialog myDialog = null;
                            EditTemplatePresenter myPresenter = new EditTemplatePresenter(myTemplate);

                            try {
                                myDialog = WebMain.injector.getMainPresenter().createSourceEditDialog(myPresenter, null, myPresenter.getName(), null, 
                                        myResponse.getTemplate().getUuid());
                                myDialog.show();

                            } catch (Exception myException) {

                                if (null != myDialog) {

                                    myDialog.abort(myException);

                                } else {

                                    Display.error("TemplateTab", 1, myException);
                                }
                            }
                        }

                    } else {

                        ResponseHandler.displayError(responseIn);
                    }
                }

            } catch (Exception myException) {

                Display.error("TemplateTab", 2, myException);
            }
        }
    };

    private ClickHandler handleEditRequest = new ClickHandler() {

        public void onClick(ClickEvent eventIn) {

            SharingDisplay myItem = getSelection();

            if (null != myItem) {

                retrieveTemplate(myItem.getUuid());
            }
        }
    };

    private ClickHandler handleLaunchRequest = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {

            SharingDisplay myItem = getSelection();

            if (null != myItem) {

                (new ResourceParameterWizard(new ResourceBasics(myItem), _txtWizardTitle, _txtHelpTarget)).show();
                (new DataViewFromTemplateDialog(myItem.getUuid())).show();
            }
        }
    };

    private ClickHandler handleCreateRequest = new ClickHandler() {

        public void onClick(ClickEvent eventIn) {

            try {

                parent.saveState();
                (new AdHocEditLauncher(AclResourceType.TEMPLATE, handleInstallComplete)).show();

            } catch (Exception myException) {

                Display.error("TemplateTab", 3, myException);
            }
        }
    };

    protected ClickHandler getExportClickHandler() {

        return handleExportRequest;
    }

    protected ClickHandler getRenameClickHandler() {

        return handleRenameRequest;
    }

    protected ClickHandler getEditClickHandler() {

        return handleEditRequest;
    }

    protected ClickHandler getCreateClickHandler() {

        return handleCreateRequest;
    }

    protected ClickHandler getDeleteClickHandler() {

        return buildDeleteDialog();
    }

    protected ClickHandler getClassificationHandler() {

        return handleEditClassificationRequest;
    }

    protected ClickHandler getLaunchClickHandler() {

        return handleLaunchRequest;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public TemplateTab(ResourceSharingView parentIn) {

        super(parentIn, "sharing.TemplateTab");
        wireInHandlers();
    }

    @Override
    public IconType getIconType() {

        return IconType.TABLE;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected String getResourceTypeString() {

        return "Template";
    }

    protected String getResourceTypePluralString() {

        return "Templates";
    }

    protected AclResourceType getResourceType() {

        return AclResourceType.TEMPLATE;
    }

    protected String getEditButtonLabel() {

        return Dialog.txtEditButton;
    }

    protected String getCreateButtonLabel() {

        return _constants.manageResources_DataViewTab_newTemplateButton();
    }

    @Override
    protected void wireInHandlers() {

        super.wireInHandlers();

        WebMain.injector.getEventBus().addHandler(TemplateNameChangeEvent.type, new TemplateNameChangeEventHandler() {

            @Override
            public void onTemplateNameChange(TemplateNameChangeEvent eventIn) {

                if (null != eventIn) {

                    renameLocalResourceEntry(eventIn.getUuid(), eventIn.getName(), eventIn.getRemarks());
                }
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void retrieveTemplate(String uuidIn) {

        VortexFuture<Response<String, TemplateResponse>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            //
            // !! Not currently bringing back names of templates we cannot access !!
            //
            myVortexFuture.addEventHandler(handleGetTemplateResponse);
            myVortexFuture.execute(DataViewDefActionsServiceProtocol.class, uuidIn).getTemplate(uuidIn, AclControlType.EDIT);
            watchBox.show();

        } catch (Exception myException) {

            Display.error("TemplateTab", 4, myException);
        }
    }
}
