package csi.client.gwt.viz.matrix.menu;

import java.util.List;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.matrix.Axis;
import csi.server.common.model.visualization.matrix.MatrixCategoryDefinition;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixSortDefinition;

/**
 * Menu Click handler for the quick sort on the matrix.
 *
 * During the init, we pass in the axis which this handler will be responsible for, and when the menu items is clicked,
 * we sort the matrix by that axis.
 *
 *
 *
 */
public class AxisQuickSortHandler extends ToggleSortHandler {
    private Axis axis;

    public AxisQuickSortHandler(MatrixPresenter presenter, MatrixMenuManager menuManager, Axis axis) {
        super(presenter, menuManager);
        this.axis = axis;
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().showLoading();

        sortMatrixByAxis(axis);
    }

    /**
     * given the axis, will sort the matrix view, toggling sort order with each call if sort is present.
     * @param axis - axis which to sort on.
     */
    private void sortMatrixByAxis(Axis axis){
        MatrixSortDefinition quickSortDef = getQuickAxisSortDef(axis);
        MatrixSettings mSettings = getPresenter().getVisualizationDef().getMatrixSettings();
        List<MatrixSortDefinition> axisQuickSortDefinitions = mSettings.getAxisQuickSortDefinitions();

        removeMeasureQuickSort();

        if(quickSortDef != null){
            quickSortDef.toggleSortOrder();
        }else {
            quickSortDef = new MatrixSortDefinition();
            quickSortDef.setCategoryDefinition(getCategoryDefinitionForAxis(axis));
            axisQuickSortDefinitions.add(quickSortDef);
        }

        saveSettings();
    }


    private void removeMeasureQuickSort(){
        //clear out the measure quick sort settings just so they aren't sitting in there all forgotten
        MatrixSettings sets = getPresenter().getVisualizationDef().getMatrixSettings();
        sets.setMeasureQuickSortOrder(SortOrder.NONE);
    }


    /**
     * grabs the category definition for the given axis from Matrix Settings
     * @param axis - axis for which the category is needed
     * @return either MatrixCategoryDefinition or null.
     */
    private MatrixCategoryDefinition getCategoryDefinitionForAxis(Axis axis){
        List<MatrixCategoryDefinition> axisCategories = getPresenter().getVisualizationDef().getMatrixSettings().getAxisCategories();

        MatrixCategoryDefinition tmp = null;

        for(MatrixCategoryDefinition catDef : axisCategories){
            if(catDef.getAxis() == axis){
                tmp = catDef;
            }
        }
        //can be nul, lets see if this works better.
        return tmp;
    }

    /**
     *  Grabs the Axis MatrixSortDefinition for Quick sort and if it matches the given axis returns.
     *
     * @param axis - axis to check
     * @return MatrixSortDefiniton if exists for that axis, or null.
     */
    private MatrixSortDefinition getQuickAxisSortDef(Axis axis){
        List<MatrixSortDefinition> axisQuickSortDefinitions = getPresenter().getVisualizationDef().getMatrixSettings().getAxisQuickSortDefinitions();
        for( MatrixSortDefinition quickSortDef: axisQuickSortDefinitions){
            if(quickSortDef.getCategoryDefinition().getAxis() == axis){
                return quickSortDef;
            }
        }
        return null;
    }

}
