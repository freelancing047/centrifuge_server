package csi.client.gwt.viz.chart.menu;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;

import com.sencha.gxt.widget.core.client.info.Info;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.common.model.visualization.chart.SortDefinition;

/**
 * Event handler for the Measure sort, if measure is passed in, it will clear out the rest and append that measure to quicksort defs, after reloads the chart
 */
public class MeasureSortHandler extends ToggleSortAxisHandler{
    private MeasureDefinition measure;


    /**
     *
     * @param presenter  presenter of the viz, we need it to update the overview and get new data after sort is there.
     * @param measure Meausre that will be used to sort on
     */
    public MeasureSortHandler(ChartPresenter presenter, MeasureDefinition measure) {

        super(presenter, null);
        this.measure = measure;
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        final List<String> drillSelections = getPresenter().getChartModel().getDrillSelections();
        getPresenter().showProgressIndicator();
//        if(getPresenter().getView().isTableTabSelected()){
            getPresenter().getView().clearTableTabClientSort();
//        }

        SortDefinition sortDefinition = new SortDefinition();
        ChartSettings chartSettings = getPresenter().getVisualizationDef().getChartSettings();
        List<MeasureDefinition> measureDefs = chartSettings.getMeasureDefinitions();
        SortDefinition quickSortDef = getQuickSortDef(chartSettings);

        emptyExistingSorts();

        //if we have the quick sort def and its not based on categories
        if(quickSortDef != null && quickSortDef.getCategoryDefinition() == null) {
            if (!measureDefs.isEmpty()) {
                //toggles the measure def if its the corresponding measure.
                if (quickSortDef.getMeasureDefinition() == measure) {
                    chartSettings.setQuickSortDef(createQuickSortDefList(quickSortDef, true));
                }

            } else if (quickSortDef.isCountStar() == chartSettings.isUseCountStarForMeasure()){
                //toggles if count star
                chartSettings.setQuickSortDef(createQuickSortDefList(quickSortDef, true));

            }

        }else {  //make a new one
            chartSettings.setQuickSortDef(createQuickSortDefList(createNewSortDefinitionForMeasure(), false));
        }

        save(drillSelections);
    }

    @Override
    public void onClick(ClickEvent event) {
        this.onMenuEvent(null);
    }


    /**
     * Checks if the chart has some sort measures defined already, and if so grabs them so we can toggle them.
     * @return SortOrder.
     */
    private SortDefinition createNewSortDefinitionForMeasure(){
        SortDefinition sortDefinition = new SortDefinition();  // default asc

        ChartSettings chartSettings = getPresenter().getVisualizationDef().getChartSettings();


        if(chartSettings.isUseCountStarForMeasure()) {
            sortDefinition.setCountStar(true);
            sortDefinition.setSortOrder(SortOrder.DESC);
        }else{
            if(chartSettings.getMeasureDefinitions().size() > 0 ) {
                sortDefinition.setMeasureDefinition(measure);
                sortDefinition.setSortOrder(SortOrder.DESC);
            }
        }

        return sortDefinition;
    }

    private void emptyExistingSorts(){
        ChartSettings chartSettings = getPresenter().getVisualizationDef().getChartSettings();
        List<SortDefinition> newQuickSortDefs = new ArrayList<>();

        for(SortDefinition s : chartSettings.getQuickSortDef()){
            if(s.getMeasureDefinition() == measure){
                newQuickSortDefs.add(s);
            }
        }

        chartSettings.setQuickSortDef(newQuickSortDefs);
    }
}
