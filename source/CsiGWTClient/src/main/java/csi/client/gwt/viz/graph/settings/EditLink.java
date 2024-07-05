package csi.client.gwt.viz.graph.settings;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.link.settings.LinkSettings;

public class EditLink extends AbstractActivity {

    private LinkProxy linkProxy;
    private GraphSettings graphSettings;

    public EditLink(LinkProxy linkProxy, GraphSettings graphSettings) {
        this.linkProxy = linkProxy;
        this.graphSettings = graphSettings;

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        LinkSettings linkSettings = new LinkSettings(graphSettings, linkProxy);
        linkSettings.show();
    }
}
