package csi.client.gwt.widget.boot;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

public class HelpWindow {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _helpWindowTitle = _constants.helpWindowTitle();
    private static final String _helpFolder = _constants.helpFolder();
    private static final String _helpUrl = GWT.getHostPageBaseURL() + "help/" + _helpFolder + "/";

    public static void display(String relativeUrlIn) {
        
        Window.open(_helpUrl + relativeUrlIn, _helpWindowTitle, null);
    }

}
