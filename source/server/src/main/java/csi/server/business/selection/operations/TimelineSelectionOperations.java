package csi.server.business.selection.operations;

import java.util.HashSet;
import java.util.Set;

import csi.server.common.model.visualization.selection.TimelineEventSelection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TimelineSelectionOperations implements SelectionOperations<TimelineEventSelection>{

    @Override
    public void add(TimelineEventSelection existingSelection, TimelineEventSelection selectionToAdd) {
        Set<Integer> itemSet = new HashSet<Integer>();
        itemSet.addAll(existingSelection.getSelectedItems());
          itemSet.addAll(selectionToAdd.getSelectedItems());
          existingSelection.makeSelectionStateForRows(itemSet);
    }

    @Override
    public void remove(TimelineEventSelection existingSelection, TimelineEventSelection removalSelection) {
        existingSelection.getSelectedItems().removeAll(removalSelection.getSelectedItems());
    }

    @Override
    public void replace(TimelineEventSelection existingSelection, TimelineEventSelection replacingSelection) {
        existingSelection.makeSelectionStateForRows(replacingSelection.getSelectedItems());
    }



}
