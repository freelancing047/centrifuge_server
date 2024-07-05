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
package csi.client.gwt.viz.chart.view;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChartGridRow {

    private String dimension;
    private List<Number> metricValues = new ArrayList<Number>();

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public List<Number> getMetricValues() {
        return metricValues;
    }

}
