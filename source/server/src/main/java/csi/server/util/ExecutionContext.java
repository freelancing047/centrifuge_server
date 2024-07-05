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
package csi.server.util;

import com.google.common.base.Throwables;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ExecutionContext {

    /**
     * @param milliseconds Milliseconds to sleep for
     */
    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }
}
