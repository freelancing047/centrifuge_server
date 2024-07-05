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
package csi.client.gwt.viz.chart.model;

//import csi.server.common.model.visualization.selection.ChartSelectionState;
import java.util.ArrayList;
import java.util.List;

import csi.client.gwt.viz.chart.overview.OverviewPresenter;
import csi.client.gwt.viz.chart.view.ChartMetricsView;
import csi.client.gwt.viz.matrix.ExpireMetrics;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.shared.core.visualization.chart.ChartType;
import csi.shared.core.visualization.chart.HighchartPagingResponse;
import csi.shared.core.visualization.chart.HighchartResponse;
import csi.shared.core.visualization.chart.HighchartSeriesData;

/**
 * Holds data related to the chart being displayed.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChartModel {
    private HighchartPagingResponse highchartResponse = new HighchartPagingResponse();
    private List<String> drillSelections = new ArrayList<String>();
    private ChartSelectionState chartSelectionState = new ChartSelectionState();
    private InitialChartData initialChartData;

    boolean showBreadcumb = true;

//    public List<String> getAxisXCategories() {
//        return highchartResponse.getCategoryNames();
//    }

    public List<String> getDrillSelections() {
        return drillSelections;
    }

    public void setDrillSelections(List<String> drillSelections) {
        this.drillSelections = drillSelections;
    }

    public ChartSelectionState getChartSelectionState() {
        return chartSelectionState;
    }

    public void setChartSelectionState(ChartSelectionState chartSelectionState) {

        this.chartSelectionState = chartSelectionState;
    }

    public HighchartPagingResponse getHighchartResponse() {
        return highchartResponse;
    }

    public ChartType getChartType() {
        return initialChartData.getChartType();
    }

    public void setInitialChartData(InitialChartData initialChartData) {
        this.initialChartData = initialChartData;
    }

    public InitialChartData getInitialChartData() {
        return initialChartData;
    }

    public void updateWith(HighchartPagingResponse highchartResponse, List<String> drillSelections, String uuid) {
        this.highchartResponse = highchartResponse;

        boolean updateMetrics = false;
        if(drillSelections.size() != this.drillSelections.size()){
           updateMetrics = true;
        }

        this.drillSelections = drillSelections;

        if(updateMetrics){
            ChartMetricsView.EVENT_BUS.fireEvent(new ExpireMetrics(uuid));
        }

    }

    public boolean toggleBreadcumb() {
        showBreadcumb = !showBreadcumb;
        return showBreadcumb;
    }

    public boolean isShowBreadcumb() {
        return showBreadcumb;
    }

    public void setShowBreadcumb(boolean showBreadcumb) {
        this.showBreadcumb = showBreadcumb;
    }

}
