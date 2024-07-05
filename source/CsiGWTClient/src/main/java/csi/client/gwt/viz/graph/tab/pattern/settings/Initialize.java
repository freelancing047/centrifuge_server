package csi.client.gwt.viz.graph.tab.pattern.settings;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.WebMain;
import csi.client.gwt.events.CsiEvent;
import csi.client.gwt.events.CsiEventHandler;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsModel;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsView;
import csi.client.gwt.viz.graph.window.legend.LegendProxy;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.common.service.api.PatternActionsServiceProtocol;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class Initialize extends AbstractPatternSettingsActivity {
    public Initialize(PatternSettings patternSettings) {
        super(patternSettings);
    }

    @Override
    public void showNodeDetails(DrawPatternNode node) {
        getView().showCriteria(new CriteriaPanel(patternSettings, node.getNode()));
    }

    @Override
    public void hideNodeDetails() {
        getView().hideCriteria();
    }

    @Override
    public void pinDetails(PatternNode node) {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        PatternSettingsView view = getView();
        view.bind(this);
        PatternSettingsModel model = patternSettings.getModel();
        Iterator legendProxy = model.getPatterns().iterator();

        while (legendProxy.hasNext()) {
            GraphPattern type = (GraphPattern) legendProxy.next();
            view.addPattern(type);
        }

        legendProxy = model.getTypes().iterator();

        while (legendProxy.hasNext()) {
            GraphType type1 = (GraphType) legendProxy.next();
            view.addType(type1);
        }

        final LegendProxy legendProxy1 = new LegendProxy(patternSettings.getGraph().getUuid());
        legendProxy1.addLoadHandler(new CsiEventHandler() {
            @Override
            public void onCsiEvent(CsiEvent event) {
                Iterator i$;
                i$ = legendProxy1.getNodeLegendItems().iterator();
                while (i$.hasNext()) {
                    GraphNodeLegendItem item1 = (GraphNodeLegendItem) i$.next();
                    if("Bundle".equals(item1.typeName)){
                        break;
                    }if("In Common".equals(item1.typeName)){
                        break;
                    }if("Newly Added".equals(item1.typeName)){
                        break;
                    }
                    GraphNodeType type = new GraphNodeType(patternSettings.getGraph(), item1);
                    getView().addType(type);
                }
                getPatterns();
            }
        });

        getView().show();
    }

    private void getPatterns() {
        VortexFuture<List<GraphPattern>> future = WebMain.injector.getVortex().createFuture();
        future.execute(PatternActionsServiceProtocol.class).getGraphPatterns(WebMain.injector.getMainPresenter().getUserInfo().getName());
        future.addEventHandler(new AbstractVortexEventHandler<List<GraphPattern>>() {
            @Override
            public void onSuccess(List<GraphPattern> results) {
                for (GraphPattern result : results) {
                    getModel().addPattern(result);
                }
                getView().setPatterns(results);
                if (patternSettings.getModel().getPatterns().isEmpty()) {
                    createNewPattern();
                } else {
                    GraphPattern editPattern = getModel().getEditPattern();
                    if (editPattern != null) {
                        String patternUuuid = editPattern.getUuid();
                        for (GraphPattern result : results) {
                            if (result.getUuid().equals(patternUuuid)) {
                                patternSettings.editPattern(result);
                                return;
                            }
                        }
                    }
                    patternSettings.editPattern(results.get(0));
                }
            }
        });
    }

    @Override
    public void close() {
        getView().hide();
    }
}
