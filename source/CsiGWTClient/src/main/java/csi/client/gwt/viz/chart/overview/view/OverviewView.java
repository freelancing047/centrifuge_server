package csi.client.gwt.viz.chart.overview.view;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.user.client.ui.IsWidget;

import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.viz.chart.overview.DragState;
import csi.client.gwt.viz.chart.overview.OverviewState;
import csi.client.gwt.viz.chart.overview.view.content.OverviewContent;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface OverviewView extends IsWidget, HasAllMouseHandlers, HasDoubleClickHandlers, HasClickHandlers {

    public static final int OVERVIEW_HEIGHT = 20;
    public static final int DRAG_BAR_WIDTH = 11;

    public void render(OverviewState overviewState, DragState dragState);
    public void setOverviewContent(OverviewContent overviewContent);
    public void setCategoryData(List<Integer> values);
    public Style getWidgetStyle();
    public void removeHighlights();

    public void highlightEndBar();
    
    public void highlightCenterBar();

    public void highlightStartBar();

    public boolean removeMouseClickHandler();
    public void setCursor(Cursor eResize);

    Button getMoveRight();

    Button getMoveLeft();

    int getWidth();

    Widget getEventReferenceWidget();
}
