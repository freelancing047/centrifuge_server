package csi.server.common.model.visualization.selection;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import csi.server.common.model.ModelObject;
import csi.shared.core.util.CsiArrayUtils;

/**
 * @author Centrifuge Systems, Inc.
 */
@Deprecated
public class MatrixRowsSelection extends ModelObject implements Selection, Serializable {

    private int[] selectedItems = new int[0];

    @Deprecated
    public MatrixRowsSelection() {
        super();
    }

    @Override
    public void clearSelection(){
        selectedItems = new int[0];
    }

    @Override
    public boolean isCleared() {
        return selectedItems == null || selectedItems.length == 0;
    }

    @Override
    public void setFromSelection(Selection selection) {
        if(!(selection instanceof MatrixRowsSelection)){
            clearSelection();
            return;
        }

        MatrixRowsSelection rowsSelection = (MatrixRowsSelection)selection;
        makeSelectionStateForRows(rowsSelection.getSelectedItems());
    }

    @Deprecated
    private void makeSelectionStateForRows(int[] selectedItems) {
        clearSelection();
        this.selectedItems = selectedItems;
    }
    @Deprecated
    public List<Integer> getSelectedItemsList() {
        return Ints.asList(getSelectedItems());
    }
    @Deprecated
    public Set<Integer> getSelectedItemsSet() {
        
        return Sets.newHashSet(Ints.asList(getSelectedItems()));
    }


    @Override
	public Selection copy() {
    	MatrixRowsSelection selection = new MatrixRowsSelection();
		selection.setFromSelection(this);
		return selection;
	}

    /**
     * This makes sure there are no id dupes
     * @param selectedItems
     */
    @Deprecated
    public void setSelectedItems(int[] selectedItems) {
        this.selectedItems = CsiArrayUtils.deDupe(selectedItems);
    }

    @Deprecated
    public int[] getSelectedItems() {
        if(selectedItems == null){
            return new int[0];
        }

        return selectedItems;
    }

}
