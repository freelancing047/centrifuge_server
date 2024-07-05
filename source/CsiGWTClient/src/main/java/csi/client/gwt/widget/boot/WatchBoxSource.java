package csi.client.gwt.widget.boot;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.WatchBoxInterface;

/**
 * Created by centrifuge on 6/23/2017.
 */
public class WatchBoxSource implements IsWatching {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private IsWatching watchingparent = null;
    private WatchBoxInterface watchBox = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public WatchBoxSource(IsWatching parentIn) {

        watchingparent = parentIn;
    }

    public WatchBoxInterface getWatchBox() {

        return (null != watchingparent) ? watchingparent.getWatchBox() : getLocalWatchBox();
    }

    public void showWatchBox() {

        getWatchBox().show();
    }

    public void showWatchBox(String messageIn) {

        getWatchBox().show(messageIn);
    }

    public void showWatchBox(String titleIn, String messageIn) {

        getWatchBox().show(titleIn, messageIn);
    }

    public void showWatchBox(VortexFuture<?> taskIn, AsyncCallback<Void> handlerIn) {

        WatchBoxInterface myWatchBox = getWatchBox();
        AsyncCallback<Void> myHandler = (null != handlerIn) ? handlerIn : callback;

        myWatchBox.showCancelButton(taskIn, myHandler);
        myWatchBox.show();
    }

    public void showWatchBox(VortexFuture<?> taskIn, AsyncCallback<Void> handlerIn, String messageIn) {

        WatchBoxInterface myWatchBox = getWatchBox();
        AsyncCallback<Void> myHandler = (null != handlerIn) ? handlerIn : callback;

        myWatchBox.showCancelButton(taskIn, myHandler);
        myWatchBox.show(messageIn);
    }

    public void showWatchBox(VortexFuture<?> taskIn, AsyncCallback<Void> handlerIn, String titleIn, String messageIn) {

        WatchBoxInterface myWatchBox = getWatchBox();
        AsyncCallback<Void> myHandler = (null != handlerIn) ? handlerIn : callback;

        myWatchBox.showCancelButton(taskIn, myHandler);
        myWatchBox.show(titleIn, messageIn);
    }

    public void hideWatchBox() {

        getWatchBox().hide();
    }

    public void updateWatchBox(String messageIn) {

        getWatchBox().show(messageIn);
    }

    public boolean watchBoxShowing() {

        return getWatchBox().active();
    }

    public void setParent(IsWatching parentIn) {

        watchingparent = parentIn;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Protected Methods                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected IsWatching getParent() {

        return watchingparent;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Private Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private WatchBoxInterface getLocalWatchBox() {

        if (null == watchBox) {

            watchBox = WatchBox.getInstance();
        }
        return watchBox;
    }

    private AsyncCallback<Void> callback = new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {

        }

        @Override
        public void onSuccess(Void result) {

        }
    };
}
