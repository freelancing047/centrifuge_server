package csi.client.gwt.viz.graph.settings;

import java.util.Date;

import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;

import csi.client.gwt.widget.drawing.DrawImage;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

public class DragNodeLayer extends Layer {

    GraphSettingsPanel graphSettingsPanel;
    private long lastFired;
    static final long debounceTime = 250;

    public DragNodeLayer(GraphSettingsPanel graphSettingsPanel) {
        super();
        this.graphSettingsPanel = graphSettingsPanel;
    }

    @Override
    public void addItem(final Renderable renderable) {
        {
            long currentMils = new Date().getTime();
            long timeSinceLastFired = currentMils - lastFired;
            if ((lastFired != 0) && (timeSinceLastFired < debounceTime)) {
                return;
            }
        }
        super.addItem(renderable);
        if (renderable instanceof DrawImage) {
            DrawImage drawImage = (DrawImage) renderable;
            drawImage.addMouseUpHandler(new MouseUpHandler() {
                @Override
                public void onMouseUp(MouseUpEvent event) {
                    long currentMils = new Date().getTime();
                    long timeSinceLastFired = currentMils - lastFired;
                    if ((lastFired != 0) && ((timeSinceLastFired - debounceTime) < 0)) {
                        return;
                    }
                    lastFired = currentMils;
                    graphSettingsPanel.endDrag(event);
                }
            });
            drawImage.addMouseMoveHandler(new MouseMoveHandler() {
                @Override
                public void onMouseMove(MouseMoveEvent event) {
                    graphSettingsPanel.dragMove(event);
                }
            });
        }
    }
}
