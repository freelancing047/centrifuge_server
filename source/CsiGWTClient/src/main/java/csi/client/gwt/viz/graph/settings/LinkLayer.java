package csi.client.gwt.viz.graph.settings;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

public class LinkLayer extends Layer {

    private GraphSettingsPanel graphSettingsPanel;

    public LinkLayer(GraphSettingsPanel graphSettingsPanel) {
        super();
        this.graphSettingsPanel = graphSettingsPanel;
    }

    @Override
    public void addItem(Renderable renderable) {
        if (renderable instanceof DrawLink) {
            final DrawLink link = (DrawLink) renderable;
            // link.addMouseDownHandler(new MouseDownHandler(){
            //
            // @Override
            // public void onMouseDown(MouseDownEvent event) {
            // graphSettingsPanel.editLink(link.getLinkProxy());
            // }
            // });
            link.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    graphSettingsPanel.editLink(link.getLinkProxy());

                }
            });
        }
        super.addItem(renderable);
    }

}
