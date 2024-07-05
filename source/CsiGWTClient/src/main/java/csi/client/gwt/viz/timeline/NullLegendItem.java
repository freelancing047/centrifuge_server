package csi.client.gwt.viz.timeline;

import csi.client.gwt.viz.timeline.model.LegendItem;
import csi.client.gwt.viz.timeline.view.drawing.TimelineTrackRenderable;
import csi.shared.core.color.ClientColorHelper;

public class NullLegendItem extends LegendItem {

    public NullLegendItem() {
        super(ClientColorHelper.rgb_to_int(84,84,84), TimelineTrackRenderable.NO_VALUE);
    }

}
