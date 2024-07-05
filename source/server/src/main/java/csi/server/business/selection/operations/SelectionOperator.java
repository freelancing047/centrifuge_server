package csi.server.business.selection.operations;

import java.io.Serializable;

import csi.server.common.model.broadcast.BroadcastRequestType;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;
import csi.server.common.model.visualization.selection.MatrixCellSelection;
import csi.server.common.model.visualization.selection.RowsSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.model.visualization.selection.TimelineEventSelection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class SelectionOperator implements Serializable {
   private final Selection existingSelection;

   public SelectionOperator(Selection existingSelection) {
      this.existingSelection = existingSelection;
   }

   public void operateOnSelection(BroadcastRequestType selectionType, Selection operandSelection) {
      if (classesDoNotMatch(operandSelection)) {
         throw new RuntimeException("Unable to perform an operation on selection; they are not the same type.");
      }
      SelectionOperations operation = instantiateOperation();

      performOperation(selectionType, operandSelection, operation);
   }

   private void performOperation(BroadcastRequestType selectionType, Selection operandSelection,
                                 SelectionOperations operation) {
      if (selectionType == BroadcastRequestType.SELECTION_ADD) {
         operation.add(existingSelection, operandSelection);
      } else if (selectionType == BroadcastRequestType.SELECTION_REMOVE) {
         operation.remove(existingSelection, operandSelection);
      } else if (selectionType == BroadcastRequestType.SELECTION_REPLACE) {
         operation.replace(existingSelection, operandSelection);
      } else {
         throw new RuntimeException("Unable to perform an operation on selection; request type is not valid");
      }
   }

   public SelectionOperations instantiateOperation() {
      SelectionOperations result = null;

      if (existingSelection instanceof TimelineEventSelection) {
         result = new TimelineSelectionOperations();
      } else if (existingSelection instanceof IntegerRowsSelection) {
         result = new TableSelectionOperations();
      } else if (existingSelection instanceof RowsSelection) {
         result = new TableSelectionOperations();
      } else if (existingSelection instanceof ChartSelectionState) {
         result = new ChartSelectionOperations();
      } else if (existingSelection instanceof SelectionModel) {
         result = new GraphSelectionOperations();
      } else if (existingSelection instanceof MatrixCellSelection) {
         result = new MatrixSelectionOperations();
      } else if (existingSelection instanceof AbstractMapSelection) {
         result = new MapSelectionOperations();
      } else {
         result = new NullSelectionOperations();
      }
      return result;
   }

   private boolean classesDoNotMatch(Selection operandSelection) {
      return (!(existingSelection instanceof AbstractMapSelection) ||
              !(operandSelection instanceof AbstractMapSelection)) &&
             !operandSelection.getClass().isAssignableFrom(existingSelection.getClass());
   }

   public Selection getSelection() {
      return existingSelection;
   }
}
