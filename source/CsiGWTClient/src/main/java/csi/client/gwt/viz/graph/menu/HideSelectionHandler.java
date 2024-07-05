package csi.client.gwt.viz.graph.menu;

import java.util.List;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;

public class HideSelectionHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public HideSelectionHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<List<Integer>> future = getPresenter().getModel().hideSelection();
        getPresenter().getGraphSurface().refresh(future);
    }
}
