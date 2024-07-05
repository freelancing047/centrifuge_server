package csi.client.gwt.viz.graph.tab.pattern.settings;

import java.util.List;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.tab.pattern.PatternTab;
import csi.client.gwt.viz.graph.tab.pattern.settings.criterion.CriterionPanel;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.HasPatternCriteria;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public interface PatternSettings {
    void show();

    PatternSettings.PatternSettingsView getView();

    PatternSettings.PatternSettingsModel getModel();

    Graph getGraph();

    void editCriteria(HasPatternCriteria item);

    void hideCriteria();

    void editCriterion(HasPatternCriteria var1, PatternCriterion var2);

    void editPattern(GraphPattern pattern);

    void removePattern(GraphPattern pattern);

    void addPattern(GraphPattern pattern);

    void removeFromPattern(HasPatternCriteria item);

    void removeCriterion(HasPatternCriteria node, PatternCriterion criterion);

    void setResults(PatternResultSet result);

    void setLoading(VortexFuture future);

    PatternTab getTab();

    void setEditPattern(GraphPattern pattern);


    public interface PatternSettingsActivity extends Activity {
        void search();

        void close();

        void addToPattern(GraphType var1);

        void showNodeDetails(DrawPatternNode var1);

        void setRequireDistinctLinks(boolean value);

        void hideNodeDetails();

        void pinDetails(PatternNode var1);

        void addLink(PatternNode var1, PatternNode var2);

        void createNewPattern();

        void deletePattern();

        void setPatternName(String name);

        void editPattern(GraphPattern pattern);

        void editLink(PatternLink link);

        boolean allowLinkDrawing();

        void copyPattern();

        void setRequireDistinctNodes(boolean value);
    }

    public interface PatternSettingsModel {
        List<GraphPattern> getPatterns();

        List<GraphType> getTypes();

        void add(GraphType var1);

        void addPattern(GraphPattern pattern);

        GraphPattern getEditPattern();

        void setEditPattern(GraphPattern pattern);

        void removePattern(GraphPattern pattern);

        void savePatterns();
    }

    public interface PatternSettingsView {
        void bind(PatternSettings.PatternSettingsActivity var1);

        void show();

        void hide();

        void addPattern(GraphPattern var1);

        void addType(GraphType var1);

        void startDragNode(DrawPatternNode var1);

        void hideDetails();

        void showDetails(DrawPatternNode var1);

        void addNodeToPattern(PatternNode node);

        void showCriteria(CriteriaPanel var1);

        void hideCriteria();

        void pinDetails(PatternNode var1);

        void showCriterion(CriterionPanel var1);

        void showCriteria();

        void endNodeDrag(MouseUpEvent var1);

        void dragMove(MouseMoveEvent var1);

        void addLinkToPattern(PatternLink var1);

        void clearPattern();

        void setPatternName(String name);

        void removePattern(GraphPattern pattern);

        void setPatterns(List<GraphPattern> patterns);

        void selectPattern(GraphPattern editPattern);

        void setEditing(HasPatternCriteria node);

        void editLink(PatternLink link);

        void updatePattern(GraphPattern pattern);

        void setRequireDistinctNodes(boolean requireDistinctNodes);

        void setRequireDistinctLinks(boolean requireDistinctLinks);
    }
}
