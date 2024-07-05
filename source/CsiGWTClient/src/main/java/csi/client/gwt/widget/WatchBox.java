package csi.client.gwt.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.MaskDialog;
import csi.server.common.service.api.TaskManagerActionServiceProtocol;
import csi.server.common.service.api.UploadServiceProtocol;

/**
 * Created by centrifuge on 12/7/2016.
 */
public class WatchBox implements WatchBoxInterface {

    private static WatchBox _shared = null;

    private final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private final String _title = _constants.dialog_WatchBoxTitle();
    private final String _message = _constants.pleaseWait();

    private VortexFuture _task = null;
    private MaskDialog _watchBoxDialog = null;
    private AsyncCallback<?> _handler = null;
    private boolean _cancelled = false;
    private boolean _showButton = false;

    private WatchBox() {

    }

    public static WatchBox getInstance() {

        if (null == _shared) {

            _shared = new WatchBox();
        }
        return _shared;
    }

    public boolean active() {

        return (null != _watchBoxDialog);
    }

    public void show() {

        show(null, null);
    }

    public void show(String messageIn) {

        show(null, messageIn);
    }

    public void show(String titleIn, String messageIn) {

        String myNewTitle = (null != titleIn) ? titleIn.trim() : null;
        String myTitle = (null != myNewTitle) ? myNewTitle : _title;
        String myNewMessage = (null != messageIn) ? messageIn.trim() : null;
        String myMessage = (null != myNewMessage) ? myNewMessage : _message;

        if (null == _watchBoxDialog) {

            _watchBoxDialog = new MaskDialog(myTitle, myMessage);
            _watchBoxDialog.show();

        } else {

            _watchBoxDialog.setMessage(myTitle, myMessage);
        }
        if (_showButton) {

            _watchBoxDialog.showCancelButton(handleCancel);
        }
    }

    public void hide() {

        hideCancelButton();
        if (null != _watchBoxDialog) {

            _watchBoxDialog.hide();
            _watchBoxDialog = null;
        }
    }

    public void showCancelButton(VortexFuture<?> taskIn, AsyncCallback<Void> handlerIn) {

        _task = taskIn;
        _handler = handlerIn;
        _showButton = true;
        if (null != _watchBoxDialog) {

            _watchBoxDialog.showCancelButton(handleCancel);
        }
    }

    public void hideCancelButton() {

        _task = null;
        _handler = null;
        _showButton = false;
        if (null != _watchBoxDialog) {

            _watchBoxDialog.hideCancelButton();
        }
    }

    private ClickHandler handleCancel = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            _cancelled = true;
            if (null != _task) {

                try {

                    _task.cancel(_handler);
                    _cancelled = false;
                    hide();

                } catch (Exception myException) {

                    Dialog.showException(myException);
                }

            } else if (null != _handler) {

                _handler.onSuccess(null);
            }
        }
    };
}
