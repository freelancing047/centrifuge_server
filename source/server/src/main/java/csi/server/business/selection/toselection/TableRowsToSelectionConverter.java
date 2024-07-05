package csi.server.business.selection.toselection;

import java.util.ArrayList;
import java.util.Set;

import csi.server.common.model.visualization.selection.IntPrimitiveSelection;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TableRowsToSelectionConverter implements RowsToSelectionConverter{

    @Override
    public IntPrimitiveSelection toSelection(Set<Integer> rows) {
        IntPrimitiveSelection rowsSelection = new IntPrimitiveSelection();

        ArrayList<Integer> selectedRows = new ArrayList<Integer>();
        for(Integer rowId : rows){
            selectedRows.add(rowId);
        }
        rowsSelection.getSelectedItems().addAll(selectedRows);

        return rowsSelection;
    }
    
    public IntegerRowsSelection toRowsSelection(Set<Integer> rows) {
        IntegerRowsSelection rowsSelection = new IntegerRowsSelection();

        ArrayList<Integer> selectedRows = new ArrayList<Integer>();
        for(Integer rowId : rows){
            selectedRows.add(rowId);
        }
        rowsSelection.getSelectedItems().addAll(selectedRows);

        return rowsSelection;
    }
}
