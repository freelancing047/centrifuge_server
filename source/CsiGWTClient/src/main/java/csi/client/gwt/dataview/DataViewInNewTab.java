package csi.client.gwt.dataview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.ErrorDialog;

/**
 * @author Centrifuge Systems, Inc.
 */
public class DataViewInNewTab {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public static String open(String uuidIn, String nameIn, String userIn) {

        String myTitle = genTitle(uuidIn, nameIn, userIn);

        if (null != myTitle) {

            String myFullUrl = buildDataViewUrl(uuidIn);
            String userAgent = Window.Navigator.getUserAgent();

            if (userAgent.contains("rv:11.0")) { //$NON-NLS-1$

                Window.open(myFullUrl, "_blank", "resizable,scrollbars,status"); //$NON-NLS-1$ //$NON-NLS-2$

            } else {

                Window.open(myFullUrl, "_blank", null); //$NON-NLS-1$
            }

        } else {

            new ErrorDialog(i18n.dataViewInNewTabErrorTitle(), i18n.dataViewInNewTabErrorMessage()).show(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return myTitle;
    }

    public static String open(String uuidIn, String nameIn) {

        return open(uuidIn, nameIn, null);
    }

    public static String genTitle(String uuidIn, String nameIn, String userIn) {

        String myUser = (null != userIn) ? userIn : WebMain.injector.getMainPresenter().getUserInfo().getName();

        if ((null != uuidIn) && (0 < uuidIn.length()) && (null != nameIn)
                && (0 < nameIn.length()) && (null != myUser) && (0 < myUser.length())) {

            return nameIn;
        }
        return (null != nameIn) ? nameIn : "";
    }

    public static String genTitle(String uuidIn, String nameIn) {

        return genTitle(uuidIn, nameIn, null);
    }

    private static String buildDataViewUrl(String uuid) {
        String baseUrl = GWT.getHostPageBaseURL();
        StringBuilder stringBuilder = new StringBuilder(baseUrl);
        stringBuilder.append("?dvuuid=").append(uuid); //$NON-NLS-1$
        return stringBuilder.toString();
    }
}
