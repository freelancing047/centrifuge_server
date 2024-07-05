package csi.client.gwt.viz.graph.tab.player;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class TimePlayerActivityMapper implements ActivityMapper {

    private TimePlayer timePlayer;

    public TimePlayerActivityMapper(TimePlayer timePlayer) {
        this.timePlayer = timePlayer;
    }

    @Override
    public Activity getActivity(Place place) {
        return new InitTimePlayer(timePlayer);
    }
}
