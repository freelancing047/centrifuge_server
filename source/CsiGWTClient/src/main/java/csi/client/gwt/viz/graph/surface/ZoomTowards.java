package csi.client.gwt.viz.graph.surface;

import java.util.Date;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

import csi.client.gwt.viz.graph.surface.GraphSurface.Model;
import csi.client.gwt.vortex.VortexFuture;

class ZoomTowards extends AbstractGraphSurfaceActivity {

    private static class ZoomCommand implements RepeatingCommand {

        private long refreshAt;
        private ZoomTowards zoomTowards;


        public ZoomCommand(ZoomTowards zoomTowards) {
            this.zoomTowards = zoomTowards;
            resetTimer();
        }


        @Override
        public boolean execute() {
            long time = new Date().getTime();
            if (time > refreshAt) {
                zoomTowards.doZoom();
                return false;
            } else {
                return true;
            }
        }


        public void resetTimer() {
            refreshAt = new Date().getTime() + MAX_DELAY;
        }
    }

    // FIXME: Constants should be loaded from server to allow for configuration
    public static final long MAX_DELAY = 250;
    private static final int TIMER_RESOLUTION = 100;
    private static final double ZOOM_FACTOR = 1.1;
    private static ZoomCommand zoomCommand;
    private int startX;
    private int startY;


    public ZoomTowards(GraphSurface graphSurface) {
        super(graphSurface);
    }


    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        zoomCommand = new ZoomCommand(this);
        MouseHandler mouseHandler = graphSurface.getMouseHandler();
        startX = mouseHandler.getMouseX();
        startY = mouseHandler.getMouseY();
        graphSurface.getToolTipManager().removeAllToolTips();
        drawNow();
        Scheduler.get().scheduleFixedDelay(zoomCommand, TIMER_RESOLUTION);
    }


    @Override
    public void zoomIn() {
        Model model = graphSurface.getModel();
        model.setZoom(model.getZoom() * ZOOM_FACTOR);
        drawNow();
        zoomCommand.resetTimer();
    }


    @Override
    public void zoomOut() {
        Model model = graphSurface.getModel();
        model.setZoom(model.getZoom() * (1D / ZOOM_FACTOR));
        drawNow();
        zoomCommand.resetTimer();
    }


    private void doZoom() {
        Model model = graphSurface.getModel();
        IsWidget view = graphSurface.getView();
        double viewWidth = view .asWidget().getOffsetWidth();
        double viewHeight = view.asWidget().getOffsetHeight();
        double newZoom = model.getZoom();
        // Assumes initial zoom is 1
        int x1 = (int) ((1 - (1 / newZoom)) * startX);
        int y1 = (int) ((1 - (1 / newZoom)) * startY);
        int x2 = (int) (x1 + (viewWidth * (1 / newZoom)));
        int y2 = (int) (y1 + (viewHeight * (1 / newZoom)));
        VortexFuture<Void> zoomToRegion = model.zoomToRegion(x1, y1, x2, y2);
        graphSurface.refresh(zoomToRegion);
    }


    // NOTE: this method is not Draw() as it should not be called via the interface.
    private void drawNow() {
        Model model = graphSurface.getModel();
        graphSurface.getView().drawTowards(model.getImage(), startX, startY, model.getZoom());
    }
}
