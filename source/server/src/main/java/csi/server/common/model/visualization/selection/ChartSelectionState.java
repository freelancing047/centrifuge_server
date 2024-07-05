package csi.server.common.model.visualization.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartSelectionState extends ModelObject implements Serializable, Selection {

    private List<DrillCategory> selectedItems = new ArrayList<DrillCategory>();
    private List<String> drillSelections = null;

    public ChartSelectionState() {
        super();
    }

    public void makeSelectionStateForCategories(Collection<DrillCategory> rows){
        clearSelection();
        selectedItems.addAll(rows);
    }

    @Override
    public void clearSelection(){
        selectedItems.clear();
    }

    @Override
    public void setFromSelection(Selection selection) {
        if(!(selection instanceof ChartSelectionState)){
            clearSelection();
            return;
        }

        ChartSelectionState chartSelectionState = (ChartSelectionState)selection;
        this.setDrillSelections(chartSelectionState.getDrillSelections());
        makeSelectionStateForCategories(chartSelectionState.getSelectedItems());
    }

    @Override
    public boolean isCleared() {
        return selectedItems.isEmpty();
    }

    public List<DrillCategory> getSelectedItems() {
        return selectedItems;
    }
    
    @Override
	public Selection copy() {
    	ChartSelectionState selection = new ChartSelectionState();
		selection.setFromSelection(this);
		return selection;
	}

    public List<String> getDrillSelections() {
        return drillSelections;
    }

    public void setDrillSelections(List<String> drillSelections) {
        this.drillSelections = drillSelections;
    }

}