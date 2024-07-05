package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.dialog.BundleDialog;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;

public class BundleHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    private Graph graph;

    public BundleHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
        this.graph = graph;
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        new BundleDialog(graph).show();
    }

}
