package csi.client.gwt.viz.graph.tab.pattern;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Tab;
import com.google.gwt.activity.shared.Activity;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.tab.pattern.result.PatternResultWidget;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternResultSetHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.gwt.PatternHighlightRequest;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;

public interface PatternTab {
    Tab getTab();

    PatternTabView getView();

    Graph getGraph();

    void setResults(PatternResultSet result);

    void show();

    void setLoading(VortexFuture future);

    PatternTabModel getModel();

    GraphPattern getMostRecentPattern();

    void setMostRecentPattern(GraphPattern pattern);

    List<PatternHighlightRequest> getHighlightRequests();

    void refresh();

    void removePendingPatternResult(PatternResultSetHandler handler);
    void addPendingPatternResult(PatternResultSetHandler handler);

    interface PatternTabActivity extends Activity {
        void search();

    }

    interface PatternTabModel {
        void setCurrentPatternColor(ClientColorHelper.Color color);

        void setCurrentResults(PatternResultSet result);
    }

    interface PatternTabView {
        Tab getTab();

        void bind(PatternTabActivity var1);

        void setLoading(VortexFuture future);

        void setResults(PatternResultWidget patternResultWidget, PatternResultSet result);

        List<PatternResultWidget> getPatternResultWidgets();

        void clearResults();
    }
}
