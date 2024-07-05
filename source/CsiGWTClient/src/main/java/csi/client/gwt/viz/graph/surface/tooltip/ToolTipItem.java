package csi.client.gwt.viz.graph.surface.tooltip;

import csi.client.gwt.viz.graph.surface.tooltip.ToolTip.ToolTipModel;


public interface ToolTipItem {

    String getValue(ToolTipModel model);

    String getLabel();
}
