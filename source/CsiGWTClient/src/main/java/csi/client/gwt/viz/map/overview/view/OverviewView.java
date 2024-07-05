package csi.client.gwt.viz.map.overview.view;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.viz.map.overview.DragState;
import csi.client.gwt.viz.map.overview.view.content.OverviewContent;

import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface OverviewView extends IsWidget, HasAllMouseHandlers, HasDoubleClickHandlers, HasClickHandlers {

    public static final int OVERVIEW_HEIGHT = 20;
    public static final int DRAG_BAR_WIDTH = 11;

    public void render(DragState dragState, String s, String s1);
    public void setOverviewContent(OverviewContent overviewContent);
    public void setCategoryData(List<Integer> values);
    public Style getWidgetStyle();
    public void removeHighlights();

    public void highlightEndBar();
    
    public void highlightCenterBar();

    public void highlightStartBar();

    public boolean removeMouseClickHandler();
    public void setCursor(Cursor eResize);


    Button getAddLeft();

    Button getMoveRight();

    Button getAddRight();

    Button getMoveLeft();

    int getWidth();

    Widget getEventReferenceWidget();
}
