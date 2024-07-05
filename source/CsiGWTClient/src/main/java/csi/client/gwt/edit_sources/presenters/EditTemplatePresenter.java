package csi.client.gwt.edit_sources.presenters;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.Response;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;

import java.util.List;

/**
 * Created by centrifuge on 5/18/2017.
 */
public class EditTemplatePresenter extends DataSourceEditorPresenter {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private WizardDialog _dialog = null;
    private DataViewDef _template = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private VortexEventHandler<Response<String, DataViewDef>> handleUpdateTemplateResponse
            = new AbstractVortexEventHandler<Response<String, DataViewDef>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            _dialog.hideWatchBox();
            // Display error message.
            Display.error(exceptionIn);
            _model.adjustButtons();
            return false;
        }

        @Override
        public void onSuccess(Response<String, DataViewDef> responseIn) {

            _dialog.hideWatchBox();
            if (ResponseHandler.isSuccess(responseIn)) {

                _parent.close();
                Display.success("Template updated successfully.");
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public EditTemplatePresenter(DataViewDef templateIn) {

        super(templateIn);
        _template = templateIn;
    }

    @Override
    public EditDataViewPresenter close() {

        _dialog = null;
        return (EditDataViewPresenter)super.close();
    }

    @Override
    public boolean isFinal() {

        return ((!((WebMain.getClientStartupInfo().isEnforceSecurityTags()
                || WebMain.getClientStartupInfo().isProvideTagBanners())))
                && (!((WebMain.getClientStartupInfo().isEnforceCapcoRestrictions()
                || WebMain.getClientStartupInfo().isProvideCapcoBanners()))));
    }

    @Override
    public void saveResults(WizardDialog dialogIn) {

        _dialog = dialogIn;

        if (null != _template) {

            _dialog.showWatchBox();
            VortexFuture<Response<String, DataViewDef>> vortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                vortexFuture.addEventHandler(handleUpdateTemplateResponse);
                vortexFuture.execute(DataViewDefActionsServiceProtocol.class, _template.getUuid()).editTemplate(_template);

            } catch (Exception myException) {

                _dialog.hideWatchBox();
                Dialog.showException(myException);
                _model.adjustButtons();
            }
        }
    }

    //
    // Request the list of required fields from the server
    //
    public void retrieveRequiredFieldList(VortexEventHandler<List<String>> handlerIn) {

        VortexFuture<List<String>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(handlerIn);
            myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).testFieldReferences(_template.getUuid());

        } catch (Exception myException) {

            Display.error("DataSourceEditorModel", 2, myException);
        }
    }

    //
    // Request the list of required fields from the server
    //
    public void retrieveRequiredCoreFieldList(VortexEventHandler<List<String>> handlerIn) {

        VortexFuture<List<String>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(handlerIn);
            myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).testCoreFieldReferences(_template.getUuid());

        } catch (Exception myException) {

            Display.error("DataSourceEditorModel", 2, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

}
