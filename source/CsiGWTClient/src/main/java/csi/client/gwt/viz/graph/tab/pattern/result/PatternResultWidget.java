package csi.client.gwt.viz.graph.tab.pattern.result;

import java.util.Collection;
import java.util.List;

import com.github.gwtbootstrap.client.ui.SplitDropdownButton;
import com.google.gwt.user.client.ui.IsWidget;

import csi.server.business.visualization.graph.pattern.model.PatternMeta;
import csi.server.common.dto.graph.gwt.PatternHighlightRequest;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.shared.core.color.ClientColorHelper;

public interface PatternResultWidget extends IsWidget {
    void setColor(ClientColorHelper.Color color);

    void search();

    void showResults(PatternResultSet patternResultSet);

    PatternResultSet getResult();

    void highlightPattern(List<PatternMeta> selectedPatterns);

    void showOnly(List<PatternMeta> selectedItems);

    void clearHighlight(List<PatternMeta> selectedPatterns);

    void addSelect(List<PatternMeta> selectedPatterns, boolean selectNodes, boolean selectLinks);

    void select(List<PatternMeta> selectedPatterns, boolean selectNodes, boolean selectLinks);

    SplitDropdownButton getSearchButton();

    PatternHighlightRequest getPatternHighlightRequest();

    void clearSelection();

    interface PatternResultWidgetModel {
    }

    interface PatternResultWidgetView extends IsWidget {
        Collection<PatternMeta> getSelection();

        void clearSelection();
    }
}
