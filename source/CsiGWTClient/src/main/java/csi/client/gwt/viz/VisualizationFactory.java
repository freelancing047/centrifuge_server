package csi.client.gwt.viz;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.chart.settings.ChartSettingsPresenter;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.client.gwt.viz.graph.settings.GraphSettings;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.map.settings.MapSettingsPresenter;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.matrix.settings.MatrixSettingsPresenter;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.client.gwt.viz.shared.settings.VisualizationSettingsPresenter;
import csi.client.gwt.viz.table.TablePresenter;
import csi.client.gwt.viz.table.settings.TableSettingsPresenter;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.viz.timeline.settings.TimelineSettingsPresenter;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.timeline.TimelineViewDef;

public class VisualizationFactory {

    public static <V extends VisualizationDef> VisualizationSettingsPresenter create(DataView dataView,
            WorksheetPresenter worksheetPresenter, VisualizationType type, SettingsActionCallback<V> callback) {

        VisualizationSettingsPresenter presenter = createPresenterFromCallback(type, callback);
        presenter.setDataView(dataView);
        presenter.setWorksheetPresenter(worksheetPresenter);
        presenter.setWorksheetUuid(worksheetPresenter.getUuid());
        presenter.setDataViewPresenter(DataViewRegistry.getInstance().dataViewPresenterForDataView(dataView.getUuid()));
        return presenter;
    }

    public static Visualization create(AbstractDataViewPresenter presenterIn, VisualizationDef visualization) {
        return createPresenterFromVisualization(presenterIn, visualization);
    }

    @SuppressWarnings("unchecked")
    private static <V extends VisualizationDef> VisualizationSettingsPresenter createPresenterFromCallback(VisualizationType type, SettingsActionCallback<V> callback) {
        switch (type) {
            case DRILL_CHART:
                return new ChartSettingsPresenter((SettingsActionCallback<DrillChartViewDef>) callback);

            case MATRIX:
                return new MatrixSettingsPresenter((SettingsActionCallback<MatrixViewDef>) callback);

            case TABLE:
                return new TableSettingsPresenter((SettingsActionCallback<TableViewDef>) callback);

            case RELGRAPH_V2:
                return new GraphSettings((SettingsActionCallback<RelGraphViewDef>) callback);

            case CHRONOS:
            	return new TimelineSettingsPresenter((SettingsActionCallback<TimelineViewDef>) callback);

            case GEOSPATIAL_V2:
            	return new MapSettingsPresenter((SettingsActionCallback<MapViewDef>) callback);

            default:
                throw new RuntimeException(CentrifugeConstantsLocator.get().visualizationFactory_dontKnowHow() + " " + type);
        }
    }

    private static Visualization createPresenterFromVisualization(AbstractDataViewPresenter presenterIn, VisualizationDef visualization) {
        switch (visualization.getType()) {
            case DRILL_CHART: {
                ChartPresenter presenter = new ChartPresenter(presenterIn, (DrillChartViewDef) visualization);
                WebMain.injector.injectInfrastructureAware(presenter);
                return presenter;
            }
            case RELGRAPH_V2: {
                return GraphImpl.create(visualization);
            }
            case TABLE: {
                TablePresenter presenter = new TablePresenter(presenterIn, (TableViewDef) visualization);
                WebMain.injector.injectInfrastructureAware(presenter);
                return presenter;
            }
            case MATRIX: {
                MatrixPresenter presenter = new MatrixPresenter(presenterIn, (MatrixViewDef) visualization);
                WebMain.injector.injectInfrastructureAware(presenter);
                return presenter;
            }
            case CHRONOS: {
            	TimelinePresenter presenter = new TimelinePresenter(presenterIn, (TimelineViewDef) visualization);
                WebMain.injector.injectInfrastructureAware(presenter);
                return presenter;
            }
            case GEOSPATIAL_V2: {
            	MapPresenter presenter = new MapPresenter(presenterIn, (MapViewDef) visualization);
                WebMain.injector.injectInfrastructureAware(presenter);
                return presenter;
            }
            default:
                throw new RuntimeException(CentrifugeConstantsLocator.get().visualizationFactory_unknowVizType()+ " " + visualization.getType());
        }
    }

}
