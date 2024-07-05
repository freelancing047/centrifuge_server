package csi.client.gwt.viz.table.menu;

import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.table.TablePresenter;

public abstract class AbstractTableMenuHandler extends AbstractMenuEventHandler<TablePresenter, TableMenuManager> {
    public AbstractTableMenuHandler(TablePresenter presenter, TableMenuManager menuManager) {
        super(presenter, menuManager);
    }
}
