package csi.client.gwt.viz.graph.surface.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class GraphContextSubmenuClickHandler implements ClickHandler {
    private String type;
    private GraphContextSubmenuCallback callback;

    public GraphContextSubmenuClickHandler(String type, GraphContextSubmenuCallback callback) {
        this.type = type;
        this.callback = callback;
    }

    @Override
    public void onClick(ClickEvent event) {
        callback.execute(type);
    }
}
