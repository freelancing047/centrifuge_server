package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class UnhideSelectionHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public UnhideSelectionHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }


    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        try {
            future.execute(GraphActionServiceProtocol.class).unhideSelection(getPresenter().getDataviewUuid(), getPresenter().getUuid());
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        getPresenter().getGraphSurface().refresh(future);
    }

}
