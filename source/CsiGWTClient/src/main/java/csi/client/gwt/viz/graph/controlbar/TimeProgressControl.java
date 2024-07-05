package csi.client.gwt.viz.graph.controlbar;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Rectangle;

public class TimeProgressControl implements IsWidget{

    private final DrawingPanel drawingPanel;

    public TimeProgressControl() {
        drawingPanel = new DrawingPanel();
        Layer layer1 = new Layer();
        Rectangle renderable = new Rectangle(8, 19, 235, 4);
        renderable.setFillStyle(CssColor.make(255, 255, 255));
        renderable.setStrokeStyle(CssColor.make(25, 25, 25));
        layer1.addItem(renderable);
        drawingPanel.addLayer(layer1);
    }

    void render() {
        drawingPanel.render();
    }

    @Override
    public Widget asWidget() {
        return drawingPanel;
    }
}
