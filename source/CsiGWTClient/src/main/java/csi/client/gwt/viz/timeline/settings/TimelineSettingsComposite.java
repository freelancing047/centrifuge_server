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
package csi.client.gwt.viz.timeline.settings;

import csi.client.gwt.viz.shared.settings.AbstractSettingsComposite;
import csi.server.common.model.visualization.timeline.TimelineSettings;
import csi.server.common.model.visualization.timeline.TimelineViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class TimelineSettingsComposite extends AbstractSettingsComposite<TimelineViewDef> {

    public TimelineSettings getTimelineSettings() {
        TimelineViewDef def = getVisualizationSettings().getVisualizationDefinition();
        return def.getTimelineSettings();
    }

}
