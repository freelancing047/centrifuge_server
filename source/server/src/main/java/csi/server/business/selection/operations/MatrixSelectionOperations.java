package csi.server.business.selection.operations;

import csi.server.common.model.visualization.selection.MatrixCellSelection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MatrixSelectionOperations implements SelectionOperations<MatrixCellSelection> {

    @Override
    public void add(MatrixCellSelection existingSelection, MatrixCellSelection selectionToAdd) {
        existingSelection.addAll(selectionToAdd.getSelectedCells());
    }

    @Override
    public void remove(MatrixCellSelection existingSelection, MatrixCellSelection removalSelection) {
        existingSelection.removeAll(removalSelection.getSelectedCells());
    }

    @Override
    public void replace(MatrixCellSelection existingSelection, MatrixCellSelection replacingSelection) {
        existingSelection.setFromSelection(replacingSelection);
    }
}
