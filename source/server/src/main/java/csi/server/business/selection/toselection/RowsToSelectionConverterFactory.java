package csi.server.business.selection.toselection;

import csi.server.business.service.FilterActionsService;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.util.sql.SQLFactory;
import csi.server.util.sql.impl.SQLFactoryImpl;

/**
 * @author Centrifuge Systems, Inc.
 */
public class RowsToSelectionConverterFactory {

    private DataView dataView;
    private VisualizationDef visualizationDef;

    private SQLFactory sqlFactory;
    private FilterActionsService filterActionsService;

    public RowsToSelectionConverterFactory(DataView dataView, VisualizationDef visualizationDef) {
        this.dataView = dataView;
        this.visualizationDef = visualizationDef;
        sqlFactory = new SQLFactoryImpl();
        filterActionsService = new FilterActionsService();
        filterActionsService.setSqlFactory(sqlFactory);
    }

    public RowsToSelectionConverter create(){
        if(visualizationDef instanceof TableViewDef) {
            return new TableRowsToSelectionConverter();
        }
        if(visualizationDef instanceof DrillChartViewDef){
            return new ChartRowsToSelectionConverter(dataView, (DrillChartViewDef)visualizationDef, sqlFactory, filterActionsService);
        }
        if(visualizationDef instanceof MatrixViewDef){
            return new MatrixRowsToSelectionConverter(dataView, (MatrixViewDef)visualizationDef, sqlFactory, filterActionsService);
        }
        if(visualizationDef instanceof RelGraphViewDef){
            return new GraphRowsToSelectionConverter(GraphServiceUtil.getGraphContext(visualizationDef.getUuid()));
        }
        if(visualizationDef instanceof TimelineViewDef){
            return new TimelineRowsToSelectionConverter(dataView, (TimelineViewDef)visualizationDef, sqlFactory, filterActionsService);
        }
        if(visualizationDef instanceof MapViewDef){
        	return new MapRowsToSelectionConverter(dataView, (MapViewDef)visualizationDef, sqlFactory, filterActionsService);
        }
        return new NullRowsToSelectionConverter();
    }
}
