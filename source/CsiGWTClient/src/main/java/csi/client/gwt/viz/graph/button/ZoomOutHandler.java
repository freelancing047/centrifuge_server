package csi.client.gwt.viz.graph.button;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.vortex.VortexFuture;

public final class ZoomOutHandler implements VizButtonHandler, ClickHandler, MouseUpHandler, MouseDownHandler {

    private static final double ZOOM_AMOUNT_ON_HOLD = 1D / 1.2;
    private static final int ZOOM_BUTTON_MOUSE_HOLD_TIME = 200;
    private static final double ZOOM_AMOUNT_ON_CLICK = 1D / 1.5;
    private Graph graph;
    protected boolean stopZoom = false;
    private RepeatingCommand zoomMoreCommand = new RepeatingCommand() {

        @Override
        public boolean execute() {
            if (!stopZoom) {
                double oldZoom = graph.getGraphSurface().getModel().getZoom();
                graph.getGraphSurface().getModel().setZoom(ZOOM_AMOUNT_ON_HOLD * oldZoom);
                graph.getGraphSurface().show();
                noClick = true;
            }
            return !stopZoom;
        }
    };
    private boolean noClick = false;

    public ZoomOutHandler(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void onClick(ClickEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            if (!noClick) {
                stopZoom = true;
                if (graph.getGraphSurface().getModel() == null) {
                    return;
                }
                VortexFuture<Void> zoomPercentFuture = graph.getGraphSurface().getModel()
                        .zoomPercent(ZOOM_AMOUNT_ON_CLICK);
                graph.getGraphSurface().refresh(zoomPercentFuture);
            }
        }
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            stopZoom = false;// reset
            noClick = false; // reset
            Scheduler.get().scheduleFixedPeriod(zoomMoreCommand, ZOOM_BUTTON_MOUSE_HOLD_TIME);
        }
    }

    @Override
    public String getTooltipText() {
        return CentrifugeConstantsLocator.get().zoomOut();
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            stopZoom = true;
            if (noClick) {
                if (graph.getGraphSurface().getModel() == null) {
                    return;
                }
                VortexFuture<Void> zoomPercentRequest = graph.getGraphSurface().getModel().zoomPercent(1);
                graph.getGraphSurface().refresh(zoomPercentRequest);
            }
        }
    }

    @Override
    public void bind(Button button) {
        button.addClickHandler(this);
        button.addMouseDownHandler(this);
        button.addMouseUpHandler(this);
    }
}