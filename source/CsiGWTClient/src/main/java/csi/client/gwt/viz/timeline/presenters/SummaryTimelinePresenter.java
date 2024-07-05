package csi.client.gwt.viz.timeline.presenters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;

import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.DetailedEventProxy;
import csi.client.gwt.viz.timeline.model.Interval;
import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.model.TimelineTrackModel;
import csi.client.gwt.viz.timeline.model.summary.SummaryEventProxy;
import csi.client.gwt.viz.timeline.scheduler.DeferredSimpleLayoutCommand;
import csi.client.gwt.viz.timeline.view.TimelineView;
import csi.client.gwt.viz.timeline.view.drawing.DetailedEventRenderable;
import csi.client.gwt.viz.timeline.view.drawing.DetailedOverviewRenderable;
import csi.client.gwt.viz.timeline.view.drawing.OverviewRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TimelineSlider;
import csi.client.gwt.viz.timeline.view.drawing.TimelineTrackRenderable;
import csi.client.gwt.viz.timeline.view.measured.drawing.HistogramOverviewRenderable;
import csi.client.gwt.viz.timeline.view.summary.SummaryCompleteEvent;
import csi.client.gwt.viz.timeline.view.summary.SummaryCompleteEventHandler;
import csi.client.gwt.viz.timeline.view.summary.SummaryTimelineView;
import csi.client.gwt.viz.timeline.view.summary.drawing.SummaryEventRenderable;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.server.common.model.visualization.timeline.TimelineCachedState;
import csi.server.common.model.visualization.timeline.TimelineSettings;
import csi.server.common.model.visualization.timeline.TimelineTrackState;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.common.service.api.ChronosActionsServiceProtocol;
import csi.shared.core.util.IntCollection;
import csi.shared.core.visualization.timeline.OverviewTrack;
import csi.shared.core.visualization.timeline.SingularTimelineEvent;
import csi.shared.core.visualization.timeline.SummarizedTimelineEvent;
import csi.shared.core.visualization.timeline.SummaryTimelineResult;
import csi.shared.core.visualization.timeline.TimelineResult;
import csi.shared.core.visualization.timeline.TimelineTrack;
import csi.shared.gwt.viz.timeline.TimeUnit;
import csi.shared.gwt.viz.timeline.TimelineRequest;

public class SummaryTimelinePresenter implements TimelineChildPresenter, HistogramOverview{

    private SummaryTimelineView view;
    private TimelinePresenter presenter;
    List<Axis> axes;
    private TimeScale timeScale;
    private EventBus eventBus;
    private List<SummaryEventRenderable> renderables = new ArrayList<>();
    private List<DetailedEventRenderable> detailedRenderables = new ArrayList<>();

    private List<OverviewRenderable> overviewRenderables;
    private Interval lastInterval = null;
    private boolean hasOverviewData = false;
    private boolean firstLoad = true;

    private static final int MIN_DURATION = 5;
    private boolean renderOverview = true;
    private SummaryCompleteEventHandler summaryCompleteHandler = new SummaryCompleteEventHandler(){


        @Override
        public void onComplete(SummaryCompleteEvent event) {
            view.getTimelineViewport().setTotalHeight(event.getHeight());
            view.updateViewport(event.getHeight());

            if((renderOverview && ((overviewRenderables == null) || (overviewRenderables.size() == 0)))) {

                updateDetailedOverview(detailedRenderables);

                renderOverview = false;
            }

            view.getTimelineViewport().setCurrentWidth(view.getOffsetWidth());
            view.updateOverviewTimeScale();
            view.render(detailedRenderables, renderables, axes, true);
            //We persist the legend colors after
            presenter.updateLegendSettings();
            if((presenter.getColors() != null) && (presenter.getColors().size() > 0)) {
                if(firstLoad) {
                    presenter.showLegend();
                }
            }


            select(presenter.getVisualizationDef().getSelection());

            presenter.showHideMenus(0);
            presenter.hideProgressIndicator();
            firstLoad = false;
            //presenter.getLegendPresenter().reset();
        }

        };
        private boolean hasHandler = false;
        private VortexEventHandler<TimelineResult> lastHandler;

