package csi.client.gwt.viz.graph.button;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;

public class FitToSizeHandler implements VizButtonHandler, ClickHandler {

    private Graph graph;

    public FitToSizeHandler(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void onClick(ClickEvent event) {
        graph.getGraphSurface().zoomToFit();
    }

    @Override
    public void bind(Button button) {
        button.addClickHandler(this);
    }

    @Override
    public String getTooltipText() {
        return CentrifugeConstantsLocator.get().fitToSize();
    }
}
