package csi.server.business.selection.torows;

import java.util.Set;

import csi.server.common.model.visualization.selection.Selection;

/**
 * Implementations convert a selection into rows.
 * @author Centrifuge Systems, Inc.
 */
public interface SelectionToRowsConverter {

    public Set<Integer> convertToRows(Selection selection, boolean ignoreBroadcast);


}