        @Override
        public void cancel() {
            if(lastHandler != null) {
                lastHandler.onCancel();
            }
        }

//        @Override
//        public void updateSelection(TimelineEventSelection selection) {
//            //Uses eventids and translates them to rowids on server
//
//
//                    if(selection != null){
//                        int count = 0;
//
//                        int[] newIds = new int[detailedRenderables.size()];
//                        for (DetailedEventRenderable eventProxy : detailedRenderables) {
//                            if (eventProxy.isSelected()) {
//                                newIds[count] = eventProxy.getEvent().getEvent().getEventDefinitionId();
//                                count++;
//                            }
//                        }
//
//                        List<Integer> newSummaryIds = new ArrayList<Integer>(renderables.size());
//                        for (SummaryEventRenderable eventProxy : renderables) {
//                            if (eventProxy.isSelected()) {
//                                newSummaryIds.addAll(eventProxy.getEvent().getEvent().getEventIds());
//                            }
//                        }
//
//                        selection.getSelectedItems().addAll(newIds);
//                        selection.getSelectedItems().addAll(newSummaryIds);
//                        selection.getSelectedItems().deDupe();
//                    }
//        }

        @Override
        public void clearSelection() {
            if(detailedRenderables != null) {
                for (DetailedEventRenderable event : detailedRenderables) {
                    event.setSelected(false);
                }
            }
            if(renderables != null) {
                for (SummaryEventRenderable event : renderables) {
                    event.setSelected(false);
                }
            }
        }
        private void updateDetailedOverview(List<DetailedEventRenderable> detailedRenderables) {
            overviewRenderables = new ArrayList<>();
            //Need to copy because this will get altered by other stuff otherwise
            TimeScale timeScale = this.timeScale.copy();
            for(DetailedEventRenderable event: detailedRenderables) {
                DetailedOverviewRenderable overviewRenderable = new DetailedOverviewRenderable(event);
                overviewRenderable.setTimeScale(timeScale);
                overviewRenderable.setViewport(view.getTimelineViewport());
                //event.setViewport(view.getTimelineViewport());
                overviewRenderables.add(overviewRenderable);
            }
            view.renderOverview(overviewRenderables);
        }
        @Override
        public void selectText(String text) {
            // TODO Auto-generated method stub

        }

        @Override
        public void resetZoom() {
            presenter.showProgressIndicator();

            adjustSummaries();
        }

        @Override
        public void zoomIn() {
            presenter.showProgressIndicator();

            adjustSummaries();
        }

        @Override
        public void panToNextEvent() {

            adjustSummaries();
        }

        @Override
        public void adjustGroups(boolean overviewRefresh) {
            adjustSummaries();
        }

        @Override
        public void selectAll() {
            for (DetailedEventRenderable event : detailedRenderables) {
                event.setSelected(true);
            }

            for (SummaryEventRenderable event : renderables) {
                event.setSelected(true);
            }
        }

        @Override
        public boolean hasSelection() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int getSearchHitCount(String text) {
            int searchHits = 0;
            for (DetailedEventRenderable event : detailedRenderables) {
                String label = event.getEvent().getLabel();
                if ((text.length() != 0) && (label != null) && label.toUpperCase().contains(text)) {
                    event.getEvent().setSearchHit(true);
                    if (event.getTrack().isVisible()) {
                     searchHits++;
                  }
                } else {
                    event.getEvent().setSearchHit(false);
                    event.getEvent().setSearchHighlight(false);
                }
            }
            return searchHits;
        }

    @Override
        public void updateOverview() {
            presenter.showProgressIndicator();
            adjustGroups(false);

        }

        @Override
        public TimeScale getTimeScale() {
            return timeScale;
        }

        @Override
        public void updateTimeScale() {
            adjustSummaries();
            axes = updateTimeScale(presenter.getCurrentInterval());
        }
        private void renderOverview() {
            if(overviewRenderables == null) {
                return;
            }
            for(OverviewRenderable renderable: overviewRenderables) {
                DetailedOverviewRenderable detailedRenderable = (DetailedOverviewRenderable) renderable;
                detailedRenderable.getTimeScale().setNumberRange(0, view.getOffsetWidth());
            }

            view.renderOverview(overviewRenderables);
            renderOverview = false;
        }

