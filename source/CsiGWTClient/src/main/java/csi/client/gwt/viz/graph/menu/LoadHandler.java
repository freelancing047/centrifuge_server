package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;

public class LoadHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {



    public LoadHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }


    @SuppressWarnings("unused")
    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().load();
    }

}
