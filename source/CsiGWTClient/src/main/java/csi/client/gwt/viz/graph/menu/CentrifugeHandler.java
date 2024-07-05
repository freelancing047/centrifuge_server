package csi.client.gwt.viz.graph.menu;

import java.util.List;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.CsiMap;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.shared.core.visualization.graph.GraphLayout;

public class CentrifugeHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {
    public CentrifugeHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        if (getPresenter().isViewLoaded()) {
        	GraphLayout oldLayout = ((RelGraphViewDef) getPresenter().getVisualizationDef()).getLayout();
            VortexFuture<List<CsiMap<String, String>>> future = getPresenter().getModel().applyLayout(GraphLayout.centrifuge);
            getPresenter().getGraphSurface().getToolTipManager().removeAllToolTips();
            getPresenter().getGraphSurface().refreshWithNewLayout(future, oldLayout);
        } else {
        	getPresenter().getModel().applyLayoutBeforeLoad(GraphLayout.centrifuge);
        	getPresenter().getModel().checkLayout(GraphLayout.centrifuge);
	    }
    }
}
