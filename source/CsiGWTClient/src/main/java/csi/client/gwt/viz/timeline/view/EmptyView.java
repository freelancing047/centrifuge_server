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
package csi.client.gwt.viz.timeline.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.BroadcastAlert;
import csi.client.gwt.viz.timeline.events.ResizeEvent;
import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.Interval;
import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.model.ViewPort;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.viz.timeline.view.drawing.DetailedEventRenderable;
import csi.client.gwt.viz.timeline.view.drawing.DetailedOverviewRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TimelineOverview;
import csi.client.gwt.viz.timeline.view.drawing.TimelineSlider;
import csi.client.gwt.viz.timeline.view.drawing.TimelineSliderRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TimelineTrackRenderable;
import csi.client.gwt.widget.InfoPanel;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.imaging.PNGImageComponent;
import csi.shared.core.visualization.timeline.TimelineTrack;

/**
 * @author Centrifuge Systems, Inc.
 */
public class EmptyView extends TimelineView<TimelineTrackRenderable, DetailedEventRenderable, Axis, DetailedOverviewRenderable> {

    private TimelinePresenter presenter;
    private HTMLPanel limitMessage;

    private static final String NO_MESSAGE_STYLE = "timeline-noresults";
    private static final int MIN_HEIGHT = 300;

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    String emptyMessage = i18n.timelineNoResultsMessage();

    @UiField
    LayoutPanel layoutPanel;

    @UiField
    InfoPanel noResultsWindow;

    private ViewPort timelineViewport = new ViewPort();


    interface SpecificUiBinder extends UiBinder<Widget, EmptyView> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public EmptyView(TimelinePresenter presenter) {
        super();

        this.presenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
//        
//        timelineAxis = new TimelineAxis();
//        timelineOverview = new TimelineOverview();
//
//        timelineOverview.getElement().getStyle().setHeight(50, Unit.PX);
//
//        timelineAxis.getElement().getStyle().setHeight(25, Unit.PX);
//        
//        timeline.getElement().getStyle().setHeight(100, Unit.PCT);

        layoutPanel.setWidgetVisible(noResultsWindow, false);

        noResultsWindow.setMessage(emptyMessage);
        noResultsWindow.addStyleName(NO_MESSAGE_STYLE);
//        noResultsWindow.setVisible(false);
        noResultsWindow.getElement().getStyle().setDisplay(Display.NONE);
    }

    @Override
    public void onResize() {
        super.onResize();
        ResizeEvent resizeEvent = new ResizeEvent();
        getEventBus().fireEvent(resizeEvent);
    }

    public void broadcastNotify(String text) {
        layoutPanel.add(new BroadcastAlert(text));
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        deregisterNativeHandler();
    }


    @Override
    public int getTimelineHeight() {
        int height = layoutPanel.getOffsetHeight();
        if (height < MIN_HEIGHT) {
            height = MIN_HEIGHT;
        }

        return height;
    }


    public int getTimelineViewportStart() {
        return timelineViewport.getStart();
    }


    public void render(List<TimelineTrackRenderable> tracks, List<DetailedEventRenderable> renderables, List<Axis> axes, boolean drawNow) {
        
    }

    public void render(List<Axis> axes) {
        render();
    }

    @Override
    public void render() {

    }

    public void redraw() {

    }


    public int getAxisHeight() {
       return 0;
    }

    public TimeScale getTimeScale() {
        return presenter.getTimeScale();
    }


    public EventBus getEventBus() {
        return presenter.getEventBus();
    }

    public TimelineSliderRenderable getTimelineSlider() {
        return null;
    }

    public void setTimelineSlider(TimelineSliderRenderable timelineSlider) {
    }

    public TimeScale setOverviewRange(Interval interval, TimelineSlider slider) {

        TimeScale timeScale = new TimeScale();
        timeScale.setDateRange(interval);
        timeScale.setNumberRange(0, this.getOffsetWidth());
        return timeScale;
    }

    public void updateViewport(double totalHeight) {

    }

    private void updateViewport() {
    }

    public void updateOverview(Interval interval) {

    }


    public void updateOverviewTimeScale() {
    }

    public int getOverviewHeight() {
        return 0;
    }

    public ViewPort getTimelineViewport() {
        return this.timelineViewport;
    }

    public void showEmpty() {
        noResultsWindow.setVisible(true);

        noResultsWindow.clear();
        noResultsWindow.setMessage(emptyMessage);
        presenter.hideGroups();
        presenter.hideLegend();

        layoutPanel.setWidgetVisible(noResultsWindow, true);
    }

    public void showLimitReached(int max, int total) {
        noResultsWindow.setVisible(true);

        noResultsWindow.clear();

        limitMessage = new HTMLPanel(i18n.timelineLimitWarningMessage(total, max));
        noResultsWindow.add(limitMessage);
        layoutPanel.setWidgetVisible(noResultsWindow, true);
    }

