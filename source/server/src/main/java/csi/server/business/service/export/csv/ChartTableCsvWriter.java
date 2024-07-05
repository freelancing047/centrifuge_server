package csi.server.business.service.export.csv;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import com.google.common.base.Throwables;

import au.com.bytecode.opencsv.CSVWriter;

public class ChartTableCsvWriter implements CsvWriter {
   private List<String> headers;
   private List<List<String>> data;

   public ChartTableCsvWriter(List<String> headers, List<List<String>> data) {
      this.headers = headers;
      this.data = data;
   }

   @Override
   public void writeCsv(File fileToWrite) {
      try (FileWriter fileWriter = new FileWriter(fileToWrite);
           CSVWriter csvWriter = new CSVWriter(fileWriter)) {
         String[] headersLine = new String[headers.size()];
         headersLine = headers.toArray(headersLine);

         csvWriter.writeNext(headersLine);

         for (List<String> line : data) {
            String[] dataLine = new String[line.size()];
            dataLine = line.toArray(dataLine);

            csvWriter.writeNext(dataLine);
         }
      } catch (Exception e) {
         Throwables.propagate(e);
      }
   }
}
