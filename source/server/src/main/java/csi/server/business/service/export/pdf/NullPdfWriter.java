package csi.server.business.service.export.pdf;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NullPdfWriter implements PdfWriter {
   private static final Logger LOG = LogManager.getLogger(NullPdfWriter.class);

   public static NullPdfWriter instance = new NullPdfWriter();

   private NullPdfWriter() {
   }

   @Override
   public void writePdf(File fileToWrite) {
      LOG.warn("Attempting to write a csv with the NullCsvWriter");
   }
}
