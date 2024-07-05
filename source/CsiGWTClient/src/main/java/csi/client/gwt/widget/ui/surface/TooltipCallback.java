package csi.client.gwt.widget.ui.surface;

import com.google.gwt.aria.client.OrientationValue;
import com.google.gwt.safehtml.shared.SafeHtml;

import csi.shared.core.util.HasLabel;

public interface TooltipCallback<T extends HasLabel> {

    /**
     * Called to get tooltip contents for a given item.
     * @param x index of ordinal along x-axis.
     * @param y index of ordinal along y-axis.
     * @param ssr Current renderable for which tooltip is required.
     * @return HTML to display in tooltip.
     */
    SafeHtml getItemTooltipContent(int x, int y, ScrollableSurfaceRenderable ssr);

    /**
     * Called to get tooltip contents for a given axis ordinal item.
     * @param orientation Axis orientation
     * @param index
     * @param axisValue
     * @return HTML to display in the tooltip
     */
    SafeHtml getAxisTooltipContent(OrientationValue orientation, int index, T axisValue);
}