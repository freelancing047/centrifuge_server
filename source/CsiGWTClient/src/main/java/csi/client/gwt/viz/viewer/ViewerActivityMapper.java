package csi.client.gwt.viz.viewer;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class ViewerActivityMapper implements ActivityMapper {
    private Viewer viewer;

    public ViewerActivityMapper(Viewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public Activity getActivity(Place place) {
        return new InitializeViewer(viewer);
    }
}
