package csi.server.business.service.export.csv;

import java.io.File;

/**
 * Implementations will write comma seperated values to the given file.
 *
 * @author Centrifuge Systems, Inc.
 */
public interface CsvWriter {

    public void writeCsv(File fileToWrite);
}
