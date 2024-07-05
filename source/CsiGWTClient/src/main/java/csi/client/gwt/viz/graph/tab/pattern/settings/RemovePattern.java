package csi.client.gwt.viz.graph.tab.pattern.settings;

import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.service.api.PatternActionsServiceProtocol;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;

public class RemovePattern extends AbstractPatternSettingsActivity {
    private GraphPattern pattern;

    public RemovePattern(PatternSettings patternSettings, GraphPattern pattern) {
        super(patternSettings);
        this.pattern = pattern;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        future.execute(PatternActionsServiceProtocol.class).deleteGraphPattern(getModel().getEditPattern());
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
            }
        });
        getModel().removePattern(pattern);
        getView().removePattern(pattern);
        List<GraphPattern> patterns = getModel().getPatterns();
        if (patterns.isEmpty()) {
            createNewPattern();
        } else {
            GraphPattern graphPattern = patterns.get(0);
            patternSettings.editPattern(graphPattern);
        }
    }
}
