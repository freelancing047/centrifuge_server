package csi.client.gwt.widget.ui.surface;

import csi.shared.core.util.HasLabel;

public interface AxisClickCallback {
        /**
         * @param event Mouse click native event.
         * @param value axis value that has been clicked
         * @param i Index of the axis value
         * @param location The x or y location of the highlight along the axis.
         * @param enable true to select, false to unselect
         */
        public <T extends HasLabel> void onClickLabel(int clientX, int clientY, T value, int i, double location,
                                                      boolean enable);
    }