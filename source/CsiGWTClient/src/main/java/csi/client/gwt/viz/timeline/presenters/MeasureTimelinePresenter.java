package csi.client.gwt.viz.timeline.presenters;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.Interval;
import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.model.measured.MeasuredTrackProxy;
import csi.client.gwt.viz.timeline.scheduler.CancelRepeatingCommand;
import csi.client.gwt.viz.timeline.utilities.NaturalTrackComparator;
import csi.client.gwt.viz.timeline.view.TimelineView;
import csi.client.gwt.viz.timeline.view.drawing.OverviewRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TimelineSlider;
import csi.client.gwt.viz.timeline.view.drawing.TrackRenderable;
import csi.client.gwt.viz.timeline.view.measured.MeasuredTimelineView;
import csi.client.gwt.viz.timeline.view.measured.drawing.HistogramOverviewRenderable;
import csi.client.gwt.viz.timeline.view.measured.drawing.MeasuredTrackRenderable;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.server.common.model.visualization.timeline.TimelineCachedState;
import csi.server.common.model.visualization.timeline.TimelineSettings;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.common.service.api.ChronosActionsServiceProtocol;
import csi.shared.core.visualization.timeline.*;
import csi.shared.gwt.viz.timeline.TimeUnit;
import csi.shared.gwt.viz.timeline.TimelineRequest;

import java.util.*;

public class MeasureTimelinePresenter implements TimelineChildPresenter, HistogramOverview{

    private static final int MIN_DURATION = 5;
    private MeasuredTimelineView view;
    private List<MeasuredTrackProxy> proxyTracks;
    private List<Axis> axes = null;

    private TimeScale timeScale;
    private EventBus eventBus;
    private TimelinePresenter presenter;
    private CancelRepeatingCommand scrollCommand = null;
    private VortexEventHandler<TimelineResult> loadTimelineHandler;
    private LoadTimelineCommand loadTimelineCommand;
    private ArrayList<MeasuredTrackProxy> hiddenProxies = new ArrayList<MeasuredTrackProxy>();
    private Set<String> filteredKeys = new HashSet<String>();
    private static final double TIME_PADDING = .05;

    public void createOverview(){

    }

    public void doPage(MeasuredTimelineResult result){

        List<MeasuredTrackProxy> proxyTracks = createProxies(result.getMeasuredTracks(), result.getStartGroup());
        this.proxyTracks = proxyTracks;

        axes = updateTimeScale(presenter.getCurrentInterval());
        List<MeasuredTrackRenderable> trackRenderables = createTrackRenderables(proxyTracks, axes);
        this.view.render(trackRenderables, trackRenderables, axes, false);
        this.view.getTimelineViewport().setCurrentWidth(this.view.getOffsetWidth());
        //this.view.updateOverviewTimeScale();
        this.view.render();
    }

    public void setup(TimelineResult result, TimelineViewDef viewDef, TimelineSettings settings, TimelineView view, int width, TimelinePresenter presenter) {
        this.view = (MeasuredTimelineView) view;

        MeasuredTimelineResult measuredResult = (MeasuredTimelineResult) result;
        this.presenter = presenter;

        List<MeasuredTrack> measuredTracks = measuredResult.getMeasuredTracks();
        HashMap<String, CommonTrack> trackModels = new HashMap<String, CommonTrack>();
        for(MeasuredTrack track: measuredTracks) {
            trackModels.put(track.getName(), track);
            if(filteredKeys.contains(track.getName())) {
                track.setVisible(false);
            }
        }
        
        filteredKeys.clear();

        this.presenter.setTrackModels(trackModels);
        List<MeasuredTrackProxy> proxyTracks = createProxies(measuredTracks, measuredResult.getStartGroup());
        this.proxyTracks = proxyTracks;

        presenter.updateGroupLegendItems();

        TimelineCachedState state = viewDef.getState();
        if(result.getOverviewData() != null){
            axes  = createHistogramOverview(result.getOverviewData(), state);
        } else {
            Interval interval = new Interval(result.getLowerTimeBound(), result.getUpperTimeBound());
            axes = updateTimeScale(interval);
        }

        List<MeasuredTrackRenderable> trackRenderables = createTrackRenderables(proxyTracks, axes);

        this.view.render(trackRenderables, trackRenderables, axes, false);
        int totalHeight = measuredResult.getGroupCount() * TrackRenderable.FULL_SUMMARY_HEIGHT;
        this.view.getTimelineViewport().setTotalHeight(totalHeight);
        this.view.updateViewport(totalHeight);

        this.view.getTimelineViewport().setCurrentWidth(width);
        this.view.updateOverviewTimeScale();
        this.view.render();

        presenter.showHideMenus(trackRenderables.size());


        presenter.addGroups();
        presenter.getLegendPresenter().reset();
        presenter.updateLegendSettings();
        presenter.getLegendPresenter().show(false);
    }

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
        ArrayList<OverviewRenderable> renderables = new ArrayList<OverviewRenderable>();
        renderables.add(renderable);
        view.renderOverview(renderables);

