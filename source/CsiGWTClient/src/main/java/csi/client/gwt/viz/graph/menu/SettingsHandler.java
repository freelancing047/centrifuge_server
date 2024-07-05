package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.settings.GraphSettings;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

public final class SettingsHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> implements
        SettingsActionCallback<RelGraphViewDef> {

    public SettingsHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        GraphSettings graphSettings = new GraphSettings(this);
        graphSettings.setDataView(getPresenter().getDataview().getDataView());
        graphSettings.setVisualizationDef(getPresenter().getModel().getRelGraphViewDef());
        graphSettings.setVisualization(getPresenter());
        graphSettings.show();
    }

    @Override
    public void onSaveComplete(RelGraphViewDef vizDef, boolean suppressLoadAfterSave) {
//        getPresenter().reload();
    }

    @Override
    public void onCancel() {
        // TODO Auto-generated method stub

    }
}