        private void adjustSummaries() {
            cancel();
            Interval currentInterval = presenter.getCurrentInterval();

            if(((detailedRenderables != null) && (detailedRenderables.size() > 0)) &&
            ((lastInterval != null) && lastInterval.contains(currentInterval))){
                Scheduler.get().scheduleDeferred(() -> {
                    axes = updateTimeScale(currentInterval);
                    if (renderOverview) {
                        renderOverview();
                    }
                    updateEvents();
                    lastInterval = currentInterval;
                    view.render(detailedRenderables, renderables, axes, true);
                    presenter.hideProgressIndicator();
                });
            } else {
                renderOverview = false;
                lastInterval = currentInterval;
                axes = updateTimeScale(presenter.getCurrentInterval());
                view.render(axes);

                TimelineRequest request = presenter.buildTimelineRequest();

                request.setStartTime(currentInterval.start);
                request.setEndTime(currentInterval.end);


                try {
                    VortexFuture<TimelineResult> future = presenter.getVortex().createFuture();
                    if(!presenter.hasProgressIndicator()) {
                        presenter.showProgressIndicator();
                    }
                    lastHandler = new VortexEventHandler<TimelineResult>(){

                        private boolean cancelled = false;

                        @Override
                        public void onSuccess(TimelineResult result) {
                            if(cancelled){
                                return;
                            }
                            clearRenderables();
                            if(!(result instanceof SummaryTimelineResult)){
                                presenter.switchView(result);
                            } else {
                                SummaryTimelineResult summaryResult = (SummaryTimelineResult) result;
                                //We don't need it if the server didn't give it to us
                                if(summaryResult.getOverviewData() == null) {
                                    if((summaryResult.getEvents() != null) && (summaryResult.getEvents().size() > 0)) {
                                        hasOverviewData = true;
                                    }
                                }
                                display(summaryResult);
                            }
                        }

                        @Override
                        public boolean onError(Throwable t) {
                            if(!cancelled){
                                clearRenderables();
                            }
                            presenter.hideProgressIndicator();
                            // TODO Auto-generated method stub
                            return false;
                        }

                        @Override
                        public void onUpdate(int taskProgress, String taskMessage) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onCancel() {
                            this.cancelled = true;
                        }};
                    future.addEventHandler(lastHandler);

                    future.execute(ChronosActionsServiceProtocol.class).loadTimeline(request);
                } catch (CentrifugeException ignored) {

                }
            }
        }

        private void clearRenderables() {
            if(renderables != null) {
               renderables.clear();
            }
            if(detailedRenderables != null) {
               detailedRenderables.clear();
            }
        }


        private void display(SummaryTimelineResult result) {

            axes = updateTimeScale(presenter.getCurrentInterval());

            createRenderables(result);
            this.view.render(detailedRenderables, renderables, axes, false);
            this.view.getTimelineViewport().setCurrentWidth(this.view.getOffsetWidth());
            renderOverview();
            //this.view.updateOverviewTimeScale();
        }

