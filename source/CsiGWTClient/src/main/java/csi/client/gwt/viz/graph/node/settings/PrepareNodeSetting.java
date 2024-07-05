package csi.client.gwt.viz.graph.node.settings;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.server.common.model.themes.graph.GraphTheme;

public class PrepareNodeSetting implements Activity {

    private NodeSettings nodeSettings;

    public PrepareNodeSetting(NodeSettings nodeSettings) {
        this.nodeSettings = nodeSettings;
        // TODO Auto-generated constructor stub
    }

    @Override
    public String mayStop() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCancel() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        
        if(nodeSettings.getGraphSettings().getCurrentTheme() != null){
            nodeSettings.setTheme(nodeSettings.getGraphSettings().getCurrentTheme());
        } else {
            nodeSettings.getGraphSettings().getTheme().addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {

            @Override
            public void onSuccess(GraphTheme result) {
                nodeSettings.setTheme(result);
            }
        });
        }

    }

}
