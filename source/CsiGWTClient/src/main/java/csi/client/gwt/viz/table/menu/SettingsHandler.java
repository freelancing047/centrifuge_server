package csi.client.gwt.viz.table.menu;


import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.client.gwt.viz.table.TablePresenter;
import csi.client.gwt.viz.table.settings.TableSettingsPresenter;
import csi.server.common.model.visualization.table.TableViewDef;

public final class SettingsHandler extends AbstractTableMenuHandler implements SettingsActionCallback<TableViewDef> {


    public SettingsHandler(TablePresenter table, TableMenuManager mgr) {
        super(table, mgr);
    }

    

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        TableSettingsPresenter presenter = new TableSettingsPresenter(this);
        presenter.setDataView(getPresenter().getDataView());
        presenter.setVisualizationDef(getPresenter().getVisualizationDef());
        presenter.setVisualization(getPresenter());
        presenter.show();
    }



    @Override
    public void onSaveComplete(TableViewDef vizDef, boolean suppressLoadAfterSave) {
    	if (!suppressLoadAfterSave) {
    	    getPresenter().setVisualizationDef(vizDef);
            getPresenter().getVisualizationDef().getSelection().clearSelection();
            getPresenter().loadVisualization();
        }
    }



    @Override
    public void onCancel() {
        // TODO Auto-generated method stub
        
    }
}
