package csi.client.gwt.viz.graph.tab.pattern.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.HeaderGroupConfig;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.tab.pattern.PatternMetaPropertiesAccess;
import csi.client.gwt.widget.gxt.grid.MultiPageCheckboxSelectionModel;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentFactory;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.business.visualization.graph.pattern.model.PatternMeta;
import csi.server.common.dto.graph.pattern.PatternResult;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.HasPatternCriteria;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PatternGrid {

    public static final int DEFAULT_COLUMN_WIDTH = 100;
    public static final int HEADER_HEIGHT = 18;
    private static final Cell<SafeHtml> safeHtmlCell = new SafeHtmlCell();
    private final PatternMetaPropertiesAccess patternMetaPropertiesAccess = GWT.create(PatternMetaPropertiesAccess.class);
    private ColumnModel<PatternMeta> columnModel;
    private ListStore<PatternMeta> resultStore;
    private MultiPageCheckboxSelectionModel<PatternMeta> selectionModel;
    private ResizeableGrid<PatternMeta> resultsGrid;
    private PatternResultSet result;

    public PatternGrid() {
        IdentityValueProvider<PatternMeta> identityValueProvider = new IdentityValueProvider<>();
        selectionModel = new MultiPageCheckboxSelectionModel<>(identityValueProvider, patternMetaPropertiesAccess.key());
        //FIXME: I need to do this on grid resize
        selectionModel.setHeaderHeight(HEADER_HEIGHT);

        List<ColumnConfig<PatternMeta, ?>> columnList = Lists.newArrayList();
        columnList.add(selectionModel.getColumn());
        columnModel = new ColumnModel<>(columnList);
        GridComponentFactory componentFactory = WebMain.injector.getGridFactory();
        GridComponentManager<PatternMeta> gridComponentManager = componentFactory.create(patternMetaPropertiesAccess.key());

        resultStore = gridComponentManager.getStore();
        buildResultsGrid();
    }

    public Grid<PatternMeta> provideGrid() {
        return resultsGrid;
    }

    private Grid<PatternMeta> buildResultsGrid() {
        resultsGrid = new ResizeableGrid<>(resultStore, columnModel);
        resultsGrid.getView().setStripeRows(true);
        resultsGrid.getView().setColumnLines(true);
        resultsGrid.setBorders(false);
        resultsGrid.setLoadMask(true);
        resultsGrid.setSelectionModel(selectionModel);
        return resultsGrid;
    }

    public List<PatternMeta> getSelectedItems() {
        return resultsGrid.getSelectionModel().getSelectedItems();
    }

    public void clearSelectedItems() {
        resultsGrid.getSelectionModel().deselectAll();
    }

    public void setPatterns(PatternResultSet result) {
        this.result = result;
        createColumns();
        loadStore();
    }

    private void loadStore() {
        resultStore.clear();
        Set<PatternResult> results = result.getResults();
        if (results == null) {
            return;
        }
        for (PatternResult patternResult : results) {
            PatternMeta patternMeta = new PatternMeta();
            patternMeta.setPattern(patternResult);
            resultStore.add(patternMeta);
        }
    }

    private void createColumns() {
        List<ColumnConfig<PatternMeta, ?>> columnList = Lists.newArrayList();
        if (result.getPattern() != null) {
            columnList.add(selectionModel.getColumn());
            createNodeColumns(columnList);
            createLinkColumns(columnList);
        }
        columnModel = new ColumnModel<>(columnList);
        createHeaderGroups();
        resultsGrid.reconfigure(resultStore, columnModel);
        resultsGrid.setAllowTextSelection(true);

    }

    private void createLinkColumns(List<ColumnConfig<PatternMeta, ?>> columnList) {
        ArrayList<PatternLink> patternLinks = Lists.newArrayList(result.getPattern().getPatternLinks());
        Collections.sort(patternLinks, new PatternLinkComparator());
        for (PatternLink patternLink : patternLinks) {
            if (!patternLink.showInResults()) {
                continue;
            }
            createLinkColumn(columnList, patternLink);
        }
    }

    private void createLinkColumn(List<ColumnConfig<PatternMeta, ?>> columnList, PatternLink patternLink) {
        ValueProvider<? super PatternMeta, String> vp = new LinkValueProvider(patternLink);
        ColumnConfig<PatternMeta, String> patternLinkColumn = new ColumnConfig<>(vp, 100, CentrifugeConstantsLocator.get().patternResult_labelColumnHeading());
        patternLinkColumn.setFixed(false);
        patternLinkColumn.setHideable(false);
        columnList.add(patternLinkColumn);
        for (PatternCriterion patternCriterion : patternLink.getCriteria()) {
            if (patternCriterion.isShowInResults() == false) {
                continue;
            }
            createCriteriaColumn(columnList, patternCriterion, patternLink);
        }
    }

    private void createNodeColumns(List<ColumnConfig<PatternMeta, ?>> columnList) {
        Set<PatternNode> patternNodes = result.getPattern().getPatternNodes();
        ArrayList<PatternNode> patternNodesList = Lists.newArrayList(patternNodes);
        Collections.sort(patternNodesList, new PatternNodeComparator());
        for (PatternNode patternNode : patternNodesList) {
            if (patternNode.showInResults() == false) {
                continue;
            }
            createNodeColumn(columnList, patternNode);
        }
    }

    private void createNodeColumn(List<ColumnConfig<PatternMeta, ?>> columnList, PatternNode patternNode) {
        ValueProvider<? super PatternMeta, String> vp = new NodeValueProvider(patternNode);
        ColumnConfig<PatternMeta, String> patternNodeColumn = new ColumnConfig<>(vp, DEFAULT_COLUMN_WIDTH, CentrifugeConstantsLocator.get().patternResult_labelColumnHeading());
        patternNodeColumn.setFixed(false);
        patternNodeColumn.setHideable(false);
        columnList.add(patternNodeColumn);
        for (PatternCriterion patternCriterion : patternNode.getCriteria()) {
            if (patternCriterion.isShowInResults() == false) {
                continue;
            }
            createCriteriaColumn(columnList, patternCriterion, patternNode);
        }
    }

    private void createHeaderGroups() {
        if (result.getPattern() != null) {
            int i = 1;
            i = createNodeHeaderGroups(i);
            createLinkHeaderGroups(i);
        }
    }

    private void createLinkHeaderGroups(int i) {
        ArrayList<PatternLink> patternLinks = Lists.newArrayList(result.getPattern().getPatternLinks());
        Collections.sort(patternLinks, new PatternLinkComparator());
        for (PatternLink patternLink : patternLinks) {
            if (patternLink.showInResults() == false) {
                continue;
            }
            i = createLinkHeaderGroup(i, patternLink);
        }
    }

    private int createLinkHeaderGroup(int i, PatternLink patternLink) {
        int size = 1;
        for (PatternCriterion patternCriterion : patternLink.getCriteria()) {
            if (patternCriterion.isShowInResults()) {
                size++;
            }

        }
        String name = patternLink.getName();
        if (Strings.isNullOrEmpty(name)) {
            name = "";
        }
        columnModel.addHeaderGroup(0, i, new HeaderGroupConfig(name, 1, size));
        i += size;
        return i;
    }

    private int createNodeHeaderGroups(int i) {
        GraphPattern pattern = result.getPattern();
        Set<PatternNode> patternNodes = pattern.getPatternNodes();
        ArrayList<PatternNode> patternNodesList = Lists.newArrayList(patternNodes);
        Collections.sort(patternNodesList, new PatternNodeComparator());
        for (PatternNode patternNode : patternNodesList) {
            if (patternNode.showInResults() == false) {
                continue;
            }
            i = createNodeHeaderGroup(i, patternNode);
        }
        return i;
    }

    private int createNodeHeaderGroup(int i, PatternNode patternNode) {
        int size = 1;
        for (PatternCriterion patternCriterion : patternNode.getCriteria()) {
            if (patternCriterion.isShowInResults()) {
                size++;
            }
        }
        String name = patternNode.getName();
        if (Strings.isNullOrEmpty(name)) {
            name = "";
        }
        columnModel.addHeaderGroup(0, i, new HeaderGroupConfig(name, 1, size));
        i += size;
        return i;
    }

    private void createCriteriaColumn(List<ColumnConfig<PatternMeta, ?>> columnList, final PatternCriterion criterion, final HasPatternCriteria patternItem) {
        ValueProvider<? super PatternMeta, SafeHtml> vps = new CriteriaValueProvider(patternItem, criterion);
        ColumnConfig<PatternMeta, SafeHtml> criteriaColumn = new ColumnConfig<>(vps, DEFAULT_COLUMN_WIDTH, criterion.getName());
        criteriaColumn.setCell(safeHtmlCell);
        criteriaColumn.setFixed(false);
        criteriaColumn.setHideable(false);
        columnList.add(criteriaColumn);
    }

    public void setLoading(boolean isLoading) {
        if (isLoading) {
            resultsGrid.mask(CentrifugeConstantsLocator.get().patternResult_loadingMask());
        } else {
            resultsGrid.unmask();
        }
    }

    private static class SafeHtmlCell extends AbstractCell<SafeHtml> {
        @Override
        public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
            sb.append(value);
        }
    }

    private static class PatternLinkComparator implements Comparator<PatternLink> {
        @Override
        public int compare(PatternLink o1, PatternLink o2) {
            if ((o1.getName() == null) && (o2.getName() == null)) {
                return 0;
            }
            return ComparisonChain.start().compare(o1.getName(), o2.getName()).result();
        }
    }

    private static class PatternNodeComparator implements Comparator<PatternNode> {

        @Override
        public int compare(PatternNode o1, PatternNode o2) {
            if ((o1.getName() == null) && (o2.getName() == null)) {
                return 0;
            }
            return ComparisonChain.start().compare(o1.getName(), o2.getName()).result();
        }
    }

    private class CriteriaValueProvider implements ValueProvider<PatternMeta, SafeHtml> {

        private final HasPatternCriteria patternItem;
        private final PatternCriterion criterion;

        public CriteriaValueProvider(HasPatternCriteria patternItem, PatternCriterion criterion) {
            this.patternItem = patternItem;
            this.criterion = criterion;
        }

        @Override
        public SafeHtml getValue(PatternMeta object) {
            String s = "";
            if (patternItem instanceof PatternNode) {
                s = object.getPattern().getPatternNodeMap().get(patternItem);
            }
            if (Strings.isNullOrEmpty(s)) {
                if (patternItem instanceof PatternLink) {
                    s = object.getPattern().getPatternLinkMap().get(patternItem);
                }
            }
            return result.getCriteriaValueMap().get(s, criterion);
        }

        @Override
        public void setValue(PatternMeta object, SafeHtml value) {
            //read-only
        }

        @Override
        public String getPath() {
            return null;
        }
    }

    private class LinkValueProvider implements ValueProvider<PatternMeta, String> {
        private final PatternLink patternLink;

        public LinkValueProvider(PatternLink patternLink) {
            this.patternLink = patternLink;
        }

        @Override
        public String getValue(PatternMeta object) {
            return result.getLabelMap().get(object.getPattern().getPatternLinkMap().get(patternLink));
        }

        @Override
        public void setValue(PatternMeta object, String value) {
            //read-only
        }

        @Override
        public String getPath() {
            return null;
        }
    }

    private class NodeValueProvider implements ValueProvider<PatternMeta, String> {
        private final PatternNode patternNode;

        private NodeValueProvider(PatternNode patternNode) {
            this.patternNode = patternNode;
        }

        @Override
        public String getValue(PatternMeta object) {
            return result.getLabelMap().get(object.getPattern().getPatternNodeMap().get(patternNode));
        }

        @Override
        public void setValue(PatternMeta object, String value) {
            //read-only
        }

        @Override
        public String getPath() {
            return null;
        }
    }
}
