package csi.client.gwt.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import csi.client.gwt.vortex.VortexFuture;

/**
 * Created by centrifuge on 6/12/2017.
 */
public interface WatchBoxInterface {

    public boolean active();
    public void show();
    public void show(String messageIn);
    public void show(String titleIn, String messageIn);
    public void hide();
    public void showCancelButton(VortexFuture<?> taskIn, AsyncCallback<Void> handlerIn);
    public void hideCancelButton();
}
