package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.selection.SelectionModel;

public class SelectAllHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public SelectAllHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<SelectionModel> vortexFuture = getPresenter().getModel().selectAll();
        getPresenter().getGraphSurface().refresh(vortexFuture);
    }
}
