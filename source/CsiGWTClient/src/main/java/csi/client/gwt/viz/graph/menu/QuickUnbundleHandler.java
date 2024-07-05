package csi.client.gwt.viz.graph.menu;

import com.google.common.collect.Lists;
import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.GraphActionServiceProtocol;

import java.util.ArrayList;
import java.util.Collection;

public class QuickUnbundleHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public QuickUnbundleHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }


    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<Void> unbundleVF = WebMain.injector.getVortex().createFuture();
        ArrayList<Integer> nodeIds = Lists.newArrayList();

//        Collection<? extends Integer> selectNodes = getPresenter().getModel().getSelectNodes();

//        nodeIds.add(item.getID());
        try {
            unbundleVF.execute(GraphActionServiceProtocol.class).unbundleSelection(getPresenter().getUuid(),getPresenter().getDataViewUuid());
            unbundleVF.addEventHandler(new AbstractVortexEventHandler<Void>() {
                @Override
                public void onSuccess(Void result) {
                    getPresenter().getGraphSurface().getToolTipManager().removeAllToolTips();
                    getPresenter().getGraphSurface().getGraph().getLegend().load();
                }
            });
        } catch (CentrifugeException ignored) {
        }
        getPresenter().getGraphSurface().refresh(unbundleVF);
        getPresenter().refreshTabs(unbundleVF);
    }

}
