package csi.client.gwt.viz.chart.overview.view;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TransparencyWidget extends Composite {
    //private static final int TRANSPARENCY_HEIGHT = OverviewView.OVERVIEW_HEIGHT - 4;

    private static final String STYLE = "chartTransparencyWidget";
    private static final String COLOR = "chartTransparencyWidgetColor";
    private static final String HIGHLIGHT = "chartTransparencyWidgetHighlight";
    private FlowPanel flowPanel = new FlowPanel();

    public TransparencyWidget(){
        initWidget(flowPanel);
        styleBar();
    }

    private void styleBar() {

        //flowPanel.setHeight(TRANSPARENCY_HEIGHT + "px");

        addStyleName(STYLE);
        addStyleName(COLOR);
        
//        getStyle().setMarginTop((OverviewViewWidget.OVERVIEW_HEIGHT - TRANSPARENCY_HEIGHT) / 2, Style.Unit.PX);
//        getStyle().setBackgroundColor("#99CCFF");
//        getStyle().setOpacity(0.4);
//        getStyle().setPosition(Style.Position.ABSOLUTE);
//        getStyle().setZIndex(index);
    }
    
    public void doHighlight(boolean highlight) {
        if(highlight) {
            removeStyleName(COLOR);
            addStyleName(HIGHLIGHT);
        } else {
            removeStyleName(HIGHLIGHT);
            addStyleName(COLOR);
        }
    }

    public void setPosition(int xPosition, int width){
        getStyle().setLeft(xPosition, Style.Unit.PX);
        getStyle().setWidth(width, Style.Unit.PX);

    }

    private Style getStyle() {
        return flowPanel.getElement().getStyle();
    }
}
