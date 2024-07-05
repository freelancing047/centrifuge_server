package csi.client.gwt.viz.shared.menu;

import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.shared.export.AvailableExportTypes;
import csi.client.gwt.viz.shared.export.VizExportPresenter;
import csi.client.gwt.viz.shared.export.model.*;
import csi.client.gwt.viz.shared.export.view.widget.VizExport;
import csi.client.gwt.viz.table.TablePresenter;
import csi.server.common.model.visualization.VisualizationType;

import java.util.*;

/**
 *
 */
public class VizExportMenuHandler<V extends Visualization, M extends AbstractMenuManager<V>> extends AbstractMenuEventHandler<V, M> {

    protected VizExportPresenter presenter;

    public VizExportMenuHandler(V presenter, M menuManager) {
        super(presenter, menuManager);
    }

    private VisualizationType CURRENT_VIZ_TYPE;

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        updateVizCache();

        VisualizationExportable exportData = ExportableFactory.createVisualizationExportable(getPresenter());

        AbstractVisualizationPresenter pres = (AbstractVisualizationPresenter) getPresenter();
        exportData.setName(pres.getDataView().getName() + "_" + pres.getName());
        presenter = new VizExportPresenter(exportData, createDialog(exportData));
    }

    private void updateVizCache() {
        this.CURRENT_VIZ_TYPE = getPresenter().getVisualizationDef().getType();
        if (CURRENT_VIZ_TYPE == VisualizationType.TABLE) {
            updateTableCache();
        }
        getPresenter().saveViewStateToVisualizationDef();
    }

    public void updateTableCache() {
        TablePresenter tbl = (TablePresenter) getPresenter();
        tbl.updateCache();
    }

    private VizExport createDialog(VisualizationExportable exportData) {
        List<ExportType> allowedTypes = AvailableExportTypes.getAvailableExportTypes(exportData);

        switch (CURRENT_VIZ_TYPE) {
            case DRILL_CHART:
                int w = 0, h = 0;
                boolean showSize = true;
                ChartPresenter c = null;
                if (getPresenter() instanceof ChartPresenter) {
                    c = (ChartPresenter) getPresenter();
                    if (c != null) {
                        w = c.getView().getOffsetWidth();
                        h = c.getView().getOffsetHeight();
                        if (c.getView().isTableTabSelected()) {
                            allowedTypes.remove(ExportType.PNG);
                            showSize = false;
                        }
                    }
                }
                return new VizExport(allowedTypes, showSize, w, h);
            case RELGRAPH:
            case RELGRAPH_V2:
                return new VizExport(allowedTypes, false, false);
            case MATRIX:
                return new VizExport(allowedTypes, false);
            case TIMELINE:
                return new VizExport(allowedTypes, false);
            default:
                return new VizExport(allowedTypes);
        }
    }
}