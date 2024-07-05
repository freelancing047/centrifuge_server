package csi.client.gwt.viz.graph.button;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;


public class TransparencyHandler implements VizButtonHandler, ClickHandler{

    private Graph graph;

    public TransparencyHandler(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void onClick(ClickEvent event) {
        graph.showTransparencyWindow();
    }

    @Override
    public void bind(Button button) {
        button.addClickHandler(this);   
    }

    @Override
    public String getTooltipText() {
        return CentrifugeConstantsLocator.get().transparency();
    }

}
