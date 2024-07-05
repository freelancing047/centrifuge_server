package csi.web.filters;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

    private static final String BUNDLE_NAME = "csi.web.filters.messages"; //$NON-NLS-1$

    private ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    Messages() {
    }

    public String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
