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

import csi.client.gwt.viz.shared.settings.AbstractSettingsComposite;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class ChartSettingsComposite extends AbstractSettingsComposite<DrillChartViewDef> {


    public ChartSettings getDrillChartSettings() {

        // some nullchecks because it was throwing an ugly error clientside.
        if(getVisualizationSettings() == null) {
            return null;
        }
        if(getVisualizationSettings().getVisualizationDefinition() == null) {
            return null;
        }

        DrillChartViewDef def = getVisualizationSettings().getVisualizationDefinition();
        return def.getChartSettings();
    }
}
