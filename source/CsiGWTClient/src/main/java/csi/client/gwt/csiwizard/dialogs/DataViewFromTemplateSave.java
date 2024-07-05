package csi.client.gwt.csiwizard.dialogs;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.LaunchRequest;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.service.api.DataViewActionServiceProtocol;

import java.util.List;

/**
 * Created by centrifuge on 6/14/2017.
 */
public class DataViewFromTemplateSave extends DataViewSave {

    List<LaunchParam> _parameters = null;
    String _templateUuid = null;
    String _templateName = null;
    boolean _migrateACL = false;

    public DataViewFromTemplateSave(WizardInterface priorDialogIn, ResourceBasics templateIn,
                                    List<LaunchParam> parametersIn, List<DataSourceDef> requiredAuthorizationsIn) {

        super(priorDialogIn, _constants.dataviewFromTemplateWizard_DialogTitle(),
                _constants.dataviewFromTemplateWizard_HelpTarget(), templateIn.getName(), requiredAuthorizationsIn);

        try{

            hideWatchBox();
            _templateUuid = templateIn.getUuid();
            _templateName = templateIn.getName();
            _parameters = parametersIn;

        } catch (Exception myException) {

            Display.error("DataViewFromTemplateSave", 1, myException);
        }
    }

    @Override
    protected void createPanel() {

    }

    protected void executeRequest(String nameIn, String remarksIn,
                                  boolean forceOverwriteIn, SharingInitializationRequest sharingRequestIn) {

        try{

            List<AuthDO> myAuthorizationList = WebMain.injector.getMainPresenter().getAuthorizationList();
            VortexFuture<Response<String, DataView>> myVortexFuture = WebMain.injector.getVortex().createFuture();
            showWatchBox(myVortexFuture, cancelCallback, _constants.dataviewLaunching_WatchBoxInfo(nameIn));
            LaunchRequest myRequest = new LaunchRequest(_templateUuid, _templateName, nameIn, remarksIn,
                    _parameters, myAuthorizationList, forceOverwriteIn, _migrateACL);

            myVortexFuture.addEventHandler(handleCreateDataViewResponse);
            myVortexFuture.execute(DataViewActionServiceProtocol.class).launchTemplate(myRequest, sharingRequestIn);


        } catch (Exception myException) {

            hideWatchBox();
            Display.error("DataViewFromTemplateSave", 2, myException);
        }
    }
}
