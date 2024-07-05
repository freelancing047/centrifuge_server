package csi.server.business.service.export.pdf;


import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.table.TableViewDef;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PdfWriterFactory {

    public static PdfWriter createPdfWriter(String dvUuid, VisualizationDef visualizationDef){
        switch (visualizationDef.getType()){
            case TABLE:
                return new TablePdfWriter(dvUuid, (TableViewDef)visualizationDef);
            case MATRIX:
                return new MatrixPdfWriter(dvUuid, (MatrixViewDef)visualizationDef);
            case DRILL_CHART:
                return new ChartPdfWriter(dvUuid, (DrillChartViewDef)visualizationDef);
        }
        return NullPdfWriter.instance;
    }
}
