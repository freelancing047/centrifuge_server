package csi.client.gwt.viz.matrix.menu;

import java.util.ArrayList;

import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixSortDefinition;

/**
 * Menu quick sort handler based on measure.
 *
 * Matrix has only one measure at a given time, so this grabs that measure out of the settings,
 * on first click it flips the existing sort definition from settings, then toggles the sort order.
 *
 * Wipes out Axis Quick Sort on each sort.
 *
 *
 */
public class MeasureQuickSortHandler extends ToggleSortHandler{
    public MeasureQuickSortHandler(MatrixPresenter presenter, MatrixMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().showLoading();

        sortMatrixByMeasure();
    }

    /**
     * Sorts the matrix based on the measure definted in settings.
     * and toggle the direction and save the matrix, causing it to refresh the sort.
     * On sort, we grab the measure sort order if we don't have any sort order defined in the
     * quick settings,
     *
     * Before sort, we wipe out the Axis Quick Sort.
     */
    private void sortMatrixByMeasure(){
        MatrixSettings mSettings = getPresenter().getVisualizationDef().getMatrixSettings();
//        getPresenter().saveOldSelection(getPresenter().getModel().getSelectedCells()/);
        //clean out Axis Quick Sort so we the sort goes by the measure. ( axis takes precedence over measure on server sort)
        mSettings.setAxisQuickSortDefinitions(new ArrayList<MatrixSortDefinition>());

        SortOrder currSort = mSettings.getMeasureQuickSortOrder();


        /* grab the Matrix SortOrder from the settings, flip it, and put in MeasureQuickSort */
        if(currSort == SortOrder.NONE){
            currSort = mSettings.getMeasureSortOrder();
        }

        if(currSort != null) {
            switch (currSort) {
                case ASC:
                    currSort = SortOrder.DESC;
                    break;
                case DESC:
                    currSort = SortOrder.ASC;
                    break;
                case NONE:
                    currSort = SortOrder.ASC;
                    break;
            }

            mSettings.setMeasureQuickSortOrder(currSort);

            saveSettings();
        }
    }

}
