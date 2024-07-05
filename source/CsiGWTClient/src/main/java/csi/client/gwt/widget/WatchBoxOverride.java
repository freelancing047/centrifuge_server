package csi.client.gwt.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import csi.client.gwt.vortex.VortexFuture;

/**
 * Created by centrifuge on 6/12/2017.
 *
 * This class allows default title and message strings to be maintained while
 * using the same WatchBox instance as competing WatchBox display scenarios.
 */
public class WatchBoxOverride implements WatchBoxInterface {

    private static WatchBox _parent = null;

    private String _title = null;
    private String _message = null;

    public WatchBoxOverride() {

        _parent = WatchBox.getInstance();
    }

    public WatchBoxOverride(String titleIn, String messageIn) {

        _parent = WatchBox.getInstance();
        _title = titleIn;
        _message = messageIn;
    }

    public void setTitle(String titleIn) {

        _title = titleIn;
    }

    public void setMessage(String messageIn) {

        _message = messageIn;
    }

    public boolean active() {

        return _parent.active();
    }

    public void show() {

        show(null, null);
    }

    public void show(String messageIn) {

        show(null, messageIn);
    }

    public void show(String titleIn, String messageIn) {

        String myTitle = (null != titleIn) ? titleIn : _title;
        String myMessage = (null != messageIn) ? messageIn : _message;

        _parent.show(myTitle, myMessage);
    }

    public void hide() {

       _parent.hide();
    }

    public void showCancelButton(VortexFuture<?> taskIn, AsyncCallback<Void> handlerIn) {

        _parent.showCancelButton(taskIn, handlerIn);
    }

    public void hideCancelButton() {

        _parent.hideCancelButton();
    }
}
