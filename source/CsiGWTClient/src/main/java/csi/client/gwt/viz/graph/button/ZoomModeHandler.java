package csi.client.gwt.viz.graph.button;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.graph.surface.GraphSurface.DragMode;

public class ZoomModeHandler implements VizButtonHandler, ClickHandler {

    private Graph graph;
    private Button zoomButton;


    public ZoomModeHandler(Graph graph) {
        this.graph = graph;
    }


    @Override
    public void onClick(ClickEvent event) {
        GraphSurface graphSurface = graph.getGraphSurface();
        if (zoomButton.isActive()) {
            graphSurface.setExplicitDragMode(DragMode.DEFAULT);
            zoomButton.setActive(false);
            event.stopPropagation();
        } else {
            graphSurface.setExplicitDragMode(DragMode.ZOOM);
        }
    }


    public void setButton(Button zoomButton) {
        this.zoomButton = zoomButton;
    }


    @Override
    public String getTooltipText() {
        return CentrifugeConstantsLocator.get().zoom();
    }

    @Override
    public void bind(Button button) {
        button.addClickHandler(this);


    }
}
