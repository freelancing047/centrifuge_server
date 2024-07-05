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
package csi.client.gwt.viz.chart.menu;

import csi.client.gwt.viz.chart.model.ChartModel;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.shared.core.visualization.chart.HighchartResponse;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DeselectAllHandler extends AbstractChartMenuEventHandler {
	private ChartModel chartModel;

    public DeselectAllHandler(ChartPresenter presenter, ChartMenuManager menuManager) {
        super(presenter, menuManager);
        chartModel = presenter.getChartModel();
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        removeSelectionsForRange();
        getPresenter().getView().setViewsNotLoaded();
        getPresenter().getView().render(true);
    }

    private void removeSelectionsForRange() {
    	//TODO: revisit this functionality, could be an interesting feature in the future
    	chartModel.getChartSelectionState().clearSelection();
    }
}
