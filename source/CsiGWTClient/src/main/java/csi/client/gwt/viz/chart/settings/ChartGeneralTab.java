/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.viz.chart.settings;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DisplayFirst;
import csi.server.common.model.visualization.chart.DrillChartViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChartGeneralTab extends ChartSettingsComposite {

    @UiField
    TextBox chartName;

    @UiField
    RadioButton chartDisplay;
    @UiField
    RadioButton tableDisplay;

    private ChartSettingsPresenter presenter;

    interface SpecificUiBinder extends UiBinder<Widget, ChartGeneralTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public ChartGeneralTab() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
        chartDisplay.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                DrillChartViewDef drillChartViewDef = getVisualizationSettings().getVisualizationDefinition();
                ChartSettings settings = drillChartViewDef.getChartSettings();
                settings.setChartDisplay(true);
                settings.setCurrentView(DisplayFirst.CHART);
            }
        });

        tableDisplay.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                DrillChartViewDef drillChartViewDef = getVisualizationSettings().getVisualizationDefinition();
                ChartSettings settings = drillChartViewDef.getChartSettings();
                settings.setChartDisplay(false);
                settings.setCurrentView(DisplayFirst.TABLE);
            }
        });
    }

    @Override
    public void updateViewFromModel() {
        DrillChartViewDef chartViewDef = getVisualizationSettings().getVisualizationDefinition();
        chartName.setValue(chartViewDef.getName());

        if (chartViewDef.getChartSettings().getChartDisplay()) {
            chartDisplay.setValue(true, true);
            tableDisplay.setValue(false, true);
        } else {
            tableDisplay.setValue(true, true);
            chartDisplay.setValue(false, true);
        }

    }

    @Override
    public void updateModelWithView() {
        DrillChartViewDef chartViewDef = getVisualizationSettings().getVisualizationDefinition();

        getVisualizationSettings().getVisualizationDefinition().setName(chartName.getValue().trim());

        if(chartDisplay.getValue()) {
            chartViewDef.getChartSettings().setChartDisplay(true);
        } else {
            chartViewDef.getChartSettings().setChartDisplay(false);
        }
    }

}
