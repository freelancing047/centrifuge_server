package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsView;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

public class PatternLinkLayer extends Layer {
    private PatternSettingsView view;

    public PatternLinkLayer(PatternSettingsView view) {
        this.view = view;
    }

    @Override
    public void addItem(Renderable renderable) {
        if (renderable instanceof DrawPatternLink) {
            final DrawPatternLink link = (DrawPatternLink) renderable;
            if (true) {
                link.addMouseOverHandler(new MouseOverHandler() {
                    @Override
                    public void onMouseOver(MouseOverEvent event) {
                        link.setOver(true);
                        DrawingPanel drawingPanel = getDrawingPanel();
                        if (drawingPanel != null) {
                            drawingPanel.render();
                        }
                    }
                });
                link.addMouseOutHandler(new MouseOutHandler() {
                    @Override
                    public void onMouseOut(MouseOutEvent event) {
                        link.setOver(false);
                        DrawingPanel drawingPanel = getDrawingPanel();
                        if (drawingPanel != null) {
                            drawingPanel.render();
                        }
                    }
                });
                link.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        view.editLink(link.getLink());
                        DrawingPanel drawingPanel = getDrawingPanel();
                        if (drawingPanel != null) {
                            drawingPanel.render();
                        }
                    }
                });
            }
        }
        super.addItem(renderable);
    }
}
