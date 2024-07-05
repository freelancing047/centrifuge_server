package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.common.base.Preconditions;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsView;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;

public class PatternNodeLayer extends Layer {
    private PatternSettingsView view;

    public PatternNodeLayer(PatternSettingsView view) {
        this.view = view;
    }

    public void addNode(final DrawPatternNode node) {
        Preconditions.checkNotNull(node);
        addHandlers(node);
        addItem(node);
    }

    private void addHandlers(final DrawPatternNode node) {
        node.addMouseDownHandler(new MouseDownHandler() {
            public void onMouseDown(MouseDownEvent event) {
                view.startDragNode(node);
            }
        });
        node.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                view.showDetails(node);
                DrawingPanel drawingPanel = getDrawingPanel();
                if (drawingPanel != null) {
                    drawingPanel.setCursor(Cursor.POINTER);
                }
            }
        });
        node.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                view.hideDetails();
                DrawingPanel drawingPanel = getDrawingPanel();
                if (drawingPanel != null) {
                    drawingPanel.setCursor(Cursor.AUTO);
                }
            }
        });
    }
}
