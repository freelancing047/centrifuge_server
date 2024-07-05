package csi.client.gwt.mainapp;

import csi.server.common.dto.user.UserSecurityInfo;

/**
 * Created by centrifuge on 5/22/2019.
 */
public interface CsiLandingPage extends CsiDisplay {

    public void finalizeWidget(UserSecurityInfo userSecurityInfoIn);

    public void reloadData();
}
