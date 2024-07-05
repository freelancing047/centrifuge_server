package csi.client.gwt.viz.chart.overview.view.content;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.HTML;

import csi.client.gwt.viz.chart.overview.OverviewPresenter;
import csi.client.gwt.viz.chart.overview.range.RangeCalculator;
import csi.shared.gwt.viz.chart.ChartOverviewColorMapper;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ColoredOverview extends HTML implements OverviewContent  {

    public static final int OVERVIEW_CONTENT_HEIGHT = 6;

    private int width = OverviewPresenter.DEFAULT_OVERVIEW_WIDTH;
    private List<Integer> buckets = new ArrayList<Integer>();

    public ColoredOverview(){
        buildUI();
    }

    @Override
    public void setCategoryData(List<Integer> colors) {
        this.buckets = colors;
    }

    @Override
    public void resize(int width) {
        this.width = width;
        setWidth(width + "px");
        buildUI();

    }

    private void buildUI() {
        if(buckets.isEmpty()) {
            setText("");
            return;
        }

        int widgetWidth = (int) Math.ceil(RangeCalculator.createBinSize(width, buckets.size()));
        
        setHTML(buildDivs(widgetWidth));
    }

    private String buildDivs(int widgetWidth) {
        StringBuilder contentBuilder = new StringBuilder();        
        
        for (int i = 0; i < buckets.size(); i++) {
            String color = ChartOverviewColorMapper.getColor(buckets.get(i));
            boolean borderLeft = (i == 0);
            boolean borderRight = ((i+1) == buckets.size());

            contentBuilder.append(DivWriter.writeDiv(color, widgetWidth, borderLeft, borderRight));
        }
        return contentBuilder.toString();
    }



}
