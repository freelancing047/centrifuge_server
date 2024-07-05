package csi.client.gwt.viz.map.overview.view;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * @author Centrifuge Systems, Inc.
 */
public class DragBar extends Composite{
    private static final String STYLE = "chartDragBar";

    private static final String DRAG_STYLE = "chartDragBarDragging";

    private static final String COLOR_STYLE = "chartDragBarNotDragging";

    private static final int BORDER_WIDTH = 1;

    private FlowPanel flowPanel = new FlowPanel();

    public DragBar(){
        initWidget(flowPanel);
        styleBar();
    }

    private void styleBar() {
        flowPanel.setWidth(OverviewViewWidget.DRAG_BAR_WIDTH - (2 * BORDER_WIDTH) + "px");
        flowPanel.setHeight(OverviewViewWidget.OVERVIEW_HEIGHT - (2 * BORDER_WIDTH) + "px");
        addStyleName(STYLE);
        addStyleName(COLOR_STYLE);

//        getStyle().setBackgroundColor("rgb(251, 89, 77)");
//        getStyle().setBorderStyle(Style.BorderStyle.SOLID);
//        getStyle().setBorderWidth(BORDER_WIDTH, Style.Unit.PX);
//        getStyle().setBorderColor("black");
//        getStyle().setProperty("borderRadius", "2px");
//        getStyle().setPosition(Style.Position.ABSOLUTE);
//        getStyle().setZIndex(index);
    }

    public void setPosition(int xPosition){
        getStyle().setLeft(xPosition, Style.Unit.PX);
    }

    private Style getStyle() {
        return flowPanel.getElement().getStyle();
    }

    public void doHighlight(boolean highlight) {
        if(highlight) {
            removeStyleName(COLOR_STYLE);
            addStyleName(DRAG_STYLE);
        } else {
            removeStyleName(DRAG_STYLE);
            addStyleName(COLOR_STYLE);
            
        }
    }
    
    

}
