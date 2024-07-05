package csi.client.gwt.viz.graph.surface;

import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.user.client.Window;
import csi.client.gwt.mainapp.SecurityBanner;

/**
 * Handle right click and stop propagation of that event.
 * Part of GraphSurface's Presenter
 */
public class GraphSurfaceContextMenuHandler implements ContextMenuHandler{

    private GraphSurface graphSurface;

    public GraphSurfaceContextMenuHandler(GraphSurface graphSurface) {
        this.graphSurface = graphSurface;
    }

    @Override
    public void onContextMenu(ContextMenuEvent event) {
        // stop the browser from opening the default context menu
        event.preventDefault();
        event.stopPropagation();
        int x1 = event.getNativeEvent().getClientX();
        int y1 = event.getNativeEvent().getClientY();
        int x2 = graphSurface.getMouseHandler().getMouseX();
        int y2 = graphSurface.getMouseHandler().getMouseY();

        x1 = ensureContextMenuWithinWindowWidth(x1);
        y1 = ensureContextMenuWithinWindowHeight(y1);

        graphSurface.getContextMenuPresenter().showMenuAt(x1, y1, x2, y2);
    }

    private int ensureContextMenuWithinWindowHeight(int y1) {
        int myMaxHeight = Window.getClientHeight() - SecurityBanner.getHeight();
        if(y1 + ContextMenuInfo.CONTEXT_MENU_HEIGHT > myMaxHeight){
            y1 = myMaxHeight - ContextMenuInfo.CONTEXT_MENU_HEIGHT;
        }
        return y1;
    }

    private int ensureContextMenuWithinWindowWidth(int x1) {
        if(x1 + ContextMenuInfo.CONTEXT_MENU_WIDTH > Window.getClientWidth()){
            x1 = Window.getClientWidth() - ContextMenuInfo.CONTEXT_MENU_WIDTH;
        }
        return x1;
    }
}
