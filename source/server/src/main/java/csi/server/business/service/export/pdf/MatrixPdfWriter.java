package csi.server.business.service.export.pdf;

import java.io.File;

import csi.server.common.model.visualization.matrix.MatrixViewDef;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MatrixPdfWriter implements PdfWriter {

    private final String dvUuid;
    private final MatrixViewDef visualizationDef;

    public MatrixPdfWriter(String dvUuid, MatrixViewDef visualizationDef) {
        this.dvUuid = dvUuid;
        this.visualizationDef = visualizationDef;
    }

    @Override
    public void writePdf(File fileToWrite) {

    }
}
