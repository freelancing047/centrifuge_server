package csi.client.gwt.mainapp;

import csi.client.gwt.WebMain;

public class ApplicationToolbarLocator {

    private static Boolean newToolBar = WebMain.getClientStartupInfo().getFeatureConfigGWT().isUseNewLogoutPage();

    public static AbstractApplicationToolbar getInstance() {
        if(newToolBar) {
            return ApplicationToolbarNew.getInstance();
        } else {
            return ApplicationToolbar.getInstance();
        }
    }
}
