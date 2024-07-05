package csi.client.gwt.viz.table.menu;

import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.table.TablePresenter;

public class SelectAllHandler extends AbstractTableMenuHandler {

    public SelectAllHandler(TablePresenter table, TableMenuManager mgr) {
        super(table, mgr);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().selectAll();
    }
}
