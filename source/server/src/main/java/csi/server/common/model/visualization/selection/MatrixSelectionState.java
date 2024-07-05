package csi.server.common.model.visualization.selection;

import java.util.ArrayList;
import java.util.Collection;

import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 * @deprecated
 */
@Deprecated
public class MatrixSelectionState extends ModelObject implements Selection {

    private ArrayList<String> xAxisCategories = new ArrayList<String>();
    private ArrayList<String> yAxisCategories = new ArrayList<String>();

    @Deprecated
    public MatrixSelectionState() {
        super();
    }

    @Deprecated
    public void makeSelectionStateForCategories(Collection<String> xAxisCategories, Collection<String> yAxisCategories){
        clearSelection();
        getxAxisCategories().addAll(xAxisCategories);
        getyAxisCategories().addAll(yAxisCategories);
    }

    @Override
    public void clearSelection(){
        xAxisCategories.clear();
        yAxisCategories.clear();
    }

    @Override
    public boolean isCleared() {
        return xAxisCategories.isEmpty() && yAxisCategories.isEmpty();
    }

    @Override
    public void setFromSelection(Selection selection) {
        if(!(selection instanceof MatrixSelectionState)){
            clearSelection();
            return;
        }

        MatrixSelectionState matrixSelectionState = (MatrixSelectionState)selection;
        makeSelectionStateForCategories(matrixSelectionState.getxAxisCategories(), matrixSelectionState.getyAxisCategories());
    }

    @Deprecated
    public ArrayList<String> getxAxisCategories() {
        return xAxisCategories;
    }

    @Deprecated
    public ArrayList<String> getyAxisCategories() {
        return yAxisCategories;
    }

	@Override
	public Selection copy() {
		MatrixSelectionState selection = new MatrixSelectionState();
		selection.setFromSelection(this);
		return selection;
	}

}