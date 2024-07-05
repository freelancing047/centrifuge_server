/*
 * Copyright Centrifuge Systems, Inc. 2012
 * 
 * @author Centrifuge Systems, Inc.
 */
package sample;

import java.util.UUID;

import sample.model.TaskStatus;

/**
 * The Class RestSampleUtils.
 */
public class RestSampleUtils
{
    
    /**
     * Gets the base url.
     *
     * @param hostname the hostname
     * @param port the port
     * @return the base url
     */
    public static String getBaseUrl(String hostname, int port) {
        return "http://" + hostname + ":" + port + "/Centrifuge/api-v1";
    }

    /**
     * Handle response.
     *
     * @param status the status
     */
    public static void handleResponse(TaskStatus status) {
        if (status != null && status.resultData != null
                && "CENTRIFUGE_SUCCESS".equals(status.resultData.operationStatus)) {
            System.out.println("Action succeeded.");
        } else {
            System.out.println("An error occurred.");
            if (status != null && status.feedback != null) {
                System.out.println(status.feedback);
            }
            if (status != null && status.errorMessage != null) {
            	System.out.println(status.errorMessage);
            }
        }
    }

    /**
     * Creates the uuid.
     *
     * @return the string
     */
    public static String createUuid() {
        return UUID.randomUUID().toString();
    }
}