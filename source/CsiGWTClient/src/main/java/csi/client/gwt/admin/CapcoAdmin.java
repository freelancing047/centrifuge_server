package csi.client.gwt.admin;

import java.util.List;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.GroupDisplay;
import csi.server.common.dto.Response;
import csi.server.common.enumerations.GroupType;
import csi.server.common.service.api.UserAdministrationServiceProtocol;

/**
 * Created by centrifuge on 5/14/2015.
 */
public class CapcoAdmin {

    public CapcoAdmin(CapcoTab tabIn, SharedItems sharedIn) {

    }

    void requestData(String searchStringIn) {

//       requestData(searchStringIn, _filterSelection);
    }

    private void requestData(String searchStringIn, String filterSelectionIn) {

//        _requestSent = true;

        try {
            VortexFuture<Response<GroupType, List<GroupDisplay>>> vortexFuture = WebMain.injector.getVortex().createFuture();

            if ((null != filterSelectionIn) && (0 < filterSelectionIn.length())) {

                vortexFuture.execute(UserAdministrationServiceProtocol.class).searchGroupGroups(GroupType.SECURITY, filterSelectionIn, searchStringIn);
            }
            else {

                vortexFuture.execute(UserAdministrationServiceProtocol.class).searchGroups(GroupType.SECURITY, searchStringIn);
            }

//            vortexFuture.addEventHandler(handleGroupInfoResponse);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }
}
