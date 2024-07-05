package csi.client.gwt.viz.chart.menu;

import csi.client.gwt.csiwizard.dialogs.ExtractTableDialog;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;

public class ChartSpawnTableHandler extends AbstractChartMenuEventHandler {

    public ChartSpawnTableHandler(ChartPresenter presenter, ChartMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        final ChartPresenter chartPresenter = getPresenter();
        chartPresenter.saveViewStateToVisualizationDef();
        if (!chartPresenter.hasSelection()) {
            ErrorDialog errorDialog = new ErrorDialog(CentrifugeConstantsLocator.get().spawnTableHandler_ErrorTitle(),
                                                        CentrifugeConstantsLocator.get().spawnTableHandler_ErrorMessage());
            errorDialog.show();
            return;
        }

        VortexFuture<Void> saveSettings = chartPresenter.saveSettings(false, false, false);
        saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {

                try {

                    String dataviewUuid = chartPresenter.getDataViewUuid();
                    String uuid = chartPresenter.getUuid();
                    ExtractTableDialog extractTableDialog = new ExtractTableDialog(dataviewUuid, uuid, chartPresenter.getVisualizationDef().getSelection());
                    extractTableDialog.show();

                } catch (Exception myException) {

                    Display.error("ChartSpawnTableHandler", 1, myException);
                }
            }
        });
    }
}