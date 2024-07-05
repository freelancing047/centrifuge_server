package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;

public class ClearMergeHighlightsHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public ClearMergeHighlightsHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }

    @SuppressWarnings("unused")
    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<Void> vortexFuture = getPresenter().getModel().clearMergeHighlights();

        vortexFuture.addEventHandler(new VortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                getPresenter().getGraphLegend().load();
            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }

            @Override
            public void onUpdate(int taskProgess, String taskMessage) {

            }

            @Override
            public void onCancel() {

            }
        });
        getPresenter().getGraphSurface().refresh(vortexFuture);
    }

}
