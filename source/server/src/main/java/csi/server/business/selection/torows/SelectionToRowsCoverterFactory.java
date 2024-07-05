package csi.server.business.selection.torows;

import csi.server.business.service.FilterActionsService;
import csi.server.business.visualization.graph.GraphContext;
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
public class SelectionToRowsCoverterFactory {

    private DataView dataView;
    private VisualizationDef visualizationDef;

    private SQLFactory sqlFactory;
    private FilterActionsService filterActionsService;

    public SelectionToRowsCoverterFactory(DataView dataView, VisualizationDef visualizationDef) {
        this.dataView = dataView;
        this.visualizationDef = visualizationDef;
        sqlFactory = new SQLFactoryImpl();
        filterActionsService = new FilterActionsService();
        filterActionsService.setSqlFactory(sqlFactory);
    }

    public SelectionToRowsConverter create(){
        if(visualizationDef instanceof DrillChartViewDef){
            return new ChartSelectionToRowsConverter(dataView, (DrillChartViewDef)visualizationDef, sqlFactory, filterActionsService);
        }
        if(visualizationDef instanceof MatrixViewDef){
            return new MatrixSelectionToRowsConverter(dataView, (MatrixViewDef)visualizationDef);
        }
        if(visualizationDef instanceof TableViewDef){
            return new TableSelectionToRowsConverter(dataView, (TableViewDef)visualizationDef, filterActionsService);
        }
        if(visualizationDef instanceof RelGraphViewDef){
            GraphContext graphContext = GraphServiceUtil.getGraphContext(visualizationDef.getUuid());
            return new GraphSelectionToRowsConverter(graphContext);
        }
        if(visualizationDef instanceof TimelineViewDef){
            return new TimelineSelectionToRowsConverter(dataView, (TimelineViewDef)visualizationDef, filterActionsService);
        }
        if(visualizationDef instanceof MapViewDef){
            return new MapSelectionToRowsConverter(dataView, (MapViewDef)visualizationDef, sqlFactory, filterActionsService);
        }
        return new NullSelectionToRowsConverter();
    }
}
