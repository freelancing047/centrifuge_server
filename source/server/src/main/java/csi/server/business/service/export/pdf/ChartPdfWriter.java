package csi.server.business.service.export.pdf;

import java.io.File;

import csi.server.common.model.visualization.chart.DrillChartViewDef;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartPdfWriter implements PdfWriter {

    private final String dvUuid;
    private final DrillChartViewDef visualizationDef;

    public ChartPdfWriter(String dvUuid, DrillChartViewDef visualizationDef) {

        this.dvUuid = dvUuid;
        this.visualizationDef = visualizationDef;
    }

    @Override
    public void writePdf(File fileToWrite) {

    }
}