        private List<DetailedEventProxy> createDetailedEvents(List<SingularTimelineEvent> events) {
            List<DetailedEventProxy> proxyEvents = new ArrayList<>();
            detailedRenderables = new ArrayList<>();
            for (SingularTimelineEvent event : events) {

                DetailedEventProxy eventProxy = new DetailedEventProxy(event);
                //            if (selection.getSelectedItems().contains(event.getRowId())) {
                //                eventProxy.setSelected(true);
                //            }
                proxyEvents.add(eventProxy);
                //            if (result.getMin() == result.getMax()) {
                //                eventProxy.setDrawableSize(2);
                //            } else {
                //                eventProxy.setDrawableSize(calculateSize(result, event));
                //            }
                TimelineTrackModel trackModel = (TimelineTrackModel) presenter.getTrackModels().get(event.getTrackValue());
                if (trackModel == null) {
                    trackModel = new TimelineTrackModel();
                    trackModel.setLabel(TimelineTrack.EMPTY_TRACK);
                    presenter.getTrackModels().put(TimelineTrack.EMPTY_TRACK, trackModel);
                    TimelineTrackRenderable track = new TimelineTrackRenderable(trackModel);
                    //                track.setViewport(getView().getTimelineViewport());
                    //                trackRenderables.add(track);
                    trackModel.setSummary(presenter.getVisualizationDef().getTimelineSettings().getShowSummary());
                    trackModel.setGroupSpace(presenter.getVisualizationDef().getTimelineSettings().getGroupNameSpace());
                    trackModel.setCollapsed(true);
                    for (TimelineTrackState state : presenter.getVisualizationDef().getState().getTrackStates()) {
                        if ((state.getTrackName() != null) && state.getTrackName().equals(TimelineTrack.EMPTY_TRACK)) {
                            trackModel.setCollapsed(state.getCollapse());
                            if (state.getVisible() == null) {
                                state.setVisible(true);
                            }
                            trackModel.setVisible(state.getVisible());
                            break;
                        }
                    }
                    if (presenter.getTrackModels().size() <= 1) {
                        trackModel.setSummary(false);
                        trackModel.setGroupSpace(false);
                        trackModel.setCollapsed(false);
                        trackModel.setAllowCollapse(false);
                        trackModel.setVisible(true);
                    }
                }
                trackModel.addEvent(eventProxy);

                DetailedEventRenderable eventRenderable = new DetailedEventRenderable(eventProxy, trackModel);
                eventRenderable.setViewport(view.getTimelineViewport());


                String colorValue = event.getColorValue();
                eventProxy.setColor(presenter.getColor(colorValue));

                detailedRenderables.add(eventRenderable);

            }

            return proxyEvents;
        }

        @Override
        public void setTimeScale(TimeScale timeScale) {
            this.timeScale = timeScale;
        }


        @Override
        public void zoom() {if(
            !presenter.hasProgressIndicator()) {
                presenter.showProgressIndicator();
            }
            adjustGroups(false);
        }

//        @Override
//        public void selectByText(String text, boolean removeExistingSelection) {
//            presenter.doSelectByText(text, removeExistingSelection, true);
//        }
//
//        private void doSelectByText(String text, boolean removeExistingSelection, boolean select) {
//            for (DetailedEventRenderable event : detailedRenderables) {
//                String colorValue = event.getEvent().getColorValue();
//                if ((colorValue == null || colorValue.equals(TimelineTrack.NULL_TRACK)) && text.equals(TimelineTrackRenderable.NO_VALUE)) {
//                    event.setSelected(select);
//                } else {
//                    if (colorValue.equals(text)) {
//                        event.setSelected(select);
//                    } else {
//                        if(event.isSelected())
//                            event.setSelected(!removeExistingSelection);
//
//                    }
//                }
//            }
//
//            for (SummaryEventRenderable event : renderables) {
//                Set<String> colorValues = event.getEvent().getEvent().getColors();
//                if ((colorValues == null || colorValues.contains(TimelineTrack.NULL_TRACK)) && text.equals(TimelineTrackRenderable.NO_VALUE)) {
//                    event.setSelected(select);
//                } else {
//                    if (colorValues.contains(text)) {
//                        event.setSelected(select);
//                    } else {
//                        if(event.isSelected())
//                            event.setSelected(!removeExistingSelection);
//
//                    }
//                }
//            }
//        }

//        @Override
//        public void deselectByText(String text) {
//            //doSelectByText(text, false, false);
//
//            presenter.doSelectByText(text, false, false);
//        }

        @Override
        public void scroll() {
            Scheduler.get().scheduleDeferred(() -> view.redraw());
        }

