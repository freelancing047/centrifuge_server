package csi.client.gwt.viz.chart.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuNav;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.SortDefinition;


/**
 * Base class for MeasureSortHandler and CategorySortHandler. This just has shared functions between the two.
 */
abstract class ToggleSortAxisHandler extends AbstractMenuEventHandler<ChartPresenter, ChartMenuManager> implements ClickHandler{

    public ToggleSortAxisHandler(ChartPresenter presenter, ChartMenuManager menuManager) {
        super(presenter, menuManager);
    }

    /**
     *  Because we currently have the quick sort model field as a list but only have one item in the list,
     *  this is a convenience method to update the list in the model.
     *
     * @param quickSortDef SortDefinition that you would like to have the chart/table sorted by
     * @param flipSortOrder true - will toggle sort order that is in the sort definition
     *
     * @return ArrayList that has the sort definition passed in as param, flipped if flipSortOrder is true.
     */
    protected List<SortDefinition> createQuickSortDefList(SortDefinition quickSortDef, boolean flipSortOrder){
        ArrayList<SortDefinition> sortDefs = new ArrayList<SortDefinition>();
        if(flipSortOrder) {
            sortDefs.add(flipSortDefSortOrder(quickSortDef));
        }else{
            sortDefs.add(quickSortDef);
        }
        return sortDefs;
    }

    /**
     * Gets a sortDefinition from a list of sort definitons.
     *
     * Because we have a list in the model, and possibly later will have more than one there.
     * @param chartSets - chart settings
     * @return either the applicable sort definition, or null.
     */
    protected SortDefinition getQuickSortDef(ChartSettings chartSets){
        if(chartSets.getQuickSortDef() != null && !chartSets.getQuickSortDef().isEmpty()){
            return chartSets.getQuickSortDef().get(0);
        }else{
            return null;
        }
    }

    /**
     * Flips the order from ASC to DESC on sort measure
     *
     * @param sortDef current sort definition
     * @return same sort def but flipped order
     */
    protected SortDefinition flipSortDefSortOrder(SortDefinition sortDef){
        if(sortDef.getSortOrder() == SortOrder.ASC){
            sortDef.setSortOrder(SortOrder.DESC);
        }else{
            sortDef.setSortOrder(SortOrder.ASC);
        }

        return sortDef;
    }


    /**
     * Saves the visibility status of the chart, reloads.
     * @param drillCategories - drillCategories that we would like to keep after refresh.
     */
    protected void save(final List<String> drillCategories){
        VortexFuture<Void> future = getPresenter().saveSettings(false);
        setVizMap();
        getPresenter().getView().setViewsNotLoaded();
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                getPresenter().loadDrillCategoriesAfterQuickSort(drillCategories);

            }
        });


    }

    /**
     * This is how we preserve the state of the chart for post load ( only relevant for measure...)??*
     */
    private void setVizMap(){
        ChartPresenter chartPresenter = getPresenter();
        if(!chartPresenter.getVisualizationDef().getChartSettings().getQuickSortDef().isEmpty()) {
            Map<String, Boolean> visMap = new HashMap<String, Boolean>();
            CsiMenuNav menu = chartPresenter.getChrome().getMenu();
            chartPresenter.setVisibilityOnChart(menu.getVisibleDynamicItems());
        }
    }

}