package csi.server.business.selection.toselection;

import java.util.Set;

import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NullRowsToSelectionConverter implements RowsToSelectionConverter {
    @Override
    public Selection toSelection(Set<Integer> rows) {
        return NullSelection.instance;
    }
}
