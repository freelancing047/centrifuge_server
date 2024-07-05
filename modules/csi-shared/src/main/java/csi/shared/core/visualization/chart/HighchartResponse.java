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

import com.google.common.html.HtmlEscapers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class HighchartResponse implements Serializable {

    /**
     * The outer list is the size of the number of measures.
     * For each measure we have a list of data the size of the category names.
     * So to get the 5th category of the 2nd measure, it's: seriesData.get(1).get(4);
     */
    private List<HighchartSeriesData> seriesData = new ArrayList<HighchartSeriesData>();

    private List<Number> totals = new ArrayList<Number>();

    /**
     * The names of the categories for this chart.
     */
    private List<String> categoryNames = new ArrayList<String>();

    /**
     * Information about each measure.
     */
    private List<SeriesInfo> seriesInfos = new ArrayList<SeriesInfo>();

    /**
     * Statistics and defaults about each measure.
     */
    private List<AxisScale> axisScales = new ArrayList<AxisScale>();

    /**
     * True if the row limit set in the config is reached
     */
    private boolean isTableLimitExceeded = false;

    private boolean isChartLimitExceeded = false;

    /**
     * Number of rows in the table for the chart.
     */
    private int rowCount;

    public List<HighchartSeriesData> getSeriesData() {
        return seriesData;
    }

    public void setSeriesData(List<HighchartSeriesData> seriesData) {
        this.seriesData = seriesData;
    }

    public List<Number> getTotals() {
        return totals;
    }

    public void setTotals(List<Number> totalsIn) {
        this.totals = totalsIn;
    }

    public List<String> getCategoryNames() {
        return categoryNames;
    }

    public void setCategoryNames(List<String> categoryNames) {
        this.categoryNames.clear();
        this.categoryNames.addAll(categoryNames);

//        this.categoryNames = categoryNames.stream().map(s -> s.replaceAll("<","&lt;").replaceAll(">","&gt;")).collect(Collectors.toList());
//        this.categoryNames = categoryNames.stream().map(s -> HtmlEscapers.htmlEscaper().escape(s)).collect(Collectors.toList());
//        this.categoryNames = categoryNames;
    }

    public int getCategorySize() {
        return getCategoryNames().size();
    }

    public List<SeriesInfo> getSeriesInfos() {
        return seriesInfos;
    }

    public void setSeriesInfos(List<SeriesInfo> seriesInfos) {
        this.seriesInfos = seriesInfos;
    }

    public List<AxisScale> getAxisScales() {
        return axisScales;
    }

    public void setAxisScales(List<AxisScale> axisScales) {
        this.axisScales = axisScales;
    }

    public boolean isTableLimitExceeded() {
        return isTableLimitExceeded;
    }

    public void setTableLimitExceeded(boolean tableLimitExceeded) {
        isTableLimitExceeded = tableLimitExceeded;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public boolean isChartLimitExceeded() {
        return isChartLimitExceeded;
    }

    public void setChartLimitExceeded(boolean chartLimitExceeded) {
        isChartLimitExceeded = chartLimitExceeded;
    }

    public HighchartPagingResponse copy() {
        HighchartPagingResponse response = new HighchartPagingResponse();
        response.setAxisScales(axisScales);
//        response.getCategoryNames().clear();//just cause
        response.getCategoryNames().addAll(categoryNames);
//        response.setCategoryNames(categoryNames);
        response.setTotals(totals);
        response.setChartLimitExceeded(isChartLimitExceeded());
        response.setRowCount(rowCount);
        response.setSeriesData(seriesData);
        response.setSeriesInfos(seriesInfos);
        response.setTableLimitExceeded(isTableLimitExceeded());

        return response;
    }

}
