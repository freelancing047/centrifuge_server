package csi.client.gwt.viz.graph.controlbar;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DefaultDateTimeFormatInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.fx.client.DragCancelEvent;
import com.sencha.gxt.fx.client.DragEndEvent;
import com.sencha.gxt.fx.client.DragHandler;
import com.sencha.gxt.fx.client.DragMoveEvent;
import com.sencha.gxt.fx.client.DragStartEvent;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

public class GraphControlBarView extends ResizeComposite {
    private final GraphControlBar graphControlBar;
    private final DragZone endDragZone;
    private final DragZone scrubberDragZone;
    private GraphControlBarPresenter presenter;
    private DragZone startDragZone;
    private CentrifugeConstants i18n = CentrifugeConstantsLocator.get();



    StartControl startControl;

    EndControl endControl;
    TimeProgressControl timeProgressControl;
    TimeScrubber timeScrubber;
    @UiField
    Button stopControl;
    @UiField
    Button playControl;
    @UiField
    AbsolutePanel controlPanel;
    InlineLabel startLabel;
    InlineLabel scrubberLabel;
    InlineLabel endLabel;
    @UiField
    HBoxLayoutContainer hbox;
    @UiField
    CenterLayoutContainer centerContainer;
    @UiField
    AbsolutePanel background;

    //TOOLTIP - sencha tooltip.
    protected ToolTip timelineTooltip;

    //DATE FORMATTER
    private final String TOOLTIP_DATE_FORMAT_PATTERN = "yyyy-MM-dd hh:mm:ss a";  // we could move this out to settings if neccessary

    DefaultDateTimeFormatInfo info = new DefaultDateTimeFormatInfo();
    DateTimeFormat dt = new DateTimeFormat(TOOLTIP_DATE_FORMAT_PATTERN, info) {};  // <= trick here
    private boolean playing;

    interface GraphControlBarUiBinder extends UiBinder<SimpleContainer, GraphControlBarView> {}

    private static GraphControlBarUiBinder ourUiBinder = GWT.create(GraphControlBarUiBinder.class);

    public GraphControlBarView(final GraphControlBar graphControlBar) {
        this.graphControlBar = graphControlBar;
        SimpleContainer controlBarElement = ourUiBinder.createAndBindUi(this);
        initWidget(controlBarElement);
        {
            controlPanel.getElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
            controlPanel.getElement().getStyle().setTop(0, Style.Unit.PX);
        }
        {
            playControl.setType(ButtonType.LINK);
        }
        {
            stopControl.setType(ButtonType.LINK);
        }
        //add drag zones
        {
            {
                startDragZone = new DragZone();
                controlPanel.add(startDragZone, 0, 0);
                startDragZone.addDragHandler(new DragHandler() {
                    @Override
                    public void onDragCancel(DragCancelEvent event) {

                    }

                    @Override
                    public void onDragEnd(DragEndEvent event) {
                        int i = startControl.asWidget().getAbsoluteLeft() - controlPanel.getAbsoluteLeft();
                        graphControlBar.setStart(i);
                    }

                    @Override
                    public void onDragMove(DragMoveEvent event) {

                    }

                    @Override
                    public void onDragStart(DragStartEvent event) {

                    }
                });
            }
            {
                endDragZone = new DragZone();
                controlPanel.add(endDragZone, 0, 0);
            }
            {
                scrubberDragZone = new DragZone();
                controlPanel.add(scrubberDragZone, 0, 0);

                scrubberDragZone.addDragHandler(new DragHandler() {
                    @Override
                    public void onDragCancel(DragCancelEvent event) {

                    }

                    @Override
                    public void onDragEnd(DragEndEvent event) {
                        int scrubberLeft = timeScrubber.asWidget().getAbsoluteLeft() + timeScrubber.asWidget().getOffsetWidth() / 2;
                        int rangeStart = timeProgressControl.asWidget().getAbsoluteLeft() + 8;

                        double range = 237;
                        presenter.scrubMoved((double) (scrubberLeft - rangeStart) / (double) range);
                    }

                    @Override
                    public void onDragMove(DragMoveEvent event) {

                    }

                    @Override
                    public void onDragStart(DragStartEvent event) {
                        presenter.scrubDragStart();

                    }
                });
            }
        }

        {
            {
                timeProgressControl = new TimeProgressControl();
                background.add(timeProgressControl, 0, 0);
            }
            {
                startControl = new StartControl();
                controlPanel.add(startControl, 2, -3);
                startDragZone.add(startControl);
                startControl.setGraphControlBarView(this);
                startControl.asWidget().setVisible(false);
            }
            {
                endControl = new EndControl();
                controlPanel.add(endControl, 225, -3);
                endDragZone.add(endControl);
                endControl.setGraphControlBarView(this);
                endControl.asWidget().setVisible(false);
            }
            {
                timeScrubber = new TimeScrubber();
                controlPanel.add(timeScrubber, 1, 14);
                scrubberDragZone.add(timeScrubber);
                timeScrubber.setGraphControlBarView(this);
                timeScrubber.asWidget().addStyleName("overlay-clear");//NON-NLS
            }

        }
        addLabelsToRoot();

        addTimelineTooltip();

        //        graphControlBar.getGraph().getGraphSurface().getView().asWidget().addHandler(new ResizeHandler() {
        //            @Override
        //            public void onResize(ResizeEvent event) {
        //                centerContainer.forceLayout();
        //            }
        //        }, ResizeEvent.getType());

    }

