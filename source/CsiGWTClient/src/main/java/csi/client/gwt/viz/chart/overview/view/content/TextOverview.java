package csi.client.gwt.viz.chart.overview.view.content;

import java.util.List;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.viz.chart.overview.OverviewPresenter;
import csi.client.gwt.viz.chart.overview.range.RangeCalculator;
import csi.client.gwt.viz.shared.export.view.InlineTextWidget;
import csi.shared.gwt.viz.chart.ChartOverviewColorMapper;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TextOverview extends DivWidget implements OverviewContent  {

    private int width = OverviewPresenter.DEFAULT_OVERVIEW_WIDTH;
    private int categories = 0;

    public TextOverview(){
        setHeight("22px");
    }


    

    @Override
    public void resize(int width) {
        this.width = width;
        setWidth(width + "px");

        resetWidths();

    }

    private void resetWidths() {
        if(categories != 0) {
            for (Widget widget : getChildren()) {
                widget.setWidth((RangeCalculator.createBinSize(width, categories)-2) + "px");
            }
        }
    }


    @Override
    public void setCategoryData(List<Integer> categoryData) {
        clear();
        this.categories = categoryData.size();
        for(int i = 0; i < categories; i++){
            InlineTextWidget widget = new InlineTextWidget();
            widget.add(ChartOverviewColorMapper.getColor(categoryData.get(i)));
            widget.getElement().getStyle().setFloat(Style.Float.LEFT);
            widget.getElement().getStyle().setTextAlign(Style.TextAlign.CENTER);
            widget.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
            widget.getElement().getStyle().setBorderColor("black");

            widget.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
            widget.getElement().getStyle().setMarginTop(-2, Style.Unit.PX);
            widget.getElement().getStyle().setHeight(22, Style.Unit.PX);

            add(widget);
        }
        resetWidths();

    }
}
