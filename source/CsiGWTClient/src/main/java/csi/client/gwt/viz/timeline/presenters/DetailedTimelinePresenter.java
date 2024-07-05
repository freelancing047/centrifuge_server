package csi.client.gwt.viz.timeline.presenters;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import csi.client.gwt.viz.timeline.model.*;
import csi.client.gwt.viz.timeline.scheduler.DeferredLayoutCommand;
import csi.client.gwt.viz.timeline.view.DetailedTimelineView;
import csi.client.gwt.viz.timeline.view.TimelineView;
import csi.client.gwt.viz.timeline.view.drawing.*;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.selection.IntPrimitiveSelection;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.server.common.model.visualization.timeline.*;
import csi.shared.core.visualization.timeline.*;
import csi.shared.gwt.viz.timeline.TimeUnit;

import java.util.*;

public class DetailedTimelinePresenter extends AbstractTimelineChildPresenter implements TimelineChildPresenter{

    private static final int MIN_DURATION = 5;
    private DetailedTimelineView view;
    private List<DetailedEventProxy> proxyEvents = new ArrayList<>();

    private TimeScale timeScale;
    private EventBus eventBus;
    private TimelinePresenter presenter;

    private DeferredLayoutCommand command;

    DetailedTimelinePresenter(){
    }


