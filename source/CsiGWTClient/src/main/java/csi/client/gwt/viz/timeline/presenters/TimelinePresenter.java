/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.viz.timeline.presenters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.base.ProgressBarBase;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.events.FindEvent;
import csi.client.gwt.events.FindEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.matrix.MatrixMetricsView;
import csi.client.gwt.viz.matrix.UpdateEvent;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.shared.chrome.VizChrome;
import csi.client.gwt.viz.shared.chrome.panel.RenderSizeChangeEvent;
import csi.client.gwt.viz.shared.chrome.panel.RenderSizeChangeEventHandler;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.viz.shared.filter.FilterCapableVisualizationPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.viz.shared.menu.SelectionOnlyOnServer;
import csi.client.gwt.viz.shared.search.Searchable;
import csi.client.gwt.viz.timeline.NaturalComparator;
import csi.client.gwt.viz.timeline.events.CrumbRemovedEvent;
import csi.client.gwt.viz.timeline.events.CrumbRemovedEventHandler;
import csi.client.gwt.viz.timeline.events.OverviewChangeEvent;
import csi.client.gwt.viz.timeline.events.OverviewChangeEventHandler;
import csi.client.gwt.viz.timeline.events.RangeSelectionEvent;
import csi.client.gwt.viz.timeline.events.RangeSelectionEventHandler;
import csi.client.gwt.viz.timeline.events.RenderFooterEvent;
import csi.client.gwt.viz.timeline.events.RenderFooterEventHandler;
import csi.client.gwt.viz.timeline.events.ResizeEvent;
import csi.client.gwt.viz.timeline.events.ResizeEventHandler;
import csi.client.gwt.viz.timeline.events.ScrollEvent;
import csi.client.gwt.viz.timeline.events.ScrollEventHandler;
import csi.client.gwt.viz.timeline.events.SelectionChangeEvent;
import csi.client.gwt.viz.timeline.events.SelectionChangeEventHandler;
import csi.client.gwt.viz.timeline.events.TimeScaleChangeEvent;
import csi.client.gwt.viz.timeline.events.TimeScaleChangeEventHandler;
import csi.client.gwt.viz.timeline.events.TooltipEvent;
import csi.client.gwt.viz.timeline.events.TooltipEventHandler;
import csi.client.gwt.viz.timeline.events.TrackChangeEvent;
import csi.client.gwt.viz.timeline.events.TrackChangeEventHandler;
import csi.client.gwt.viz.timeline.events.TrackDrillEvent;
import csi.client.gwt.viz.timeline.events.TrackDrillEventHandler;
import csi.client.gwt.viz.timeline.events.TrackSelectionEvent;
import csi.client.gwt.viz.timeline.events.TrackSelectionEventHandler;
import csi.client.gwt.viz.timeline.events.TrackingCompleteEvent;
import csi.client.gwt.viz.timeline.events.TrackingCompleteEventHandler;
import csi.client.gwt.viz.timeline.events.ZoomEvent;
import csi.client.gwt.viz.timeline.events.ZoomEventHandler;
import csi.client.gwt.viz.timeline.menu.TimelineMenuManager;
import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.DetailedEventProxy;
import csi.client.gwt.viz.timeline.model.GroupingItem;
import csi.client.gwt.viz.timeline.model.Interval;
import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.model.TimelineTrackModel;
import csi.client.gwt.viz.timeline.model.ViewPort;
import csi.client.gwt.viz.timeline.presenters.legend.TimelineColorLegendPresenter;
import csi.client.gwt.viz.timeline.presenters.legend.TimelineGroupLegendPresenter;
import csi.client.gwt.viz.timeline.view.EmptyView;
import csi.client.gwt.viz.timeline.view.SearchBox;
import csi.client.gwt.viz.timeline.view.TimelineView;
import csi.client.gwt.viz.timeline.view.TimelineView.ViewMode;
import csi.client.gwt.viz.timeline.view.drawing.DetailedEventRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TooltipRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TrackRenderable;
import csi.client.gwt.viz.timeline.view.metrics.TimelineMetricsView;
import csi.client.gwt.viz.timeline.view.summary.drawing.SummaryEventRenderable;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.server.common.model.visualization.timeline.TimelineCachedState;
import csi.server.common.model.visualization.timeline.TimelineField;
import csi.server.common.model.visualization.timeline.TimelineLegendDefinition;
import csi.server.common.model.visualization.timeline.TimelineSettings;
import csi.server.common.model.visualization.timeline.TimelineTrackState;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.common.service.api.ChronosActionsServiceProtocol;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.util.IntCollection;
import csi.shared.core.visualization.map.MetricsDTO;
import csi.shared.core.visualization.timeline.CommonTrack;
import csi.shared.core.visualization.timeline.DetailedTimelineResult;
import csi.shared.core.visualization.timeline.MeasuredTimelineResult;
import csi.shared.core.visualization.timeline.MeasuredTrack;
import csi.shared.core.visualization.timeline.SingularTimelineEvent;
import csi.shared.core.visualization.timeline.SummarizedTimelineEvent;
import csi.shared.core.visualization.timeline.SummaryTimelineResult;
import csi.shared.core.visualization.timeline.TimelineResult;
import csi.shared.core.visualization.timeline.TimelineTrack;
import csi.shared.core.visualization.timeline.Tooltip;
import csi.shared.gwt.viz.timeline.TimeUnit;
import csi.shared.gwt.viz.timeline.TimelineRequest;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TimelinePresenter extends AbstractVisualizationPresenter<TimelineViewDef, FullSizeLayoutPanel> implements
        FilterCapableVisualizationPresenter, Searchable, SelectionOnlyOnServer {


    private static final int PERSIST_DELAY = 1000;

    static final int MAX_DOT_SIZE = 7;

    private int limit;


    public enum Layout {TIGHT, LOOSE, GRAPH}

    private Alert progressIndicator;

    private ProgressBar progressBar;

    private TimelineView timelineView;
    private EmptyView emptyView = new EmptyView(this);

    private String identifier;

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private HashMap<String, CommonTrack> trackModels = new HashMap<String, CommonTrack>();


    private EventBus eventBus = GWT.create(SimpleEventBus.class);

    private TimelineColorLegendPresenter legendPresenter;
    private TimelineGroupLegendPresenter groupingPresenter;

    private TrackSelectionEventHandler trackSelectionEventHandler;
    private TimeScaleChangeEventHandler timeScaleChangeEventHandler;
    private CrumbRemovedEventHandler crumbRemovedEventHandler;
    private OverviewChangeEventHandler overviewChangeEventHandler;
    private RangeSelectionEventHandler rangeSelectionEventHandler;
    private RangeSelectionEventHandler rangeDeselectionEventHandler;
    private ResizeEventHandler resizeHandler;
    private ZoomEventHandler zoomHandler;
    private TooltipEventHandler tooltipHandler;
    private TrackingCompleteEventHandler trackingCompleteHandler;

    private long initialStartTime;

    private long initialEndTime;

    private ScrollEventHandler scrollHandler;

    private ContentPanel legend;
    private ContentPanel groupPanel;

    private Map<String, Integer> colors = new HashMap<String, Integer>();

    private SearchBox searchBox;

    private String oldText = null;

    public int currentHit = 0;
    private int searchHits = 0;

    private Double trackTotalHeight = null;

    private boolean showWarningDialog = false;

    private RepeatingCommand scrollSaveCommand;

    private RepeatingCommand extentSaveCommand;

    private RepeatingCommand legendSaveCommand;

    public SortOrder sort = null;

    protected boolean updateSort = true;

    private Interval interval = null;

    private int lastWidth = -1;

    private int lastHeight = -1;

    private MeasureTimelinePresenter measurePresenter = new MeasureTimelinePresenter();
    private DetailedTimelinePresenter detailedPresenter = new DetailedTimelinePresenter();
    private TimelineEmptyPresenter emptyPresenter = new TimelineEmptyPresenter();
    private SummaryTimelinePresenter summaryPresenter = new SummaryTimelinePresenter();

    private ViewMode mode = ViewMode.EMPTY;

    private TimeUnit summaryLevel;

    private SelectionChangeEventHandler selectionChangeEventHandler;

    private int eventCount;


    public TimelinePresenter(AbstractDataViewPresenter dvPresenterIn, TimelineViewDef visualizationDef) {
        super(dvPresenterIn, visualizationDef);
        limit = WebMain.getClientStartupInfo().getTimelineTypeLimit();
        addHandlers();
        this.legendPresenter = new TimelineColorLegendPresenter(this);
        legendPresenter.createLegend(visualizationDef.isReadOnly());

        groupingPresenter = new TimelineGroupLegendPresenter(this);
        groupingPresenter.create();

        detailedPresenter.setEventBus(eventBus);
        measurePresenter.setEventBus(eventBus);

        addFindHandler();
        addTrackDrillHandler();
        addRenderFooterHandler();
    }

    private void addTrackDrillHandler() {
        eventBus.addHandler(TrackDrillEvent.type, new TrackDrillEventHandler() {

            @Override
            public void onDrill(TrackDrillEvent event) {
                TimelinePresenter.this.drillToCategory(event.getTrackName());
            }
        });
    }
    private void addRenderFooterHandler() {
        eventBus.addHandler(RenderFooterEvent.type, new RenderFooterEventHandler() {

            @Override
            public void onRender(RenderFooterEvent event) {
                getCurrentChildPresenter().renderFooter();
            }
        });
    }

    private void drillToCategory(String trackName) {
        getCurrentChildPresenter().cancel();

        measurePresenter.rememberTrackStates(trackModels);
        if (timelineView != null) {
            getView();
        }

        trackTotalHeight = null;
        showProgressIndicator();

        VortexFuture<TimelineResult> future = getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<TimelineResult>() {
            @Override
            public void onSuccess(final TimelineResult result) {
                handleServerResult(result);
//                showLegend();
            }

            @Override
            public boolean onError(Throwable t) {
                hideProgressIndicator();
                displayLoadingError(t);
                return false;
            }
        });

        Scheduler.get().scheduleDeferred(() -> {
            try {
                getVisualizationDef().getState().setFocusedTrack(trackName);
                TimelineRequest request = buildTimelineRequest();
                request.setCalculateOverview(true);
                future.execute(ChronosActionsServiceProtocol.class).loadTimeline(request);
            } catch (CentrifugeException ignored) {
            }
        });
    }

    private void addFindHandler() {
        eventBus.addHandler(FindEvent.type, new FindEventHandler(this) {

            @Override
            protected void find() {

                ((TimelinePresenter) getPresenter()).showFind();

            }
        });
    }

    public void showFind() {

        if(searchBox == null) {
            return;
        }
        if (oldText != null) {
            searchBox.setText(oldText);
            oldText = null;
        }
        getMenuManager().enable(MenuKey.HIDE_SEARCH);
        getMenuManager().hide(MenuKey.SHOW_SEARCH);
        searchBox.setVisible(true);
        searchBox.focus();
        searchText(searchBox.getText());
    }

    public void hideFind() {

        if ((searchBox != null) && (getMenuManager() != null)) {
            getMenuManager().hide(MenuKey.HIDE_SEARCH);
            getMenuManager().enable(MenuKey.SHOW_SEARCH);
            oldText = searchBox.getText();
            searchBox.setText("");
            searchBox.setVisible(false);
            searchText(searchBox.getText());
        }

    }

    private ClickHandler closeSearchHandler = new ClickHandler(){

        @Override
        public void onClick(ClickEvent event) {
            hideFind();
            adjustGroups();
        }
     };


     private DetailedEventProxy searchEvent = null;
     private String searchTrackName = null;

    private ClickHandler rightSearchHandler = new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                findNext(searchEvent, searchTrackName);

                if((searchEvent  != null) && (searchEvent.getTrack() != null) && searchEvent.getTrack().isCollapsed()){
                    searchEvent.getTrack().setCollapsed(false);
                    adjustGroups();
                }

                scrollToNextEvent(searchEvent);
                panToNextEvent(searchEvent);
            }};

    private ClickHandler leftSearchHandler = new ClickHandler(){

        @Override
        public void onClick(ClickEvent event) {
            findPrevious(searchEvent, searchTrackName);

            if((searchEvent  != null) && (searchEvent.getTrack() != null) && searchEvent.getTrack().isCollapsed()){
                searchEvent.getTrack().setCollapsed(false);
                adjustGroups();
            }
            scrollToPreviousEvent(searchEvent);
            panToPreviousEvent(searchEvent);
        }};


    @Override
    public void setChrome(VizChrome vizChrome) {
        super.setChrome(vizChrome);
        createSearchWindow();
    }

    private void createSearchWindow() {
        Scheduler.get().scheduleDeferred(() -> {
            if (searchBox == null) {
                searchBox = new SearchBox(TimelinePresenter.this);
                getChrome().addSearchBox(searchBox);
                searchBox.addCloseClickHandler(closeSearchHandler);
                searchBox.addLeftButtonClickHandler(leftSearchHandler );
                searchBox.addRightButtonClickHandler(rightSearchHandler);
                //getMenuManager().hide(MenuKey.HIDE_SEARCH);
            }
            hideFind();
        });
    }

    public void searchText(String text) {
        currentHit = 0;
        text = text.toUpperCase();

        int lastHits = searchHits;
        searchHits = getCurrentChildPresenter().getSearchHitCount(text);

        searchBox.updateCount(currentHit, searchHits);
        if(((lastHits != 0) || (searchHits != 0))) {
         getTimelineView().redraw();
      }
    }

    public void findNext(DetailedEventProxy eventProxy, String trackName) {
        if (searchHits == 0) {
            return;
        }
        //This will actually loop around the entire list of tracks in a circle.
        Set<String> keys = trackModels.keySet();
        List<String> list = asSortedList(keys, sort);
        removeHiddenTracks(list);

        if (list.isEmpty()) {
            return;
        }

        if ((trackName == null) || !list.contains(trackName)) {
            trackName = list.get(0);
        }
        trackName = trackName.trim();
        CommonTrack track = trackModels.get(trackName);
        DetailedEventProxy nextHit = null;

        int trackIndex = list.indexOf(trackName);
        if(!(track instanceof TimelineTrackModel)) {
            return;
        }
        List<DetailedEventProxy> events = ((TimelineTrackModel) track).getEvents();

        int ii;

        if (eventProxy == null) {
            ii = 0;
        } else {
            ii = events.indexOf(eventProxy);
            ii++;
            if (ii >= events.size()) {
                ii = 0;
                trackIndex++;
            }
            eventProxy.setSearchHighlight(false);
        }


        int tries = list.size() + 1;
        while (tries > 0) {
            if (trackIndex >= list.size()) {
                trackIndex = 0;
                currentHit = 0;
            }
            track = trackModels.get(list.get(trackIndex));
            events = ((TimelineTrackModel) track).getEvents();
            for (; ii < events.size(); ii++) {
                if (events.get(ii).isSearchHit()) {
                    nextHit = events.get(ii);
                    trackName = track.getLabel();
                    break;
                }
            }
            if (nextHit != null) {
                break;
            }
            ii = 0;
            trackIndex++;
            tries--;
        }

        if (nextHit == null) {
            return; //No hits
        }

        searchEvent = (nextHit);
        searchTrackName = (trackName);

        nextHit.setSearchHighlight(true);
        currentHit++;
        if (currentHit > searchHits) {
            currentHit = 1;
        }
        searchBox.updateCount(currentHit, searchHits);
        getTimelineView().redraw();
    }

    void removeHiddenTracks(List<String> list) {
        List<String> toBeRemoved = trackModels.keySet().stream().filter(key -> !trackModels.get(key).isVisible()).collect(Collectors.toList());
        list.removeAll(toBeRemoved);
    }

    public void findPrevious(DetailedEventProxy eventProxy, String trackName) {

        if (searchHits == 0) {
            return;
        }
        //This will actually loop around the entire list of tracks in a circle.
        Set<String> keys = trackModels.keySet();
        List<String> list = asSortedList(keys, sort);
        removeHiddenTracks(list);

        if (list.isEmpty()) {
            return;
        }

        if ((trackName == null) || !list.contains(trackName)) {
            trackName = list.get(list.size() - 1);
        }

        trackName = trackName.trim();
        CommonTrack track = trackModels.get(trackName);
        DetailedEventProxy nextHit = null;

        int trackIndex = list.indexOf(trackName);
        if(!(track instanceof TimelineTrackModel)) {
            return;
        }
        List<DetailedEventProxy> events = ((TimelineTrackModel) track).getEvents();

        int ii;

        if (eventProxy == null) {
            ii = events.size() - 1;
        } else {
            ii = events.indexOf(eventProxy);
            ii--;

            if (ii < 0) {
                trackIndex--;
            }
            eventProxy.setSearchHighlight(false);
        }

        int tries = list.size() + 1;
        while (tries > 0) {
            if (trackIndex < 0) {
                currentHit = searchHits;
                trackIndex = list.size() - 1;

            }
            track = trackModels.get(list.get(trackIndex));
            if(!(track instanceof TimelineTrackModel)) {
                return;
            } else {
                events = ((TimelineTrackModel) track).getEvents();
                if (ii < 0) {
                    ii = events.size() - 1;
                }
                for (; ii >= 0; ii--) {
                    if (events.get(ii).isSearchHit()) {
                        nextHit = events.get(ii);
                        trackName = track.getLabel();
                        break;
                    }
                }
                if (nextHit != null) {
                    break;
                }
                ii = -1;
                trackIndex--;
                tries--;
            }

        }

        if (nextHit == null) {
            return; //No hits
        }

        searchEvent = (nextHit);
        searchTrackName = (trackName);

        nextHit.setSearchHighlight(true);
        currentHit--;
        if (currentHit <= 0) {
            currentHit = searchHits;
        }
        searchBox.updateCount(currentHit, searchHits);
        getTimelineView().redraw();
    }

    void addLegend() {
        Scheduler.get().scheduleDeferred(() -> {
            if (legend == null) {
                legend = legendPresenter.getDisplay();
                legend.setHeight(150);
                legend.setWidth(VizPanel.DEFAULT_WINDOW_SIZE);
                legend.addStyleName("overlay-clear");
                legend.setBodyStyle("pad-text");
                legend.setCollapsible(true);
                getChrome().addWindow(legend);
                final Widget parent = legend.getParent();
                //FIXME: I don't like this approach...
                chrome.getEventBus().addHandler(RenderSizeChangeEvent.type, new RenderSizeChangeEventHandler() {
                    @Override
                    public void onAttach(RenderSizeChangeEvent event) {
                        ((AbsolutePanel) parent).setWidgetPosition(legend, parent.getElement().getOffsetWidth() - 90 - legend.getOffsetWidth(), 35);
                    }
                });
                Scheduler.get().scheduleFixedDelay(() -> {
                    chrome.getEventBus().addHandler(VizPanel.ChromeResizeEvent.TYPE, new VizPanel.ChromeResizeEventHandler() {
                        @Override
                        public void onChromeResize() {
                            int oldLeft = legend.getAbsoluteLeft() - parent.getAbsoluteLeft();
                            VBoxLayoutContainer buttonGroupContainer = ((VizPanel) chrome).getButtonGroupContainer();
                            if (buttonGroupContainer != null) {
                                int oldWidth = (buttonGroupContainer.getAbsoluteLeft() - parent.getAbsoluteLeft()) + buttonGroupContainer.getOffsetWidth();
                                if (oldWidth > 0) {
                                    int left = oldLeft;
                                    left += (parent.getOffsetWidth() - oldWidth - 50);
                                    if (left < 0) {
                                        left = 0;
                                    }
                                    int top = legend.getAbsoluteTop() - parent.getAbsoluteTop();
                                    ((AbsolutePanel) parent).setWidgetPosition(legend, left, top);
                                }
                            }
                        }
                    });
                    return false;
                }, 1000);
                resetLegend();
            }
        });
    }

    void addGroups() {
        Scheduler.get().scheduleDeferred(() -> {
            if (groupPanel == null) {
                groupPanel = groupingPresenter.getDisplay();
                groupPanel.setHeight(150);
                groupPanel.setWidth(VizPanel.DEFAULT_WINDOW_SIZE);
                groupPanel.addStyleName("overlay-clear");
                groupPanel.setBodyStyle("pad-text");
                groupPanel.setCollapsible(true);
                getChrome().addWindow(groupPanel);
                groupPanel.setVisible(false);
                final Widget parent = groupPanel.getParent();
                //FIXME: I don't like this approach...
                chrome.getEventBus().addHandler(RenderSizeChangeEvent.type, new RenderSizeChangeEventHandler() {
                    @Override
                    public void onAttach(RenderSizeChangeEvent event) {
                        ((AbsolutePanel) parent).setWidgetPosition(groupPanel, 35, 35);
                    }
                });
                Scheduler.get().scheduleFixedDelay(() -> {
                    chrome.getEventBus().addHandler(VizPanel.ChromeResizeEvent.TYPE, new VizPanel.ChromeResizeEventHandler() {
                        @Override
                        public void onChromeResize() {
                            int oldLeft = groupPanel.getAbsoluteLeft() - parent.getAbsoluteLeft();
                            VBoxLayoutContainer buttonGroupContainer = ((VizPanel) chrome).getButtonGroupContainer();
                            if (buttonGroupContainer != null) {
                                int oldWidth = (buttonGroupContainer.getAbsoluteLeft() - parent.getAbsoluteLeft()) + buttonGroupContainer.getOffsetWidth();
                                if (oldWidth > 0) {
                                    int left = oldLeft;
                                    left += (parent.getOffsetWidth() - oldWidth - 50);
                                    if (left < 0) {
                                        left = 0;
                                    }
                                    int top = groupPanel.getAbsoluteTop() - parent.getAbsoluteTop();
                                    if (top < 0) {
                                        top = 15;
                                    }
                                    ((AbsolutePanel) parent).setWidgetPosition(groupPanel, left, top);
                                }
                            }
                        }
                    });
                    return false;
                }, 1000);
                resetGroup();
            }
            updateGroupLegendItems();
        });
    }

    void updateGroupLegendItems() {
        if ((trackModels == null) || trackModels.isEmpty()) {
            getMenuManager().hide(MenuKey.HIDE_GROUPS);
            getMenuManager().hide(MenuKey.SHOW_GROUPS);
            groupingPresenter.hide();
            return;
        }
        if (trackModels.size() == 1) {
            groupingPresenter.hide();
            getMenuManager().enable(MenuKey.SHOW_GROUPS);
            getMenuManager().hide(MenuKey.HIDE_GROUPS);
        } else {
            groupingPresenter.show(true);
        }
        //Probably should store this list instead of re-doing sort every time
        List<String> list = asSortedList(trackModels.keySet(), sort);
        if (list.size() > limit) {
            list = list.subList(0, limit);
        }
        groupingPresenter.clear();
        for (String key : list) {
            if ((key != null) && !key.equals(TimelineTrack.NULL_TRACK)) {
                GroupingItem group = new GroupingItem(trackModels.get(key).isVisible(), key);
                groupingPresenter.addGroup(group);
            }
        }
    }

    public void resetLegend() {
        Scheduler.get().scheduleDeferred(this::resetLegendHard);
    }

    private void resetLegendHard() {
        if(legend == null) {
            return;
        }
        legend.getElement().getStyle().setVisibility(Visibility.VISIBLE);
        final Widget parent = legend.getParent();
        int offset = VizPanel.DEFAULT_WINDOW_SIZE;
        if (legend.getOffsetWidth() > offset) {
            offset = legend.getOffsetWidth();
        }
        if (parent != null) {
            ((AbsolutePanel) parent).setWidgetPosition(legend, parent.getElement().getOffsetWidth() - 90 - offset, 35);
        }
        legend.getElement().getStyle().setPosition(Position.ABSOLUTE);
    }

    public void resetGroup() {
        Scheduler.get().scheduleDeferred(this::resetGroupHard);
    }

    private void resetGroupHard() {
        if(groupPanel == null) {
            return;
        }
        groupPanel.getElement().getStyle().setVisibility(Visibility.VISIBLE);
        final Widget parent = groupPanel.getParent();
        int offset = VizPanel.DEFAULT_WINDOW_SIZE;
        if (groupPanel.getOffsetWidth() > offset) {
            offset = groupPanel.getOffsetWidth();
        }
        if (parent != null) {
            ((AbsolutePanel) parent).setWidgetPosition(groupPanel, 35, 35);
        }
        groupPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
    }

    private void updateScrollSettings(final int start) {
        RepeatingCommand scrollSaveCommand = new RepeatingCommand() {
            @Override
            public boolean execute() {
                //this way we only save the latest request
                if (this.equals(getCurrentScrollSave())) {
                    if (getVisualizationDef().getState() == null) {
                        TimelineCachedState cachedState = new TimelineCachedState();
                        cachedState.setTrackStates(new HashSet<>());
                        getVisualizationDef().setState(cachedState);
                    }
                    getVisualizationDef().getState().setScrollPosition(start);
                    //saveSettings(false);
                }
                return false;
            }
        };
        setCurrentScrollSave(scrollSaveCommand);
        Scheduler.get().scheduleFixedDelay(scrollSaveCommand, PERSIST_DELAY);
    }

    private void setCurrentScrollSave(RepeatingCommand scrollSaveCommand) {
        this.scrollSaveCommand = scrollSaveCommand;
    }

    private RepeatingCommand getCurrentScrollSave() {
        return this.scrollSaveCommand;
    }

    private void updateTimeSettings(final long start, final long end) {
        RepeatingCommand extentSaveCommand = new RepeatingCommand() {
            @Override
            public boolean execute() {
                //this way we only save the latest request
                if (this.equals(getCurrentExtentSave())) {
                    if (getVisualizationDef().getState() == null) {
                        getVisualizationDef().setState(new TimelineCachedState());
                    }
                    getVisualizationDef().getState().setStartPosition(start);
                    getVisualizationDef().getState().setEndPosition(end);
                    MatrixMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
                    //saveSettings(false);
                }
                return false;
            }
        };
        setCurrentExtentSave(extentSaveCommand);
        Scheduler.get().scheduleFixedDelay(extentSaveCommand, PERSIST_DELAY);
    }

    private void setCurrentExtentSave(RepeatingCommand extentSaveCommand ) {
        this.extentSaveCommand = extentSaveCommand;
    }

    private RepeatingCommand getCurrentExtentSave() {
        return this.extentSaveCommand;
    }

    private void addHandlers() {
        TrackChangeEventHandler trackChangeHandler = new TrackChangeEventHandler() {
            @Override
            public void onChange(TrackChangeEvent event) {
                CommonTrack trackModel = event.getTrackModel();
                getCurrentChildPresenter().tracksChanged();
                boolean exists = false;
                Set<TimelineTrackState> states = null;
                if (getVisualizationDef().getState() != null) {
                    states = getVisualizationDef().getState().getTrackStates();
                    for (TimelineTrackState state : states) {
                        if (state.getTrackName().equals(trackModel.getLabel())) {
                            state.setCollapse(trackModel.isCollapsed());
                            state.setVisible(trackModel.isVisible());
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    TimelineTrackState state = new TimelineTrackState();
                    state.setCollapse(trackModel.isCollapsed());
                    state.setVisible(trackModel.isVisible());
                    state.setTrackName(trackModel.getLabel());
                    if (getVisualizationDef().getState() == null) {
                        TimelineCachedState cachedState = new TimelineCachedState();
                        states = new HashSet<>();
                        cachedState.setTrackStates(states);
                        getVisualizationDef().setState(cachedState);
                    }
                    states.add(state);
                }
                hideProgressIndicator();
                //saveSettings(false);
            }
        };

        selectionChangeEventHandler = new SelectionChangeEventHandler() {

            @Override
            public void onSelect(SelectionChangeEvent event) {
                BaseRenderable renderable = event.getEvent();
                if (renderable instanceof DetailedEventRenderable) {
                    SingularTimelineEvent selectableEvent = ((DetailedEventRenderable) renderable).getEvent().getEvent();
                    int eventId = selectableEvent.getEventDefinitionId();
                    if (selectableEvent.isSelected()) {
                        getVisualizationDef().getSelection().getSelectedItems().add(eventId);
                    } else {
                        getVisualizationDef().getSelection().getSelectedItems().remove(eventId);
                    }
                } else if (renderable instanceof SummaryEventRenderable) {
                    SummarizedTimelineEvent selectableEvent = ((SummaryEventRenderable) renderable).getEvent().getEvent();
                    IntCollection eventIds = selectableEvent.getEventIds();
                    if (selectableEvent.isSelected()) {
                        getVisualizationDef().getSelection().getSelectedItems().addAll(eventIds);
                    } else {
                        getVisualizationDef().getSelection().getSelectedItems().removeAll(eventIds);
                    }
                }
            }
        };

        scrollHandler = new ScrollEventHandler() {

            @Override
            public void onScroll(ScrollEvent event) {
                ViewPort timelineViewport = getTimelineView().getTimelineViewport();
                if (getTimelineView().getTimelineViewport().getTotalHeight() <= getTimelineView().getTimelineViewport().getCurrentHeight()) {
                    //no scroll if viewport fits in timeline
                    return;
                }
                int start = timelineViewport.getStart();
                if (event.isUp() != null) {
                    if (event.isUp()) {
                        start = (int) (start + (timelineViewport.getCurrentHeight() / 5));
                    } else {
                        start = (int) (start - (timelineViewport.getCurrentHeight() / 5));
                    }
                    if (start > 0) {
                        start = 0;
                    } else if ((start + timelineViewport.getTotalHeight()) < (getTimelineView().getTimelineHeight() - getTimelineView().getAxisHeight() - getTimelineView().getOverviewHeight())) {
                        start = (int) (getTimelineView().getTimelineHeight() - getTimelineView().getAxisHeight() - getTimelineView().getOverviewHeight() - timelineViewport.getTotalHeight());
                    }
                    timelineViewport.setStart(start);
                }
                getTimelineView().redraw();
                Scheduler.get().scheduleDeferred(() -> {
                    if ((getTimelineView().getOffsetHeight() == lastHeight) && validateSize()) {
                    } else {
                        lastHeight = getTimelineView().getOffsetHeight();
                        lastWidth = getTimelineView().getOffsetWidth();
                    }
                    getCurrentChildPresenter().scroll();
                });
                updateScrollSettings(start);
            }


        };

        tooltipHandler = new TooltipEventHandler() {

            @Override
            public void createTooltip(final TooltipEvent tooltipEvent) {
                List<TimelineField> list = getVisualizationDef().getTimelineSettings().getFieldList();
                if ((list == null) || list.isEmpty()) {
                    return;
                }
                VortexFuture<Tooltip> future = getVortex().createFuture();
                future.addEventHandler(new AbstractVortexEventHandler<Tooltip>() {
                    @Override
                    public void onSuccess(Tooltip result) {
                        tooltipEvent.getLayer().removeAll();
                        TooltipRenderable tooltip = new TooltipRenderable(tooltipEvent.getRenderable());
                        tooltip.setTooltip(result);
                        tooltipEvent.getLayer().bringToFront(tooltip);
                        tooltipEvent.getLayer().addItem(tooltip);
                        tooltip.setMaxHeight(tooltipEvent.getHeight() - timelineView.getAxisHeight() - timelineView.getOverviewHeight());
                        tooltip.setMaxWidth(tooltipEvent.getWidth());
                        getTimelineView().redraw();
                    }

                    @Override
                    public boolean onError(Throwable t) {
                        displayLoadingError(t);
                        return false;
                    }
                });
                try {
                    future.execute(ChronosActionsServiceProtocol.class).createTooltip(getDataViewUuid(), getVisualizationDef().getUuid(), ((DetailedEventRenderable) (tooltipEvent.getRenderable())).getEvent().getEvent());
                } catch (CentrifugeException e) {
                    displayLoadingError(e);
                }
            }
        };

        rangeSelectionEventHandler = new RangeSelectionEventHandler() {

            @Override
            public void onRangeSelection(RangeSelectionEvent event) {
                double startX = event.getStartX();
                double endX = event.getEndX();

                boolean select = event.isSelect();
                VortexFuture<IntCollection> future = WebMain.injector.getVortex().createFuture();
                long from = getCurrentChildPresenter().getTimeScale().toTime(startX);
                long to = getCurrentChildPresenter().getTimeScale().toTime(endX);

                ArrayList<String> trackNames = new ArrayList<>();
                if (event.getTrackName() != null) {
                    trackNames.add(event.getTrackName());
                } else {
                    trackNames = getVisibleTracks();
                }

                if (trackNames.isEmpty()) {
                    future.execute(ChronosActionsServiceProtocol.class).getItems(dvPresenter.getUuid(), visualizationDef.getUuid(), from, to, null);
                } else {
                    future.execute(ChronosActionsServiceProtocol.class).getItems(dvPresenter.getUuid(), visualizationDef.getUuid(), from, to, trackNames);
                }

                future.addEventHandler(new AbstractVortexEventHandler<IntCollection>() {
                    @Override
                    public void onSuccess(IntCollection changedItems) {
                        if ((changedItems != null) && !changedItems.isEmpty()) {
                            if (select) {
                                getVisualizationDef().getSelection().getSelectedItems().addAll(changedItems);
                            } else {
                                getVisualizationDef().getSelection().getSelectedItems().removeAll(changedItems);
                            }
                        }

                        getCurrentChildPresenter().clearSelection();
                        getCurrentChildPresenter().select(getVisualizationDef().getSelection());

                        if (getCurrentChildPresenter() instanceof MeasureTimelinePresenter) {
                            MeasureTimelinePresenter currentChildPresenter = (MeasureTimelinePresenter) getCurrentChildPresenter();
                            currentChildPresenter.adjustGroups(false);
                        }
                    }
                });
            }
        };

        overviewChangeEventHandler = new OverviewChangeEventHandler() {

            @Override
            public void onChange(OverviewChangeEvent overviewChangeEvent) {
                long start = overviewChangeEvent.getStart();
                long end = overviewChangeEvent.getEnd();

                interval = new Interval(start, end);

                //trackTotalHeight = null;
                //layoutTracks();
                getCurrentChildPresenter().updateOverview();
                updateTimeSettings(start, end);

                TimelineMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
            }

        };

        trackSelectionEventHandler = new TrackSelectionEventHandler() {

            @Override
            public void onTrackSelection(TrackSelectionEvent event) {
                if (event.isSelect()) {
                    getVisualizationDef().getSelection().getSelectedItems().addAll(event.getIds());
                } else {
                    getVisualizationDef().getSelection().getSelectedItems().removeAll(event.getIds());
                }
            }
        };

        timeScaleChangeEventHandler = new TimeScaleChangeEventHandler() {

            @Override
            public void onTimeScaleChange(TimeScaleChangeEvent event) {
                double x0 = event.getStartX();
                //			    if(x0 < 0){
                //			        x0=0;
                //			    }

                long start = getTimeScale().toTime(x0);

                if (start < initialStartTime) {
                    start = initialStartTime;
                }

                double x1 = event.getEndX();
                //                if(x1 > getTimelineView().getOffsetWidth()){
                //                    x1=getTimelineView().getOffsetWidth();
                //                }

                long end = getTimeScale().toTime(x1);
                //millis makes this hard, so we add a milli to account for rounding
                end = end + 1;
                if (end > initialEndTime) {
                    end = initialEndTime;
                }

                interval = new Interval(start, end);
                List<Axis> axes = updateTimeScale(interval);

                //trackTotalHeight = null;
                //layoutTracks();
                getCurrentChildPresenter().updateTimeScale();
                getTimelineView().render(axes);

                updateTimeSettings(start, end);
                TimelineMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
            }
        };

        crumbRemovedEventHandler = new CrumbRemovedEventHandler() {

            @Override
            public void onRemove(CrumbRemovedEvent event) {
                TimelinePresenter.this.drillToCategory(null);
                TimelineMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
            }
        };

        zoomHandler = new ZoomEventHandler() {

            @Override
            public void onZoom(ZoomEvent event) {
                long end = getTimeScale().getInterval().end;
                long start = getTimeScale().getInterval().start;

                getCurrentChildPresenter().zoom();
                updateTimeSettings(start, end);

                TimelineMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
            }
        };

        resizeHandler = new ResizeEventHandler() {


            @Override
            public void onResize(ResizeEvent event) {
                trackTotalHeight = null;
                Scheduler.get().scheduleDeferred(() -> {
                    if (getTimeScale() != null) {
                        getTimeScale().setNumberRange(0, getTimelineView().getOffsetWidth());
                    }
                    if ((getTimelineView().getOffsetHeight() == lastHeight) && (getTimelineView().getOffsetWidth() == lastWidth)) {
                        //no reason to re layout
                        return;
                    } else {
                        //size change, gotta layout
                        lastHeight = getTimelineView().getOffsetHeight();
                        lastWidth = getTimelineView().getOffsetWidth();
                        getCurrentChildPresenter().resize();
                    }
                    if (getMetrics() != null) {
                        getMetrics().positionLegend();
                    }
                    TimelineMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
                });
            }
        };

        trackingCompleteHandler = new TrackingCompleteEventHandler() {

            @Override
            public void onComplete(TrackingCompleteEvent event) {
                updateProgressIndicator(100);
                double trueHeight;
                if (trackModels.size() == 0) {
                    trueHeight = 1;
                }
                if ((trackModels.size() == 1) && (event.getTrack().getHeight() < 1.0)) {
                    event.getTrack().setHeight(1.0);
                }
                trueHeight = event.getTrack().getEndY(); //- getTimelineView().getTimelineViewportStart();
                getTimelineView().getTimelineViewport().setTotalHeight(trueHeight);
                getTimelineView().updateViewport(trueHeight);
                getTimelineView().updateOverviewTimeScale();
                //getTimelineView().render(tracks, events, axis, drawNow);
                Scheduler.get().scheduleDeferred(() -> {
                    getCurrentChildPresenter().adjustGroups(true);
                    hideProgressIndicator();
                });
                if (showWarningDialog) {
                    getTimelineView().showLimitReached();
                    hideProgressIndicator();
                }
            }

        };

        eventBus.addHandler(TrackChangeEvent.type, trackChangeHandler);
        eventBus.addHandler(TooltipEvent.type, tooltipHandler);
        eventBus.addHandler(ZoomEvent.type, zoomHandler);
        eventBus.addHandler(ResizeEvent.type, resizeHandler);
        eventBus.addHandler(TimeScaleChangeEvent.type, timeScaleChangeEventHandler);
        eventBus.addHandler(TrackSelectionEvent.type, trackSelectionEventHandler);
        eventBus.addHandler(OverviewChangeEvent.type, overviewChangeEventHandler);
        eventBus.addHandler(RangeSelectionEvent.type, rangeSelectionEventHandler);
        eventBus.addHandler(ScrollEvent.type, scrollHandler);
        eventBus.addHandler(TrackingCompleteEvent.type, trackingCompleteHandler);
        eventBus.addHandler(CrumbRemovedEvent.type, crumbRemovedEventHandler);
        eventBus.addHandler(SelectionChangeEvent.type, selectionChangeEventHandler);
    }

    public ArrayList<String> getVisibleTracks() {
        ArrayList<String> trackNames = Lists.newArrayList();
        for (CommonTrack commonTrack : getTrackModels().values()) {
            if (commonTrack.isVisible()) {
                trackNames.add(commonTrack.getLabel());
            }
        }
        return trackNames;
    }

    @Override
    public boolean hasSelection() {
        TimelineEventSelection selection = getVisualizationDef().getSelection();
        //getCurrentChildPresenter().updateSelection(selection);
        return (getVisualizationDef().getSelection() != null) && !getVisualizationDef().getSelection().getSelectedItems().isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Visualization> AbstractMenuManager<V> createMenuManager() {
        return (AbstractMenuManager<V>) new TimelineMenuManager(this);
    }

    public void hideProgressIndicator() {
        if (progressIndicator != null) {
            if (progressIndicator.isAttached()) {
                getChrome().getMainLP().remove(progressIndicator);
                progressBar.clear();
                progressBar = null;
                progressIndicator.close();
                progressIndicator = null;
            }
        }
        TimelineMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
    }

    @Override
    public void reload() {
        WebMain.injector.getVortex().execute((Callback<TimelineViewDef>) result -> {
            setVisualizationDef(result);
            if (getVisualizationDef().isSuppressLoadAtStartup()) {
                if (isViewLoaded()) {
                    resetViewport();
                    loadVisualization();
                }
            } else {
                resetViewport();
                handleViewLoadOrLoadVisualization();
            }
        }, VisualizationActionsServiceProtocol.class).getVisualization(dvPresenter.getUuid(), getUuid());
    }

    boolean hasProgressIndicator() {
        return progressBar != null;
    }

    public void showProgressIndicator() {
        hideProgressIndicator();
        if (getChrome() != null) {
            createProgressIndicator(getChrome().getMainLP());
        }
    }

    private void createProgressIndicator(Panel panel) {
        if (progressIndicator == null) {
            progressIndicator = new Alert(i18n.timelinePresenterLoading(), AlertType.INFO); //$NON-NLS-1$
            panel.add(progressIndicator);
            progressBar = new ProgressBar(ProgressBarBase.Style.ANIMATED);
            progressBar.setPercent(25);
            progressIndicator.setClose(false);
            progressIndicator.add(progressBar);
            progressIndicator.setHeight("50px");//FIXME: set using stylename
            progressIndicator.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
        }
    }

    void updateProgressIndicator(int percent) {
        if (progressIndicator != null) {
            progressBar.setPercent(percent);
        }
    }


    @Override
    public void loadVisualization() {
        getCurrentChildPresenter().cancel();
        trackTotalHeight = null;
        showProgressIndicator();
        VortexFuture<TimelineResult> future = getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<TimelineResult>() {
            @Override
            public void onSuccess(final TimelineResult result) {
                handleServerResult(result);
            }

            @Override
            public boolean onError(Throwable t) {
                hideProgressIndicator();
                displayLoadingError(t);
                return false;
            }
        });
        Scheduler.get().scheduleDeferred(() -> {
            try {
                TimelineRequest request = buildTimelineRequest();
                future.execute(ChronosActionsServiceProtocol.class).loadTimeline(request);
            } catch (CentrifugeException ignored) {
            }
        });
    }

    private TimelineChildPresenter getCurrentChildPresenter() {
        switch(this.mode){
            case DETAILED:
                return detailedPresenter;
            case SUMMARY:
                return summaryPresenter;
            case MEASURE:
                return measurePresenter;
            default:
                return emptyPresenter;
        }
    }

    public TimelineRequest buildTimelineRequest() {
        TimelineRequest request = new TimelineRequest();
        request.setTrackName(getTrack());
        request.setCalculateOverview(true);
        request.setEndTime(null);
        request.setStartTime(null);
        request.setSummaryLevel(summaryLevel);

        request.setDvUuid(getDataView().getUuid());
        request.setVizUuid(getVisualizationDef().getUuid());
        request.setStartGroupIndex(0);
        request.setGroupLimit(getChrome().getMainLP().getOffsetHeight()/TrackRenderable.FULL_SUMMARY_HEIGHT);

        request.setVizWidth(getChrome().getMainLP().getOffsetWidth());

//
//        getCurrentChildPresenter().updateSelection(getVisualizationDef().getSelection());
        request.setEventIdSelection(getVisualizationDef().getSelection().getSelectedItems());

        return request;
    }

    public Integer getColor(String value) {
        int color = 0;

        if (value == null) {
            return color;
        }

        if ((colors.get(value) != null) && (colors.get(value) != 0)) {
            color = colors.get(value);
        } else {
            color = ClientColorHelper.get().randomHueWheel().getIntColor();
            colors.put(value, color);
        }

        return color;
    }

    private void handleServerResult(final TimelineResult result) {
        updateProgressIndicator(50);
        TimelineViewDef viewDef = getVisualizationDef();
        TimelineSettings settings = viewDef.getTimelineSettings();
        clearPresenterData();

        getChrome().removeFullScreenWindow();
        getVisualizationDef().getState().setFocusedTrack(result.getTrackName());
        this.eventCount = result.getTotalEvents();
        this.summaryLevel = result.getSummaryLevel();
        if (hasOldSelection()) {
            getVisualizationDef().getSelection().setFromSelection(popOldSelection());
        }

        if(result instanceof DetailedTimelineResult){
            this.setMode(ViewMode.DETAILED);
            TimelineView view = createTimelineView();
            Scheduler.get().scheduleDeferred(() -> {
                detailedPresenter.setup(result, viewDef, settings, view, TimelinePresenter.this);
                showLegend();
            });
        } else if(result instanceof SummaryTimelineResult){
            this.setMode(ViewMode.SUMMARY);
            TimelineView view = createTimelineView();
            Scheduler.get().scheduleDeferred(() -> {
                trackModels.clear();
                updateGroupLegendItems();
                summaryPresenter.setup(result, viewDef, settings, view, getView().getOffsetWidth(), TimelinePresenter.this);
                showLegend();
                hideProgressIndicator();
            });
        } else if(result instanceof MeasuredTimelineResult){
            this.setMode(ViewMode.MEASURE);
            TimelineView view = createTimelineView();
            Scheduler.get().scheduleDeferred(() -> {
                hideLegend();
                updateGroupLegendItems();
                measurePresenter.setup(result, viewDef, settings, view, getView().getOffsetWidth(), TimelinePresenter.this);
                hideProgressIndicator();
                getCurrentChildPresenter().resize();
            });
        }

        appendNotificationText(NotificationLabel.FILTER, getVisualizationDef().getFilter() != null);
        appendBroadcastIcon();
        TimelineMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(getUuid()));
    }

    private void setMode(ViewMode detailed) {
        this.mode = detailed;
    }

    // fix this.
    public ViewMode getMode() {
        return mode;
    }

    public MetricsDTO getViewMetrics(){
        MetricsDTO dto = new MetricsDTO();

        TimelineChildPresenter currentChildPresenter = getCurrentChildPresenter();
//        currentChildPresenter.

        int visibleEventCount= 0;
        int hiddenEventCount = 0;
        Set<TimelineTrackModel> tracks = new HashSet<>();
        Set<TimelineTrackModel> hiddenTracks = new HashSet<>();


        if (currentChildPresenter instanceof DetailedTimelinePresenter) {
            DetailedTimelinePresenter pres = (DetailedTimelinePresenter) currentChildPresenter;
            List<DetailedEventProxy> events = pres.getEvents();

            for (DetailedEventProxy event : events) {
                Long startTime = event.getStartTime();
                Long endTime = event.getEndTime();

                if(event.getTrack().isVisible()){
                    if(isEventInView(startTime, endTime)) {
                        visibleEventCount++;
                        tracks.add(event.getTrack());
                    }
                }else{
                    if(isEventInView(startTime, endTime)) {
                        hiddenEventCount++;
                        hiddenTracks.add(event.getTrack());
                    }
                }
            }
        }

        if(this.mode == TimelineView.ViewMode.DETAILED){
            dto.add("Events", visibleEventCount);
        }

        if(tracks.size() > 1) {
            dto.add("Groups with Events", tracks.size());
        }

        if(hiddenEventCount != 0){
            dto.add("Hidden Events", hiddenEventCount);
        }

        if(hiddenTracks.size()>0){
            dto.add("Hidden Groups", hiddenTracks.size());
        }


        return dto;
    }


    private boolean isEventInView(Long startTime, Long endTime){
        if((startTime != null) && (endTime != null)){
            //starts and ends in view
            if ((startTime <= getCurrentInterval().start) && (endTime >= getCurrentInterval().end)) {
                return true;
            }

            //left out
            if ((startTime >= getCurrentInterval().start) && (startTime <= getCurrentInterval().end) ) {
                return true;
            }

            //right out
            if ((endTime >= getCurrentInterval().start) && (endTime <= getCurrentInterval().end)) {
                return true;
            }

        }else if(startTime != null){
            if ((startTime >= getCurrentInterval().start) && (startTime <= getCurrentInterval().end)) {
                return true;
            }
        }else if(endTime!= null){
            if ((endTime >= getCurrentInterval().start) && (endTime <= getCurrentInterval().end)) {
                return true;
            }
        }
        return false;
    }




    private void clearPresenterData() {
        validateVisualizationSettings();
        colors.clear();
        legendPresenter.clearLegend();
        this.summaryLevel = null;
        this.eventCount = 0;
    }

    void updateLegendSettings() {
        TimelineViewDef viewDef = getVisualizationDef();
        TimelineSettings settings = viewDef.getTimelineSettings();
        if (settings != null) {
            List<TimelineLegendDefinition> legendItems = new ArrayList<>();
            settings.setLegendItems(legendItems);
            for (Map.Entry<String, Integer> entry : colors.entrySet()) {
                TimelineLegendDefinition legendDefinition = new TimelineLegendDefinition();
                legendDefinition.setColor(entry.getValue());
                legendDefinition.setValue(entry.getKey());
                legendItems.add(legendDefinition);
            }
            RepeatingCommand legendSaveCommand = new RepeatingCommand() {
                @Override
                public boolean execute() {
                    //this way we only save the latest request
                    if (this.equals(getCurrentLegendSave())) {
                        //saveSettings(false);
                    }
                    return false;
                }
            };
            setCurrentLegendSave(legendSaveCommand);
            Scheduler.get().scheduleFixedDelay(legendSaveCommand, PERSIST_DELAY);
        }
    }

    private void setCurrentLegendSave(RepeatingCommand legendSaveCommand) {
        this.legendSaveCommand = legendSaveCommand;
    }

    private RepeatingCommand getCurrentLegendSave() {
        return this.legendSaveCommand;
    }

    void showHideMenus(int trackSize) {
        if (trackSize <= 1) {
            getMenuManager().disable(MenuKey.SORT_ASC);
            getMenuManager().disable(MenuKey.SORT_DSC);
            getMenuManager().hide(MenuKey.SORT_ASC);
            getMenuManager().hide(MenuKey.SORT_DSC);
        } else {
            if (this.sort == SortOrder.ASC) {
                getMenuManager().disable(MenuKey.SORT_ASC);
                getMenuManager().hide(MenuKey.SORT_ASC);
                getMenuManager().enable(MenuKey.SORT_DSC);
            } else {
                getMenuManager().disable(MenuKey.SORT_DSC);
                getMenuManager().hide(MenuKey.SORT_DSC);
                getMenuManager().enable(MenuKey.SORT_ASC);
            }
        }
    }

    void validateVisualizationSettings() {
        if (getVisualizationDef().getTimelineSettings().getShowSummary() == null) {
            getVisualizationDef().getTimelineSettings().setShowSummary(true);
        }

        if (getVisualizationDef().getTimelineSettings().getGroupNameSpace() == null) {
            getVisualizationDef().getTimelineSettings().setGroupNameSpace(true);
        }
    }

    boolean showLimitMessage(DetailedTimelineResult result) {
        if (result.isLimitReached()) {
            showWarningDialog = true;
            getTimelineView().showLimitReached(result.getTotalEvents(), result.getEventMax());
            hideProgressIndicator();
            hideGroups();
            hideLegend();
        } else {
            showWarningDialog = false;
        }
        return showWarningDialog;
    }


    public Map<String, Integer> getColors(){
        return colors;
    }

    void cleanTrackStates() {
        List<TimelineTrackState> toRemove = new ArrayList<>();
        for (TimelineTrackState state : getVisualizationDef().getState().getTrackStates()) {
            Set<String> keys = trackModels.keySet();

            if (!keys.contains(state.getTrackName())) {
                toRemove.add(state);
            }
        }
        getVisualizationDef().getState().getTrackStates().removeAll(toRemove);
    }



    public List<Axis> updateTimeScale(Interval interval) {
        getCurrentChildPresenter().setTimeScale(new TimeScale());

        getCurrentChildPresenter().getTimeScale().setDateRange(interval);
        getCurrentChildPresenter().getTimeScale().setNumberRange(0, getTimelineView().getOffsetWidth());

        List<Axis> axes = Axis.allRelevant(interval, 0, 0, getEventBus());

        for (Axis axis : axes) {
            axis.setTimeScale(getCurrentChildPresenter().getTimeScale());
        }
        getTimelineView().updateOverview(interval);
        return axes;
    }



    private static List<String> asSortedList(List<String> list, SortOrder sort) {
        Collections.sort(list, new NaturalComparator());
        if (sort == SortOrder.DESC) {
            Collections.reverse(list);
        }
        return list;
    }

    public static List<String> asSortedList(Collection<String> c, SortOrder sort) {
        List<String> list = new ArrayList<>(c);
        asSortedList(list, sort);
        return list;
    }

    @Override
    public void applySelection(Selection selection) {
        saveOldSelection(selection);
        visualizationDef.getSelection().clearSelection();

        getCurrentChildPresenter().clearSelection();
        getCurrentChildPresenter().select((TimelineEventSelection) selection);

        visualizationDef.getSelection().setFromSelection(popOldSelection());
        //loadVisualization();
    }

    @Override
    public FullSizeLayoutPanel createView() {

        FullSizeLayoutPanel fullSizeLayoutPanel = new FullSizeLayoutPanel();
        fullSizeLayoutPanel.setHeight("100%");
        fullSizeLayoutPanel.setWidth("100%");
        return fullSizeLayoutPanel;
    }

    private TimelineView createTimelineView() {

        if(timelineView != null){
            timelineView.setVisible(false);
            timelineView.removeFromParent();
        }

        switch(this.mode){
        case DETAILED:
            timelineView = detailedPresenter.createView(this);
            timelineView.setVisible(true);
            break;
        case SUMMARY:
            timelineView = summaryPresenter.createView(this);
            timelineView.setVisible(true);
            break;
        case MEASURE:
            timelineView = measurePresenter.createView(this);
            timelineView.setVisible(true);
            break;
        default:
            timelineView = emptyView;
            timelineView.setVisible(true);
            break;

        }
        getView().add(timelineView);
        timelineView.setHeight("100%");
        timelineView.setWidth("100%");
        return timelineView;
    }


    @Override
    public void saveViewStateToVisualizationDef() {
        TimelineEventSelection selection = getVisualizationDef().getSelection();
        //selection.clearSelection();

        if (sort != null) {
            if (sort == SortOrder.ASC) {
               getVisualizationDef().getTimelineSettings().setSortAscending(true);
            } else {
               getVisualizationDef().getTimelineSettings().setSortAscending(false);
            }
        }
        //selection.setSelectedItems(selectedIds);
    }

    @Override
    public void broadcastNotify(String text) {
        lastWidth = -1;
        lastHeight = -1;
        getTimelineView().broadcastNotify(text);
        appendBroadcastIcon();
    }

    private void resetViewport() {
        getVisualizationDef().getState().setScrollPosition(0);
        getVisualizationDef().getState().setStartPosition(Long.MIN_VALUE);
        getVisualizationDef().getState().setEndPosition(Long.MAX_VALUE);

        getVisualizationDef().getState().getTrackStates().clear();
        deselectAll();

        //summaryPresenter.clear();
        //detailedPresenter.clear();
        //measurePresenter.clear();

    }


    /**
     * used when exporting just the visualization, without the legend.
     * @return
     */
    @Override
    public ImagingRequest getImagingRequest() {
//        List<ImagingRequest> imagingRequest = getTimelineView().getImagingRequest();
        return getTimelineView().getTimelineImagingRequest();
    }


    /**
     *
     * @return
     */
    public List<ImagingRequest> getBundledImagingRequest(){
        return getTimelineView().getImagingRequest();
    }


    @Override
    public VortexFuture<Void> saveSettings(final boolean refreshOnSuccess, final boolean isStructural) {
        summaryLevel = null;
        getVisualizationDef().getState().setFocusedTrack(null);
        saveViewStateToVisualizationDef();
        hideFind();

        if(getCurrentChildPresenter() != null){
            getCurrentChildPresenter().resetState();
        }
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {

            vortexFuture.execute(VisualizationActionsServiceProtocol.class).saveSettings(getVisualizationDef(),
                    getDataViewUuid(), isStructural);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
        VortexEventHandler<Void> loadviz = new AbstractVortexEventHandler<Void>() {

            @Override
            public boolean onError(Throwable t) {
                return true;
            }

            @Override
            public void onSuccess(Void v) {
                if (refreshOnSuccess) {
                    if (getVisualizationDef().isSuppressLoadAtStartup()) {
                        if (isViewLoaded()) {
                            loadVisualization();
                        }
                    } else {
                        handleViewLoadOrLoadVisualization();
                    }
                    getChrome().setName(getName());
                }

                if (getVisualizationDef().getFilter() != null) {
                    showFilterLabel(true);
                } else {
                    showFilterLabel(false);
                }
                appendBroadcastIcon();

            }
        };
        vortexFuture.addEventHandler(loadviz);
        return vortexFuture;
    }


    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }





    public TimeScale getTimeScale() {
        return getCurrentChildPresenter().getTimeScale();
    }

    public void deselectAll() {
        getVisualizationDef().getSelection().clearSelection();
        getCurrentChildPresenter().clearSelection();
        getTimelineView().redraw();
    }

//
//    /**
//     * Selects based on text of legend matching the colorValue.
//     *
//     * @param text
//     */
//    public void selectByText(String text) {
//        getCurrentChildPresenter().selectText(text);
//
//
//        getTimelineView().redraw();
//    }

    /**
     * Used to select items ont he timeline based on color coode from the legend.
     *
     * @param text                    - color value of the legend item that corresponds to a group on the timeline.
     * @param removeExistingSelection - true  - deselects all values other than in the matching group.
     *                                false - does not care about cells that are not matching the color group.
     */
    public void selectByText(String text, boolean removeExistingSelection) {
        doSelectByText(text, removeExistingSelection, true);
    }


    public void deselectByText(String text) {
        doSelectByText(text, false, false);
    }



    public void setSelectionByGroup(String track, boolean visible){
        CommonTrack trackModel = trackModels.get(track);
        if ((trackModel != null) && (trackModel instanceof TimelineTrackModel)) {
            for(DetailedEventProxy event : ((TimelineTrackModel) trackModel).getEvents()){
                event.setSelected(visible);
            }
            getTimelineView().redraw();
        }
        else if((trackModel != null) && (trackModel instanceof MeasuredTrack)) {
            String label = trackModel.getLabel();
            VortexFuture<TimelineEventSelection> future = WebMain.injector.getVortex().createFuture();
            future.addEventHandler(new AbstractVortexEventHandler<TimelineEventSelection>() {
                @Override
                public void onSuccess(TimelineEventSelection result) {
                    applySelection(result);
                }
            });
            future.execute(ChronosActionsServiceProtocol.class).doServerTrackSelection(getUuid(), getVisualizationDef().getSelection(), visible, label);
        }

    }







    public void selectAll() {
        // if we are drilled in, we scope select all. we don't want to clear selection, this should be appending.
        if(getVisualizationDef().getState().getFocusedTrack() != null){
            String focusedTrack = getVisualizationDef().getState().getFocusedTrack();
            VortexFuture<TimelineEventSelection> future = WebMain.injector.getVortex().createFuture();
            future.addEventHandler(new AbstractVortexEventHandler<TimelineEventSelection>() {
                @Override
                public void onSuccess(TimelineEventSelection result) {
                    applySelection(result);
                }
            });
            future.execute(ChronosActionsServiceProtocol.class).doServerTrackSelection(getUuid(), getVisualizationDef().getSelection(), true, focusedTrack);
        }else {
//            int from = 1;
//            int to = this.eventCount;
//            ContiguousSet<Integer> range = ContiguousSet.create(Range.closed(from, to), DiscreteDomain.integers());
//            int[] c = Ints.toArray(range);
            int[] c = new int[eventCount];
            for(int ii=1; ii <= eventCount;ii++){
                c[ii-1] = ii;
            }
            getVisualizationDef().getSelection().clearSelection();
            getVisualizationDef().getSelection().setSelectedItems(new IntCollection(c));
            getCurrentChildPresenter().selectAll();
        }

        getTimelineView().redraw();
    }

    public void hideLegend() {
        legendPresenter.hide();
        getMenuManager().hide(MenuKey.HIDE_LEGEND);
        getMenuManager().enable(MenuKey.SHOW_LEGEND);
    }

    public void showLegend() {
        resetLegendHard();
        legendPresenter.show(true);
        getMenuManager().enable(MenuKey.HIDE_LEGEND);
        getMenuManager().hide(MenuKey.SHOW_LEGEND);
    }


    public boolean isLegendVisible(){
        return legendPresenter.isVisible();
    }


    public void hideGroups() {
            groupingPresenter.hide();
//        getMenuManager().hide(MenuKey.HIDE_GROUPS);
//        getMenuManager().enable(MenuKey.SHOW_GROUPS);
    }

    public void showGroups() {
        resetGroupHard();
        groupingPresenter.isUserClosed = false;
        groupingPresenter.show(true);
//        getMenuManager().enable(MenuKey.HIDE_GROUPS);
//        getMenuManager().hide(MenuKey.SHOW_GROUPS);
    }

    public TimelineColorLegendPresenter getLegendPresenter() {
        return legendPresenter;
    }

    public void resetZoom() {

        interval = new Interval(initialStartTime, initialEndTime);
        List<Axis> axes = updateTimeScale(interval);

        getCurrentChildPresenter().resetZoom();
        //layoutTracks();
        getTimelineView().render(axes);
        updateTimeSettings(initialStartTime, initialEndTime);
    }

    public void zoomIn() {

        TimeScale timeScale = getCurrentChildPresenter().getTimeScale();
        long duration = timeScale.getEnd() - timeScale.getStart();

        duration = duration / 4;

        if (duration >= 2) {
            interval = new Interval(timeScale.getStart() + duration, timeScale.getEnd() - duration);
            List<Axis> axes = updateTimeScale(interval);

            getCurrentChildPresenter().zoomIn();
            //layoutTracks();
            getTimelineView().render(axes);

            updateTimeSettings(interval.start, interval.end);
        }


    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    private void panToNextEvent(DetailedEventProxy event) {
        if(event == null) {
            return;
        }
        TimeScale timeScale = getCurrentChildPresenter().getTimeScale();
        if ((timeScale.getStart() == initialStartTime) && (timeScale.getEnd() == initialEndTime)) {
            //no-op have all points
            return;
        }

        if ((event.getStartTime() > timeScale.getStart()) && (event.getStartTime() < timeScale.getEnd())) {
            //also good
            return;
        }

        long duration = timeScale.getEnd() - timeScale.getStart();

        long buffer = (long) (duration - (duration * .2));

        long newStart = event.getStartTime() - buffer;
        long newEnd = newStart + duration;

        if (newStart < initialStartTime) {
            long diff = initialStartTime - newStart;
            newStart = initialStartTime;
            newEnd = newEnd + diff;
        }

        if (newEnd > initialEndTime) {
            long diff = newEnd - initialEndTime;
            newEnd = initialEndTime;
            newStart = newStart - diff;
        }

        interval = new Interval(newStart, newEnd);
        List<Axis> axes = updateTimeScale(interval);
        getCurrentChildPresenter().panToNextEvent();
        //layoutTracks();
        getTimelineView().render(axes);

    }

    private void panToPreviousEvent(DetailedEventProxy event) {
        TimeScale timeScale = getCurrentChildPresenter().getTimeScale();
        if ((timeScale.getStart() == initialStartTime) && (timeScale.getEnd() == initialEndTime)) {
            //no-op have all points
            return;
        }

        if ((event.getStartTime() > timeScale.getStart()) && (event.getStartTime() < timeScale.getEnd())) {
            //also good
            return;
        }

        long duration = timeScale.getEnd() - timeScale.getStart();

        long buffer = (long) (duration - (duration * .8));

        long newStart = event.getStartTime() - buffer;
        long newEnd = newStart + duration;

        if (newStart < initialStartTime) {
            long diff = initialStartTime - newStart;
            newStart = initialStartTime;
            newEnd = newEnd + diff;
        }

        if (newEnd > initialEndTime) {
            long diff = newEnd - initialEndTime;
            newEnd = initialEndTime;
            newStart = newStart - diff;
        }

        interval = new Interval(newStart, newEnd);
        List<Axis> axes = updateTimeScale(interval);
        //updateEvents();
        //layoutTracks();
        getTimelineView().render(axes);

    }

    private void scrollToPreviousEvent(DetailedEventProxy event) {

        if (searchHits == 0) {
            return;
        }
        ViewPort timelineViewport = getTimelineView().getTimelineViewport();
        int start = timelineViewport.getStart();

        int timelineViewportHeight = getTimelineView().getTimelineHeight() - getTimelineView().getAxisHeight() - getTimelineView().getOverviewHeight();

        int y = event.getY() + start;

        if ((y > 0) && (y < timelineViewportHeight)) {
            return;
        } else {
            start = (start - y) + (DetailedEventRenderable.EVENT_HEIGHT * 2);
        }

        if (start > 0) {
            start = 0;
        } else if ((start + timelineViewport.getTotalHeight()) < timelineViewportHeight) {
            start = (int) (getTimelineView().getTimelineHeight() - getTimelineView().getAxisHeight() - getTimelineView().getOverviewHeight() - timelineViewport.getTotalHeight());
        }

        timelineViewport.setStart(start);
        //updateEvents();
        //layoutTracks();
    }


    private void scrollToNextEvent(DetailedEventProxy event) {

        if ((searchHits == 0) || (event == null)) {
            return;
        }
        ViewPort timelineViewport = getTimelineView().getTimelineViewport();
        int start = timelineViewport.getStart();

        int timelineViewportHeight = getTimelineView().getTimelineHeight() - getTimelineView().getAxisHeight() - getTimelineView().getOverviewHeight();

        int y = event.getY() + start;

        if ((y > 0) && (y < timelineViewportHeight)) {
            return;
        } else {
            start = (start - y - (DetailedEventRenderable.EVENT_HEIGHT * 2)) + timelineViewportHeight;
        }

        if (start > 0) {
            start = 0;
        } else if ((start + timelineViewport.getTotalHeight()) < timelineViewportHeight) {
            start = (int) (getTimelineView().getTimelineHeight() - getTimelineView().getAxisHeight() - getTimelineView().getOverviewHeight() - timelineViewport.getTotalHeight());
        }

        timelineViewport.setStart(start);

        //layoutTracks();
    }


    public void collapseGroups(boolean collapse) {
        //no groups, no collapse
        if (getVisualizationDef().getTimelineSettings().getGroupByField() == null) {
            return;
        }

        Set<TimelineTrackState> states;
        if (getVisualizationDef().getState() == null) {
            TimelineCachedState cachedState = new TimelineCachedState();
            states = new HashSet<>();
            cachedState.setTrackStates(states);
            getVisualizationDef().setState(cachedState);
        } else {
            states = getVisualizationDef().getState().getTrackStates();
        }
        for (Map.Entry<String, CommonTrack> entry : trackModels.entrySet()) {
            CommonTrack trackModel = entry.getValue();
            trackModel.setCollapsed(collapse);
            TimelineTrackState state = new TimelineTrackState();
            state.setCollapse(collapse);
            state.setTrackName(entry.getKey());
            states.add(state);
        }

        getVisualizationDef().getState().setTrackStates(states);
        getCurrentChildPresenter().adjustGroups(false);
        //saveSettings(false);
    }


    public void doSort(SortOrder sort) {
        this.sort = sort;

        this.updateSort = false;
        reload();
    }

    public SortOrder getSort() {
        return sort;
    }

    public void makeGroupsVisible(boolean visible) {
        for (CommonTrack trackModel : trackModels.values()) {
            if (trackModel != null) {
                trackModel.setVisible(visible);
            }
        }

        Set<TimelineTrackState> states;
        if (getVisualizationDef().getState() != null) {
            states = getVisualizationDef().getState().getTrackStates();
            states.clear();

            for (Map.Entry<String, CommonTrack> entry : trackModels.entrySet()) {
                CommonTrack trackModel = entry.getValue();
                if (trackModel != null) {
                    trackModel.setVisible(visible);
                    TimelineTrackState state = new TimelineTrackState();
                    state.setTrackName(entry.getKey());
                    state.setCollapse(trackModel.isCollapsed());
                    state.setVisible(trackModel.isVisible());
                    states.add(state);
                }
            }
        }

        getCurrentChildPresenter().tracksChanged();
        updateGroupLegendItems();
    }

    private boolean validateSize() {
        return ((lastWidth == -1) || (lastHeight == -1)) || ((getTimelineView().getOffsetWidth() == lastWidth) && (getTimelineView().getOffsetHeight() == lastHeight));
    }

    @Override
    public void clearBroadcastNotification() {
        lastWidth = -1;
        lastHeight = -1;
        super.clearBroadcastNotification();
    }

    public boolean toggleTrack(String text) {
        showProgressIndicator();
        CommonTrack trackModel = trackModels.get(text);
        if (trackModel != null) {
            trackModel.setVisible(!trackModel.isVisible());
            eventBus.fireEvent(new TrackChangeEvent(trackModel));
            return trackModel.isVisible();
        } else {
            return false;
        }
    }

    public boolean isTrackVisible(String text) {
        CommonTrack trackModel = trackModels.get(text);
        return trackModel.isVisible();
    }

    public void resetSort() {
        sort = null;
    }


    public List<String> getVisibleLegendItems(){
        return legendPresenter.getVisItems();
    }

    private TimelineView getTimelineView() {
        if(timelineView == null){
            createTimelineView();
        }
        return timelineView;
    }

    public void setTimelineView(TimelineView timelineView) {
        this.timelineView = timelineView;
    }

    boolean isUpdateSort() {
        return updateSort;
    }

    void setUpdateSort(boolean updateSort) {
        this.updateSort = updateSort;
    }

    public void setSort(SortOrder sort) {
        this.sort = sort;
    }

    SearchBox getSearchBox() {
        return searchBox;
    }

    public void setSearchBox(SearchBox searchBox) {
        this.searchBox = searchBox;
    }

    Double getTrackTotalHeight() {
        return trackTotalHeight;
    }

    void setTrackTotalHeight(Double trackTotalHeight) {
        this.trackTotalHeight = trackTotalHeight;
    }

    HashMap<String, CommonTrack> getTrackModels() {
        return trackModels;
    }

    void setTrackModels(HashMap<String, CommonTrack> trackModels) {
        this.trackModels = trackModels;
    }

    boolean isShowWarningDialog() {
        return showWarningDialog;
    }

    void setShowWarningDialog(boolean showWarningDialog) {
        this.showWarningDialog = showWarningDialog;
    }

    public void setColors(HashMap<String, Integer> hashMap) {
        this.colors = hashMap;
    }

    private void adjustGroups() {
        getCurrentChildPresenter().adjustGroups(false);
    }

    long getInitialStartTime() {
        return initialStartTime;
    }

    void setInitialStartTime(long initialStartTime) {
        this.initialStartTime = initialStartTime;
    }

    long getInitialEndTime() {
        return initialEndTime;
    }

    void setInitialEndTime(long initialEndTime) {
        this.initialEndTime = initialEndTime;
    }

    public Interval getCurrentInterval() {
        return interval;
    }

    void setCurrentInterval(Interval currentInterval) {
        this.interval = currentInterval;
    }

    void switchView(TimelineResult result) {

        TimelineChildPresenter childPresenter = getCurrentChildPresenter();

        if(childPresenter != null){
            childPresenter.resetState();
        }

        if(result instanceof DetailedTimelineResult){
            this.setMode(ViewMode.DETAILED);
            TimelineView view = createTimelineView();
            detailedPresenter.setup(result, getVisualizationDef(), getVisualizationDef().getTimelineSettings(), view, this);
        } else if(result instanceof SummaryTimelineResult){
            this.setMode(ViewMode.SUMMARY);
            TimelineView view = createTimelineView();
            Scheduler.get().scheduleDeferred(() -> {
                summaryPresenter.setup(result, getVisualizationDef(), getVisualizationDef().getTimelineSettings(), view, getView().getOffsetWidth(), TimelinePresenter.this);
                hideProgressIndicator();
            });
        } else if(result instanceof MeasuredTimelineResult){
            this.setMode(ViewMode.MEASURE);
            TimelineView view = createTimelineView();
            Scheduler.get().scheduleDeferred(() -> {
                measurePresenter.setup(result, getVisualizationDef(), getVisualizationDef().getTimelineSettings(), view, getView().getOffsetWidth(), TimelinePresenter.this);
                hideProgressIndicator();
            });
        }
    }

    void validateAndUpdateColorLegend(Set<String> colorsToKeep) {
        if (getColors() != null) {
            List<String> colorsToRemove = new ArrayList<>();

            for (String color : getColors().keySet()) {
                if (!colorsToKeep.contains(color)) {
                    colorsToRemove.add(color);
                }
            }

            for (String color : colorsToRemove) {
                getColors().remove(color);
            }

        } else {
            setColors(new HashMap<>());
        }

        if ((getVisualizationDef().getTimelineSettings().getColorByField() != null) && (colorsToKeep.contains(null)
                || colorsToKeep.contains(TimelineTrack.NULL_TRACK))) {
            getColors().put(TimelineTrack.EMPTY_TRACK, 0);
        }

        if (getTrackModels().size() > 1) {
            getTrackModels().remove(TimelineTrack.NULL_TRACK);
        }
        getColors().remove(TimelineTrack.NULL_TRACK);
        getLegendPresenter().populateLegend(getColors());
    }

    public String getTrack() {
        return getVisualizationDef().getState().getFocusedTrack();
    }



    public void updateFromColorLegend(String key, List<String> itemOrderList) {

        getCurrentChildPresenter().updateColor(key, itemOrderList);

        //saveLegendCache();
    }

    void setSearchTrackName(String object) {
        searchTrackName = object;
    }

    private void doSelectByText(String text, boolean removeExistingSelection, boolean doSelect) {
        TimelineEventSelection selection = getVisualizationDef().getSelection();
        if(removeExistingSelection) {
            deselectAll();
        }

        VortexFuture<TimelineEventSelection> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {

            ArrayList<CommonTrack> values = Lists.newArrayList(getTrackModels().values());
            values.removeIf(commonTrack -> !commonTrack.isVisible());
            ArrayList<String> visibleTrackNames = Lists.newArrayList();
            if (!Strings.isNullOrEmpty(getTrack())) {
                visibleTrackNames.add(getTrack());
            }else {
                for (CommonTrack value : values) {
                    visibleTrackNames.add(value.getLabel());
                }
            }
            vortexFuture.execute(ChronosActionsServiceProtocol.class).doServerSelection(getVisualizationDef().getUuid(), text, selection, doSelect, visibleTrackNames);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
        VortexEventHandler<TimelineEventSelection> handleSelection = new AbstractVortexEventHandler<TimelineEventSelection>() {

            @Override
            public boolean onError(Throwable t) {
                return true;
            }

            @Override
            public void onSuccess(TimelineEventSelection v) {
                //getVisualizationDef().getSelection().setFromSelection(v);
                applySelection(v);
            }
        };
        vortexFuture.addEventHandler(handleSelection);
    }

    TimelineMetricsView metrics = null;
    public void showMetrics() {
        if(metrics == null){
            metrics = new TimelineMetricsView(this);
        }
        Scheduler.get().scheduleDeferred(() -> metrics.show());
    }

    public TimelineMetricsView getMetrics() {
        return metrics;
    }

    public void save() {
        saveViewStateToVisualizationDef();
        hideFind();
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {

            Boolean isStructural = false;
            vortexFuture.execute(VisualizationActionsServiceProtocol.class).saveSettings(getVisualizationDef(),
                    getDataViewUuid(), isStructural, false);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
        VortexEventHandler<Void> loadviz = new AbstractVortexEventHandler<Void>() {

            @Override
            public boolean onError(Throwable t) {
                return true;
            }

            @Override
            public void onSuccess(Void v) {

                if (getVisualizationDef().getFilter() != null) {
                    showFilterLabel(true);
                } else {
                    showFilterLabel(false);
                }
                appendBroadcastIcon();

            }
        };
        vortexFuture.addEventHandler(loadviz);
    }
    public void preZoom(ZoomEvent event) {
        long end = getTimeScale().getInterval().end;
        long start = getTimeScale().getInterval().start;
        List<Axis> axes;
        long diff = end - start;
        long change;
//        if(event.getMagnitude() < 1) {
//            change = (long) (diff *  .016);
//        } else {
            change = (long) (diff * .016);
//        }
//                                if(recenter){
//                                    start = (xTime - diff/2);
//                                    end = (xTime + diff/2);
//                                }
        //We zoom in by a zoomLevel and try to use the mouse x as the center
        if (event.isIn()) {

            start = (start + change);
            end = (end - change);

        } else {
            start = start - change;
            end = end + change;


        }

        if (start < initialStartTime) {
            start = initialStartTime;
        }

        if (end > initialEndTime) {
            end = initialEndTime;
        }

        if (start > end) {
            start = end - 3;
        }

        if (end < start) {
            end = start + 3;
        }

        interval = new Interval(start, end);

        axes = updateTimeScale(interval);
        //trackTotalHeight = null;
        //layoutTracks();

        getTimelineView().getOverview().render();
//        getTimelineView().renderOverview(axes);

    }
}
