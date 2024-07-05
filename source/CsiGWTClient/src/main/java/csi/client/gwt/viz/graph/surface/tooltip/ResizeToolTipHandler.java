package csi.client.gwt.viz.graph.surface.tooltip;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;

final class ResizeToolTipHandler implements ResizeHandler {


    private ToolTip toolTip;

    ResizeToolTipHandler(ToolTip toolTip) {
        this.toolTip = toolTip;
    }

    @Override
    public void onResize(ResizeEvent event) {
        toolTip.getView().updateBodyHeight();
    }
}