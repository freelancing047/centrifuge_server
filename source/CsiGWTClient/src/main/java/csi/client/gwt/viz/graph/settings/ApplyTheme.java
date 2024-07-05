package csi.client.gwt.viz.graph.settings;

import java.util.Collection;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.settings.GraphSettings.Presenter;
import csi.client.gwt.viz.graph.settings.GraphSettings.View;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.themes.graph.GraphTheme;

public class ApplyTheme extends AbstractActivity implements Presenter {

    private GraphSettings graphSettings;

    public ApplyTheme(GraphSettings graphSettings) {
        this.graphSettings = graphSettings;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        if(graphSettings.getCurrentTheme() != null){
            apply(graphSettings.getCurrentTheme());
        } else if(graphSettings.getThemeUuid() != null){
            VortexFuture<GraphTheme> getThemeFuture = graphSettings.getTheme();
            getThemeFuture.addEventHandler(new FutureThemeHandler());
        }
    }

    public void apply(GraphTheme graphTheme) {
        View view = graphSettings.getView();
        view.setTheme(graphTheme);// change the theme selector
        view.removeAllNodes();// We are going to need to display new nodes.
        Collection<NodeProxy> nodeProxies = graphSettings.getNodeProxies();
        for (NodeProxy nodeProxy : nodeProxies) {
            nodeProxy.apply(graphSettings);// the theme knows how to enforce itself
            view.addNode(nodeProxy);// add the nodes back to the view.
        }
        for(LinkProxy linkProxy : graphSettings.getLinkProxies()){
            linkProxy.apply(graphSettings);
            view.addLink(linkProxy);
        }

        view.redraw();// will probably need to redraw\
        // FIXME: the redraw should logically come from setting the activity back to Show
        // graphSettings.show();// return to a state of rest
    }

    private class FutureThemeHandler extends AbstractVortexEventHandler<GraphTheme> {

        @Override
        public void onSuccess(GraphTheme graphTheme) {
            apply(graphTheme);
        }
    }
}
