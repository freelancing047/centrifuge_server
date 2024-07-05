package csi.client.gwt.viz.chart.overview.view.content;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.dom.client.Style;

import csi.client.gwt.viz.chart.overview.view.OverviewViewWidget;

/**
 * @author Centrifuge Systems, Inc.
 */
public class OverviewContentContainer extends DivWidget {

    public OverviewContentContainer() {
        styleBar();
    }

    public void setWidth(int width){
        getStyle().setWidth(width, Style.Unit.PX);
    }

    private void styleBar() {
        getStyle().setPosition(Style.Position.ABSOLUTE);
        getStyle().setHeight(OverviewViewWidget.OVERVIEW_HEIGHT, Style.Unit.PX);
        getStyle().setOverflow(Style.Overflow.HIDDEN);
    }

    private Style getStyle() {
        return getElement().getStyle();
    }

}