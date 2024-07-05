package csi.client.gwt.viz.graph.button;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.graph.surface.GraphSurface.DragMode;

public class DefaultModeHandler implements VizButtonHandler, ClickHandler {

    private Graph graph;
    private Button defaultButton;


    public DefaultModeHandler(Graph graph) {
        this.graph = graph;
    }


    @Override
    public void onClick(ClickEvent event) {
        GraphSurface graphSurface = graph.getGraphSurface();
        if (defaultButton.isActive()) {
            event.stopPropagation();
            // TODO:create DragMode.NULL
            graphSurface.setExplicitDragMode(DragMode.DEFAULT);
        } else {
            graphSurface.setExplicitDragMode(DragMode.DEFAULT);
        }
    }
    

    public void setButton(Button defaultButton) {
        this.defaultButton = defaultButton;
    }


    @Override
    public void bind(Button button) {
        button.addClickHandler(this);
    }

    @Override
    public String getTooltipText() {
        return CentrifugeConstantsLocator.get().defaultGraphModeTooltip();
    }
}
