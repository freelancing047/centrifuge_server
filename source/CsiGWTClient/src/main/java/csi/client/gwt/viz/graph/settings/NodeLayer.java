package csi.client.gwt.viz.graph.settings;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

public class NodeLayer extends Layer {

    GraphSettingsPanel graphSettingsPanel;
    private boolean onDark;

    public NodeLayer(GraphSettingsPanel graphSettingsPanel) {
        super();
        this.graphSettingsPanel = graphSettingsPanel;

    }

    public void addNode(final DrawNode drawNode) {
        checkNotNull(drawNode);
        drawNode.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                graphSettingsPanel.startDragNode(drawNode);
            }
        });
        drawNode.setOnDark(onDark);
        drawNode.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                graphSettingsPanel.getGraphSettings().showDetails(drawNode.getNodeProxy());
                getDrawingPanel().setCursor(Cursor.POINTER);
            }
        });
        drawNode.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                graphSettingsPanel.hideDetails();
                getDrawingPanel().setCursor(Cursor.AUTO);
            }
        });
        // NOTE: not sure i need this
        // drawNode.addClickHandler(new ClickHandler() {
        //
        // @Override
        // public void onClick(ClickEvent event) {
        // graphSettingsPanel.getGraphSettings().editNode(drawNode.getNodeProxy());
        //
        // }
        // });
        super.addItem(drawNode);
    }

    public void setOnDark(boolean value) {
        this.onDark = value;
        for (Renderable dn : things) {
            if (dn instanceof DrawNode) {
                DrawNode drawNode = (DrawNode) dn;
                drawNode.setOnDark(value);
            }
        }
    }
}