    private EventBus getEventBus() {
        return this.eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    public void setup(TimelineResult result, TimelineViewDef viewDef, TimelineSettings settings, TimelineView view, TimelinePresenter presenter) {
        DetailedTimelineResult detailedResult = (DetailedTimelineResult) result;

        this.presenter = presenter;
        setupDetailedView(detailedResult, viewDef, settings, view);

    }

    private void setupDetailedView(final DetailedTimelineResult result, TimelineViewDef viewDef, TimelineSettings settings,
                                   TimelineView view) {
        if (settings != null) {
            List<TimelineLegendDefinition> legendItems = settings.getLegendItems();
            if (legendItems != null) {
                for (TimelineLegendDefinition definition : legendItems) {
                    presenter.getColors().put(definition.getValue(), definition.getColor());
                }
            }

            if (viewDef.getState() == null) {
                viewDef.setState(new TimelineCachedState());
                viewDef.getState().setTrackStates(new HashSet<>());
                //saveSettings(false);
            }

            view.getTimelineViewport().setStart(viewDef.getState().getScrollPosition());

            if (presenter.isUpdateSort()) {
                if (settings.getSortAscending() == null || settings.getSortAscending()) {
                    presenter.setSort(SortOrder.ASC);
                } else {
                    presenter.setSort(SortOrder.DESC);
                }
            } else {
                presenter.setUpdateSort(true);
            }
        }
        if (presenter.getSearchBox() != null) {
            presenter.setSearchTrackName(null);
        }
        presenter.hideFind();
        view.setTrackName(presenter.getTrack());
        
        if(result.isColorLimit() || result.isGroupLimit()) {
            ((DetailedTimelineView)getView()).showTypeLimitReached(result.isColorLimit());
            presenter.hideProgressIndicator();
            presenter.hideGroups();
            presenter.hideLegend();
        } else if (result.getEvents().size() > 0) {
            view.show();
            presenter.setTrackTotalHeight(null);
            Scheduler.get().scheduleDeferred(() -> {
                processSingularResult(result, (DetailedTimelineView)getView());
                presenter.addLegend();
                presenter.addGroups();
                //We persist the legend colors after
                presenter.updateLegendSettings();
                presenter.getLegendPresenter().reset();
                presenter.getLegendPresenter().show(false);
            });
        } else {
            //            if (result.isLimitReached()) {
            //                showWarningDialog = true;
            //                getView().showLimitReached(result.getEventsFound(), result.getEventMax());
            //                hideProgressIndicator();
            //                hideGroups();
            //                hideLegend();
            //            } else {
            presenter.setShowWarningDialog(false);
            view.showEmpty();
            presenter.hideProgressIndicator();
            //            }
        }
    }

    private void processSingularResult(DetailedTimelineResult result, DetailedTimelineView view) {

        if(presenter.showLimitMessage(result)){
            return;
        }

        List<TimelineTrack> tracks = result.getTracks();
        if (tracks == null) {
            tracks = new ArrayList<>();
        }

        Set<String> colorsToKeep = new HashSet<>();

        //Create tracks
        presenter.setTrackModels(new HashMap<>());
        final List<TimelineTrackRenderable> trackRenderables = new ArrayList<>();

        presenter.validateVisualizationSettings();

        presenter.showHideMenus(tracks.size());

        processTracks(tracks, colorsToKeep, trackRenderables);

        TimelineEventSelection selection = presenter.getVisualizationDef().getSelection();

        processEvents(result, view, colorsToKeep, trackRenderables, selection);


    }

    private void refactorTrackHeight() {

        Set<String> keys = presenter.getTrackModels().keySet();
        List<String> list = presenter.asSortedList(keys, presenter.getSort());
        presenter.removeHiddenTracks(list);
        if (list.size() == 0) {
            getView().getTimelineViewport().setTotalHeight(1);
            getView().updateViewport(1);
            getView().render();
            return;
        }
        TimelineTrackModel lastTrack = null;
        for (String trackName : list) {

            TimelineTrackModel track = (TimelineTrackModel) presenter.getTrackModels().get(trackName);
            if (!track.isVisible()) {
                continue;
            }

            double trackTop;
            if (lastTrack == null) {
                trackTop = 0;
            } else {
                trackTop = lastTrack.getTop();
                if (lastTrack.isCollapsed()) {
                    trackTop += lastTrack.getCollapsedHeight();
                } else {
                    trackTop += lastTrack.getHeight();
                }
            }

            int yDiff = (int) (trackTop) - track.getStartY();

            track.setTop(trackTop);
            track.setStartY((int) (trackTop));
            double trackHeight;

            if (track.isCollapsed()) {
                trackHeight = track.getCollapsedHeight();
            } else {
                trackHeight = track.getHeight();
            }

            track.setEndY((int) ((track.getTop() + trackHeight)));

            lastTrack = track;

            for (DetailedEventProxy proxyEvent : track.getEvents()) {
                proxyEvent.setY(proxyEvent.getY() + yDiff);
            }

        }

        if (presenter.getTrackModels().keySet().size() == 1 && lastTrack.getHeight() < 1.0) {
            lastTrack.setHeight(1.0);
        }
        double trueHeight = lastTrack.getEndY(); //- getView().getTimelineViewportStart();
        getView().getTimelineViewport().setTotalHeight(trueHeight);
        getView().updateViewport(trueHeight);
        getView().render();

    }

    public void setCommand(DeferredLayoutCommand command) {
        this.command = command;
    }

    @Override
    public void cancel(){
        if (command != null) {
            command.setInterrupt(true);
            command = null;
        }
    }

    private void postLayout(List<TimelineTrackRenderable> trackRenderables,
                            List<DetailedEventRenderable> renderables, TimeScale timeScale, List<Axis> axes) {

        List<DetailedOverviewRenderable> overviewRenderables = new ArrayList<>();
        for (DetailedEventRenderable eventRenderable : renderables) {
            if (command == null || command.isInterrupt()) {
                return;
            }
            DetailedOverviewRenderable overviewRenderable = new DetailedOverviewRenderable(eventRenderable);
            overviewRenderable.setTimeScale(timeScale);
            overviewRenderable.setViewport(getView().getTimelineViewport());
            eventRenderable.setViewport(getView().getTimelineViewport());
            overviewRenderables.add(overviewRenderable);
        }

        getView().renderOverview(overviewRenderables);

        getView().render(trackRenderables, renderables, axes, false);
    }


    private double calculateSize(DetailedTimelineResult result, SingularTimelineEvent event) {

        if (event.getDotSize() == null) {
            return 1;
        }

        double size = (event.getDotSize() - result.getMin()) / (result.getMax() - result.getMin()) * TimelinePresenter.MAX_DOT_SIZE;

        double trueSize = (size);

        if (trueSize > TimelinePresenter.MAX_DOT_SIZE) {
            trueSize = TimelinePresenter.MAX_DOT_SIZE;
        } else if (trueSize < 1) {
            trueSize = 1;
        }

        return trueSize;
    }
    
    private void processEvents(DetailedTimelineResult result, DetailedTimelineView view, Set<String> colorsToKeep,
                               final List<TimelineTrackRenderable> trackRenderables, IntPrimitiveSelection selection) {
        //Create Events
        List<SingularTimelineEvent> events = result.getEvents();
        final List<DetailedEventRenderable> renderables = new ArrayList<>();

        DetailedEventRenderable smallestStartTime = null;
        DetailedEventRenderable greatestStartTime = null;
        DetailedEventRenderable greatestEndTime = null;

        proxyEvents = new ArrayList<>();
        //TODO: Schedule Deferred possibly
        for (SingularTimelineEvent event : events) {

            DetailedEventProxy eventProxy = new DetailedEventProxy(event);
            if (selection.getSelectedItems().contains(event.getEventDefinitionId())) {
                eventProxy.setSelected(true);
            }
            proxyEvents.add(eventProxy);
            if (result.getMin() == result.getMax()) {
                eventProxy.setDrawableSize(2);
            } else {
                eventProxy.setDrawableSize(calculateSize(result, event));
            }
            TimelineTrackModel trackModel = (TimelineTrackModel) presenter.getTrackModels().get(event.getTrackValue());
            if (trackModel == null) {
                trackModel = new TimelineTrackModel();
                trackModel.setLabel(TimelineTrack.EMPTY_TRACK);
                presenter.getTrackModels().put(TimelineTrack.EMPTY_TRACK, trackModel);
                TimelineTrackRenderable track = new TimelineTrackRenderable(trackModel);
                track.setViewport(getView().getTimelineViewport());
                trackRenderables.add(track);
                trackModel.setSummary(presenter.getVisualizationDef().getTimelineSettings().getShowSummary());
                trackModel.setGroupSpace(presenter.getVisualizationDef().getTimelineSettings().getGroupNameSpace());
                trackModel.setCollapsed(true);
                for (TimelineTrackState state : presenter.getVisualizationDef().getState().getTrackStates()) {
                    if (state.getTrackName() != null && state.getTrackName().equals(TimelineTrack.EMPTY_TRACK)) {
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

            if (smallestStartTime == null || smallestStartTime.getStartTime() == null) {
                smallestStartTime = eventRenderable;
            }
            if (greatestStartTime == null || greatestStartTime.getStartTime() == null) {
                greatestStartTime = eventRenderable;
            }
            if (greatestEndTime == null || greatestEndTime.getEndTime() == null) {
                greatestEndTime = eventRenderable;
            }

            if (eventRenderable.getStartTime() != null && smallestStartTime.compareTo(eventRenderable) > 0) {
                smallestStartTime = eventRenderable;
            }

            if (greatestStartTime.compareTo(eventRenderable) < 0) {
                greatestStartTime = eventRenderable;
            }

            if (greatestEndTime.compareEnd(eventRenderable) < 0) {
                greatestEndTime = eventRenderable;
            }

            colorsToKeep.add(event.getColorValue());
            eventProxy.setColor(presenter.getColor(event.getColorValue()));

            renderables.add(eventRenderable);

        }

        presenter.validateAndUpdateColorLegend(colorsToKeep);
        //TODO: do we need to show legend here or are we good?

        //Create Time Interval
        long endTime = 0L;
        if (greatestStartTime.getStartTime() != null && (greatestEndTime.getEndTime() == null || greatestStartTime.getStartTime() > greatestEndTime.getEndTime())) {
            endTime = greatestStartTime.getStartTime();
        } else if (greatestEndTime.getEndTime() != null) {
            endTime = greatestEndTime.getEndTime();
        }

        long startTime = smallestStartTime.getStartTime();

        long duration = endTime - startTime;

        if (duration < MIN_DURATION) {
            duration = MIN_DURATION;
            startTime--;
            endTime++;
        }
        //Pad the start and end time to make sure all events fit properly in initial view.
        double padTime = TimeUnit.calculateTimePadding(duration);
        startTime = (long) (startTime - padTime);
        endTime = (long) (endTime + padTime);

        //we have initial start/end times we can reference later
        presenter.setInitialStartTime(startTime);
        presenter.setInitialEndTime(endTime);

        TimelineViewDef viewDef = presenter.getVisualizationDef();
        TimelineCachedState state = viewDef.getState();
        if (state != null) {
            if (state.getStartPosition() > startTime)
                startTime = state.getStartPosition();

            if (state.getEndPosition() < endTime)
                endTime = state.getEndPosition();
        }

        Interval overviewInterval = new Interval(presenter.getInitialStartTime(), presenter.getInitialEndTime());
        final TimeScale timeScale = view.setOverviewRange(overviewInterval, new TimelineSlider());
        

        Interval interval = new Interval(startTime, endTime);
        presenter.setCurrentInterval(interval);
        final List<Axis> axes = presenter.updateTimeScale(interval);

        Scheduler.get().scheduleDeferred(() -> {
            layoutTracks();
            postLayout(trackRenderables, renderables, timeScale, axes);
            presenter.updateProgressIndicator(75);
        });
    }


    

    private void processTracks(List<TimelineTrack> tracks, Set<String> colorsToKeep,
                               final List<TimelineTrackRenderable> trackRenderables) {
        for (TimelineTrack trackName : tracks) {
            TimelineTrackModel trackModel = new TimelineTrackModel();

            trackModel.setSummary(presenter.getVisualizationDef().getTimelineSettings().getShowSummary());
            trackModel.setGroupSpace(presenter.getVisualizationDef().getTimelineSettings().getGroupNameSpace());
            trackModel.setCollapsed(true);
            trackModel.setVisible(true);
            for (TimelineTrackState state : presenter.getVisualizationDef().getState().getTrackStates()) {
                if (state.getTrackName() != null && state.getTrackName().equals(trackName.getName())) {
                    trackModel.setCollapsed(state.getCollapse());
                    if (state.getVisible() == null) {
                        state.setVisible(true);
                    }
                    trackModel.setVisible(state.getVisible());
                    break;
                }
            }

            trackModel.setLabel(trackName.getName());
            presenter.getTrackModels().put(trackName.getName(), trackModel);
            TimelineTrackRenderable track = new TimelineTrackRenderable(trackModel);
            track.setViewport(getView().getTimelineViewport());
            if (trackName.getColor() == 0) {
                track.setTrackColor(0);
            } else {
                track.setTrackColor(presenter.getColor(trackName.getName()));
                colorsToKeep.add(trackName.getName());
            }
            trackRenderables.add(track);
        }
        
      //Clears out track states that don't exist in data
        presenter.cleanTrackStates();

        //Case of no groupings, make one track
        if (tracks.size() == 0) {
            TimelineTrackModel trackModel = new TimelineTrackModel();
            trackModel.setLabel(TimelineTrack.NULL_TRACK);
            presenter.getTrackModels().put(TimelineTrack.NULL_TRACK, trackModel);
            TimelineTrackRenderable track = new TimelineTrackRenderable(trackModel);
            track.setViewport(getView().getTimelineViewport());
            trackRenderables.add(track);
            trackModel.setCollapsed(false);
            trackModel.setVisible(true);
            trackModel.setSummary(false);
            trackModel.setGroupSpace(false);
            trackModel.setAllowCollapse(false);
            track.setBackground(false);
        }
    }
    
    private void layoutTracks() {
        if (presenter.getTrackTotalHeight() == null) {
            if (command != null) {
                command.setInterrupt(true);
                command = null;
            }

            presenter.showProgressIndicator();
            presenter.updateProgressIndicator(50);
            presenter.setTrackTotalHeight((double) 0);
            Set<String> keys = presenter.getTrackModels().keySet();
            List<String> list = presenter.asSortedList(keys, presenter.getSort());
            if (list.size() == 0) {
                presenter.updateProgressIndicator(100);

                getView().getTimelineViewport().setTotalHeight(1);
                getView().updateViewport(1);
                getView().updateOverviewTimeScale();
                getView().render();
                presenter.hideProgressIndicator();
                if (presenter.isShowWarningDialog()) {
                    // TODO ???  shouldn't this be no data?
                    getView().showLimitReached();
                    return;
                }
            }

            double min = getView().getTimelineHeight() == 0 ? 0 : 30. / getView().getTimelineHeight();
            boolean shadow = false;

            for (String trackKey : list) {

                TimelineTrackModel trackModel = (TimelineTrackModel) presenter.getTrackModels().get(trackKey);
                List<DetailedEventProxy> events = trackModel.getEvents();
                //To lay out efficiently, we sort the model by time
                events = sortEvents(events);
                trackModel.setShadow(shadow);
                shadow = !shadow;

                command = new DeferredLayoutCommand(view);

                command.setTrack(trackModel);
                command.setEvents(trackModel.getEvents());
                command.setPresenter(presenter);
//                command.setTrack(trackModel);
                command.setOrderedTrackList(list);
                command.setTrackModels(presenter.getTrackModels());
                command.setEventBus(eventBus);
                setCommand(command);

                Scheduler.get().scheduleEntry(command);
                //We only do the first one in list, command handles rest
                break;

            }

        }
    }
    
    private void updateEvents() {
        if(!presenter.hasProgressIndicator()) {
            presenter.showProgressIndicator();
        }
        Interval currentInterval = presenter.getCurrentInterval();

        List<Axis> axes = updateTimeScale(currentInterval);
        updateTimeScale(currentInterval);
        //sortEvents(proxyEvents);

        int size = proxyEvents.size();
        int incr = size / 100;
        int count = 0;
        int state = 0;

        presenter.updateProgressIndicator(state);
        for (DetailedEventProxy event : proxyEvents) {
            if (!event.isVisible() || event.getStartTime() == null)
                continue;

            double num = timeScale.toNum(event.getStartTime());
            int x = (int) num;

            event.setX(x);

            if (event.getEndTime() != null) {
                num = timeScale.toNum(event.getEndTime());
                x = (int) num;
                event.setEndX(x);
            }
            
            if (event.getRight() != null) {
                event.setSpaceToRight((int) (timeScale.toNum(event.getRight()) - event.getX()) + 1);
            }

            if (count % incr == 0) {
                state++;
                presenter.updateProgressIndicator(state);
            }
        }

        presenter.hideProgressIndicator();
        
        this.view.render(axes);
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

    public static List<DetailedEventProxy> sortEvents(List<DetailedEventProxy> events) {
        Collections.sort(events, (o1, o2) -> {

            if (o1.getStartTime() == null) {
                return -1;
            }
            if (o2.getStartTime() == null) {
                return 1;
            }
            if (o1.getStartTime().longValue() == o2.getStartTime().longValue()) {
                try {
                    if (o1.getEvent().getEventDefinitionId() >= o2.getEvent().getEventDefinitionId()) {
                        return 1;
                    } else {
                        return -1;
                    }
                } catch (Exception exception) {
                    //no-op
                }
            }
            return o1.getStartTime().longValue() >= o2.getStartTime().longValue() ? 1 : -1;
        });

        return events;
    }


//    @Override
//    public void updateSelection(TimelineEventSelection selection) {
//        int[] newIds = new int[proxyEvents.size()];
//        if(selection != null){
//            int count = 0;
//            for (DetailedEventProxy eventProxy : proxyEvents) {
//                if (eventProxy.isSelected()) {
//                    newIds[count] = eventProxy.getEvent().getEventDefinitionId();
//                    count++;
//                }
//            }
//            
//            //Zero is an invalid event id, we have it from the array above, and can remove it safely
//            selection.getSelectedItems().addAll(newIds);
//            selection.getSelectedItems().deDupe();
//            selection.getSelectedItems().remove(0);
//        }
//    }

    public List<DetailedEventProxy> getEvents() {
        return this.proxyEvents;
    }


    @Override
    public void clearSelection() {
        for (DetailedEventProxy event : proxyEvents) {
            event.setSelected(false);
        }
    }


    @Override
    public void selectText(String text) {
        for (DetailedEventProxy event : proxyEvents) {
            String colorValue = event.getEvent().getColorValue();
            if ((colorValue == null || colorValue.equals(TimelineTrack.NULL_TRACK)) && text.equals(TrackRenderable.NO_VALUE)) {
                event.setSelected(true);
            } else {
                if (colorValue.equals(text)) {
                    event.setSelected(true);
                } else {
                    event.setSelected(false);
                }
            }
        }
    }
    
    
    @Override
    public void resetZoom() {

        updateEvents();
    }


    @Override
    public void zoomIn() {
        updateEvents();
        
    }


    @Override
    public void panToNextEvent() {
        updateEvents();
        
    }


    @Override
    public void adjustGroups(boolean overviewRefresh) {

        refactorTrackHeight();

        getView().redraw();
    }


    @Override
    public void selectAll() {

        for (DetailedEventProxy event : proxyEvents) {
            if (event.getTrack().isVisible())
                event.setSelected(true);
        }

    }


    @Override
    public boolean hasSelection() {

        for (DetailedEventProxy event : proxyEvents) {
            if (event.isSelected()) {
                return true;
            }
        }
        
        return false;
    }


    @Override
    public int getSearchHitCount(String text) {
        int searchHits = 0;
        for (DetailedEventProxy event : proxyEvents) {
            String label = event.getEvent().getLabel();
            if (text.length() != 0 && label != null && label.toUpperCase().contains(text)) {
                event.setSearchHit(true);
                if (event.getTrack().isVisible())
                    searchHits++;
            } else {
                event.setSearchHit(false);
                event.setSearchHighlight(false);
            }
        }
        return searchHits;
    }


    @Override
    public void updateOverview() {
        updateEvents();
    }


    @Override
    public TimeScale getTimeScale() {
        return timeScale;
    }


    @Override
    public void updateTimeScale() {
        updateEvents();
    }


    @Override
    public void zoom() {

        updateEvents();
    }


    @Override
    public void setTimeScale(TimeScale timeScale) {
        this.timeScale = timeScale;
    }

//
//    @Override
//    public void selectByText(String text, boolean removeExistingSelection) {
//        List<Integer> deselected = new ArrayList<Integer>();
//        for (DetailedEventProxy event : proxyEvents) {
//            String colorValue = event.getEvent().getColorValue();
//            if ((colorValue == null || colorValue.equals(TimelineTrack.NULL_TRACK)) && text.equals(TimelineTrackRenderable.NO_VALUE)) {
//                event.setSelected(true);
//            } else {
//                if (colorValue.equals(text)) {
//                    event.setSelected(true);
//                } else {
//                    if (removeExistingSelection) {
//                        event.setSelected(false);
//                        deselected.add(event.getEvent().getRowId());
//                    }
//
//                }
//            }
//        }
//        
//
//        presenter.getVisualizationDef().getSelection().getSelectedItems().removeAll(deselected);
//    }

//
//    @Override
//    public void deselectByText(String text) {
//
//        List<Integer> deselected = new ArrayList<Integer>();
//        for (DetailedEventProxy event : proxyEvents) {
//            String colorValue = event.getEvent().getColorValue();
//            if ((colorValue == null || colorValue.equals(TimelineTrack.NULL_TRACK)) && text.equals(TimelineTrackRenderable.NO_VALUE)) {
//                event.setSelected(false);
//                deselected.add(event.getEvent().getRowId());
//            } else {
//                if (colorValue.equals(text)) {
//                    event.setSelected(false);
//                    deselected.add(event.getEvent().getRowId());
//                }
//            }
//        }
//        
//        presenter.getVisualizationDef().getSelection().getSelectedItems().removeAll(deselected);
//    }


    @Override
    public void scroll() {
        adjustGroups(false);
    }


    @Override
    public void resetState() {

        proxyEvents = new ArrayList<>();
        
        view.reset();
        view.setVisible(false);
    }


    public TimelineView createView(TimelinePresenter timelinePresenter) {
//        if(this.view == null){
            this.view = new DetailedTimelineView(timelinePresenter);
//        }
        return this.view;
    }


    @Override
    public void resize() {
        layoutTracks();
    }


    @Override
    public void updateColor(String colorValue, List<String> itemOrderList) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void select(TimelineEventSelection selection) {
        for(DetailedEventProxy event: proxyEvents){
            if(selection.getSelectedItems().contains(event.getEvent().getEventDefinitionId()))
                event.setSelected(true);
        }
        
        view.redraw();
    }


    public void clear() {
        resetState();
        setTimeScale(null);
    }


    @Override
    public void renderFooter() {
        getView().renderFooter();
    }


    @Override
    public TimelineView getView() {
        return view;
    }


    @Override
    public boolean hasItems() {
        // TODO Auto-generated method stub
        return proxyEvents != null && proxyEvents.size() > 0;
    }


    @Override
    public void tracksChanged() {
        adjustGroups(false);
    }


}
