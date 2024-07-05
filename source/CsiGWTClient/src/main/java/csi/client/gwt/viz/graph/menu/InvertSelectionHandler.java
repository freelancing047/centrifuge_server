package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.selection.SelectionModel;

public class InvertSelectionHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

 
    public InvertSelectionHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }


    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<SelectionModel> futureRequest = getPresenter().getModel().invertSelection();
        getPresenter().getGraphSurface().refresh(futureRequest);
    }
}
