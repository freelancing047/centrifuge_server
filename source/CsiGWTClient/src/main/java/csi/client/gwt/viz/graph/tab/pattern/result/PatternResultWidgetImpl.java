package csi.client.gwt.viz.graph.tab.pattern.result;

import java.util.Collection;
import java.util.List;

import com.github.gwtbootstrap.client.ui.SplitDropdownButton;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.tab.pattern.PatternTab;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettingsImpl;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.graph.pattern.model.PatternMeta;
import csi.server.common.dto.graph.gwt.PatternHighlightRequest;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.GraphActionServiceProtocol;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;

public class PatternResultWidgetImpl implements PatternResultWidget {
    private final PatternResultPanel view;
    private final PatternResultWidgetModelImpl model;
    private final PatternTab tab;
    private final Graph graph;
    private final PatternResultSet result;

    public PatternResultWidgetImpl(Graph graph, PatternTab patternTab) {
        this.graph = graph;
        tab = patternTab;
        view = new PatternResultPanel(this);
        model = new PatternResultWidgetModelImpl();
        result = new PatternResultSet();
    }

    public PatternResultWidgetImpl(Graph graph, PatternTab patternTab, PatternResultSet result) {
        this.graph = graph;
        this.result = result;
        tab = patternTab;
        view = new PatternResultPanel(this);
        model = new PatternResultWidgetModelImpl();
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    @Override
    public void setColor(ClientColorHelper.Color color) {
        model.setColor(color);
        view.setColor(color);
        if (view.autoHighlightCheckBox.getValue()) {
            view.onHighlight(null);
        }
    }

    @Override
    public void search() {
        PatternSettingsImpl patternSettings = new PatternSettingsImpl(tab);
        //        patternSettings.show();
        GraphPattern mostRecentPattern = tab.getMostRecentPattern();
        if (mostRecentPattern != null) {
            patternSettings.setEditPattern(mostRecentPattern);
        }
    }

    @Override
    public void showResults(PatternResultSet patternResultSet) {

    }

    @Override
    public PatternResultSet getResult() {
        return result;
    }

    @Override
    public void highlightPattern(List<PatternMeta> selectedPatterns) {
        VortexFuture<Void> highlightPatternFuture = WebMain.injector.getVortex().createFuture();

        List<PatternHighlightRequest> requests = Lists.newArrayList();
        for (PatternHighlightRequest patternHighlightRequest : tab.getHighlightRequests()) {
            requests.add(patternHighlightRequest);
        }
        highlightPatternFuture.execute(GraphActionServiceProtocol.class).highlightPatterns(graph.getUuid(), requests);
        graph.getGraphSurface().refresh(highlightPatternFuture);
    }

    @Override
    public void showOnly(List<PatternMeta> selectedItems) {
        VortexFuture<SelectionModel> selectVF = WebMain.injector.getVortex().createFuture();
        graph.getGraphSurface().refresh(selectVF);
        selectVF.execute(GraphActionServiceProtocol.class).showOnlyPatterns(graph.getUuid(), selectedItems);
    }

    @Override
    public void clearHighlight(List<PatternMeta> selectedPatterns) {
        VortexFuture<Void> highlightPatternFuture = WebMain.injector.getVortex().createFuture();
        List<PatternHighlightRequest> requests = Lists.newArrayList();
        for (PatternResultWidget patternResultWidget : tab.getView().getPatternResultWidgets()) {
            patternResultWidget.clearSelection();
        }
        highlightPatternFuture.execute(GraphActionServiceProtocol.class).highlightPatterns(graph.getUuid(), requests);
        graph.getGraphSurface().refresh(highlightPatternFuture);
    }

    @Override
    public void addSelect(List<PatternMeta> selectedPatterns, boolean selectNodes, boolean selectLinks) {
        VortexFuture<SelectionModel> selectVF = WebMain.injector.getVortex().createFuture();
        selectVF.execute(GraphActionServiceProtocol.class).addSelectPatterns(graph.getUuid(), selectedPatterns, selectNodes, selectLinks);
        graph.getGraphSurface().refresh(selectVF);
    }

    @Override
    public void select(List<PatternMeta> selectedPatterns, boolean selectNodes, boolean selectLinks) {
        VortexFuture<SelectionModel> selectVF = WebMain.injector.getVortex().createFuture();
        selectVF.execute(GraphActionServiceProtocol.class).selectPatterns(graph.getUuid(), selectedPatterns, selectNodes, selectLinks);
        graph.getGraphSurface().refresh(selectVF);
    }

    @Override
    public SplitDropdownButton getSearchButton() {
        return view.getSearchButton();
    }

    @Override
    public PatternHighlightRequest getPatternHighlightRequest() {
        PatternHighlightRequest patternHighlightRequest = new PatternHighlightRequest();
        patternHighlightRequest.setColor(String.valueOf(model.getColor().getIntColor()));
        Collection<PatternMeta> patternMetas = view.getSelection();
        patternHighlightRequest.addPatterns(patternMetas);
        return patternHighlightRequest;
    }

    @Override
    public void clearSelection() {
        view.clearSelection();
    }
}
