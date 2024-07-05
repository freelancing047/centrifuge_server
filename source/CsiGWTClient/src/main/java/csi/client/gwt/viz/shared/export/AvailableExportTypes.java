package csi.client.gwt.viz.shared.export;

import java.util.List;

import com.google.common.collect.Lists;

import csi.client.gwt.viz.map.presenter.MapLegendContainer;
import csi.client.gwt.viz.shared.export.model.*;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.timeline.TimelineViewDef;

/**
 * This drives the ExportTypes that are allowed on the ExportViewDialog and VizExport.
 *
 * @author Centrifuge Systems, Inc.
 */
public class AvailableExportTypes {

    private AvailableExportTypes() {
    }


    public static VisualizationDef getVisualisationDef(VisualizationExportable exportable){

        VisualizationDef modelObject = null;
        if(exportable instanceof CompoundVisualizationExportData){
            CompoundVisualizationExportData tmp = (CompoundVisualizationExportData) exportable;
            modelObject = tmp.getData();
        }else if(exportable instanceof VisualizationExportData){
            VisualizationExportData tmp = (VisualizationExportData) exportable;
            modelObject = tmp.getData();
        }
        return modelObject;

    }

    public static List<ExportType> getAvailableExportTypes(VisualizationExportable exportable) {
        if (exportable.getExportableType() == ExportableType.DATA_VIEW) {
            return getAvailableExportTypesForDataView();
        }
        if (exportable.getExportableType() == ExportableType.WORKSHEET) {
            return getAvailableExportTypesForWorksheet();
        }

        if (exportable.getExportableType() == ExportableType.VISUALIZATION) {

            VisualizationDef modelObject = getVisualisationDef(exportable);

            if (modelObject instanceof TableViewDef) {
                return getAvailableExportTypesForTable();
            }
            if (modelObject instanceof MatrixViewDef) {
                return getAvailableExportTypesForMatrix();
            }
            if (modelObject instanceof DrillChartViewDef) {
                return getAvailableExportTypesForChart();
            }
            if (modelObject instanceof RelGraphViewDef) {
                return getAvailableExportTypesForGraph();
            }
            if (modelObject instanceof TimelineViewDef) {
                return getAvailableExportTypesForTimeline();
            }
            if(modelObject instanceof MapViewDef){
                return getAvailableExportTypesForMap();
            }
        }

        return Lists.newArrayList();
    }

    public static ExportType getDefaultExportType(VisualizationExportable exportable) {
        if (exportable.getExportableType() == ExportableType.DATA_VIEW) {
            return ExportType.XML;
        }
        if (exportable.getExportableType() == ExportableType.WORKSHEET) {
            return ExportType.PNG;
        }

        if (exportable.getExportableType() == ExportableType.VISUALIZATION) {

            VisualizationDef modelObject = getVisualisationDef(exportable);

            if (modelObject instanceof TableViewDef) {
                return ExportType.CSV;
            }
            if (modelObject instanceof MatrixViewDef) {
                return ExportType.PNG;
            }
            if (modelObject instanceof DrillChartViewDef) {
                return ExportType.PNG;
            }
            if (modelObject instanceof RelGraphViewDef) {
                return ExportType.PNG;
            }
            if (modelObject instanceof TimelineViewDef) {
                return ExportType.CSV;
            }
            if(modelObject instanceof MapViewDef){
                return ExportType.CSV;
            }
        }
        return ExportType.NULL;
    }

    private static List<ExportType> getAvailableExportTypesForDataView() {
        return Lists.newArrayList(ExportType.XML);
    }

    private static List<ExportType> getAvailableExportTypesForWorksheet() {
        return Lists.newArrayList(ExportType.PNG);
    }

    private static List<ExportType> getAvailableExportTypesForTable() {
        return Lists.newArrayList(ExportType.CSV);
    }

    private static List<ExportType> getAvailableExportTypesForTimeline() {
        return Lists.newArrayList(ExportType.CSV, ExportType.PNG);
    }

    private static List<ExportType> getAvailableExportTypesForMatrix() {
        return Lists.newArrayList(ExportType.PNG, ExportType.CSV);
    }

    private static List<ExportType> getAvailableExportTypesForChart() {
        return Lists.newArrayList(ExportType.PNG, ExportType.CSV);
    }

    private static List<ExportType> getAvailableExportTypesForGraph() {
        return Lists.newArrayList(ExportType.PNG, ExportType.ANX);
    }

    private static List<ExportType> getAvailableExportTypesForMap() {
        return Lists.newArrayList(ExportType.CSV, ExportType.PNG);
    }

}
