package csi.server.business.selection.operations;

import csi.server.common.model.visualization.selection.Selection;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface SelectionOperations<T extends Selection> {

    void add(T existingSelection, T selectionToAdd);

    void remove(T existingSelection, T removalSelection);

    void replace(T existingSelection, T replacingSelection);

}
