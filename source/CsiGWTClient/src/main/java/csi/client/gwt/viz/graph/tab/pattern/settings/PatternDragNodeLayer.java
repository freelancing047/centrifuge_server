package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;

import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsView;
import csi.client.gwt.widget.drawing.DrawImage;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

public class PatternDragNodeLayer extends Layer {
    private PatternSettingsView view;

    public PatternDragNodeLayer(PatternSettingsView view) {
        this.view = view;
    }

    @Override
    public void addItem(Renderable renderable) {
        super.addItem(renderable);
        if(renderable instanceof DrawImage) {
            DrawImage drawImage = (DrawImage)renderable;
            drawImage.addMouseUpHandler(new MouseUpHandler() {
                @Override
                public void onMouseUp(MouseUpEvent event) {
                    view.endNodeDrag(event);
                }
            });
            drawImage.addMouseMoveHandler(new MouseMoveHandler() {
                @Override
                public void onMouseMove(MouseMoveEvent event) {
                    view.dragMove(event);
                }
            });
        }

    }
}
