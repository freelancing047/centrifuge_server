package csi.client.gwt.viz.graph.settings;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.settings.GraphSettings.Presenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.server.common.model.themes.graph.GraphTheme;

public class CreateLink implements Presenter {

    private GraphSettings graphSettings;
    private NodeProxy node1;
    private NodeProxy node2;

    public CreateLink(GraphSettings graphSettings, NodeProxy node1, NodeProxy node2) {
        this.graphSettings = graphSettings;
        this.node1 = node1;
        this.node2 = node2;
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
    public void applyTheme(GraphTheme result) {
        LinkProxy linkProxy = LinkProxy.create(node1, node2);
        linkProxy.apply(graphSettings);
        graphSettings.addLink(linkProxy);// add to model
        graphSettings.getView().addLink(linkProxy);// add to view
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        
        if(graphSettings.getCurrentTheme() != null){
            applyTheme(graphSettings.getCurrentTheme());
        } else {
            
        graphSettings.getTheme().addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {

            @Override
            public void onSuccess(GraphTheme result) {
                applyTheme(result);
            }

            
        });}
    }
}
