package csi.client.gwt.viz.table.menu;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.SpinoffDialog;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.table.TablePresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;

public class SpinoffHandler extends AbstractTableMenuHandler {

    public SpinoffHandler(TablePresenter table, TableMenuManager mgr) {
        super(table, mgr);
    }

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        final TablePresenter presenter = getPresenter();
        presenter.saveViewStateToVisualizationDef();
        if (!presenter.hasSelection()) {
            ErrorDialog errorDialog = new ErrorDialog(i18n.tableSpinoffError(), i18n.tableSpinoffErrorMessage()); 
            errorDialog.show();
            return;
        }
        VortexFuture<Void> saveSettings = presenter.saveSettings(false, false, false);
        saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                String dataviewUuid = presenter.getDataViewUuid();
                String uuid = presenter.getUuid();
                SpinoffDialog spinoffDialog = new SpinoffDialog(dataviewUuid, uuid, presenter.getVisualizationDef().getSelection());
                spinoffDialog.show();

            }
        });
    }
}
