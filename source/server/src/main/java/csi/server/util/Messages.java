package csi.server.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

    private static final String BUNDLE_NAME = "csi.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getFriendlyMessage(Throwable ex) {
        String msg = ex.getMessage();

        while (ex.getCause() != null) {
            ex = ex.getCause();
            if (ex.getMessage() != null && ex.getMessage().trim().length() > 0) {
                msg = ex.getMessage();
            }
        }

        if (msg == null) {
            // e.g., an NPE does not have a message.
            String exceptionName = ex.getClass().getName();
            if (exceptionName == null) {
                exceptionName = "Exception";
            }
            int inx = exceptionName.lastIndexOf(".") + 1;
            msg = String.format("%s thrown on server", exceptionName.substring(inx));
        }

        return msg;
    }

    public static String getFriendlyMessage(String message) {
        if (message.equals(getString("Substring.Exception")))
            return getString("Substring.Friendly");

        return message;
    }
}
