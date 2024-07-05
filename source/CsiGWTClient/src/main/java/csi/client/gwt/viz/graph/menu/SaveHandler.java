package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;

public class SaveHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public SaveHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }

    @SuppressWarnings("unused")
    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        // NOTE: REMOVING beacause it seems someone else is already doing this
        // getPresenter().getModel().saveSettings();
        getPresenter().getModel().save();
    }

}
