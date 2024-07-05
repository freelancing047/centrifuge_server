package csi.server.business.selection.operations;

import csi.server.common.model.visualization.selection.SelectionModel;

/**
 * @author Centrifuge Systems, Inc.
 */
public class GraphSelectionOperations implements SelectionOperations<SelectionModel> {

    @Override
    public void add(SelectionModel existingSelection, SelectionModel selectionToAdd) {
        existingSelection.merge(selectionToAdd);
    }

    @Override
    public void remove(SelectionModel existingSelection, SelectionModel removalSelection) {
        existingSelection.nodes.removeAll(removalSelection.nodes);
        existingSelection.links.removeAll(removalSelection.links);
        existingSelection.nodeKeys.removeAll(removalSelection.nodeKeys);
        existingSelection.linkKeys.removeAll(removalSelection.linkKeys);
    }

    @Override
    public void replace(SelectionModel existingSelection, SelectionModel replacingSelection) {
        existingSelection.clearSelection();
        existingSelection.resetKeys();
        existingSelection.nodes.addAll(replacingSelection.nodes);
        existingSelection.links.addAll(replacingSelection.links);
        existingSelection.nodeKeys.addAll(replacingSelection.nodeKeys);
        existingSelection.linkKeys.addAll(replacingSelection.linkKeys);
    }

}
