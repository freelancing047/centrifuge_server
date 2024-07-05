package csi.client.gwt.viz.graph.window.legend;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class GraphLegendActivityMapper implements ActivityMapper {

    private GraphLegend graphLegend;


    // statemachine
    public GraphLegendActivityMapper(GraphLegend graphLegend) {
        this.graphLegend = graphLegend;
    }


    @Override
    public Activity getActivity(Place place) {
        return new ShowLegend(graphLegend);
    }
}