    public void setStartTooltipX(int x) {
        Element startLabelElement = getStartLabel().getElement();
        setTooltipX(x, startLabelElement);
    }

    private void setTooltipX(int x, Element element) {

        element.getStyle().setLeft(x, Style.Unit.PX);
        element.getStyle().setTop(hbox.getAbsoluteTop() - 30, Style.Unit.PX);
        element.getStyle().setZIndex(XDOM.getTopZIndex());
    }

    public void setScrubberX(int i) {
        int halfWidth = timeScrubber.asWidget().getOffsetWidth() / 2;
        controlPanel.setWidgetPosition(timeScrubber.asWidget(), i - halfWidth, 14);
    }

    public void setScrubberPercent(double percent) {
        if(percent > 1){
            percent = 1;
        }
        if(percent < 0){
            percent = 0;
        }
//        int i = (int) (percent *controlPanel.getOffsetWidth());
        int i = (int) (percent * 225) + 8;
        setScrubberX(i);
    }

    public void ensureScrubberTransition() {
        timeScrubber.asWidget().removeStyleName("transition-clear");
    }

    public void forceRender() {
        timeProgressControl.render();
    }

    public HBoxLayoutContainer getHbox() {
        return hbox;
    }


    protected void addTimelineTooltip(){

        timeScrubber.getControlButton().addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                if(graphControlBar.getModel().getCurrentTime() != null){
//                    ToolTipConfig startTimeTooltipConfig = new ToolTipConfig("Start Time", dt.format(new Date(graphControlBar.getModel().getStartTime())));
//                    timelineTooltip = new ToolTip(timeScrubber.asWidget(), startTimeTooltipConfig);
//                }else {
                    ToolTipConfig currentTimeToolTipConfig = new ToolTipConfig(i18n.timePlayerTab_tooltip(), dt.format(graphControlBar.getModel().getCurrentTime()));
                    timelineTooltip = new ToolTip(timeScrubber.asWidget(), currentTimeToolTipConfig);
                }
            }
        });

    }


    private void addLabelsToRoot() {
        RootPanel rootPanel = RootPanel.get();
        {
            startLabel = new InlineLabel("March"); //FIXME: Should be i18n when this is implemented
            startLabel.addStyleName("graph-player-start-label");//NON-NLS
            rootPanel.add(startLabel);
        }
        {
            endLabel = new InlineLabel("May"); //FIXME: Should be i18n when this is implemented
            endLabel.addStyleName("graph-player-start-label");//NON-NLS
            rootPanel.add(endLabel);
        }
        {
            scrubberLabel = new InlineLabel("April"); //FIXME: Should be i18n when this is implemented
            scrubberLabel.addStyleName("graph-player-start-label");//NON-NLS
            rootPanel.add(scrubberLabel);
        }
    }

    void bind(GraphControlBarPresenter presenter) {
        this.presenter = presenter;
    }

    @UiHandler("playControl")
    public void onPlayClick(ClickEvent event) {
        presenter.togglePlaying();
    }

    @UiHandler("stopControl")
    public void onStopClick(ClickEvent event) {
        presenter.stopPlayer();
        //        timeProgressControl.render();
    }

    public void setPlaying(boolean playing) {
        IconType icon;
        this.playing = playing;
        if (playing) {
            icon = IconType.PAUSE;
        } else {
            icon = IconType.PLAY;
        }
        playControl.setIcon(icon);
    }

    public AbsolutePanel getControlPanel() {
        return controlPanel;
    }

    public InlineLabel getStartLabel() {
        return startLabel;
    }

    @Override
    public void onResize() {

        super.onResize();
        centerContainer.onResize();
        timeProgressControl.render();

    }

    private void sizeElements() {
        //size panel
        //size timeprogressbar
        //        timeProgressControl.asWidget().setWidth(width + "px");
    }

    private void positionElements() {
        //move panel
        //move timeprogressbar
        //move start
        //move end
        //move scrubber

        //labels should move automagicallys...

    }

    //
    //    private void initDrawingPanel() {
    //        controlPanel.setWidgetPosition(timeProgressControl.asWidget(), 0, 0);
    //
    //    }
    //
    //    private void initButton3() {
    //        controlPanel.setWidgetPosition(timeScrubber.asWidget(), 2, 9);
    //    }
    //
    //    private void initButton2() {
    //        controlPanel.setWidgetPosition(endControl.asWidget(), 550, 8);
    //
    //    }
    //
    //    private void initStartButton() {
    //        controlPanel.setWidgetPosition(startControl.asWidget(), 30, 8);
    //    }



    public void showStartTooltip(boolean show) {
//        startLabel.setVisible(show);
        startLabel.getElement().getStyle().setOpacity(show ? 1 : 0);
    }

    public void showEndTooltip(boolean show) {
        endLabel.setVisible(show);
    }

    public void showScrubberTooltip(boolean show) {
        timeScrubber.setVisible(show);
    }

    public boolean isPlaying() {
        return playing;
    }
}
