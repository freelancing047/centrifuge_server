package csi.client.gwt.viz.timeline.presenters;

import java.util.List;

import csi.client.gwt.viz.timeline.model.Axis;
import csi.server.common.model.visualization.timeline.TimelineCachedState;
import csi.shared.core.visualization.timeline.OverviewTrack;

public interface HistogramOverview {
    
    List<Axis> createHistogramOverview(OverviewTrack overviewData, TimelineCachedState state);

}
