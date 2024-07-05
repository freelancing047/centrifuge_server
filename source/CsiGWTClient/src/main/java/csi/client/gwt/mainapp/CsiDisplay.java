package csi.client.gwt.mainapp;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Created by centrifuge on 12/21/2018.
 */
public interface CsiDisplay extends IsWidget {


    public void saveState();
    public void restoreState();
    public void forceExit();
}
