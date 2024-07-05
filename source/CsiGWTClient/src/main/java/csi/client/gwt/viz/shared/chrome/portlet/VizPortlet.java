package csi.client.gwt.viz.shared.chrome.portlet;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.widget.core.client.Portlet;

import csi.client.gwt.viz.shared.chrome.VizChrome;

public interface VizPortlet extends VizChrome {

    
    public interface View extends IsWidget {

    }

    interface Model {
    }

    interface Presenter extends Activity{
    }

    Portlet getAsPortlet();
}
