package csi.client.gwt.viz.graph.settings;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.settings.GraphSettings.Presenter;
import csi.client.gwt.viz.graph.settings.fielddef.FieldProxy;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.server.common.model.FieldDef;
import csi.server.common.model.themes.graph.GraphTheme;

public class CreateNode extends AbstractActivity implements Presenter {

    private GraphSettings graphSettings;
    private FieldProxy fieldProxy;

    public CreateNode(GraphSettings graphSettings, FieldProxy fieldProxy) {
        checkNotNull(fieldProxy);
        this.graphSettings = graphSettings;
        this.fieldProxy = fieldProxy;
    }
    
    public CreateNode(GraphSettings graphSettings, FieldDef fieldDef) {
        checkNotNull(fieldDef);
        this.graphSettings = graphSettings;
        String fieldName = fieldDef.getFieldName();

        if (!Strings.isNullOrEmpty(fieldName) && !(null == fieldDef.getValueType())) {
            this.fieldProxy = new FieldProxy(fieldDef, fieldName);
        }
        
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        final NodeProxy nodeProxy = graphSettings.getNodeProxyFactory().create(fieldProxy);
        if(graphSettings.getCurrentTheme() != null){
            applyTheme(nodeProxy, graphSettings.getCurrentTheme());
        } else {
            graphSettings.getTheme().addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {

                @Override
                public void onSuccess(GraphTheme result) {
                    applyTheme(nodeProxy, result);
                }
            });
        }
    }

    public void applyTheme(final NodeProxy nodeProxy, GraphTheme result) {
        graphSettings.addNode(nodeProxy);
        nodeProxy.apply(graphSettings);
        graphSettings.getView().addNode(nodeProxy);
        graphSettings.getView().redraw();
    }
}
