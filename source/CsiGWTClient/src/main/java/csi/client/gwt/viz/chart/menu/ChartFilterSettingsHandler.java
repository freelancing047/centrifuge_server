package csi.client.gwt.viz.chart.menu;

import com.github.gwtbootstrap.client.ui.event.HideEvent;
import com.github.gwtbootstrap.client.ui.event.HideHandler;

import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.shared.filter.FilterSettingsHandler;
import csi.client.gwt.viz.shared.filter.ManageFilterDialog;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.filter.Filter;

public class ChartFilterSettingsHandler extends FilterSettingsHandler<ChartPresenter> {
	private ChartPresenter chartPresenter;

	public ChartFilterSettingsHandler(ChartPresenter presenter, AbstractMenuManager<ChartPresenter> menuManager) {
		super(presenter, menuManager);
		chartPresenter = presenter;
	}
	
    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        Filter filter = chartPresenter.getVisualizationDef().getFilter();
        String dataViewUuid = chartPresenter.getDataViewUuid();
        final ManageFilterDialog dialog = new ManageFilterDialog(filter, dataViewUuid);
        dialog.addHideHandler(new HideHandler() {
            @Override
            public void onHide(HideEvent hideEvent) {
            	chartPresenter.getVisualizationDef().setFilter(dialog.getSelectedFilter());
                VortexFuture<Void> future = getPresenter().saveSettings(false);
                future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                       	chartPresenter.getVisualizationDef().getSelection().clearSelection();
                       	chartPresenter.f1();
                    }
                });
            }
        });
        dialog.show();
    }
}
