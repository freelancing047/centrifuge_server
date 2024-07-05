package csi.server.business.selection.toselection;

import java.util.List;
import java.util.Set;

import csi.server.business.selection.MatrixPair;
import csi.server.business.service.FilterActionsService;
import csi.server.business.service.matrix.MatrixData;
import csi.server.business.service.matrix.MatrixDataService;
import csi.server.business.service.matrix.MatrixQueryBuilder;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.selection.MatrixCellSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.sql.SQLFactory;
import csi.shared.core.visualization.matrix.Cell;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MatrixRowsToSelectionConverter implements RowsToSelectionConverter {
   private final DataView dataView;
   private final MatrixViewDef visualizationDef;
   private final SQLFactory sqlFactory;
   private final FilterActionsService filterActionsService;

   public MatrixRowsToSelectionConverter(DataView dataView, MatrixViewDef visualizationDef,
                                         SQLFactory sqlFactory, FilterActionsService filterActionsService) {
      this.dataView = dataView;
      this.visualizationDef = visualizationDef;
      this.sqlFactory = sqlFactory;
      this.filterActionsService = filterActionsService;
   }

//   public MatrixRowsToSelectionConverter(DataView dataView, MatrixViewDef visualizationDef, SQLFactory sqlFactory, FilterActionsService filterActionsService) {
//      this.dataView = dataView;
//      this.visualizationDef = visualizationDef;
//      this.sqlFactory = sqlFactory;
//      this.filterActionsService = filterActionsService;
//   }
//
//   public Selection toAxisSelection(Set<Integer> rows) {
//      MatrixSelectionState matrixSelectionState = new MatrixSelectionState();
//      MatrixQueryBuilder matrixQueryBuilder = new MatrixQueryBuilder();
//
//      matrixQueryBuilder.setViewDef(visualizationDef);
//      matrixQueryBuilder.setDataView(dataView);
//      matrixQueryBuilder.setSqlFactory(sqlFactory);
//      matrixQueryBuilder.setFilterActionsService(filterActionsService);
//
//      List<Integer> idsAsList = new ArrayList<Integer>();
//
//      idsAsList.addAll(rows);
//
//      List<Pair> pairs = matrixQueryBuilder.rowIdsToSelectionInfo(idsAsList);
//
//      for (Pair p : pairs) {
//         matrixSelectionState.getxAxisCategories().add(p.x);
//         matrixSelectionState.getyAxisCategories().add(p.y);
//      }
//      return matrixSelectionState;
//}

   public List<MatrixPair> toAxisSelection(List<Integer> rows) {
      MatrixQueryBuilder matrixQueryBuilder = new MatrixQueryBuilder();

      matrixQueryBuilder.setViewDef(visualizationDef);
      matrixQueryBuilder.setDataView(dataView);
      matrixQueryBuilder.setSqlFactory(sqlFactory);
      matrixQueryBuilder.setFilterActionsService(filterActionsService);
      return matrixQueryBuilder.rowIdsToSelectionInfo(rows);
   }

   public List<Integer> widenRows(List<Integer> rows) {
      MatrixQueryBuilder matrixQueryBuilder = new MatrixQueryBuilder();

      matrixQueryBuilder.setViewDef(visualizationDef);
      matrixQueryBuilder.setDataView(dataView);
      matrixQueryBuilder.setSqlFactory(sqlFactory);
      matrixQueryBuilder.setFilterActionsService(filterActionsService);
      return matrixQueryBuilder.widenRowSelection(rows);
   }

   @Override
   public Selection toSelection(Set<Integer> rows) {
      MatrixCellSelection rowsSelection = new MatrixCellSelection();
      MatrixViewDef viewDef = CsiPersistenceManager.findObject(MatrixViewDef.class, visualizationDef.getUuid());

      if ((rows == null) || rows.isEmpty()) {
         rowsSelection.clearSelection();
      } else {
         MatrixData mData = MatrixDataService.getMatrixData(viewDef, dataView.getUuid(), false);
         int cellSize = mData.getAllCells().size();

         for (int i = 0; i < cellSize; i++) {
            Cell cell = mData.getAllCells().get(i);

            for (Integer integer : mData.getCellIdMap().get(Integer.valueOf(i))) {
               if (rows.contains(integer)) {
                  rowsSelection.select(cell.getX(), cell.getY());
                  break;
               }
            }
         }
      }
      return rowsSelection;
   }
}
