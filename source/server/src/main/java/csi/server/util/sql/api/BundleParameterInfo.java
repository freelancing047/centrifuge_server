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
package csi.server.util.sql.api;

import java.io.Serializable;

import csi.server.common.enumerations.CsiDataType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class BundleParameterInfo implements Serializable {

	public enum BundleParameterName{
		LENGTH, TRIM, REGEX, REGEX_REPLACE, FLAGS, DELIMITER, INDEX, REPLACEMENT, DIVISOR, START, DECIMAL_PLACES;
	}
	
    private BundleParameterName name;
    // Type of the parameter
    private CsiDataType dataType;
    // Size of the control for the value field
    private int valueSpan;

    public BundleParameterInfo() {
    }

    public BundleParameterInfo(BundleParameterName name, CsiDataType dataType, int valueSpan) {
        this.name = name;
        this.dataType = dataType;
        this.valueSpan = valueSpan;
    }

    public BundleParameterName getName() {
        return name;
    }

    public CsiDataType getDataType() {
        return dataType;
    }


    public int getValueSpan() {
        return valueSpan;
    }

    @Override
    public String toString() {
        return "BundleParameterInfo [name=" + name + ", dataType=" + dataType + ", valueSpan=" + valueSpan + "]";
    }

}
