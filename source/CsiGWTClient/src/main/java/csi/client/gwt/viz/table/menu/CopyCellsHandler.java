package csi.client.gwt.viz.table.menu;

import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.table.TablePresenter;

public class CopyCellsHandler<V extends Visualization, M extends AbstractMenuManager<V>> extends AbstractMenuEventHandler {

    public CopyCellsHandler(V presenter, M menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {

        ((TablePresenter)getPresenter()).startCopy();
    }

}
