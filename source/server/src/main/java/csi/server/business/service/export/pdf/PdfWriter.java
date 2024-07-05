package csi.server.business.service.export.pdf;

import java.io.File;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface PdfWriter {

    public void writePdf(File fileToWrite);
}
