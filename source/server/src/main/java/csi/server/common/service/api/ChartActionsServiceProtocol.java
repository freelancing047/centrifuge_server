/** 
 *  Copyright (c) 2008-2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.common.service.api;

import java.util.List;

import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.DisplayFirst;
import csi.shared.core.visualization.chart.*;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface ChartActionsServiceProtocol extends VortexService {

    /**
     * Gets all the data for the request chart visualization.
     * @param request Identifies the visualization
     * @return Data needed to render the chart.
     */
    public HighchartResponse getChart(HighchartRequest request);

    /**
     * Gets data for the overview which controls the visible area of the chart.
     * @param overviewRequest Data needed to compute the correct overview.
     * @return A list of colors representing a bin. A bin can contain one or more categories.
     */

    public OverviewResponse getOverview(OverviewRequest overviewRequest);

    public HighchartPagingResponse getChartPage(HighchartPagingRequest request);

    public List<String> getQueryCategories(String vizUuid, String query, List<String> drills);

    public Integer getCategoryRangeIndex(String uuid, List<String> drills, String category);

    public List<String> selectCategories(List<ChartCriterion> criteria, String vizUuid, List<String> drills);

    public List<String> selectAll(String uuid, List<String> drillSelections);

    public List<ChartMetrics> getChartMetrics(HighchartPagingRequest requestForHighchartData );

    public boolean updateCurrentView(String vizUuid, DisplayFirst view);



}
