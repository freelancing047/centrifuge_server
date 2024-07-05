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

import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.shared.core.util.IntCollection;
import csi.shared.core.visualization.map.MetricsDTO;
import csi.shared.core.visualization.timeline.SingularTimelineEvent;
import csi.shared.core.visualization.timeline.TimelineResult;
import csi.shared.core.visualization.timeline.Tooltip;
import csi.shared.gwt.viz.timeline.TimelineRequest;
import csi.shared.gwt.vortex.VortexService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface ChronosActionsServiceProtocol extends VortexService {

	
	public TimelineResult loadTimeline(TimelineRequest request) throws CentrifugeException;

    public Tooltip createTooltip(String dataviewUuid, String vizUuid, SingularTimelineEvent timelineEvent) throws CentrifugeException;

    public MetricsDTO getTotalMetrics(String uuid, String group);

    public MetricsDTO getViewMetrics(TimelineRequest req, List<String> visibleGroups);

    TimelineEventSelection doServerSelection(String uuid, String color, TimelineEventSelection currentSelection,
                                             boolean doSelect, ArrayList<String> trackNames);

    TimelineEventSelection doServerTrackSelection(String uuid, TimelineEventSelection currentSelection, boolean doSelect, String trackName);

    IntCollection getItems(String dataviewUuid, String vizUuid, long from, long to, ArrayList<String> trackName);
}
