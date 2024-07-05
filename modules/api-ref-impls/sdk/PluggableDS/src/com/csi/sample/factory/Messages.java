/*
 * Copyright Centrifuge Systems, Inc. 2012
 * 
 * @author Centrifuge Systems, Inc.
 */
package com.csi.sample.factory;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The Messages class is use to facilitate externalizing text messages for internalization purposes.
 * 
 * The messages.properties file located in directory src/com/sci/sample/factory contains the text 
 * messages used in this sample implementation.
 */
public class Messages {

    /** The Constant BUNDLE_NAME. */
    private static final String BUNDLE_NAME = "com.csi.sample.factory.messages"; 

    /** The Constant RESOURCE_BUNDLE. */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Instantiates a new messages.
     */
    private Messages() {
    }

    /**
     * Gets the string.
     *
     * @param key the key
     * @return the string
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
