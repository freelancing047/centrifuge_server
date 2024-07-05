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
package csi.client.gwt.viz.chart.presenter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.viz.chart.model.ChartModel;
import csi.client.gwt.viz.chart.view.ChartMetricsView;
import csi.client.gwt.viz.matrix.ExpireMetrics;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class BreadcrumLinkClickHandler implements ClickHandler {

    private final DrillSelectionCallback drillSelectionCallback;
    private final ChartModel chartModel;
    private final int anchorIndex; // Zero based.

    public BreadcrumLinkClickHandler(DrillSelectionCallback drillSelectionCallback, ChartModel chartModel, int anchorIndex) {
        this.drillSelectionCallback = drillSelectionCallback;
        this.chartModel = chartModel;
        this.anchorIndex = anchorIndex;
    }

    @Override
    public void onClick(ClickEvent event) {
        // NOTE: subList() returns an object of type ArrayList$SubList which is GWT doesn't like (not marked
        // Serializable). Therefore, we construct a new array capture the sublist.
        List<String> selected = new ArrayList<String>(chartModel.getDrillSelections().subList(0, anchorIndex));
        drillSelectionCallback.drillCategorySelected(selected);
    }
}
