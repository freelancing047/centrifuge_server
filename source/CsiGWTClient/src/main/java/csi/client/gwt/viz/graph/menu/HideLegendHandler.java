package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;

public class HideLegendHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public HideLegendHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().hideLegend();
    }
}
