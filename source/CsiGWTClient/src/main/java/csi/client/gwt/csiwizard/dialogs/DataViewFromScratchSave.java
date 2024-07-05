package csi.client.gwt.csiwizard.dialogs;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.service.api.DataViewActionServiceProtocol;

import java.util.List;

/**
 * Created by centrifuge on 6/22/2017.
 */
public class DataViewFromScratchSave extends DataViewSave {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private AdHocDataSource _container = null;
    List<LaunchParam> _parameters;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DataViewFromScratchSave(AdHocDataSource containerIn, WizardInterface priorDialogIn,
                                   String titleIn, String helpIn, List<LaunchParam> parameterListIn,
                                   List<DataSourceDef> requiredAuthorizationsIn)
            throws CentrifugeException {

        super(priorDialogIn, titleIn, helpIn, null, requiredAuthorizationsIn);

        _container = containerIn;
        _parameters = parameterListIn;
    }

    @Override
    protected void createPanel() {

    }

    @Override
    protected void executeRequest(String nameIn, String remarksIn, boolean overWriteIn,
                                  SharingInitializationRequest sharingRequestIn) {

        VortexFuture<Response<String, DataView>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            showWatchBox(myVortexFuture, cancelCallback, _constants.dataviewLaunching_WatchBoxInfo(nameIn));
            myVortexFuture.addEventHandler(handleCreateDataViewResponse);
            myVortexFuture.execute(DataViewActionServiceProtocol.class).createDataView("", nameIn, remarksIn,
                                                        _container, _parameters,
                                                        WebMain.injector.getMainPresenter().getAuthorizationList(),
                                                        overWriteIn, sharingRequestIn);

        } catch (Exception myException) {

            hideWatchBox();
            Display.error(_txtFailureDialogTitle, myException.getMessage());
        }
    }
}
