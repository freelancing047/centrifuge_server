package csi.client.gwt.viz.graph.link.settings;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.server.common.model.themes.graph.GraphTheme;

public class PrepareLinkSettings implements Activity {

    private LinkSettings linkSettings;

    public PrepareLinkSettings(LinkSettings linkSettings) {
        this.linkSettings = linkSettings;
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
        
        if(linkSettings.getGraphSettings().getCurrentTheme() != null){
            linkSettings.setTheme(linkSettings.getGraphSettings().getCurrentTheme());
        } else {
            linkSettings.getGraphSettings().getTheme().addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {
    
                @Override
                public void onSuccess(GraphTheme result) {
                    linkSettings.setTheme(result);
                }
            });
        }
    }
}
