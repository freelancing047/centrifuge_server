package csi.server.business.service.export.csv;

import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.timeline.TimelineViewDef;

/**
 * Creates the appropriate implementation of a CsvWriter
 * @author Centrifuge Systems, Inc.
 */
public class CsvWriterFactory {

    public static CsvWriter createCsvWriter(String dvUuid, VisualizationDef visualizationDef, boolean useSelectionOnly){
        switch (visualizationDef.getType()){
            case TABLE:
                return new TableCsvWriter(dvUuid, (TableViewDef)visualizationDef, useSelectionOnly);
            case MATRIX:
                return new MatrixCsvWriter(dvUuid, (MatrixViewDef)visualizationDef, useSelectionOnly);
            case DRILL_CHART:
                return new ChartCsvWriter(dvUuid, (DrillChartViewDef)visualizationDef, useSelectionOnly);
            case CHRONOS:
                return new TimelineCsvWriter(dvUuid, (TimelineViewDef)visualizationDef, useSelectionOnly);
            case GEOSPATIAL: case GEOSPATIAL_V2:
                return new MapCSVWriter(dvUuid, (MapViewDef) visualizationDef, useSelectionOnly);
        }

        return NullCsvWriter.instance;
    }

}
