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
package csi.client.gwt.util;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface GenericCallback<R, P> {

    public static final GenericCallback<Void,Void> NO_OP_CALLBACK = new GenericCallback<Void, Void>(){

        @Override
        public Void onCallback(Void parameter) {
            //no op
            return null;
        }
        
    };
    
    R onCallback(P parameter);
}
