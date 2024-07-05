package csi.client.gwt.viz.timeline.view;

import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ResizeComposite;

import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.Interval;
import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.model.ViewPort;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.viz.timeline.view.drawing.OverviewRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TimelineOverview;
import csi.client.gwt.viz.timeline.view.drawing.TimelineSlider;
import csi.shared.core.imaging.ImagingRequest;

public abstract class TimelineView<T,E,X,O extends OverviewRenderable> extends ResizeComposite{
    protected String trackName = null;


    public String getTrackName() {
        return trackName;
    }

    public static enum ViewMode{
        DETAILED,
        SUMMARY,
        MEASURE,
        EMPTY
    }
    
    private HandlerRegistration nativeHandlerRegistration;
    
    public void onLoad() {
        super.onLoad();
    }

    public void onUnload() {
        super.onUnload();
    }
    
    protected void deregisterNativeHandler() {

        if (this.nativeHandlerRegistration != null) {
            nativeHandlerRegistration.removeHandler();
            nativeHandlerRegistration = null;
        }
    }

    public abstract int getTimelineHeight();

    public HandlerRegistration getNativeHandlerRegistration() {
        return nativeHandlerRegistration;
    }


    public void setNativeHandlerRegistration(HandlerRegistration nativeHandlerRegistration) {
        this.nativeHandlerRegistration = nativeHandlerRegistration;
    }

    public abstract void render();
    public abstract void render(List<T> tracks, List<E> events, List<X> axis, boolean drawNow);
    public abstract void render(List<X> axis);

    public abstract void redraw();

    public abstract ViewPort getTimelineViewport();

    public abstract int getAxisHeight();

    public abstract int getOverviewHeight();
    

    public abstract void showLimitReached();

    public abstract void renderOverview(List<O> overviewRenderables) ;
    
    public abstract void updateOverviewTimeScale();

    public abstract void updateViewport(double trueHeight);

    public abstract void showLimitReached(int eventsFound, int eventMax);

    public abstract void showEmpty();

    public abstract void updateOverview(Interval interval);

    public abstract ImagingRequest getTimelineImagingRequest();

    public abstract List<ImagingRequest> getImagingRequest();

    public abstract List<Axis> getAxes();

    public abstract void broadcastNotify(String text);

    public abstract void show();

    public abstract TimeScale setOverviewRange(Interval overviewInterval, TimelineSlider timelineSlider);

    public abstract void setTrackName(String track);

    public abstract void renderFooter();

    public abstract TimelinePresenter getPresenter();

    public abstract TimelineOverview getOverview();
}
