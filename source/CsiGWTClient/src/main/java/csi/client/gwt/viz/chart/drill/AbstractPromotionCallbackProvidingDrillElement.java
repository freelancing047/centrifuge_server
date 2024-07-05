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
package csi.client.gwt.viz.chart.drill;

import com.google.gwt.user.client.ui.ResizeComposite;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractPromotionCallbackProvidingDrillElement extends ResizeComposite {

    private ChartPromotionCallback chartPromotionCallback;

    public ChartPromotionCallback getChartPromotionCallback() {
        return chartPromotionCallback;
    }

    public void setChartPromotionCallback(ChartPromotionCallback chartPromotionCallback) {
        this.chartPromotionCallback = chartPromotionCallback;
    }

}
