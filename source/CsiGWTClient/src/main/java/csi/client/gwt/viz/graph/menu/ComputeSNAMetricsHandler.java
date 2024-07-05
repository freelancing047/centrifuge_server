package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;

public class ComputeSNAMetricsHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {


    public ComputeSNAMetricsHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }

    @SuppressWarnings("unused")
    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<Void> future = getPresenter().getModel().computeSNA();
        // TODO: do i need to refresh graph at this point?
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                getPresenter().showNodesTabSnaColumns();
            }
        });
        getPresenter().refreshTabs(future);
    }

}
