package csi.client.gwt.error;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.StatusCodeException;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ErrorDialog;

public class ModalErrorHandler implements UncaughtExceptionHandler {
   private static final Logger LOG = Logger.getLogger("Unhandled Client Exception Logger");

    @Override
    public void onUncaughtException(Throwable e) {
        if (e instanceof JavaScriptException) {
           LOG.fine("Client Error" + e.getMessage());
            return;
        } else if (e instanceof UmbrellaException) {
            // ignore exceptions caused by invalid pixel in
            // @com.emitrom.lienzo.client.core.NativeContext2D::getImageDataPixelColor
            // only happens in Dev Mode in some browsers
            if (!GWT.isProdMode() && GWT.isClient()) {
                if (e.getMessage().contains("getImageDataPixelColor")) {
                   LOG.fine("Client Error" + e.getMessage());
                    return;
                }
            }
        }

        final CentrifugeConstants centrifugeConstants = CentrifugeConstantsLocator.get();
        final Dialog dialog;
        Throwable ex= e;
        while(ex.getCause()!=null) {
            ex = ex.getCause();
        }
        final int errorHashCode = e.hashCode();
        if(e instanceof StatusCodeException && (((StatusCodeException) e).getStatusCode()) == 0){
            dialog = new ErrorDialog(centrifugeConstants.lostConnectionErrorDialogTitle(),
                    centrifugeConstants.lostConnectionErrorDialogGeneralMessage());
            LOG.warning("Client Error (" + errorHashCode + ")" + ex.getMessage());
        } else if (e instanceof IncompatibleRemoteServiceException) {
        	dialog = new ErrorDialog(centrifugeConstants.incompatibleRemoteServiceException_Title(),
        			centrifugeConstants.incompatibleRemoteServiceException_Message());
        } else if (e.getMessage().contains("\n")) {
            dialog = new ErrorDialog(centrifugeConstants.uncaughtErrorDialog_Title(),
                    centrifugeConstants.uncaughtErrorDialog_GeneralMessage(errorHashCode));
            LOG.warning("Client Error (" + errorHashCode + ")" + ex.getMessage());
        } else {
            dialog = new ErrorDialog(centrifugeConstants.uncaughtErrorDialog_Title(), e.getMessage());

            LOG.warning("Client Error" + ex.getMessage());
        }
        
        dialog.setMaxHeigth("400px");
        dialog.show();
    }
}
