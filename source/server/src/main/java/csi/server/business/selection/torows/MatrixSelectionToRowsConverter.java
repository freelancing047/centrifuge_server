
package csi.server.business.selection.torows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mongodb.DBObject;

import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.service.matrix.MatrixData;
import csi.server.business.service.matrix.storage.AbstractMatrixCacheStorageService;
import csi.server.business.service.matrix.storage.MatrixCacheStorage;
import csi.server.business.service.matrix.storage.postgres.BSONtoMatrixData;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.selection.MatrixCellSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.shared.core.visualization.matrix.Cell;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MatrixSelectionToRowsConverter implements SelectionToRowsConverter {
    private final MatrixViewDef visualizationDef;

    public MatrixSelectionToRowsConverter(DataView dataView, MatrixViewDef visualizationDef) {
        this.visualizationDef = visualizationDef;
    }

    @Override
    public Set<Integer> convertToRows(Selection selection, boolean ignoreBroadcast) {
        Set<Integer> integers = new HashSet<Integer>();
        if (!(selection instanceof MatrixCellSelection)) {
            return integers;
        }

        MatrixCellSelection cellSelection = (MatrixCellSelection) selection;
        AbstractMatrixCacheStorageService storageService = AbstractMatrixCacheStorageService.instance();
        boolean isCache = storageService.hasVisualizationData(visualizationDef.getUuid());
        if (isCache) {
            MatrixData mData;
            MatrixCacheStorage matrixStorage = storageService.getMatrixCacheStorage(visualizationDef.getUuid());
            DBObject storedMatrix = (DBObject) matrixStorage.getResult();
            BSONtoMatrixData transformer = new BSONtoMatrixData();
            try {
                mData = transformer.apply(storedMatrix);
                int howMany =  mData.getAllCells().size();
                for (int i = 0; i < howMany; i++) {
                    Cell cell = mData.getAllCells().get(i);
                    if (cellSelection.contains(cell.getX(), cell.getY())) {
                        integers.addAll(mData.getCellIdMap().get(i));
                    }
                }
            }
            catch (Exception e) {

            }
        }
        return integers;
    }

    public Set<Integer> filterBroadcastFromSelection(MatrixCellSelection selection){
        BroadcastResult result = AbstractBroadcastStorageService.instance().getBroadcast(visualizationDef.getUuid());
        if((result != null) && (result.getBroadcastFilter() != null)
                && (result.getBroadcastFilter().getSelectedItems() != null) && !result.getBroadcastFilter().isCleared()){

            List<Integer> broadcastItems = result.getBroadcastFilter().getSelectedItems();

            //FIXME:
            //List<Integer> selectionSet = selection.getSelectedItemsList();
            List<Integer> selectionSet = new ArrayList<Integer>();

            Set<Integer> resultSet = null;
            if(result.isExcludeRows()){
                resultSet = Sets.difference(ImmutableSet.copyOf(selectionSet), ImmutableSet.copyOf(broadcastItems));
            } else {
                resultSet = Sets.intersection(ImmutableSet.copyOf(broadcastItems), ImmutableSet.copyOf(selectionSet));
            }

            return resultSet;
        } else {
            //FIXME:
            //return selection.getSelectedItemsSet();
            return new HashSet<Integer>();
        }

    }

    /**
     * This method should be removed once RowsSelection is properly converted to Integers
     *
     * @param resultSet
     * @return
     */
   private Set<Integer> convertStringsToInteger(Collection<String> resultSet) {
      Set<Integer> result = Collections.emptySet();

      if (!resultSet.isEmpty()) {
         result = new HashSet<Integer>();

         for (String each : resultSet) {
            try {
               result.add(Integer.decode(each));
            } catch (Exception e) {
               //Hopefully never happens
            }
         }
      }
      return result;
   }
}
