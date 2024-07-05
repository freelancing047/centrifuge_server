package csi.server.business.selection.toselection;

import java.util.Set;

import csi.server.common.model.visualization.selection.Selection;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface RowsToSelectionConverter  {
    Selection toSelection(Set<Integer> rows);
}
