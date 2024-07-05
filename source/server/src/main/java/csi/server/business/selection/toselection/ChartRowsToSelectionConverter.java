package csi.server.business.selection.toselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import csi.server.business.service.FilterActionsService;
import csi.server.business.service.chart.ChartQueryBuilder;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.util.sql.SQLFactory;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartRowsToSelectionConverter implements RowsToSelectionConverter{

    private final DrillChartViewDef visualizationDef;
    private final DataView dataView;
    private final SQLFactory sqlFactory;
    private final FilterActionsService filterActionsService;

    public ChartRowsToSelectionConverter(DataView dataView, DrillChartViewDef visualizationDef, SQLFactory sqlFactory, FilterActionsService filterActionsService) {
        this.dataView = dataView;
        this.visualizationDef = visualizationDef;
        this.sqlFactory = sqlFactory;
        this.filterActionsService = filterActionsService;
    }

    @Override
    public Selection toSelection(Set<Integer> rows) {
        ChartSelectionState chartSelectionState = new ChartSelectionState();

        ChartQueryBuilder chartQueryBuilder = new ChartQueryBuilder();
        chartQueryBuilder.setDataView(dataView);
        chartQueryBuilder.setViewDef(visualizationDef);
        chartQueryBuilder.setSqlFactory(sqlFactory);
        chartQueryBuilder.setFilterActionsService(filterActionsService);

        List<Integer> idsAsList = new ArrayList<Integer>();
        idsAsList.addAll(rows);

        if(!idsAsList.isEmpty()){
            List<DrillCategory> drillCategories = chartQueryBuilder.rowIdsToSelectionInfo(idsAsList);
            chartSelectionState.makeSelectionStateForCategories(drillCategories);
        }

        return chartSelectionState;
    }
}
