package csi.server.business.selection.operations;

import csi.server.common.model.visualization.selection.NullSelection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NullSelectionOperations implements SelectionOperations<NullSelection> {

    @Override
    public void add(NullSelection existingSelection, NullSelection selectionToAdd) {

    }

    @Override
    public void remove(NullSelection existingSelection, NullSelection removalSelection) {

    }

    @Override
    public void replace(NullSelection existingSelection, NullSelection replacingSelection) {

    }

}
