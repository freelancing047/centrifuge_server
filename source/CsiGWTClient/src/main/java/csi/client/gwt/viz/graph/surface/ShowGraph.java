package csi.client.gwt.viz.graph.surface;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.surface.GraphSurface.Model;
import csi.client.gwt.viz.graph.surface.GraphSurface.View;

import static com.google.common.base.Preconditions.checkNotNull;

class ShowGraph extends AbstractGraphSurfaceActivity {
    private boolean drawScheduled = false;
    private ScheduledCommand drawCommand = () -> {
        drawNow();
        drawScheduled = false;
    };

    private void drawNow() {
        Model model = checkNotNull(graphSurface.getModel());
        View view = checkNotNull(graphSurface.getView());
        view.draw(model.getImage(), model.getxOffset(), model.getyOffset(), model.getZoom());
    }

    ShowGraph(GraphSurface graphSurface) {
        super(graphSurface);
    }

    @Override
    public void draw() {
        // since this may not be quick we should finish execution loop first.
        if (!drawScheduled) {
            drawScheduled = true;
            Scheduler.get().scheduleDeferred(drawCommand);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        graphSurface.setPresenter(this);
        View view = graphSurface.getView();
        view.setBackgroundFill(graphSurface.getBackgroundColor());// TODO: need to update this when settings change.
        draw();
        view.setCursor(Cursor.AUTO);
    }

    @Override
    public void startDrag(int x, int y) {
        CSIActivityManager manager = graphSurface.getActivityManager();
        switch (graphSurface.getExplicitDragMode()) {
            case PAN: {
                doPanGraph(x, y, manager);
                break;
            }
            case SELECT: {
                doDragSelectItems(manager);
                break;
            }
            case ZOOM: {
                doDragZoomGraph(manager);
                break;
            }
            default: {
                switch (graphSurface.getDragMode(false)) {
                    case SELECT: {
                        doDragSelectItems(manager);
                        break;
                    }
                    case ZOOM: {
                        doDragZoomGraph(manager);
                        break;
                    }
                    case DESELECT: {
                        doDragDeselectItems(manager);
                        break;
                    }
                    default: {
                        if (isBackground(x, y)) {
                            doPanGraph(x, y, manager);
                        } else {
                            manager.setActivity(new DragItems(graphSurface));
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    private void doPanGraph(int x, int y, CSIActivityManager manager) {
        PanGraph activity = new PanGraph(graphSurface);
        manager.setActivity(activity);
        activity.startDrag(x, y);
    }

    private void doDragSelectItems(CSIActivityManager manager) {
        DragSelectItems selectActivity = new DragSelectItems(graphSurface);
        manager.setActivity(selectActivity);
    }

    private void doDragZoomGraph(CSIActivityManager manager) {
        DragZoomGraph nextActivity = new DragZoomGraph(graphSurface);
        manager.setActivity(nextActivity);
    }

    private void doDragDeselectItems(CSIActivityManager manager) {
        DragDeselectItems deselectActivity = new DragDeselectItems(graphSurface);
        manager.setActivity(deselectActivity);
    }

    @Override
    public void updateCursor() {
        int x = graphSurface.getMouseHandler().getMouseX();
        int y = graphSurface.getMouseHandler().getMouseY();

        View view = graphSurface.getView();
        switch (graphSurface.getExplicitDragMode()) {
            case PAN:
                view.setCursor(Cursor.MOVE);
                break;
            case SELECT:
                view.setCursor(Cursor.POINTER);
                break;
            case ZOOM:
                view.setCursor(Cursor.CROSSHAIR);
                break;
            case DEFAULT:
                switch (graphSurface.getDragMode(false)) {
                    case SELECT:
                    case DESELECT:
                        view.setCursor(Cursor.POINTER);
                        break;
                    case ZOOM:
                        view.setCursor(Cursor.CROSSHAIR);
                        break;
                    default:
                        view.setCursor(Cursor.DEFAULT);
                        break;
                }
                break;
            default:
                view.setCursor(Cursor.DEFAULT);
                break;
        }
    }

    @Override
    public void zoomIn() {
        CSIActivityManager manager = graphSurface.getActivityManager();
        graphSurface.getModel().setZoom(1.1);
        ZoomTowards nextActivity = new ZoomTowards(graphSurface);
        graphSurface.setPresenter(nextActivity);
        manager.setActivity(nextActivity);
    }

    @Override
    public void zoomOut() {
        CSIActivityManager manager = graphSurface.getActivityManager();
        graphSurface.getModel().setZoom(1D / 1.1);
        ZoomTowards nextActivity = new ZoomTowards(graphSurface);
        graphSurface.setPresenter(nextActivity);
        manager.setActivity(nextActivity);
    }

    @Override
    public void hoverHere(int x, int y) {
        if (!isBackground(x, y)) {
            graphSurface.getToolTipManager().createTempTooltip(x, y);
        }
    }
}
