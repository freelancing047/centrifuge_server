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
package csi.shared.core.visualization.chart;

import com.google.common.base.Objects;
import com.google.common.base.MoreObjects;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class AxisScale implements Serializable {

    private double sum;
    private double minValue;
    private double maxValue;

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("sum", getSum()) //
                .add("min", getMinValue()) //
                .add("max", getMaxValue()) //
                .toString();
    }

}
