package csi.server.common.model.visualization.selection;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.shared.core.util.IntCollection;

/**
 * We use this to save on some space. It also provides convenience methods.
 *
 * @author jdanberg
 *
 */
public class IntPrimitiveSelection extends RowsSelection implements IsSerializable {
   public IntPrimitiveSelection() {
      super();
      selectedItems = new IntCollection();
   }

   @Override
   public Selection copy() {
      IntPrimitiveSelection selection = new IntPrimitiveSelection();
      selection.setFromSelection(this);
      return selection;
   }

   @Override
   public IntCollection getSelectedItems() {
      return (IntCollection) selectedItems;
   }

   @Override
   public void setFromSelection(Selection selection) {
      if (!(selection instanceof IntPrimitiveSelection)) {
         clearSelection();
         return;
      }
      IntPrimitiveSelection rowsSelection = (IntPrimitiveSelection) selection;
      makeSelectionStateForRows(rowsSelection.getSelectedItems());
   }
}

//   private IntCollection intSelectedItems;
//
//   public IntPrimitiveSelection() {
//      super();
//      intSelectedItems = new IntCollection();
//   }
//
//   public IntCollection getSelectedIntCollection() {
//      return intSelectedItems;
//   }
//
//   @Override
//   public List<Integer> getSelectedItems() {
//      return Arrays.asList((Integer[]) intSelectedItems.toArray());
//   }
//
//   @Override
//   public void setSelectedItems(Collection<Integer> c) {
//      if (c != null) {
//         intSelectedItems.addAll(c);
//      }
//   }
//
//   @Override
//   public void clearSelection() {
//      intSelectedItems.clear();
//   }
//
//   @Override
//   public boolean isCleared() {
//      return intSelectedItems.isEmpty();
//   }
//
//   @Override
//   public void setFromSelection(Selection selection) {
//      clearSelection();
//
//      if (selection instanceof IntPrimitiveSelection) {
//         setSelectedItems(((IntPrimitiveSelection) selection).getSelectedItems());
//      }
//   }
//
//   @Override
//   public Selection copy() {
//      IntPrimitiveSelection selection = new IntPrimitiveSelection();
//
//      selection.setSelectedItems(getSelectedItems());
//      return selection;
//   }
