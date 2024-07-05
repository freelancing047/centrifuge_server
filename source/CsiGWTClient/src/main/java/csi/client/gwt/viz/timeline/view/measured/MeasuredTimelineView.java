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
package csi.client.gwt.viz.timeline.view.measured;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.SecurityBanner;
import csi.client.gwt.viz.chart.view.Breadcrumb;
import csi.client.gwt.viz.chart.view.CollapsibleBreadcrumbs;
import csi.client.gwt.viz.shared.BroadcastAlert;
import csi.client.gwt.viz.timeline.events.ResizeEvent;
import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.Interval;
import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.model.ViewPort;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.viz.timeline.view.TimelineView;
import csi.client.gwt.viz.timeline.view.drawing.OverviewRenderable;
import csi.client.gwt.viz.timeline.view.drawing.ScrollbarRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TimelineAxis;
import csi.client.gwt.viz.timeline.view.drawing.TimelineOverview;
import csi.client.gwt.viz.timeline.view.drawing.TimelineSlider;
import csi.client.gwt.viz.timeline.view.drawing.TimelineSliderRenderable;
import csi.client.gwt.viz.timeline.view.measured.drawing.HistogramTimelineOverview;
import csi.client.gwt.viz.timeline.view.measured.drawing.MeasureFrame;
import csi.client.gwt.viz.timeline.view.measured.drawing.MeasuredTrackRenderable;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.server.common.util.ValuePair;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.imaging.PNGImageComponent;
import csi.shared.core.visualization.timeline.TimelineTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MeasuredTimelineView extends TimelineView<MeasuredTrackRenderable, MeasuredTrackRenderable, Axis, OverviewRenderable> {

    private TimelinePresenter presenter;
    private String limitMessage;

    private static final String NO_MESSAGE_STYLE = "timeline-noresults";
    private static final int MIN_HEIGHT = 300;
    String emptyMessage = i18n.timelineNoResultsMessage();

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    @UiField
    LayoutPanel layoutPanel;

    @UiField
    MeasureFrame timeline;

    @UiField
    HistogramTimelineOverview timelineOverview;

    @UiField
    TimelineAxis timelineAxis;
    @UiField
    CollapsibleBreadcrumbs drillBreadcrumb;

    public DrawingPanel getZoomDrawingPanel() {
        return zoomDrawingPanel;
    }

    @UiField
    DrawingPanel zoomDrawingPanel;


    private ViewPort timelineViewport = new ViewPort();

    private List<MeasuredTrackRenderable> tracks = new ArrayList<MeasuredTrackRenderable>();
    private List<Axis> axes = new ArrayList<Axis>();
    private TimelineSliderRenderable timelineSlider = new TimelineSliderRenderable();
    private ScrollbarRenderable scrollbar = new ScrollbarRenderable(timelineViewport);


    interface SpecificUiBinder extends UiBinder<Widget, MeasuredTimelineView> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public MeasuredTimelineView(TimelinePresenter presenter) {
        super();

        this.presenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
        timeline.setZoomDrawingPanel(zoomDrawingPanel);
        timeline.setTimelinePresenter(presenter);
        zoomDrawingPanel.getElement().getParentElement().getStyle().setProperty("pointerEvents","none");
        createBreadcrumb(presenter);

        timeline.setEventBus(presenter.getEventBus());
        timelineOverview.setEventBus(presenter.getEventBus());
        timelineOverview.setTimelineView(this);
    }

    public void createBreadcrumb(TimelinePresenter presenter) {
        Breadcrumb breadcrumb = new Breadcrumb(presenter.getVisualizationDef().getTimelineSettings().getGroupByField().getFieldName());
        drillBreadcrumb.add(breadcrumb);
    }

    @Override
    public void onResize() {
        super.onResize();
        ResizeEvent resizeEvent = new ResizeEvent();
        getEventBus().fireEvent(resizeEvent);
        timeline.resetBackground();
    }

    @Override
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
        int height = timeline.getHeight();
        if (height < MIN_HEIGHT) {
            height = MIN_HEIGHT;
        }

        return height;
    }


    public int getTimelineViewportStart() {
        return timelineViewport.getStart();
    }


    public void render(List<MeasuredTrackRenderable> tracks, List<MeasuredTrackRenderable> renderables, List<Axis> axes, boolean drawNow) {
        this.tracks = tracks;
        this.axes = axes;

        if (drawNow)
            render();
    }

    public void render(List<Axis> axes) {
        this.axes = axes;
        render();
    }

    @Override
    public void render() {
        timeline.setAxes(this.axes);
        timeline.setTracks(this.tracks);
        
        timelineAxis.setAxes(this.axes);

        updateViewport();
        timeline.setScrollbar(this.scrollbar);
        redraw();
    }

    public void redraw() {
        timeline.setVisible(true);
        timelineOverview.setVisible(true);
        timelineAxis.setVisible(true);

        int axisHeight = timelineAxis.renderAxis();

        int overviewHeight = timelineOverview.getOffsetHeight();

        timeline.render(axisHeight + overviewHeight);
        timelineOverview.forceStart();
        timelineOverview.render();
    }


    public int getAxisHeight() {
        if (timelineAxis.getOffsetHeight() > 0) {
            return timelineAxis.getOffsetHeight();
        } else {
            return 1;
        }
    }

    public TimeScale getTimeScale() {
        return presenter.getTimeScale();
    }


    public EventBus getEventBus() {
        return presenter.getEventBus();
    }

    public TimelineSliderRenderable getTimelineSlider() {
        return timelineSlider;
    }

    public void setTimelineSlider(TimelineSliderRenderable timelineSlider) {
        this.timelineSlider = timelineSlider;
    }

    public TimeScale setOverviewRange(Interval interval, TimelineSlider slider) {

        TimeScale timeScale = new TimeScale();
        timeScale.setDateRange(interval);
        timeScale.setNumberRange(0, this.getOffsetWidth());
        timelineOverview.setTimeScale(timeScale);
        timelineSlider.setTimeScale(timeScale);
        timelineSlider.setTimelineSlider(slider);
        timelineOverview.addSlider(timelineSlider);
        return timeScale;
    }
    
    public void updateVirtualWidth() {
        int width = this.getOffsetWidth();
        timelineOverview.getTimeScale().setNumberRange(0, width);
        timelineSlider.getTimeScale().setNumberRange(0, width);
    }

    public void updateViewport(double totalHeight) {
        this.timelineViewport.setTotalHeight(totalHeight);
        updateViewport();

    }

    private void updateViewport() {
        int newMaxHeight = this.getOffsetHeight() - this.timelineOverview.getOffsetHeight() - this.timelineAxis.getAxisHeight() - 2;
        this.timelineViewport.setCurrentHeight(newMaxHeight);
        int offsetWidth = this.getOffsetWidth();
        if(offsetWidth > 0){
            this.timelineViewport.setCurrentWidth(offsetWidth);
        }
        scrollbar.adjustIfNecessary();
    }

    public void updateOverview(Interval interval) {

        if(timelineSlider == null) {
            return;
        }
        
        timelineSlider.moveLeftThumb(interval.start);
        timelineSlider.moveRightThumb(interval.end);
    }

    @Override
    public void renderOverview(List<OverviewRenderable> renderables) {
        timelineOverview.setOverviewRenderables(renderables);
    }

    public void updateOverviewTimeScale() {
        timelineOverview.updateTimeScale(0, this.getOffsetWidth());
    }

    public int getOverviewHeight() {
        return timelineOverview.getOffsetHeight();
    }

    public ViewPort getTimelineViewport() {
        return this.timelineViewport;
    }

    public void showEmpty() {
        this.tracks = new ArrayList<MeasuredTrackRenderable>();
        this.axes = new ArrayList<Axis>();

        timeline.setVisible(false);
        timelineOverview.setVisible(false);
        timelineAxis.setVisible(false);
        presenter.hideLegend();
        presenter.hideGroups();

        presenter.getChrome().addFullScreenWindow(i18n.timelineNoResultsMessage(), IconType.INFO_SIGN);
    }

    public void showLimitReached(int max, int total) {
        this.tracks = new ArrayList<MeasuredTrackRenderable>();
        this.axes = new ArrayList<Axis>();

        timeline.setVisible(false);
        timelineOverview.setVisible(false);
        timelineAxis.setVisible(false);
    }

    public void showLimitReached() {
        this.tracks = new ArrayList<MeasuredTrackRenderable>();
        this.axes = new ArrayList<Axis>();

        timeline.setVisible(false);
        timelineOverview.setVisible(false);
        timelineAxis.setVisible(false);
    }


    public void show() {
        this.tracks = new ArrayList<MeasuredTrackRenderable>();
        this.axes = new ArrayList<Axis>();

        timeline.setVisible(true);
        timelineOverview.setVisible(true);
        timelineAxis.setVisible(true);
        presenter.getChrome().removeFullScreenWindow();
    }

    public void reset() {
        tracks.clear();
        axes.clear();

        timeline.reset();
        timelineOverview.reset();
        timelineAxis.reset();

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
        return axes;
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
        // size of the image
        int totalHeight = timeline.getCanvas().getOffsetHeight() + timelineAxis.getCanvas().getOffsetHeight() + timelineOverview.getCanvas().getOffsetHeight();
        int timelineWidth = timeline.getCanvas().getOffsetWidth();

        ImagingRequest imagingRequest = new ImagingRequest();

        imagingRequest.setName(this.presenter.getDataView().getName() + "_" + this.presenter.getName() + "_Timeline");

        imagingRequest.setWidth(timelineWidth);
        imagingRequest.setHeight(totalHeight);

        Boolean enableSecurity = WebMain.injector.getMainPresenter().getUserInfo().getDoCapco();
        String text = SecurityBanner.getBannerText();
        if(text != null && enableSecurity) {
            imagingRequest.setHeight(totalHeight + 50);
            Canvas tempCanvas = Canvas.createIfSupported();
            tempCanvas.setWidth(String.valueOf(imagingRequest.getWidth()));
            tempCanvas.setCoordinateSpaceWidth(imagingRequest.getWidth());
            tempCanvas.setHeight(25 + "px");
            tempCanvas.setCoordinateSpaceHeight(25);

            Context2d ctx = tempCanvas.getContext2d();
            ValuePair<String, String> colors = SecurityBanner.getColors(text, text);
            ctx.setFillStyle(colors.getValue2());
            ctx.fillRect(0, 0, imagingRequest.getWidth(), 25);
            ctx.setFillStyle(colors.getValue1());
            ctx.setFont("normal 400 13px Arial");
            ctx.setTextAlign(Context2d.TextAlign.CENTER);
            while(ctx.measureText(text).getWidth() >= imagingRequest.getWidth()) {
                createNewFont(ctx, ctx.getFont(), text, imagingRequest.getWidth());
            }
            ctx.fillText(text, imagingRequest.getWidth()/2, 17);
            String dataUrl = tempCanvas.toDataUrl("image/png");
            PNGImageComponent topBanner = new PNGImageComponent();
            topBanner.setData(dataUrl);
            topBanner.setX(0);
            topBanner.setY(0);
            PNGImageComponent bottomBanner = new PNGImageComponent();
            bottomBanner.setData(dataUrl);
            bottomBanner.setX(0);
            bottomBanner.setY(timeline.getCanvas().getOffsetHeight() + timelineAxis.getHeight() +
                    timelineOverview.getHeight() + 25);
            imagingRequest.addComponent(topBanner);
            imagingRequest.addComponent(bottomBanner);
        }

        PNGImageComponent mainPNG = new PNGImageComponent();
        mainPNG.setData(timeline.getCanvas().toDataUrl());
        mainPNG.setX(0);
        mainPNG.setY(0);

        PNGImageComponent tlAxis = new PNGImageComponent();
        tlAxis.setData(timelineAxis.getCanvas().toDataUrl());
        tlAxis.setX(0);
        tlAxis.setY(timeline.getCanvas().getOffsetHeight());

        PNGImageComponent tlOverview = new PNGImageComponent();

        tlOverview.setData(timelineOverview.getCanvas().toDataUrl());
        tlOverview.setX(0);
        tlOverview.setY(timeline.getCanvas().getOffsetHeight() + timelineAxis.getHeight());

        if(enableSecurity) {
            mainPNG.setY(25);
            tlAxis.setY(timeline.getCanvas().getOffsetHeight()+25);
            tlOverview.setY(timeline.getCanvas().getOffsetHeight() + timelineAxis.getHeight()+25);
        }
        imagingRequest.addComponent(mainPNG);
        imagingRequest.addComponent(tlAxis);
        imagingRequest.addComponent(tlOverview);


        return imagingRequest;
    }

    private void createNewFont(Context2d ctx, String font, String text, int width) {
        String[] fontValues = font.split(" ");
        String fontSize = fontValues[2];
        String fontSizeValues[] = fontSize.split("p");
        int fontSizeInt = Integer.parseInt(fontSizeValues[0]);
        String newFont = fontValues[0] + " " + fontValues[1] + " " + fontSizeInt + "px"
                + " " + fontSizeValues[3];
        ctx.setFont(newFont);
        if(ctx.measureText(text).getWidth() >= width){
            createNewFont(ctx, ctx.getFont(), text, width);
        }
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
                    ctx.fillText(MeasuredTrackRenderable.NO_VALUE, _x + 10, _y + 5);
                }
            }

        }

        ctx.stroke();

        return c.toDataUrl("image/png");
    }

    @Override
    public void setTrackName(String track) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void renderFooter() {
        timelineAxis.setAxes(this.axes);
        timelineAxis.renderAxis();
    }

    @Override
    public TimelinePresenter getPresenter() {
        return presenter;
    }

    @Override
    public TimelineOverview getOverview() {
        return timelineOverview;
    }

    public void setTimelineHeight(int pixels){
        timeline.setHeight(pixels+"px");

    }


}
