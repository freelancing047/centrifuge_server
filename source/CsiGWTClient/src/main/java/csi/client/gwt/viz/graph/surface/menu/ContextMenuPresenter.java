package csi.client.gwt.viz.graph.surface.menu;

import csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum;

public interface ContextMenuPresenter {

    void showMenuAt(final int x, final int y, int surface_x, int surface_y);

    void handleMouseOverAction(final int x, final int y, GraphContextActionEnum action);

    void handleSelectedAction(GraphContextActionEnum action);

    void setReadOnly();
}
