package csi.client.gwt.viz.chart.overview.view.content;

import java.util.List;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.dom.client.Style;

import csi.client.gwt.viz.chart.overview.view.OverviewViewWidget;

/**
 * @author Centrifuge Systems, Inc.
 */
public class HorizontalBarForOverview extends DivWidget implements OverviewContent{

    public HorizontalBarForOverview(){
        getStyle().setBackgroundColor("brown");
        getStyle().setColor("brown");
        getStyle().setHeight(4, Style.Unit.PX);
        getStyle().setMarginTop((OverviewViewWidget.OVERVIEW_HEIGHT / 2) - 2, Style.Unit.PX);
    }

    private Style getStyle() {
        return getElement().getStyle();
    }


    @Override
    public void resize(int width) {
        getStyle().setWidth(width, Style.Unit.PX);
    }


    @Override
    public void setCategoryData(List<Integer> categoryData) {
        // TODO Auto-generated method stub
        
    }
}
