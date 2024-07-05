package csi.client.gwt.viz.table.menu;

import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.table.TablePresenter;

public class DeselectAllHandler extends  AbstractTableMenuHandler {


    public DeselectAllHandler(TablePresenter table, TableMenuManager mgr) {
        super(table, mgr);
    }


    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().deselectAll();;
    }
}
