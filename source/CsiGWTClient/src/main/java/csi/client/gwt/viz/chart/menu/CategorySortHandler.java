package csi.client.gwt.viz.chart.menu;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;

import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.SortDefinition;

/**
 * Handler for the category sort under the Edit Menu of the Chart Viz.
 * @author Centrifuge Systems, Inc.
 */
public class CategorySortHandler extends ToggleSortAxisHandler {

    public CategorySortHandler(ChartPresenter presenter, ChartMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        ChartSettings chartSets = getPresenter().getVisualizationDef().getChartSettings();
        final List<String> drillSelections = getPresenter().getChartModel().getDrillSelections();

            getPresenter().showProgressIndicator();
            getPresenter().getView().clearTableTabClientSort();
//        }
        // clear out the no longer needed sort defs
        emptyQuickSortDefs();

        SortDefinition quickSortDef = getQuickSortDef(chartSets);

        // if we already have a cat def there but its for the wrong drill level, clear that out.
        if(quickSortDef != null && chartSets.getCategoryDefinitions().get(getPresenter().getCurrentDrillLevel()) != quickSortDef.getCategoryDefinition()){
            quickSortDef = null;
        }


        // if we are already sorted based on this category,just flip the sort order
        if(quickSortDef != null && !quickSortDef.isCountStar() && quickSortDef.getMeasureDefinition() == null){
            chartSets.setQuickSortDef(createQuickSortDefList(quickSortDef, true));
        }else {
            SortDefinition catQuickSortDef = new SortDefinition();

            List<CategoryDefinition> categoryDefinitions = chartSets.getCategoryDefinitions();


            if (getPresenter().getCurrentDrillLevel() <= categoryDefinitions.size()) {
                catQuickSortDef.setCategoryDefinition(categoryDefinitions.get(getPresenter().getCurrentDrillLevel()));
            }

            // determine sort order
            SortOrder smartSortOrder = getSortOrderFromExistingSortDefinitions();

            // handles the toggling, if this is true - that means we are just flipping the sort,
            // only triggered on the first invocation if there was a sort order defited in the settings of the chart
            if(smartSortOrder != SortOrder.NONE){
                catQuickSortDef.setSortOrder(smartSortOrder);

                catQuickSortDef = flipSortDefSortOrder(catQuickSortDef);
            }else{
                //Set default/first sort order
                catQuickSortDef.setSortOrder(SortOrder.DESC);
            }

            chartSets.setQuickSortDef(createQuickSortDefList(catQuickSortDef, false));
        }

        save(drillSelections);
    }


    /**
     * Get the sort order out of settings menu for the category def. if it exists, fires from the onMenuEvent
     *
     * @return SortOrder.NONE, SortOrder from settings if configured.
     */
    public SortOrder getSortOrderFromExistingSortDefinitions(){
        List<SortDefinition> sortDefinitions = getPresenter().getVisualizationDef().getChartSettings().getSortDefinitions();
        SortOrder smartSortOrder = SortOrder.NONE;
        for(SortDefinition sd : sortDefinitions){
            if(sd.getMeasureDefinition() == null && sd.getCategoryDefinition() != null){
                smartSortOrder = sd.getSortOrder();
                return smartSortOrder;
            }
        }

        return smartSortOrder;
    }

    /**
     * Removes all SortDefinitions that are based on a measure.
     */
    private void emptyQuickSortDefs(){
        ChartSettings chartSets = getPresenter().getVisualizationDef().getChartSettings();
        List<SortDefinition> cleanSortDefs = new ArrayList<SortDefinition>();

        for(SortDefinition sortDef : chartSets.getQuickSortDef()){
            if(sortDef.getMeasureDefinition() == null){
                cleanSortDefs.add(sortDef);
            }
        }
        chartSets.setQuickSortDef(cleanSortDefs);
    }

    @Override
    public void onClick(ClickEvent event) {
        this.onMenuEvent(null);
    }
}