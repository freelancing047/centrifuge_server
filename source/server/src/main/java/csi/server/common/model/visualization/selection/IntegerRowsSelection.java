package csi.server.common.model.visualization.selection;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class IntegerRowsSelection extends RowsSelection {
   public IntegerRowsSelection() {
      super();
      selectedItems = Lists.newArrayList();
   }

   @Override
   public Selection copy() {
      IntegerRowsSelection selection = new IntegerRowsSelection();
      selection.setFromSelection(this);
      return selection;
   }

   @Override
   public List<Integer> getSelectedItems() {
      return (ArrayList<Integer>) selectedItems;
   }

   @Override
   public void setFromSelection(Selection selection) {
      if (!(selection instanceof IntegerRowsSelection)) {
         clearSelection();
         return;
      }
      IntegerRowsSelection rowsSelection = (IntegerRowsSelection) selection;
      makeSelectionStateForRows(rowsSelection.getSelectedItems());
   }
}
//   private List<Integer> selectedItems;
//
//   public IntegerRowsSelection() {
//      super();
//      selectedItems = new ArrayList<Integer>();
//   }
//
//   @Override
//   public List<Integer> getSelectedItems() {
//      return selectedItems;
//   }
//
//   @Override
//   public void setSelectedItems(Collection<Integer> c) {
//      if (c != null) {
//         selectedItems.addAll(c);
//      }
//   }
//
//   @Override
//   public void clearSelection() {
//      selectedItems.clear();
//   }
//
//   @Override
//   public boolean isCleared() {
//      return selectedItems.isEmpty();
//   }
//
//   @Override
//   public void setFromSelection(Selection selection) {
//      clearSelection();
//
//      if (selection instanceof IntegerRowsSelection) {
//         setSelectedItems(((IntegerRowsSelection) selection).getSelectedItems());
//      }
//   }
//
//   @Override
//   public Selection copy() {
//      IntegerRowsSelection selection = new IntegerRowsSelection();
//
//      selection.setSelectedItems(getSelectedItems());
//      return selection;
//   }

