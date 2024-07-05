package csi.client.gwt.viz.graph.button;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.graph.surface.GraphSurface.DragMode;

public class PanModeHandler implements VizButtonHandler, ClickHandler {

    private Graph graph;
    private Button panButton;


    public PanModeHandler(Graph graph) {
        this.graph = graph;
    }


    @Override
    public void onClick(ClickEvent event) {
        GraphSurface graphSurface = graph.getGraphSurface();
        if (panButton.isActive()) {
            event.stopPropagation();
            // TODO:create DragMode.NULL
            graphSurface.setExplicitDragMode(DragMode.DEFAULT);
            panButton.setActive(false);
        } else {
            graphSurface.setExplicitDragMode(DragMode.PAN);
        }
    }


    public void setButton(Button panButton) {
        this.panButton = panButton;
    }


    @Override
    public void bind(Button button) {
        button.addClickHandler(this);
    }

    @Override
    public String getTooltipText() {
        return CentrifugeConstantsLocator.get().pan();
    }
}
