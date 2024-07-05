package csi.client.gwt.viz.graph.window.transparency;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class TransparencySettingsActivityMapper implements ActivityMapper {

    private TransparencySettings settings;

    public TransparencySettingsActivityMapper(TransparencySettings settings) {
        this.settings = settings;
    }

    @Override
    public Activity getActivity(Place place) {
        return new ShowTransparencyWindow(settings);
    }

}
