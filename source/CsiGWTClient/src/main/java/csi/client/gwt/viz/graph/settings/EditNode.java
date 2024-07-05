package csi.client.gwt.viz.graph.settings;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.node.settings.NodeSettings;

public class EditNode extends AbstractActivity {

    private NodeProxy nodeProxy;
    private GraphSettings graphSettings;

    public EditNode(NodeProxy nodeProxy, GraphSettings graphSettings) {
        this.nodeProxy = nodeProxy;
        this.graphSettings = graphSettings;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        NodeSettings nodeSettings = new NodeSettings(graphSettings, nodeProxy);
        nodeSettings.show();
    }
}