        return axes;
    }

    private List<MeasuredTrackRenderable> createTrackRenderables(List<MeasuredTrackProxy> proxyTracks, List<Axis> axes) {
        List<MeasuredTrackRenderable> tracks = new ArrayList<MeasuredTrackRenderable>();
        //Use higher fidelity axis
        Axis axis = axes.get(axes.size()-1);    

        for(MeasuredTrackProxy proxy: proxyTracks){
            proxy.setAxis(axis);

            MeasuredTrackRenderable renderable = new MeasuredTrackRenderable(proxy);
            renderable.setViewport(view.getTimelineViewport());
            tracks.add(renderable);
        }

        return tracks;
    }

    private List<MeasuredTrackProxy> createProxies(List<MeasuredTrack> measuredTracks, int start) {
        List<MeasuredTrackProxy> proxyTracks = new ArrayList<MeasuredTrackProxy>();
        

        List<MeasuredTrack> list = sort(measuredTracks, presenter.getSort());   
        
        int index = 0;
        for(MeasuredTrack track: list){
            MeasuredTrackProxy trackProxy = new MeasuredTrackProxy(track);
            track.setIndex(index);
            index++;
            if(start % 2 == 0){
                trackProxy.setBanding(true);
            }
            proxyTracks.add(trackProxy);
            start++;
        }
        
   
        
        return proxyTracks;
    }

    private List<MeasuredTrack> sort(List<MeasuredTrack> measuredTracks, SortOrder sort) {
        Collections.sort(measuredTracks, new NaturalTrackComparator());
        if (sort == SortOrder.DESC) {
            Collections.reverse(measuredTracks);
        }
        return measuredTracks;
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

    private EventBus getEventBus() {
        return this.eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void cancel() {
        if(loadTimelineHandler != null) {
            loadTimelineHandler.onCancel();
        }
        if(loadTimelineCommand != null) {
            loadTimelineCommand.cancel();
        }
    }

    @Override
    public void clearSelection() {
        presenter.showProgressIndicator();
        adjustGroups(false);

    }

    @Override
    public void selectText(String text) {
        // TODO Auto-generated method stub

    }

    @Override
    public void resetZoom() {
        presenter.showProgressIndicator();
        adjustGroups(false);

    }

    @Override
    public void zoomIn() {
        presenter.showProgressIndicator();
        adjustGroups(false);

    }


    @Override
    public void zoom() {
        presenter.showProgressIndicator();
        adjustGroups(false);

    }


    @Override
    public void panToNextEvent() {
        // TODO Auto-generated method stub

    }

    @Override
    public void adjustGroups(boolean overviewRefresh) {
        cancel();
        int start = getView().getTimelineViewport().getStart();

        TimelineRequest request = presenter.buildTimelineRequest();

        for (String key : presenter.getTrackModels().keySet()) {
            if (!presenter.getTrackModels().get(key).isVisible()) {
                request.getFilteredKeys().add(key);
            }
        }
        request.setCalculateOverview(false);
        request.setStartGroupIndex(Math.abs(start / TrackRenderable.FULL_SUMMARY_HEIGHT));

        Interval currentInterval = presenter.getCurrentInterval();
        request.setStartTime(currentInterval.start);
        request.setEndTime(currentInterval.end);
        axes = updateTimeScale(presenter.getCurrentInterval());
        view.render(axes);

        VortexFuture<TimelineResult> future = presenter.getVortex().createFuture();
        loadTimelineHandler = new VortexEventHandler<TimelineResult>() {
            private boolean cancelled = false;

            @Override
            public void onSuccess(TimelineResult result) {
                if (cancelled) {
                    return;
                }
                MeasuredTimelineResult measuredResult = (MeasuredTimelineResult) result;

                List<MeasuredTrack> measuredTracks = measuredResult.getMeasuredTracks();
                HashMap<String, CommonTrack> trackModels = new HashMap<String, CommonTrack>();
                for (MeasuredTrack track : measuredTracks) {
                    trackModels.put(track.getName(), track);
                }

                presenter.setTrackModels(trackModels);
                refactorTrackHeights(measuredTracks);
                doPage(measuredResult);
                if (overviewRefresh) {
                    view.updateOverviewTimeScale();
                    view.redraw();
                }
                presenter.hideProgressIndicator();
            }

            @Override
            public boolean onError(Throwable t) {
                if (!cancelled) {
                    proxyTracks.clear();
                    if (overviewRefresh) {
                        view.redraw();
                    }
                    view.render();
                }
                presenter.hideProgressIndicator();
                return false;
            }

            @Override
            public void onUpdate(int taskProgess, String taskMessage) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onCancel() {
                cancelled = true;
            }
        };
        future.addEventHandler(loadTimelineHandler);

        this.loadTimelineCommand = new LoadTimelineCommand(future, request);
        Scheduler.get().scheduleDeferred(loadTimelineCommand);
    }

    private MeasuredTimelineView getView() {
        return view;
    }

    public MeasuredTimelineView createView(TimelinePresenter presenter){
        view = new MeasuredTimelineView(presenter);
        return view;
    }

    @Override
    public void selectAll() {
        presenter.showProgressIndicator();
        adjustGroups(false);
    }
    
    private void refactorTrackHeights(List<MeasuredTrack> measuredTracks) {
        int minTrackHeight = 40;
        int maxTrackHeight = 120;
        double totalTrackHeight = measuredTracks.size()*minTrackHeight;
        double range = maxTrackHeight - minTrackHeight;
        int minTrackValue = Integer.MAX_VALUE;
        int maxTrackValue = 0;
        int index = 0;
        for (MeasuredTrack measuredTrack : measuredTracks) {
            if (measuredTrack == null) {
                continue;
            }

            measuredTrack.setIndex(index);
            index++;
            if (!measuredTrack.isVisible()) {
                continue;
            }
            int trackValue = 0;
            for (MeasuredTrackItem trackItem : measuredTrack.getMeasures()) {
                trackValue += trackItem.getValue();
            }
            if (trackValue < minTrackValue) {
                minTrackValue = trackValue;
            }
            if (trackValue > maxTrackValue) {
                maxTrackValue = trackValue;
            }
        }
        for (MeasuredTrack measuredTrack : measuredTracks) {
            if (measuredTrack == null || !measuredTrack.isVisible()) {
                totalTrackHeight -= minTrackHeight;
                continue;
            }

            int trackValue = 0;
            for (MeasuredTrackItem trackItem : measuredTrack.getMeasures()) {
                trackValue += trackItem.getValue();
            }
            double trackHeight = 0;
            if (maxTrackValue-minTrackValue != 0) {
                trackHeight = range * ((double) (trackValue - minTrackValue) / (double) (maxTrackValue-minTrackValue));
            }
            totalTrackHeight += trackHeight;
            measuredTrack.setHeight(trackHeight+minTrackHeight);
        }


        view.setTimelineHeight(Math.max(getView().getZoomDrawingPanel().getHeight(),(int) Math.ceil(totalTrackHeight)));
        
    }
    
    protected class LoadTimelineCommand implements ScheduledCommand{
        private boolean cancelled;
        private VortexFuture<TimelineResult> future;
        private TimelineRequest request;
        
        LoadTimelineCommand(VortexFuture<TimelineResult> future, TimelineRequest request) {
            this.future = future;
            this.request = request;
        }

        @Override
        public void execute() {
            if(!cancelled) {
                try {
                    future.execute(ChronosActionsServiceProtocol.class).loadTimeline(request);
                }  catch (CentrifugeException ignored) {
                }
            }
        }
        
        public void cancel() {
            cancelled = true;
        }
    }

    @Override
    public boolean hasSelection() {
        // TODO Auto-generated method stub
        for (MeasuredTrackProxy proxyTrack : proxyTracks) {
            for (MeasuredTrackItem trackItem : proxyTrack.getTrack().getMeasures()) {
                if (trackItem.isSelected()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getSearchHitCount(String text) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void updateOverview() {
        axes = updateTimeScale(presenter.getCurrentInterval());
        presenter.showProgressIndicator();
        adjustGroups(false);
    }


    @Override
    public TimeScale getTimeScale() {
        return timeScale;
    }

    @Override
    public void updateTimeScale() {

        presenter.showProgressIndicator();
        axes = updateTimeScale(presenter.getCurrentInterval());
        adjustGroups(false);

    }


    @Override
    public void setTimeScale(TimeScale timeScale) {
        this.timeScale = timeScale;
    }


    //    @Override
    //    public void selectByText(String text, boolean removeExistingSelection) {
    //        // TODO Auto-generated method stub
    //
    //    }
    //
    //    @Override
    //    public void deselectByText(String text) {
    //        // TODO Auto-generated method stub
    //
    //    }

    @Override
    public void scroll() {
        /*if(scrollCommand != null){
            scrollCommand.cancel();
        }
        scrollCommand = new CancelRepeatingCommand(){

            @Override
            public boolean execute() {

                if(isCancel()){
                    return false;
                } else {
                    adjustGroups(false);
                }

                return false;
            }
            };
        Scheduler.get().scheduleFixedDelay(scrollCommand, 500);*/

    }

    @Override
    public void resetState() {

        proxyTracks = null;
        axes = new ArrayList<Axis>();

        view.reset();
        view.setVisible(false);
    }

    @Override
    public void resize() {
        presenter.showProgressIndicator();
        adjustGroups(true);
    }

    @Override
    public void updateColor(String colorValue, List<String> itemOrderList) {
        // TODO Auto-generated method stub

    }

    @Override
    public void select(TimelineEventSelection selection) {
        // TODO Auto-generated method stub

    }

    public void clear() {
        resetState();
        setTimeScale(new TimeScale());
    }

    @Override
    public void renderFooter() {
        getView().renderFooter();
    }

    @Override
    public boolean hasItems() {
        // TODO Auto-generated method stub
        return proxyTracks != null && proxyTracks.size() > 0;
    }

    @Override
    public void tracksChanged() {
//        Set<String> keys = new HashSet<String>();
//        for(String key: presenter.getTrackModels().keySet()) {
//            if(presenter.getTrackModels().get(key).isVisible()) {
//                keys.add(key);
//            }
//        }
//
//        proxyTracks.addAll(hiddenProxies);
//        List<MeasuredTrackProxy> visibleProxies = new ArrayList<MeasuredTrackProxy>();
//        hiddenProxies = new ArrayList<MeasuredTrackProxy>();
//        
        
        List<MeasuredTrack> measuredTracks = new ArrayList<MeasuredTrack>();
        for(String key: presenter.getTrackModels().keySet()) {
            measuredTracks.add((MeasuredTrack) presenter.getTrackModels().get(key));            
        }
        
        
        List<MeasuredTrackProxy> tracks = createProxies(measuredTracks, 0);

        this.proxyTracks = tracks;
        
        
        refactorTrackHeights(measuredTracks);
//        
//        proxyTracks = visibleProxies;
        updateRenderables();
    }

    private void updateRenderables() {
        List<MeasuredTrackRenderable> trackRenderables = createTrackRenderables(proxyTracks, axes);
        this.view.render(trackRenderables, trackRenderables, axes, false);
        this.view.getTimelineViewport().setCurrentWidth(this.view.getOffsetWidth());
        //this.view.updateOverviewTimeScale();
        this.view.render();
    }

    public void rememberTrackStates(HashMap<String, CommonTrack> trackModels) {
        for(String key: presenter.getTrackModels().keySet()) {
            if(!presenter.getTrackModels().get(key).isVisible()) {
                filteredKeys.add(key);
            }
        }
    }


}
