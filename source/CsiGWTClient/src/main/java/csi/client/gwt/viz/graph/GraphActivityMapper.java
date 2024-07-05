package csi.client.gwt.viz.graph;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class GraphActivityMapper implements ActivityMapper {

    @SuppressWarnings("unused")
    private Graph graph;


    public GraphActivityMapper(Graph graph) {
        this.graph = graph;
    }


    @Override
    public Activity getActivity(Place place) {
        return null;
    }
}