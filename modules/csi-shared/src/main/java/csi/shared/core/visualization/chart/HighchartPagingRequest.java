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


/**
 * Chart request.
 * @author Centrifuge Systems, Inc.
 */
public class HighchartPagingRequest extends HighchartRequest {
    
    private int start;
    private int limit;
    private int vizWidth;
    private boolean overviewNeeded = true;
    
    public int getLimit() {
        return limit;
    }
    public void setLimit(int limit) {
        this.limit = limit;
    }
    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }
    public int getVizWidth() {
        return vizWidth;
    }
    public void setVizWidth(int vizWidth) {
        this.vizWidth = vizWidth;
    }
    public boolean isOverviewNeeded() {
        return overviewNeeded;
    }
    public void setOverviewNeeded(boolean overviewNeeded) {
        this.overviewNeeded = overviewNeeded;
    }


}
