package csi.client.gwt.viz.graph.button;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.graph.surface.GraphSurface.DragMode;

public class SelectModeHandler implements VizButtonHandler, ClickHandler {

    private Graph graph;
    private Button selectButton;


    public SelectModeHandler(Graph graph) {
        this.graph = graph;
    }


    @Override
    public void onClick(ClickEvent event) {
        GraphSurface graphSurface = graph.getGraphSurface();
        if (selectButton.isToggled()) {
            event.stopPropagation();
            graphSurface.setExplicitDragMode(DragMode.DEFAULT);
            selectButton.setActive(false);
        } else {
            graphSurface.setExplicitDragMode(DragMode.SELECT);
        }
    }


    public void setButton(Button selectButton) {
        this.selectButton = selectButton;
    }


    @Override
    public void bind(Button button) {
        button.addClickHandler(this);
    }

    @Override
    public String getTooltipText() {
        return CentrifugeConstantsLocator.get().select();
    }
}
