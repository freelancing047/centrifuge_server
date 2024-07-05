package csi.client.gwt.viz.graph.surface;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.surface.GraphSurface.Model;
import csi.client.gwt.viz.graph.surface.GraphSurface.View;
import csi.client.gwt.vortex.VortexFuture;

class DragZoomGraph extends AbstractGraphSurfaceActivity {

    private double startX;
    private double startY;


    public DragZoomGraph(GraphSurface graphSurface) {
        super(graphSurface);
        MouseHandler mouseHandler = graphSurface.getMouseHandler();
        startX = mouseHandler.getMouseX();
        startY = mouseHandler.getMouseY();
    }


    @Override
    public void drag(int deltaX, int deltaY) {
        super.drag(deltaX, deltaY);
        Model model = graphSurface.getModel();
        View view = graphSurface.getView();
        if (model != null) {
            view.draw(model.getImage(), model.getxOffset(), model.getyOffset(), model.getZoom());
        }
        MouseHandler mouseHandler = graphSurface.getMouseHandler();
        double mouseX = mouseHandler.getMouseX();
        double mouseY = mouseHandler.getMouseY();
        double viewWidth = view.asWidget().getOffsetWidth();
        double widthRatio = (startX - mouseX) / viewWidth;
        double viewHeight = view.asWidget().getOffsetHeight();
        double heightRatio = (startY - mouseY) / viewHeight;
        if (Math.abs(widthRatio) > Math.abs(heightRatio)) {
            if (((widthRatio > 0) && (heightRatio > 0)) || ((widthRatio < 0) && (heightRatio < 0))) {
                mouseY = (viewHeight * -widthRatio) + startY;
            } else {
                mouseY = (viewHeight * widthRatio) + startY;
            }
        } else {
            if (((widthRatio > 0) && (heightRatio > 0)) || ((widthRatio < 0) && (heightRatio < 0))) {
                mouseX = ((viewWidth * -heightRatio) + startX);
            } else {
                mouseX = ((viewWidth * heightRatio) + startX);
            }
        }
        view.drawRect((int) startX, (int) startY, (int) mouseX, (int) mouseY, CentrifugeConstantsLocator.get().dragZoom_message());
    }


    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        graphSurface.getToolTipManager().removeAllToolTips();
    }


    @Override
    public void stopDrag(int mouseX, int mouseY) {
        Model model = graphSurface.getModel();
        VortexFuture<Void> zoomToRegion = model.zoomToRegion((int) startX, (int) startY, mouseX, mouseY);
        graphSurface.refresh(zoomToRegion);
    }
}