        public void setup(TimelineResult result, TimelineViewDef viewDef, TimelineSettings settings, TimelineView timelineView,
                int offsetWidth, TimelinePresenter presenter) {
            SummaryTimelineResult summaryResult = (SummaryTimelineResult) result;
            this.view = (SummaryTimelineView) timelineView;
            this.presenter = presenter;


            presenter.addLegend();

            presenter.addGroups();
            presenter.hideGroups();
            presenter.hideLegend();
            eventBus = presenter.getEventBus();
            TimelineCachedState state = viewDef.getState();
            hasOverviewData = result.getOverviewData() != null;
            if(overviewRenderables != null) {
                overviewRenderables.clear();
                overviewRenderables = null;
            }
            if(hasOverviewData && (result.getEvents() != null) && (result.getEvents().size() > 0)){
                axes  = createHistogramOverview(result.getOverviewData(), state);

                renderOverview = false;
            } else {
                hasOverviewData = false;
                presenter.showProgressIndicator();
                Interval interval = new Interval(result.getLowerTimeBound(), result.getUpperTimeBound());
                if((summaryResult.getSingularEvents() != null) && (summaryResult.getSingularEvents().size() > 0)) {
                    axes = createDetailedOverview(summaryResult.getSingularEvents());
                    renderOverview = true;
                } else {
                    renderOverview = false;
                    axes = updateTimeScale(interval);
                }
            }
            view.setTrackName(presenter.getTrack());


            initColors(summaryResult.getLegendInfo());

            createRenderables(summaryResult);

            presenter.validateAndUpdateColorLegend(summaryResult.getLegendInfo());
//            presenter.showLegend();

            //        List<MeasuredTrackRenderable> trackRenderables = createTrackRenderables(proxyTracks, axes);
            //        this.view.render(trackRenderables, trackRenderables, axes, false);
            //        int totalHeight = measuredResult.getGroupCount() * TrackRenderable.FULL_SUMMARY_HEIGHT;

            SummaryCompleteEventHandler handler = summaryCompleteHandler ;
            if(!hasHandler){
                hasHandler = true;
                eventBus.addHandler(SummaryCompleteEvent.type, handler);
            }


        }

        private void initColors(Set<String> legendInfo) {

            if(legendInfo == null) {
                return;
            }
            for(String legendItem: legendInfo) {
                presenter.getColor(legendItem);
            }

        }

        private void createRenderables(SummaryTimelineResult summaryResult) {
            if(summaryResult.getSingularEvents() != null){

                List<DetailedEventProxy> proxyEvents = createDetailedEvents(summaryResult.getSingularEvents());
                Collections.sort(proxyEvents);
                layoutDetailedEvents(proxyEvents, eventBus, timeScale);


                if(renderables != null){
                    renderables.clear();
                }
            } else {
                List<SummaryEventProxy> proxyEvents = createProxyEvents(summaryResult.getEvents());

                Collections.sort(proxyEvents);
                layoutSummaryEvents(proxyEvents, eventBus, timeScale);
                renderables = createSummaryRenderables(proxyEvents);
                if(detailedRenderables != null){
                    detailedRenderables.clear();
                }
            }
        }

        private List<SummaryEventRenderable> createSummaryRenderables(List<SummaryEventProxy> proxyEvents) {
            List<SummaryEventRenderable> renderables = new ArrayList<>();


            for(SummaryEventProxy proxyEvent: proxyEvents){
                SummaryEventRenderable renderable = new SummaryEventRenderable(proxyEvent);
                renderable.setViewport(this.view.getTimelineViewport());

                proxyEvent.setColor(presenter.getColor(proxyEvent.getEvent().getColorValue()));
                renderables.add(renderable);
            }
            return renderables;
        }

    private List<SummaryEventProxy> createProxyEvents(List<SummarizedTimelineEvent> events) {

            List<SummaryEventProxy> proxies = new ArrayList<>();
            for(SummarizedTimelineEvent event: events){
                SummaryEventProxy proxy = new SummaryEventProxy(event);
                proxies.add(proxy);
            }

            return proxies;
        }


    private void layoutSummaryEvents(List<SummaryEventProxy> events, EventBus eventBus, TimeScale timeScale) {
        DeferredSimpleLayoutCommand<SummaryEventProxy> command = new DeferredSimpleLayoutCommand<>();
        command.setEvents(events);
        command.setEventBus(eventBus);
        command.setTimeScale(timeScale);
        command.setTimelineHeight(view.getOffsetHeight());
        Scheduler.get().scheduleDeferred(command);
    }

    private void layoutDetailedEvents(List<DetailedEventProxy> events, EventBus eventBus, TimeScale timeScale) {
        DeferredSimpleLayoutCommand<DetailedEventProxy> command = new DeferredSimpleLayoutCommand<>();
        command.setEvents(events);
        command.setEventBus(eventBus);
        command.setTimeScale(timeScale);
        command.setTimelineHeight(view.getOffsetHeight());
        Scheduler.get().scheduleDeferred(command);
    }

    public TimelineView createView(TimelinePresenter presenter) {
//            if(view == null){
                view = new SummaryTimelineView(presenter);
//            }

            return view;
        }

