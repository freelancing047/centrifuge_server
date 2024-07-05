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

import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.chart.settings.ChartSettingsPresenter;
import csi.client.gwt.viz.chart.view.ChartMetricsView;
import csi.client.gwt.viz.matrix.ExpireMetrics;
import csi.client.gwt.viz.matrix.UpdateEvent;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.server.common.model.visualization.chart.DrillChartViewDef;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartSettingsHandler extends AbstractChartMenuEventHandler implements
        SettingsActionCallback<DrillChartViewDef> {

    public ChartSettingsHandler(ChartPresenter presenter, ChartMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        ChartSettingsPresenter presenter = new ChartSettingsPresenter(this);
        presenter.setDataView(getPresenter().getDataView());
        presenter.setVisualizationDef(getPresenter().getVisualizationDef());
        presenter.setVisualization(getPresenter());
        presenter.show();


    }

    @Override
    public void onSaveComplete(DrillChartViewDef visualizationDef, boolean suppressLoadAfterSave) {
        getPresenter().getVisualizationDef().getSelection().clearSelection();
//        getPresenter().clearSelectedView();
        getPresenter().f1();
        getPresenter().getView().setViewsNotLoaded();
        ChartMetricsView.EVENT_BUS.fireEvent(new ExpireMetrics(getPresenter().getUuid()));
    }

    @Override
    public void onCancel() {
        // Noop
    }
}
