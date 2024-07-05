package csi.server.business.service.export.csv;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Null object pattern for a CsvWriter
 * 
 * @author Centrifuge Systems, Inc.
 */
public class NullCsvWriter implements CsvWriter {
   private static final Logger LOG = LogManager.getLogger(NullCsvWriter.class);

   public static NullCsvWriter instance = new NullCsvWriter();

   private NullCsvWriter() {
   }

   @Override
   public void writeCsv(File fileToWrite) {
      LOG.warn("Attempting to write a csv with the NullCsvWriter");
   }
}
