package csi.client.gwt.viz.graph.surface;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.surface.GraphSurface.Model;
import csi.client.gwt.viz.graph.surface.GraphSurface.View;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.selection.SelectionModel;

class DragDeselectItems extends AbstractGraphSurfaceActivity {

    private int startX;
    private int startY;

    public DragDeselectItems(GraphSurface graphSurface) {
        super(graphSurface);
        MouseHandler mouseHandler = graphSurface.getMouseHandler();
        // TODO:move to start method
        startX = mouseHandler.getMouseX();
        startY = mouseHandler.getMouseY();
    }

    @Override
    public void drag(int deltaX, int deltaY) {
        super.drag(deltaX, deltaY);
        Model model = graphSurface.getModel();
        View view = graphSurface.getView();
        view.draw(model.getImage(), model.getxOffset(), model.getyOffset(), model.getZoom());

        MouseHandler mouseHandler = graphSurface.getMouseHandler();
        view.drawRect(startX, startY, mouseHandler.getMouseX(), mouseHandler.getMouseY(),
                CentrifugeConstantsLocator.get().dragDeselect_message());
    }

    @Override
    public void stopDrag(int mouseX, int mouseY) {
        VortexFuture<SelectionModel> dragSelectRegionRequest = graphSurface.getGraph().getModel()
                .deselectRegion(startX, startY, mouseX, mouseY);
        graphSurface.refresh(dragSelectRegionRequest);
    }
    @Override
    public void clickHere(int x, int y) {
        //No-Op

    }
}
