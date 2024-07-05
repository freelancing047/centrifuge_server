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
package csi.client.gwt.viz.chart.settings;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChartAdvancedTab extends ChartSettingsComposite {

    @UiField
    CheckBox enablePiechartLabel;
    @UiField
    CheckBox pieLabelShowValue;
    @UiField
    CheckBox pieLabelShowPercentage;
    @UiField
    TextBox piechartLabelThreshold;
    @UiField
    CheckBox enablePiechartLegend;
    @UiField
    CheckBox pieLegendShowValue;
    @UiField
    CheckBox pieLegendShowPercentage;

    interface SpecificUiBinder extends UiBinder<Widget, ChartAdvancedTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public ChartAdvancedTab() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void updateViewFromModel() {
    	DrillChartViewDef visualizationDef = (DrillChartViewDef) getVisualizationSettings().getVisualizationDefinition();
    	ChartSettings chartSettings = visualizationDef.getChartSettings();
    	enablePiechartLabel.setValue(chartSettings.isPieLabelEnabled());
    	pieLabelShowValue.setValue(chartSettings.isPieLabelShowValue());
    	pieLabelShowPercentage.setValue(chartSettings.isPieLabelShowPercentage());
    	piechartLabelThreshold.setValue(chartSettings.getPieLabelPercentageThreshold() + "");
    	enablePiechartLegend.setValue(chartSettings.isPieLegendEnabled());
    	pieLegendShowValue.setValue(chartSettings.isPieLegendShowValue());
    	pieLegendShowPercentage.setValue(chartSettings.isPieLegendShowPercentage());
    }

    @Override
    public void updateModelWithView() {
    	DrillChartViewDef visualizationDef = (DrillChartViewDef) getVisualizationSettings().getVisualizationDefinition();
    	ChartSettings chartSettings = visualizationDef.getChartSettings();
    	chartSettings.setPieLabelEnabled(enablePiechartLabel.getValue());
    	chartSettings.setPieLabelShowValue(pieLabelShowValue.getValue());
    	chartSettings.setPieLabelShowPercentage(pieLabelShowPercentage.getValue());
    	if(piechartLabelThreshold.getValue().length() > 0){
    		chartSettings.setPieLabelPercentageThreshold(Double.parseDouble(piechartLabelThreshold.getValue()));
    	}
    	chartSettings.setPieLegendEnabled(enablePiechartLegend.getValue());
    	chartSettings.setPieLegendShowValue(pieLegendShowValue.getValue());
    	chartSettings.setPieLegendShowPercentage(pieLegendShowPercentage.getValue());
    }

	public TextBox getPiechartLabelThreshold() {
		return piechartLabelThreshold;
	}

	public void setPiechartLabelThreshold(TextBox piechartLabelThreshold) {
		this.piechartLabelThreshold = piechartLabelThreshold;
	}

}
