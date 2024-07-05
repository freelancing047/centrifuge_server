package csi.client.gwt.edit_sources.presenters;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.events.ReopenDataViewEvent;
import csi.client.gwt.mainapp.SecurityBanner;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.Response;
import csi.server.common.model.dataview.DataView;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;

import java.util.List;

/**
 * Created by centrifuge on 5/18/2017.
 */
public class EditDataViewPresenter extends DataSourceEditorPresenter {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private WizardDialog _dialog = null;
    private DataView _dataView = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler errorResponseHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            WebMain.injector.getMainPresenter().refreshDataView();
        }
    };

    private VortexEventHandler<Response<String, DataView>> handleUpdateDataViewResponse
            = new AbstractVortexEventHandler<Response<String, DataView>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            _dialog.hideWatchBox();
            _dialog = null;
            // Display error message.
            Display.error("EditDataViewPresenter", 1, exceptionIn);
            WebMain.injector.getMainPresenter().refreshDataView();
            return false;
        }

        @Override
        public void onSuccess(Response<String, DataView> responseIn) {

            _dialog.hideWatchBox();
            _dialog = null;
            if (ResponseHandler.isSuccess(responseIn, errorResponseHandler)) {

                DataView myDataView = responseIn.getResult();
                String myUuid = myDataView.getUuid();
                String myName = myDataView.getName();

                WebMain.injector.getEventBus().fireEvent(new ReopenDataViewEvent(myUuid, myName));
//                WebMain.injector.getMainPresenter().closeExistingDataViewAndOpenNewOne(((DataView) _resource).getUuid());
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public EditDataViewPresenter(DataView dataViewIn) {

        super(dataViewIn);
        _dataView = dataViewIn;
    }

    @Override
    public void restoreState() {

        if (null != _dataView) {

            SecurityBanner.displayBanner(_dataView.getMeta());
        }
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

        try {

            if (null != _dataView) {

                _dataView.setNeedsRefresh(true);
                _dataView.setSpinoff(false);

                _dialog.showWatchBox("Updating DataView framework on server.");
                try {

                    VortexFuture<Response<String, DataView>> vortexFuture = WebMain.injector.getVortex().createFuture();

                    vortexFuture.execute(DataViewActionServiceProtocol.class).editDataview(_dataView);
                    vortexFuture.addEventHandler(handleUpdateDataViewResponse);

                } catch (Exception myException) {

                    _dialog.hideWatchBox();
                    Display.error("EditDataViewPresenter", 2, myException);
                    _model.adjustButtons();
                }
            }

        } catch (Exception myException) {

            Display.error("EditDataViewPresenter", 3, myException);
        }
    }

    //
    // Request the list of required fields from the server
    //
    public void retrieveRequiredFieldList(VortexEventHandler<List<String>> handlerIn) {

        String myUuid = ((null != _dataView) && (null != _dataView.getMeta())) ? _dataView.getMeta().getUuid() : null;

        if (null != myUuid) {

            VortexFuture<List<String>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.addEventHandler(handlerIn);
                myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).testFieldReferences(myUuid);

            } catch (Exception myException) {

                Display.error("AbstractDataViewPresenter", 15, myException);
            }
        }
    }

    //
    // Request the list of required fields from the server
    //
    public void retrieveRequiredCoreFieldList(VortexEventHandler<List<String>> handlerIn) {

        String myUuid = ((null != _dataView) && (null != _dataView.getMeta())) ? _dataView.getMeta().getUuid() : null;

        if (null != myUuid) {

            VortexFuture<List<String>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.addEventHandler(handlerIn);
                myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).testCoreFieldReferences(myUuid);

            } catch (Exception myException) {

                Display.error("AbstractDataViewPresenter", 15, myException);
            }
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
