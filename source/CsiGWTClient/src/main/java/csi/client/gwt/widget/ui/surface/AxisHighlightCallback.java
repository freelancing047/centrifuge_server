package csi.client.gwt.widget.ui.surface;

import csi.shared.core.util.HasLabel;

public interface AxisHighlightCallback {

    /**
     * @param event Mouse over/out native event.
     * @param value Axis value that has been highlighted.
     * @param i Index of the axis value
     * @param location The x or y location of the highlight along the axis.
     * @param enable true if the highlight has been enabled (mouse-over), false if highlight is now disabled (mouse-out)
     */
    public <T extends HasLabel> void highlight(int clientX, int clientY, T value, int i, double location,
                                               boolean enable);
}