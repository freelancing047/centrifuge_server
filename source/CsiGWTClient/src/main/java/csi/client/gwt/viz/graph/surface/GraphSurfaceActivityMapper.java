package csi.client.gwt.viz.graph.surface;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

class GraphSurfaceActivityMapper implements ActivityMapper {

    private GraphSurface graphSurface;


    public GraphSurfaceActivityMapper(GraphSurface graphSurface) {
        this.graphSurface = graphSurface;
    }


    @SuppressWarnings("unused")
    @Override
    public Activity getActivity(Place place) {
        return new ShowGraph(graphSurface);
    }
}
