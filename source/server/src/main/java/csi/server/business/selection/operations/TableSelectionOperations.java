package csi.server.business.selection.operations;

import java.util.HashSet;
import java.util.Set;

import csi.server.common.model.visualization.selection.RowsSelection;
import csi.shared.core.util.IntCollection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TableSelectionOperations implements SelectionOperations<RowsSelection> {

   @Override
   public void add(RowsSelection existingSelection, RowsSelection selectionToAdd) {
      Set<Integer> itemSet = new HashSet<Integer>();

      itemSet.addAll(existingSelection.getSelectedItems());
      itemSet.addAll(selectionToAdd.getSelectedItems());
      existingSelection.makeSelectionStateForRows(itemSet);
   }

   @Override
   public void remove(RowsSelection existingSelection, RowsSelection removalSelection) {
      IntCollection collectionA = new IntCollection();

      collectionA.addAll(existingSelection.getSelectedItems());
      collectionA.removeAll(removalSelection.getSelectedItems());
      // .removeAll(removalSelection.getSelectedItems());
      existingSelection.makeSelectionStateForRows(collectionA);
   }

   @Override
   public void replace(RowsSelection existingSelection, RowsSelection replacingSelection) {
      existingSelection.makeSelectionStateForRows(replacingSelection.getSelectedItems());
   }
}
