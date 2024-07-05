package csi.server.business.service.export.csv;

import java.io.File;
import java.io.FileWriter;

import com.google.common.base.Throwables;

import csi.server.business.service.matrix.MatrixData;
import csi.server.business.service.matrix.MatrixDataService;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.selection.MatrixCellSelection;
import csi.shared.core.visualization.matrix.Cell;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Writes matrix data displayed on the visualization as a CSV
 *
 * @author Centrifuge Systems, Inc.
 */
public class MatrixCsvWriter implements CsvWriter {
   private final String dvUuid;
   private final MatrixViewDef visualizationDef;
   private final boolean useSelectionOnly;

   public MatrixCsvWriter(String dvUuid, MatrixViewDef visualizationDef, boolean useSelectionOnly) {
      this.dvUuid = dvUuid;
      this.visualizationDef = visualizationDef;
      this.useSelectionOnly = useSelectionOnly;
   }

   @Override
   public void writeCsv(File fileToWrite) {
      try (FileWriter fileWriter = new FileWriter(fileToWrite);
           CSVWriter csvWriter = new CSVWriter(fileWriter)) {
         MatrixData matrixData = MatrixDataService.getMatrixData(visualizationDef, dvUuid, false);
         MatrixCellSelection selection = visualizationDef.getSelection();

         for (Cell c : matrixData.getAllCells()) {
            if (useSelectionOnly) {
               if (selection.getSelectedCells().contains(new MatrixCellSelection.CellPosition(c.getX(), c.getY()))) {
                  csvWriter.writeNext(getRowFromCell(c, matrixData));
               }
            } else {
               csvWriter.writeNext(getRowFromCell(c, matrixData));
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
         Throwables.propagate(e);
      }
   }

   private String xName() {
      return visualizationDef.getMatrixSettings().getAxisX().get(0).getComposedName();
   }

   private String yName() {
      return visualizationDef.getMatrixSettings().getAxisY().get(0).getComposedName();
   }

   private String mName() {
      return visualizationDef.getMatrixSettings().isUseCountForMeasure()
                ? "Count(*)"
                : visualizationDef.getMatrixSettings().getMatrixMeasureDefinition().getComposedName();
   }

   private String[] getCellExportHeaders() {
      return new String[] { xName(), yName(), mName() };
   }

   private String[] getRowFromCell(Cell cell, MatrixData matrixData) {
      String[] fRow = new String[3];

      fRow[0] = matrixData.getxCategories().get(cell.getX()).getLabel();
      fRow[1] = matrixData.getyCategories().get(cell.getY()).getLabel();
      fRow[2] = String.valueOf(cell.getValue().doubleValue());
      return fRow;
   }
}
