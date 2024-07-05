package csi.client.gwt.viz.graph.node.settings;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.settings.GraphSettings;
import csi.server.common.model.themes.graph.GraphTheme;

public class NodeSettings {

    private class ShowSoon implements Scheduler.RepeatingCommand {

        private NodeSettings nodeSettings;

        public ShowSoon(NodeSettings nodeSettings) {
            this.nodeSettings = nodeSettings;
        }

        @Override
        public boolean execute() {
//            if (theme == null) {
//                return true;
//            }
            activityManager.setActivity(new ShowNodeSettings(nodeSettings));
            return false;
        }
    }

    private NodeSettingsModel nodeSettingsModel;
    private NodeSettingsDialog nodeSettingsDialog;
    private CSIActivityManager activityManager;
    private EventBus eventBus;
    private GraphSettings graphSettings;
    private GraphTheme theme;

    private NodeProxy nodeProxy;

    public NodeSettings(GraphSettings graphSettings, NodeProxy nodeProxy) {
        this.graphSettings = graphSettings;
        this.nodeProxy = nodeProxy;
        nodeSettingsModel = new NodeSettingsModel(this, nodeProxy);
        nodeSettingsDialog = new NodeSettingsDialog(this);
        eventBus = new SimpleEventBus();
        activityManager = new CSIActivityManager(new NodeSettingsActivityMapper(), eventBus);
        activityManager.setActivity(new PrepareNodeSetting(this));
    }

    public void close() {
        graphSettings.show();
    }

    public void delete() {
        graphSettings.deleteNode(nodeProxy);
    }

//    public DataViewPresenter getDataviewPresenter() {
//        return graphSettings.getDataview();
//    }

    public GraphSettings getGraphSettings() {
        return graphSettings;
    }

    public NodeSettingsModel getModel() {
        return nodeSettingsModel;
    }

    public GraphTheme getTheme() {
        return theme;
    }

    public String getThemeName() {
        return graphSettings.getThemeUuid();
    }

    public NodeSettingsDialog getView() {
        return nodeSettingsDialog;
    }

    public void save() {
        activityManager.setActivity(new SaveNodeSettings(this));
    }

    public void setTheme(GraphTheme theme) {
        this.theme = theme;
    }

    public void show() {
        // I really need a GraphTheme, perhaps i should require one in the constructor...
        Scheduler.get().scheduleFixedDelay(new ShowSoon(this), 25);
    }
}
