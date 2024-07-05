package csi.client.gwt.viz.graph.tab.pattern;

import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.SplitDropdownButton;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabLink;
import com.github.gwtbootstrap.client.ui.base.HtmlWidget;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.events.GraphEvent;
import csi.client.gwt.viz.graph.events.GraphEventHandler;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.tab.GraphTab;
import csi.client.gwt.viz.graph.tab.pattern.PatternTab.PatternTabActivity;
import csi.client.gwt.viz.graph.tab.pattern.PatternTab.PatternTabView;
import csi.client.gwt.viz.graph.tab.pattern.result.PatternResultWidget;
import csi.client.gwt.viz.graph.tab.pattern.result.PatternResultWidgetImpl;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.pattern.PatternResultSet;

public class PatternTabViewImpl implements PatternTabView {
    public static final int SEARCH_HISTORY_SIZE = 8;
    private static final PatternTabUiBinder uiBinder = (PatternTabUiBinder) GWT.create(PatternTabUiBinder.class);
    @UiField
    GraphTab tab;
    @UiField
    CardLayoutContainer cardContainer;

    private PatternTabActivity activity;
    private HandlerRegistration uncolorHandlerRegistration;
    private PatternTab patternTab;
    private BiMap<NavLink, PatternResultSet> navlinkToResultMap = HashBiMap.create(16);
    private List<PatternResultSet> recentResults = Lists.newArrayList();
    private Map<PatternResultSet, PatternResultWidget> prwMap = Maps.newHashMap();
    private Icon spinner;
    private int spinnerDepth = 0;

