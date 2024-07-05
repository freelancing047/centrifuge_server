package csi.client.gwt.viz.graph.controlbar;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class GraphControlBarActivityMapper implements ActivityMapper{
    private final GraphControlBar graphControlBar;

    public GraphControlBarActivityMapper(GraphControlBar graphControlBar) {
        this.graphControlBar = graphControlBar;
    }

    @Override
    public Activity getActivity(Place place) {
        return new ShowGraphPlayer(graphControlBar);
    }
}
