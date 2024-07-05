package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;

public class UnhideAllHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {


    public UnhideAllHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }


    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<Void> future = getPresenter().getModel().unhideAll();
        getPresenter().getGraphSurface().refresh(future);
    }
}
