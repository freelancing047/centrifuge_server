package csi.client.gwt.viz.table.menu;

import csi.client.gwt.csiwizard.dialogs.ExtractTableDialog;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.table.TablePresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;

/**
 * Created by centrifuge on 5/9/2018.
 */
public class SpawnTableHandler extends AbstractTableMenuHandler {

    public SpawnTableHandler(TablePresenter table, TableMenuManager mgr) {
        super(table, mgr);
    }

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        final TablePresenter presenter = getPresenter();
        presenter.saveViewStateToVisualizationDef();
        if (!presenter.hasSelection()) {
            ErrorDialog errorDialog = new ErrorDialog(i18n.spawnTableHandler_ErrorTitle(),
                                                        i18n.spawnTableHandler_ErrorMessage());
            errorDialog.show();
            return;
        }
        VortexFuture<Void> saveSettings = presenter.saveSettings(false, false, false);
        saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {

                try {

                    String dataviewUuid = presenter.getDataViewUuid();
                    String uuid = presenter.getUuid();
                    ExtractTableDialog extractTableDialog = new ExtractTableDialog(dataviewUuid,
                            uuid, presenter.getVisualizationDef().getSelection());
                    extractTableDialog.show();

                } catch (Exception myException) {

                    Display.error("SpawnTableHandler", 1, myException);
                }
            }
        });
    }
}