    public void showLimitReached() {

        noResultsWindow.setVisible(true);

        noResultsWindow.clear();

        if (limitMessage != null)
            noResultsWindow.add(limitMessage);
        layoutPanel.setWidgetVisible(noResultsWindow, true);
    }


    public void show() {

        noResultsWindow.setVisible(false);

        layoutPanel.setWidgetVisible(noResultsWindow, false);
    }

    public void reset() {

    }


    public List<ImagingRequest> getImagingRequest() {

        List<ImagingRequest> bundledImagingRequest = new ArrayList<>();
        bundledImagingRequest.add(getTimelineImagingRequest());

        // if we have a legend, make a new imaging request for it.
        if (this.presenter.isLegendVisible()) {
            bundledImagingRequest.add(getLegendImagingRequest());
        }

        return bundledImagingRequest;
    }

    @Override
    public List<Axis> getAxes() {
        return Lists.newArrayList();
    }

    public ImagingRequest getLegendImagingRequest(){
        ImagingRequest legendReq = new ImagingRequest();
        int[] size =getCanvasDimensionsForLegend();


        PNGImageComponent leg = new PNGImageComponent();
        legendReq.setWidth(size[0]);
        legendReq.setHeight(size[1]);
        leg.setData(getLegendAsCanvas(size));
        legendReq.setName(this.presenter.getDataView().getName() + "_" + this.presenter.getName() + "_Legend");
//        legendReq.setName(escapeFilename("_Legend").trim());
        legendReq.addComponent(leg);

        return legendReq;
    }

    public ImagingRequest getTimelineImagingRequest(){

        ImagingRequest imagingRequest = new ImagingRequest();

        return imagingRequest;
    }


    public int[] getCanvasDimensionsForLegend(){
        Canvas c = Canvas.createIfSupported();
        Context2d ctx  = c.getContext2d();
        // this is so we always have enough room for the title.
        int maxWidth = (int) ctx.measureText("Timeline Legend:").getWidth();
        List<String> visItems = this.presenter.getVisibleLegendItems();
        Map<String, Integer> cl = this.presenter.getColors();
        List<String> items = TimelinePresenter.asSortedList(cl.keySet(), presenter.getSort());
        int count = 0;
        // this just gets the max width of the column
        for (String key : items) {
            if (visItems.contains(key)) {
                TextMetrics textMetrics = ctx.measureText(key);
                maxWidth = textMetrics.getWidth() > maxWidth ? (int) textMetrics.getWidth() : maxWidth;
                count++;
            }
        }

        int[] ret = new int[2];
        ret[0] = maxWidth + 20;
        ret[1] = (count + 2) * 20;

        return ret;
    }


    public String getLegendAsCanvas(int[] size) {
        int _x = 5, _y = 10;
        int maxWidth = 0;

        List<String> visItems = this.presenter.getVisibleLegendItems();
        Map<String, Integer> cl = this.presenter.getColors();
        List<String> items = TimelinePresenter.asSortedList(cl.keySet(), presenter.getSort());

        Canvas c = Canvas.createIfSupported();
        Context2d ctx = c.getContext2d();

        c.setHeight(size[1] + "px");
        c.setCoordinateSpaceHeight(size[1]*5);
        c.setWidth(size[0] + "px");
        c.setCoordinateSpaceWidth(size[0]*5);

        ctx.setFillStyle("black");
        ctx.fillText("Timeline Legend:", _x, _y);

        for (String key : items) {
            if(visItems.contains(key)) {
                if (!key.equals(TimelineTrack.EMPTY_TRACK)) {
                    _y += 20;
                    Integer colorInt = cl.get(key);

                    int red = (colorInt >> 16) & 0xFF;
                    int green = (colorInt >> 8) & 0xFF;
                    int blue = colorInt & 0xFF;

                    ctx.setFillStyle("rgb(" + red + "," + green + "," + blue + ")");
                    ctx.fillRect(_x, _y, 5, 5);
                    ctx.fillText(key, _x + 7, _y + 5);

                } else { // EMPTY TRACK
                    ctx.setFillStyle("rgb(84,84,84)");
                    _y += 20;
                    ctx.fillRect(_x, _y, 5, 5);
                    ctx.fillText(TimelineTrackRenderable.NO_VALUE, _x + 10, _y + 5);
                }
            }

        }

        ctx.stroke();

        return c.toDataUrl("image/png");
    }

    @Override
    public void renderOverview(List<DetailedOverviewRenderable> overviewRenderables) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTrackName(String track) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void renderFooter() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public TimelinePresenter getPresenter() {
        return null;
    }

    @Override
    public TimelineOverview getOverview() {
        return null;
    }


}
