package csi.client.gwt.viz.graph.tab.pattern.settings;

import csi.client.gwt.viz.graph.tab.pattern.PatternTab;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.server.common.dto.graph.pattern.PatternResultSet;

public class PatternResultSetHandler extends AbstractVortexEventHandler<PatternResultSet> {
    private final PatternTab patternTab;
    private boolean isAborted;

    public PatternResultSetHandler(PatternTab patternTab) {
        this.patternTab = patternTab;
        patternTab.addPendingPatternResult(this);
    }

    @Override
    public void onSuccess(PatternResultSet result) {
        if(!isAborted) {
            patternTab.setResults(result);
        }
        patternTab.removePendingPatternResult(this);
    }

    public void abort() {
        isAborted = true;
    }
}
