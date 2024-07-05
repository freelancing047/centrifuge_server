package csi.server.business.selection.torows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import csi.server.business.helper.QueryHelper;
import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.service.FilterActionsService;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.selection.IntPrimitiveSelection;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;
import csi.server.common.model.visualization.selection.RowsSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.dao.CsiPersistenceManager;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TableSelectionToRowsConverter implements SelectionToRowsConverter {
    private final TableViewDef visualizationDef;

    public TableSelectionToRowsConverter(DataView dataviewIn, TableViewDef visualizationDef, FilterActionsService filterActionsService) {
        this.visualizationDef = visualizationDef;
    }

   @Override
   public Set<Integer> convertToRows(Selection selection, boolean ignoreBroadcast) {
      Set<Integer> result = new HashSet<Integer>();

      if (selection instanceof IntegerRowsSelection) {
         IntegerRowsSelection mySelection = (IntegerRowsSelection) selection;

         if ((mySelection.getSelectedItems() != null) && (!mySelection.getSelectedItems().isEmpty())) {
            //TableQueryBuilder tableQueryBuilder = new TableQueryBuilder(_dataview, visualizationDef);
            result = filterBroadcastFromSelection(mySelection);
                //return getRowsFromCacheTable(tableQuery);
         }
      } else if (selection instanceof IntPrimitiveSelection) {
         IntPrimitiveSelection mySelection = (IntPrimitiveSelection) selection;

         if ((mySelection.getSelectedItems() != null) && !mySelection.getSelectedItems().isEmpty()) {
            //TableQueryBuilder tableQueryBuilder = new TableQueryBuilder(_dataview, visualizationDef);
            result = filterBroadcastFromSelection(mySelection);
            //return getRowsFromCacheTable(tableQuery);
         }
      }
      return result;
   }

   private Set<Integer> getRowsFromCacheTable(String tableQuery) {
      Set<Integer> rows = new HashSet<Integer>();

      try (Connection conn = CsiPersistenceManager.getCacheConnection();
           ResultSet rs = QueryHelper.executeSingleQuery(conn, tableQuery, null)) {
         while (rs.next()) {
            rows.add(Integer.valueOf(rs.getInt(1)));
         }
      } catch (Exception e) {
         Throwables.propagate(e);
      }
      return rows;
   }

    public Set<Integer> filterBroadcastFromSelection(IntegerRowsSelection selection){
        BroadcastResult result = AbstractBroadcastStorageService.instance().getBroadcast(visualizationDef.getUuid());
        if((result != null) && (result.getBroadcastFilter() != null)
                && (result.getBroadcastFilter().getSelectedItems() != null) && !result.getBroadcastFilter().isCleared()){

            List<Integer> broadcastItems = result.getBroadcastFilter().getSelectedItems();

            Set<Integer> broadcastSet = ImmutableSet.copyOf(broadcastItems);
            Set<Integer> selectionSet = ImmutableSet.copyOf(selection.getSelectedItems());

            Set<Integer> resultSet = new HashSet<Integer>();
            if(result.isExcludeRows()){
                resultSet.addAll(Sets.difference(selectionSet, broadcastSet));
            } else {
                resultSet.addAll(Sets.intersection(broadcastSet, selectionSet));
            }

            return resultSet;
        } else {
            return new HashSet<Integer>(selection.getSelectedItems());
        }

    }

    public Set<Integer> filterBroadcastFromSelection(RowsSelection selection){
        BroadcastResult result = AbstractBroadcastStorageService.instance().getBroadcast(visualizationDef.getUuid());
        if((result != null) && (result.getBroadcastFilter() != null)
                && (result.getBroadcastFilter().getSelectedItems() != null) && !result.getBroadcastFilter().isCleared()){

            List<Integer> broadcastItems = result.getBroadcastFilter().getSelectedItems();

            Set<Integer> broadcastSet = ImmutableSet.copyOf(broadcastItems);
            Set<Integer> selectionSet = ImmutableSet.copyOf(selection.getSelectedItems());

            Set<Integer> resultSet = new HashSet<Integer>();
            if(result.isExcludeRows()){
                resultSet.addAll(Sets.difference(selectionSet, broadcastSet));
            } else {
                resultSet.addAll(Sets.intersection(broadcastSet, selectionSet));
            }

            return resultSet;
        } else {
            return new HashSet<Integer>(selection.getSelectedItems());
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
