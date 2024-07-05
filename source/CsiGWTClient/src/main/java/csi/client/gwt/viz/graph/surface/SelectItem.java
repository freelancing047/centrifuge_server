package csi.client.gwt.viz.graph.surface;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.selection.SelectionModel;

class SelectItem extends AbstractGraphSurfaceActivity {

    private int mouseX;
    private int mouseY;

    public SelectItem(GraphSurface graphSurface) {
        super(graphSurface);
    }

    public void setMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        VortexFuture<SelectionModel> selectPointRequest = graphSurface.getGraph().getModel()
                .selectPoint(mouseX, mouseY);
        //FIXME: Perhaps I do not need to refresh every time?
        graphSurface.refresh(selectPointRequest);
    }
}
