package csi.client.gwt.viz.chart.view;

import com.sencha.gxt.widget.core.client.info.Info;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;

import csi.client.gwt.viz.chart.model.AxisData;
import csi.client.gwt.viz.chart.overview.OverviewPresenter;
import csi.client.gwt.viz.chart.overview.range.Range;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;

/**
 * Determines how to draw the horizontal labels. This includes skipping some labels if necessary.
 * @author Centrifuge Systems, Inc.
 */
public class HorizontalAxisLabelsFormatter implements AxisLabelsFormatter {

    private static final int MARGIN = 50;
    private static final int COUNTER_RESET = 9999;
    private static final int MAX_CHARS_IN_LABEL = 10;
    private static final int WIDTH_OF_CATEGORY_LABEL = 30;
    private final ChartPresenter chartPresenter;

    private long counter = 0;

    public HorizontalAxisLabelsFormatter(ChartPresenter chartPresenter){
        this.chartPresenter = chartPresenter;
    }

    @Override
    public String format(AxisLabelsData axisLabelsData) {
//        if(shouldSkipDisplayingLabel(axisLabelsData))
//            return "";


        String s = getAxisLabel(axisLabelsData);

        if(s.trim().equals("")){
            s = "NULL";
        }

        if (labelIsNotTooLong(s)) {
            return s;
        } else {
            return shortenedLabel(s);
        }
        
    }

    public int calculateSkipAmountForLabels(AxisLabelsData axisLabelsData) {

        AxisData axisData = (AxisData) axisLabelsData.getNativeData();
        int highchartWidth = axisData.getWidth();
        //axisData.disableOverflow();
        //axisData.enableEndOnTick();
        if(highchartWidth == 0){
            highchartWidth = chartPresenter.getView().getOffsetWidth() - MARGIN;
            if(highchartWidth == 0){
                return 1;
            }
        }
        
        OverviewPresenter overviewPresenter = chartPresenter.getOverviewPresenter();
        Range range = overviewPresenter.getCategoryRange();
        
        
        long totalCategories = range.getDifference();
        long numberOfCategoriesThatFit = (highchartWidth - 50)/WIDTH_OF_CATEGORY_LABEL;//overviewPresenter.getIndividualBinCount() * overviewPresenter.getCategoryRange().getDifference();
        int ratio = 1;
        if(numberOfCategoriesThatFit > 0) {
            ratio = (int) ((totalCategories / numberOfCategoriesThatFit) + .5);
            if (ratio < 1) {
                ratio = 1;
            }
        }
        return ratio;
    }

    private String getAxisLabel(AxisLabelsData axisLabelsData) {
        try {
            double d = axisLabelsData.getValueAsDouble();
            return String.valueOf(d);
        } catch (Exception e1) {
            try {
                return axisLabelsData.getValueAsString();
            } catch (Exception e2) {
                long val = axisLabelsData.getValueAsLong();
                return String.valueOf(val);
            }
        }

    }

    private boolean shouldSkipDisplayingLabel(AxisLabelsData axisLabelsData) {
        if(counter>COUNTER_RESET){
            counter = 0;
        }
        return counter++ % calculateSkipAmountForLabels(axisLabelsData) != 0;
    }

    private boolean labelIsNotTooLong(String s) {
        return s.length() <= MAX_CHARS_IN_LABEL;
    }

    private String shortenedLabel(String s) {
        return s.substring(0, MAX_CHARS_IN_LABEL - 1);
    }
}

