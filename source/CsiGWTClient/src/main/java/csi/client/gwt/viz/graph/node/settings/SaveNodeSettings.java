package csi.client.gwt.viz.graph.node.settings;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;


public class SaveNodeSettings implements Activity {

    private NodeSettings nodeSettings;

    public SaveNodeSettings(NodeSettings nodeSettings) {
        this.nodeSettings = nodeSettings;
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        NodeSettingsModel model = nodeSettings.getModel();
        NodeSettingsDialog view = nodeSettings.getView();
        model.save();
        view.close();
        nodeSettings.close();
    }
}