        public List<Axis> updateTimeScale(Interval interval) {
            timeScale = new TimeScale();

            timeScale.setDateRange(interval);
            timeScale.setNumberRange(0, view.getOffsetWidth());

            List<Axis> axes = Axis.allRelevant(interval, 0, 0, getEventBus());

            for (Axis axis : axes) {
                axis.setTimeScale(timeScale);
            }
            view.updateOverview(interval);
            return axes;
        }

        @Override
        public List<Axis> createHistogramOverview(OverviewTrack overviewData, TimelineCachedState state) {

            Long endTime = overviewData.getOverviewEnd();
            Long startTime = overviewData.getOverviewStart();
            long duration = endTime - startTime;

            if (duration < MIN_DURATION) {
                duration = MIN_DURATION;
                startTime--;
                endTime++;
            }
            //Pad the start and end time to make sure all events fit properly in initial view.
            double padTime = TimeUnit.calculateTimePadding(duration);
            if (padTime < 1) {
                padTime = 1;
            }
            startTime = (long) (startTime - padTime);
            endTime = (long) (endTime + padTime);

            //we have initial start/end times we can reference later
            presenter.setInitialStartTime(startTime);
            presenter.setInitialEndTime(endTime);


            //        if (state != null) {
            //            if (state.getStartPosition() > startTime)
            //                startTime = state.getStartPosition();
            //
            //            if (state.getEndPosition() < endTime)
            //                endTime = state.getEndPosition();
            //        }

            Interval overviewInterval = new Interval(presenter.getInitialStartTime(), presenter.getInitialEndTime());
            final TimeScale timeScale = view.setOverviewRange(overviewInterval, new TimelineSlider());

            Interval interval = new Interval(startTime, endTime);
            presenter.setCurrentInterval(interval);
            final List<Axis> axes = updateTimeScale(interval);

            HistogramOverviewRenderable renderable = new HistogramOverviewRenderable();
            renderable.setTrack(overviewData);
            renderable.setTimeScale(timeScale);
            ArrayList<OverviewRenderable> renderables = new ArrayList<>();
            renderables.add(renderable);
            view.renderOverview(renderables);

            return axes;
        }

        private List<Axis> createDetailedOverview(List<SingularTimelineEvent> events) {


            long upperBound = Long.MIN_VALUE;
            long lowerBound = Long.MAX_VALUE;
            for(SingularTimelineEvent event: events) {

                Long startTime = event.getStartTime();
                if(startTime != null){
                    //events.add(event);

                    if(lowerBound > startTime){
                        lowerBound = startTime;
                    }
                    if(upperBound < startTime){
                        upperBound = startTime;
                    }
                    Long endTime = event.getEndTime();
                    if(endTime != null){
                        if(lowerBound > endTime){
                            lowerBound = endTime;
                        }
                        if(upperBound < endTime){
                            upperBound = endTime;
                        }
                    }
                }

            }
            long minTime = lowerBound;
            long maxTime = upperBound;

            long duration = upperBound - lowerBound;

            if (duration < MIN_DURATION) {
                duration = MIN_DURATION;
                minTime--;
                maxTime++;
            }
            //Pad the start and end time to make sure all events fit properly in initial view.
            double padTime = TimeUnit.calculateTimePadding(duration);
            if (padTime < 1) {
                padTime = 1;
            }
            minTime = (long) (minTime - padTime);
            maxTime = (long) (maxTime + padTime);

            //we have initial start/end times we can reference later
            presenter.setInitialStartTime(minTime);
            presenter.setInitialEndTime(maxTime);


            //        if (state != null) {
            //            if (state.getStartPosition() > startTime)
            //                startTime = state.getStartPosition();
            //
            //            if (state.getEndPosition() < endTime)
            //                endTime = state.getEndPosition();
            //        }

            Interval overviewInterval = new Interval(presenter.getInitialStartTime(), presenter.getInitialEndTime());
            final TimeScale timeScale = view.setOverviewRange(overviewInterval, new TimelineSlider());

            Interval interval = new Interval(minTime, maxTime);
            presenter.setCurrentInterval(interval);
            final List<Axis> axes = updateTimeScale(interval);

//            for(SingularTimelineEvent event: events) {
//                DetailedOverviewRenderable renderable = new DetailedOverviewRenderable(event);
//
//            }
//            HistogramOverviewRenderable renderable = new HistogramOverviewRenderable();
//            renderable.setTrack(overviewData);
//            renderable.setTimeScale(timeScale);
//            ArrayList<HistogramOverviewRenderable> renderables = new ArrayList<HistogramOverviewRenderable>();
//            renderables.add(renderable);
//            view.renderOverview(renderables);

            return axes;
        }

