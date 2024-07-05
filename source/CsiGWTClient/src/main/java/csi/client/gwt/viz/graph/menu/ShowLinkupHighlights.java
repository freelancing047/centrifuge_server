package csi.client.gwt.viz.graph.menu;


import com.google.gwt.dev.util.Pair;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;
import csi.shared.gwt.vortex.CsiPair;

public class ShowLinkupHighlights extends AbstractMenuEventHandler<Graph, GraphMenuManager> {


    public ShowLinkupHighlights(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }


    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        VortexFuture<CsiPair<Boolean, Boolean>> future = getPresenter().getModel().showLinkupHighlights();
        getPresenter().getGraphSurface().refresh(future);
    }
}
