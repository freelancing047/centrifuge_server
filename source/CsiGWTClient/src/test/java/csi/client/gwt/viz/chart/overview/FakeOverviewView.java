package csi.client.gwt.viz.chart.overview;

import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.viz.chart.overview.view.OverviewView;
import csi.client.gwt.viz.chart.overview.view.content.OverviewContent;

/**
 * @author Centrifuge Systems, Inc.
 */
public class FakeOverviewView implements OverviewView {

    @Override
    public void render(OverviewState overviewState, DragState dragState) {

    }

    @Override
    public void setOverviewContent(OverviewContent overviewContent) {

    }


    @Override
    public Style getWidgetStyle() {
        return null;
    }

    @Override
    public boolean removeMouseClickHandler() {
        return false;
    }

    @Override
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
        return null;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {

    }

    @Override
    public Widget asWidget() {
        return null;
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return null;
    }

    @Override
    public void setCategoryData(List<Integer> values) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeHighlights() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void highlightEndBar() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void highlightCenterBar() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void highlightStartBar() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCursor(Cursor eResize) {
        // TODO Auto-generated method stub
        
    }


}