        private void updateEvents() {
            if(!presenter.hasProgressIndicator()) {
                presenter.showProgressIndicator();
            }
            //sortEvents(proxyEvents);

            int size = detailedRenderables.size();
            int incr = size / 100;
            int count = 0;
            int state = 0;

            presenter.updateProgressIndicator(state);
            for (DetailedEventRenderable event : detailedRenderables) {
                if (event.getStartTime() == null) {
                  continue;
               }

                double num = timeScale.toNum(event.getStartTime());
                int x = (int) num;

                event.setX(x);

                DetailedEventProxy eventProxy = event.getEvent();
                if (event.getEndTime() != null) {
                    num = timeScale.toNum(event.getEndTime());
                    x = (int) num;
                    eventProxy.setEndX(x);
                }
//
                if (eventProxy.getRight() != null) {
                    eventProxy.setSpaceToRight((int) (timeScale.toNum(eventProxy.getRight()) - eventProxy.getX()) + 1);
                }

                if ((count % incr) == 0) {
                    state++;
                    presenter.updateProgressIndicator(state);
                }
            }

            presenter.hideProgressIndicator();

            this.view.render();
        }

        private EventBus getEventBus() {
            return this.eventBus;
        }

        public void setEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
        }

        @Override
        public void resetState() {

            renderables = null;
            axes = null;
            firstLoad = true;

            view.reset();
            view.setVisible(false);
        }

        @Override
        public void resize() {
            presenter.showProgressIndicator();
            view.updateOverviewTimeScale();
            renderOverview = true;
            adjustSummaries();
        }

        @Override
        public void updateColor(String colorValue, List<String> itemOrderList) {
            Integer color = presenter.getColor(colorValue);
            for(SummaryEventRenderable renderable: renderables){
                SummaryEventProxy event = renderable.getEvent();
                int eventColor = event.getColor();

                if(eventColor == color){
                    Set<String> colors = event.getEvent().getColors();
                    for(String priorityColor: itemOrderList){
                        if(colors.contains(priorityColor)){
                            event.getEvent().setColorValue(priorityColor);
                            event.setColor(presenter.getColor(event.getColorValue()));
                            break;
                        }
                    }
                } else if(event.getEvent().getColors().contains(colorValue)){
                    Set<String> colors = event.getEvent().getColors();
                    for(String priorityColor: itemOrderList){
                        if(colors.contains(priorityColor)){
                            event.getEvent().setColorValue(priorityColor);
                            event.setColor(presenter.getColor(event.getColorValue()));
                            break;
                        }
                    }
                }
            }

            this.view.redraw();
        }

        @Override
        public void select(TimelineEventSelection selection) {
            IntCollection selectedItems = selection.getSelectedItems();
            if(selectedItems == null) {
                return;
            }
            HashSet<Integer> selectedHash = new HashSet<>(selectedItems);

            if(detailedRenderables != null) {
                for(DetailedEventRenderable event: detailedRenderables){
                    if(selectedHash.contains(event.getEvent().getEvent().getEventDefinitionId())) {
                     event.setSelected(true);
                  }
                }
            }

            if(renderables != null) {
                for(SummaryEventRenderable event: renderables){
                    for(int eventId: event.getEvent().getEvent().getEventIds()){
                        if(selectedHash.contains(eventId)) {
                           event.setSelected(true);
                        }
                    }
                }
            }

            view.redraw();
        }

        public void clear() {
            resetState();
            setTimeScale(null);
        }

        @Override
        public void renderFooter() {
            view.renderFooter();
        }

        @Override
        public boolean hasItems() {
            return ((renderables != null) && (renderables.size() >0)) || ((detailedRenderables != null) && (detailedRenderables.size() > 0));
        }

        @Override
        public void tracksChanged() {
            adjustGroups(false);
        }
}
