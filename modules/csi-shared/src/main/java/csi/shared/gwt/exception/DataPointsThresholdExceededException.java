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

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class DataPointsThresholdExceededException extends CsiClientException {

    private int threshold;
    private long actual;

    public DataPointsThresholdExceededException() {
        super();
    }

    public DataPointsThresholdExceededException(int threshold, long actual) {
        super();
        this.threshold = threshold;
        this.actual = actual;
    }

    public int getThreshold() {
        return threshold;
    }

    public long getActual() {
        return actual;
    }

}
