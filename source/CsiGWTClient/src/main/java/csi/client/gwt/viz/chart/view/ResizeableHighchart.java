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

import org.moxieapps.gwt.highcharts.client.*;
import org.moxieapps.gwt.highcharts.client.Animation.Easing;
import org.moxieapps.gwt.highcharts.client.ChartTitle.VerticalAlign;

import com.google.gwt.user.client.ui.RequiresResize;

import csi.client.gwt.viz.chart.model.CsiBeforeChartLoadHandler;
import csi.client.gwt.viz.chart.model.CsiChartLoadHandler;

/**
 * Allows the chart to respond to size changes.
 * @author Centrifuge Systems, Inc.
 *
 */
public class ResizeableHighchart extends Chart implements RequiresResize {

    public static final int ANIMATION_DURATION = 0;
    private CsiBeforeChartLoadHandler beforeChartLoadHandler = null;
    private CsiChartLoadHandler loadHandler = null;

    public ResizeableHighchart() {
        setAnimation(new Animation().setDuration(ANIMATION_DURATION).setEasing(Easing.SWING));
        setCredits(new Credits().setEnabled(false));
        setExporting(new Exporting().setEnabled(false));
    }
    
    @Override
    protected void onLoad() {
        if(beforeChartLoadHandler != null){
            beforeChartLoadHandler.execute();
        }
        
        super.onLoad();
        
        if(loadHandler != null){
            loadHandler.execute();
        }
        
    }

    @Override
    public void onResize() {
//        if (isAttached()) {
//            setSizeToMatchContainer();
//        }
    }

    public void setTitle(String titleText, String description, ChartTitle.Align titleAlignment) {
        ChartTitle title = new ChartTitle();

        Style style = new Style();
        style.setFont("12px tahoma,arial,helvetica,sans-serif");
        title.setStyle(style);
        title.setText(titleText);
        title.setVerticalAlign(VerticalAlign.BOTTOM);
        title.setAlign(titleAlignment);
        ChartSubtitle subtitle = new ChartSubtitle();
        subtitle.setText(description);
        setTitle(title, subtitle);
    }
    
    public void addBeforeLoadHandler(CsiBeforeChartLoadHandler handler){
        this.beforeChartLoadHandler = handler;
    }
    
    public void addLoadHandler(CsiChartLoadHandler handler){
        this.loadHandler = handler;
    }

}
