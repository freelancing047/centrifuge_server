package csi.client.gwt.viz.chart.menu;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.shared.SpinoffDialog;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;

public class ChartSpinoffHandler extends AbstractChartMenuEventHandler {

    public ChartSpinoffHandler(ChartPresenter presenter, ChartMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        final ChartPresenter chartPresenter = getPresenter();
        chartPresenter.saveViewStateToVisualizationDef();
        if (!chartPresenter.hasSelection()) {
            ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().spinoffHandler_ErrorTitle(), CentrifugeConstantsLocator.get().spinoffHandler_ErrorMessage());
            errorDialog.show();
            return;
        }

        VortexFuture<Void> saveSettings = chartPresenter.saveSettings(false, false, false);
        saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                String dataviewUuid = chartPresenter.getDataViewUuid();
                String uuid = chartPresenter.getUuid();
                SpinoffDialog spinoffDialog = new SpinoffDialog(dataviewUuid, uuid, chartPresenter.getVisualizationDef().getSelection());
                spinoffDialog.show();
            }
        });
    }
}
