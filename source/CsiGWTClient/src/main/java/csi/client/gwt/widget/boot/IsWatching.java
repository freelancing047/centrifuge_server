package csi.client.gwt.widget.boot;

import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.WatchBoxInterface;

/**
 * Created by centrifuge on 9/14/2016.
 */
public interface IsWatching {

    public WatchBoxInterface getWatchBox();
    public void showWatchBox();
    public void showWatchBox(String messageIn);
    public void showWatchBox(String titleIn, String messageIn);
    public void hideWatchBox();
    public boolean watchBoxShowing();
}
