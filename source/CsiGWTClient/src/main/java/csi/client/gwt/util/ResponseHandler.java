package csi.client.gwt.util;

import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.i18n.ServerMessageStrings;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.Response;
import csi.server.common.dto.ResponseArgument;
import csi.server.common.enumerations.ServerMessage;
import csi.server.common.util.StringUtil;

/**
 * Created by centrifuge on 5/5/2015.
 */
public class ResponseHandler {

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    public static boolean isSuccess(Response<?, ?> responseIn) {

        return isSuccess(responseIn, null, null);
    }

    public static boolean isSuccess(Response<?, ?> responseIn, ClickHandler clickHandlerIn) {

        return isSuccess(responseIn, null, clickHandlerIn);
    }

    public static boolean isSuccess(Response<?, ?> responseIn, String titleIn) {

        return isSuccess(responseIn, titleIn, null);
    }

    public static boolean isSuccess(Response<?, ?> responseIn, String titleIn, ClickHandler clickHandlerIn) {

        if (null == responseIn) {

            Display.error(_constants.unexpectedNullFromServer());
            return false;
        }
        if (!responseIn.isSuccess()) {

            StringBuilder myBuffer = new StringBuilder();
            ServerMessage myMessage = responseIn.getMessage();
            String myMessageString =  ServerMessageStrings.exists(myMessage) ? ServerMessageStrings.get(myMessage) : null;
            String myException = responseIn.getException();
            ResponseArgument myArgument = responseIn.getArgument();
            boolean mySpecialFlag = false;

            if (null != myArgument) {

                myMessageString = StringUtil.replaceArguments(myMessageString, new String[]{myArgument.getMessage()});
            }

            if ((null != myMessageString) && (0 < myMessageString.length())) {

                myBuffer.append(myMessageString);

                if ((null != myException) && (0 < myException.length())) {

                    myBuffer.append('\n');
                    myBuffer.append(myException);
                    mySpecialFlag = true;
                }

            } else if ((null != myException) && (0 < myException.length())) {

                myBuffer.append(myException);

            } else {

                myBuffer.append(_constants.serverMessage_UnknownError());
            }
            if (null != titleIn) {

                Display.error(titleIn, myBuffer.toString(), mySpecialFlag, clickHandlerIn);
                
            } else {

                Display.error(myBuffer.toString(), mySpecialFlag, clickHandlerIn);
            }
        }

        return responseIn.isSuccess();
    }

    public static void displayError(Response<?, ?> responseIn, String titleIn) {

        isSuccess(responseIn, titleIn);
    }

    public static void displayError(Response<?, ?> responseIn) {

        isSuccess(responseIn);
    }
}

