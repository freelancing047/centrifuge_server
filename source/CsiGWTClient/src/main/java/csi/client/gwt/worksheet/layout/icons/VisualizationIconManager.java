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
package csi.client.gwt.worksheet.layout.icons;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.model.visualization.VisualizationType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class VisualizationIconManager {

    public enum IconSize {
        SMALL, MEDIUM, LARGE;
    }

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static final VisualizationIcons visualizationIcons = GWT.create(VisualizationIcons.class);

    public static ImageResource getIcon(VisualizationType type, IconSize size, boolean hover) {
        if (!hover) {
            switch (type) {
                case CHRONOS:
                    return visualizationIcons.iconTimeline();
                case DRILL_CHART:
                    return visualizationIcons.iconChart();
                case GEOSPATIAL_V2:
                case MAP_CHART:
                    return visualizationIcons.iconGeo();
                case MATRIX:
                    return visualizationIcons.iconBubble();
                case RELGRAPH_V2:
                    return visualizationIcons.iconGraph();
                case TABLE:
                    return visualizationIcons.iconTable();
                default:
                    return null;
            }
        } else {
                switch (type) {
                    case CHRONOS:
                        return visualizationIcons.iconTimelineHover();
                    case DRILL_CHART:
                        return visualizationIcons.iconChartHover();
                    case GEOSPATIAL_V2:
                        return visualizationIcons.iconGeoHover();
                    case MAP_CHART:
                        return visualizationIcons.iconGeoHover();
                    case MATRIX:
                        return visualizationIcons.iconBubbleHover();
                    case RELGRAPH_V2:
                        return visualizationIcons.iconGraphHover();
                    case TABLE:
                        return visualizationIcons.iconTableHover();
                    default:
                        return null;
                }
            }
    }

	public static String getAltText(VisualizationType type) {
		switch (type) {
        case CHRONOS:
            return i18n.visualizationIconManagerTimelineAlt(); //$NON-NLS-1$
        case DRILL_CHART:
            return i18n.visualizationIconManagerChartAlt(); //$NON-NLS-1$
        case GEOSPATIAL_V2:
        case MAP_CHART:
            return i18n.visualizationIconManagerMapAlt(); //$NON-NLS-1$
        case MATRIX:
            return i18n.visualizationIconManagerMatrixAlt(); //$NON-NLS-1$
        case RELGRAPH_V2:
            return i18n.visualizationIconManagerGraphAlt(); //$NON-NLS-1$
        case TABLE:
            return i18n.visualizationIconManagerTableAlt(); //$NON-NLS-1$
        default:
            return null;
    }
	}
}
