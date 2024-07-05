package csi.client.gwt.viz.table.menu;

import com.github.gwtbootstrap.client.ui.event.HideEvent;
import com.github.gwtbootstrap.client.ui.event.HideHandler;

import csi.client.gwt.viz.shared.filter.FilterSettingsHandler;
import csi.client.gwt.viz.shared.filter.ManageFilterDialog;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.table.TablePresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.filter.Filter;

public class TableFilterSettingsHandler extends FilterSettingsHandler<TablePresenter>{
	private TablePresenter tablePresenter;

	public TableFilterSettingsHandler(TablePresenter presenter, AbstractMenuManager<TablePresenter> menuManager) {
		super(presenter, menuManager);
		tablePresenter = presenter;
	}

	@Override
    public void onMenuEvent(CsiMenuEvent event) {
        Filter filter = tablePresenter.getVisualizationDef().getFilter();
        String dataViewUuid = tablePresenter.getDataViewUuid();
        final ManageFilterDialog dialog = new ManageFilterDialog(filter, dataViewUuid);
        dialog.addHideHandler(new HideHandler() {
            @Override
            public void onHide(HideEvent hideEvent) {
            	tablePresenter.getVisualizationDef().setFilter(dialog.getSelectedFilter());
                VortexFuture<Void> future = getPresenter().saveSettings(true);
                future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                    	tablePresenter.getVisualizationDef().getSelection().clearSelection();
                    	tablePresenter.loadVisualization();
                    }
                });
            }
        });
        dialog.show();
    }
}
