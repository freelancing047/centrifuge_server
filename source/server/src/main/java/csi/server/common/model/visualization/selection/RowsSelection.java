package csi.server.common.model.visualization.selection;

import java.io.Serializable;
import java.util.Collection;

import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 */
@SuppressWarnings("serial")
public abstract class RowsSelection extends ModelObject implements Selection, Serializable {
   protected Collection selectedItems;

   protected RowsSelection() {
      super();
   }

   @Override
   public void clearSelection() {
      selectedItems.clear();
   }

   @Override
   public boolean isCleared() {
      return selectedItems.isEmpty();
   }

   public void makeSelectionStateForRows(Collection rows) {
      clearSelection();
      getSelectedItems().addAll(rows);
   }

   public Collection getSelectedItems() {
      return selectedItems;
   }

   public void setSelectedItems(Collection c) {
      selectedItems.addAll(c);
   }
}
//public abstract class AbstractRowsSelection extends ModelObject implements Selection {
//   protected AbstractRowsSelection() {
//      super();
//   }
//
//   public abstract Collection<Integer> getSelectedItems();
//   public abstract void setSelectedItems(Collection<Integer> c);
//
//   public void makeSelectionStateForRows(Collection<Integer> rows) {
//      clearSelection();
//      setSelectedItems(rows);
//   }
//}
