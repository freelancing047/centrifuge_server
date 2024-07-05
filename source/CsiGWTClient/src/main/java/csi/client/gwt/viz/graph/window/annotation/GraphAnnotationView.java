package csi.client.gwt.viz.graph.window.annotation;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class GraphAnnotationView extends Composite {

    private FluidContainer fluidContainer;

    private InlineLabel textContainer;

    private GraphAnnotation presenter;

    public GraphAnnotationView(GraphAnnotation graphAnnotation) {

        this.presenter = graphAnnotation;
        buildUI();


    }

    public void populateAnnotation(String text) {
        SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
        safeHtmlBuilder.appendEscaped(text);
        textContainer.getElement().setInnerHTML(text);

    }

    private void buildUI() {

        fluidContainer = new FluidContainer();

        fluidContainer.addStyleName("legend-container");//NON-NLS
        initWidget(fluidContainer);

        textContainer = new InlineLabel();
        fluidContainer.add(textContainer);

    }

    @Override
    public Widget asWidget() {
        return fluidContainer;
    }

}
