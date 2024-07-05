/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.shared.gwt.exception;

import com.google.gwt.user.client.rpc.SerializationException;

/**
 * Base type for all exceptions that are sent to clients.
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class CsiClientException extends SerializationException {

    public CsiClientException() {
        super();
    }

    public CsiClientException(String msg) {
        super(msg);
    }

}
