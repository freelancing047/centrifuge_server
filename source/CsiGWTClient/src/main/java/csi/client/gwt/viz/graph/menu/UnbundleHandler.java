package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class UnbundleHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public UnbundleHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }


    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        try {
            future.execute(GraphActionServiceProtocol.class).unbundleEntireGraph(getPresenter().getUuid(),getPresenter().getDataviewUuid());
            future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                @Override
                public void onSuccess(Void result) {
                    getPresenter().getLegend().load();

                    getPresenter().getGraphSurface().getToolTipManager().removeAllToolTips();
                }
            });
        } catch (CentrifugeException e) {
        }
        getPresenter().getGraphSurface().refresh(future);
        getPresenter().refreshTabs(future);
    }

}
