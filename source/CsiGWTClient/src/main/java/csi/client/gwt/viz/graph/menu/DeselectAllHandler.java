package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.selection.SelectionModel;

public class DeselectAllHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public DeselectAllHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }

    @SuppressWarnings("unused")
    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<SelectionModel> futureRequest = getPresenter().getModel().clearSelection();
        getPresenter().getGraphSurface().refresh(futureRequest);
    }
}