    public PatternTabViewImpl(final Graph graph, final PatternTab patternsTab) {
        this.patternTab = patternsTab;
        Preconditions.checkNotNull(graph);

        this.tab = (GraphTab) uiBinder.createAndBindUi(this);

        graph.addGraphEventHandler(GraphEvents.GRAPH_LOAD_COMPLETE, new GraphEventHandler() {
            public void onGraphEvent(GraphEvent event) {
                //Fixme: For now I will add a result panel with no results
                cardContainer.add(new PatternResultWidgetImpl(graph, patternsTab));
            }
        });

        this.tab.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                graph.showControlBar(false);
            }
        });

        TabLink tabLink = tab.asTabLink();
        HtmlWidget w = new HtmlWidget("sup", "&nbsp;" + CentrifugeConstantsLocator.get().patternTab_betaTag());
        tabLink.add(w);
    }

    private void uncolorTab() {
        Element element = tab.asTabLink().getAnchor().getElement();
        element.getStyle().setBackgroundColor("transparent");//NON-NLS
        if (uncolorHandlerRegistration != null) {
            uncolorHandlerRegistration.removeHandler();
        }
        setTabTitle(null);
    }

    private void colorTab(String color) {
        Element element = tab.asTabLink().getAnchor().getElement();
        element.getStyle().setBackgroundColor(color);
    }

    public Tab getTab() {
        return this.tab;
    }

    public void bind(PatternTab.PatternTabActivity activity) {
        this.activity = activity;
    }

    @Override
    public void setLoading(VortexFuture future) {
        if (spinner == null) {
            spinner = new Icon(IconType.SPINNER);
            spinner.setSpin(true);
            tab.asTabLink().add(spinner);
        }
        spinnerDepth++;
        future.addEventHandler(new AbstractVortexEventHandler() {
            @Override
            public void onSuccess(Object result) {
                maybeRemoveSpinner();
            }
        });
    }

    private void maybeRemoveSpinner() {
        spinnerDepth--;
        if (spinnerDepth < 1) {
            spinnerDepth = 0;
            if(spinner != null) {
                try {
                    spinner.removeFromParent();
                    spinner = null;
                }catch (Exception ignored){
                    //should just continue.
                }
            }
        }
    }

    @Override
    public void setResults(PatternResultWidget patternResultWidget, PatternResultSet result) {
        prwMap.put(result, patternResultWidget);
        cardContainer.add(patternResultWidget);
        cardContainer.setActiveWidget(patternResultWidget);
        createResultsNavigation(result);
        {//All this stuff has to do with coloring the tab.
            //Might not need this.
            colorTab("DarkSeaGreen"); //NON-NLS
            setTabTitle(CentrifugeConstantsLocator.get().patternTab_searchSuccessMessage());
            parseResultMessage(result);
            addTabColorClearingHandler(patternResultWidget);
        }
        final SplitDropdownButton searchButton = patternResultWidget.getSearchButton();
        searchButton.getTriggerWidget().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                searchButton.getMenuWiget().clear();
                List<PatternResultSet> resultSets = Lists.reverse(recentResults);
                for (PatternResultSet recentResult : resultSets) {
                    NavLink navLink = navlinkToResultMap.inverse().get(recentResult);
                    searchButton.add(navLink);
                    int gridHeight = cardContainer.getOffsetHeight();
                    UnorderedList menuWidget = searchButton.getMenuWiget();
                    Style menuStyle = menuWidget.getElement().getStyle();
                    //FIXME:MAGIC NUMBER
                    menuStyle.setProperty("maxHeight", gridHeight - 45, Style.Unit.PX); //NON-NLS
                    menuStyle.setOverflowY(Style.Overflow.AUTO);
                    //FIXME: add items to list
                    //FIXME: style current resultset
                    //navLink.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
                }
            }
        });
    }

    @Override
    public List<PatternResultWidget> getPatternResultWidgets() {
        return Lists.newArrayList(prwMap.values());
    }

    @Override
    public void clearResults() {
        spinnerDepth = -1;
        maybeRemoveSpinner();
        cardContainer.clear();
        navlinkToResultMap.clear();
        prwMap.clear();
        recentResults.clear();
        uncolorTab();
        cardContainer.add(new PatternResultWidgetImpl(patternTab.getGraph(), patternTab));
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                cardContainer.forceLayout();
            }
        });
    }

    private void addTabColorClearingHandler(PatternResultWidget result) {
        uncolorHandlerRegistration = result.asWidget().addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                uncolorTab();
                event.stopPropagation();
            }
        }, ClickEvent.getType());
    }

    private void createResultsNavigation(final PatternResultSet result) {
        if (result.getPattern() != null) {
            String label = result.getPattern().getName() + " - " + result.getResults().size() + "";

            if (result.getNotice() != null) {

                switch (result.getNotice()) {

                    case PERMUTATION_LIMIT_REACHED:
                        label += "+";
                        break;
                    case COMBINATION_LIMIT_REACHED:
                        label += "+";
                        break;
                    case NEO4J_UNREACHABLE:
                        label += "!";
                        break;
                    //FIXME: need notice for timeout.
                    case ERROR:
                        label += "!";
                        break;
                }
            }
            NavLink navLink = new NavLink(label);
            navLink.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    PatternResultWidget patternResultWidget = prwMap.get(result);
                    cardContainer.setActiveWidget(patternResultWidget);
                }
            });
            navlinkToResultMap.put(navLink, result);
            recentResults.add(result);
            int size = recentResults.size();
            if (size > SEARCH_HISTORY_SIZE) {
                evictResult();
            }
        }

    }

    private void evictResult() {
        PatternResultSet patternResultSet = recentResults.get(0);
        recentResults.remove(patternResultSet);
        navlinkToResultMap.inverse().remove(patternResultSet);
        PatternResultWidget patternResultWidget = prwMap.remove(patternResultSet);
        cardContainer.remove(patternResultWidget);
    }

    private void parseResultMessage(PatternResultSet result) {
        if (result.getNotice() != null) {
            switch (result.getNotice()) {
                case PERMUTATION_LIMIT_REACHED:
                    colorTab("red");//NON-NLS
                    setTabTitle(CentrifugeConstantsLocator.get().patternTab_permutationLimitMessage());
                    break;
                case COMBINATION_LIMIT_REACHED:
                    colorTab("orange");//NON-NLS
                    setTabTitle(CentrifugeConstantsLocator.get().patternTab_combinationLimitMessage());
                    break;
                case NEO4J_UNREACHABLE:
                    colorTab("pink");//NON-NLS
                    setTabTitle(CentrifugeConstantsLocator.get().patternTab_noSearchService());
                    break;
                case ERROR:
                    colorTab("pink");//NON-NLS
                    setTabTitle(CentrifugeConstantsLocator.get().patternTab_otherError());
                    break;
                case NOOP:
                    uncolorTab();
                    setTabTitle(CentrifugeConstantsLocator.get().patternTab_searchSuccessMessage());
                    break;
                default:
                    colorTab("DarkSeaGreen");//NON-NLS
                    setTabTitle(CentrifugeConstantsLocator.get().patternTab_searchSuccessMessage());
                    //TODO: create notice for OK
                    break;
            }
        }
    }

    private void setTabTitle(String s) {
        tab.asTabLink().getAnchor().setTitle(s);
    }

    @SuppressWarnings("WeakerAccess")
    interface PatternTabUiBinder extends UiBinder<GraphTab, PatternTabViewImpl> {
    }
}
