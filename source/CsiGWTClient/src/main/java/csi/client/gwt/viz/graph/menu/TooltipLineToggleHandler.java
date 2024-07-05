package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.shared.menu.MenuKey;

public class TooltipLineToggleHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    private MenuKey key;

    public TooltipLineToggleHandler(MenuKey toggleTooltipAnchorsAlways, Graph graph, GraphMenuManager menuFactory) {
        super(graph, menuFactory);
        this.key = toggleTooltipAnchorsAlways;
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        switch (key) {

            case TOGGLE_TOOLTIP_ANCHORS_ALWAYS:
                getPresenter().getGraphSurface().getToolTipManager().setShowOnHoverOnly(false);
                break;
            case TOGGLE_TOOLTIP_ANCHORS_HOVER:
                getPresenter().getGraphSurface().getToolTipManager().setShowOnHoverOnly(true);
                break;
            default:
                break;
        }
        getPresenter().getGraphSurface().refresh();
    }

}
