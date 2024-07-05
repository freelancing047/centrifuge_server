package csi.server.business.selection.torows;

import java.util.Set;

import com.google.common.collect.Sets;

import csi.server.common.model.visualization.selection.Selection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NullSelectionToRowsConverter implements SelectionToRowsConverter{
    @Override
    public Set<Integer> convertToRows(Selection selection, boolean excludeRows) {
        return Sets.newHashSet();
    }

}
