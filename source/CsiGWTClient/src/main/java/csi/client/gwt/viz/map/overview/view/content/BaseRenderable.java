package csi.client.gwt.viz.map.overview.view.content;

import com.emitrom.lienzo.client.core.util.Console;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.event.shared.SimpleEventBus;

import csi.client.gwt.widget.drawing.Renderable;

public abstract class BaseRenderable implements Renderable, HasAllMouseHandlers, HasClickHandlers,
        HasDoubleClickHandlers {

    private ResettableEventBus eventBus = new ResettableEventBus(new SimpleEventBus());

    private PostRenderingProcess postRenderingProcess = null;
    public BaseRenderable() {

    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }
    

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return eventBus.addHandler(MouseDownEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
        return eventBus.addHandler(MouseUpEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return eventBus.addHandler(MouseOutEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return eventBus.addHandler(MouseOverEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
        return eventBus.addHandler(MouseMoveEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
        return eventBus.addHandler(MouseWheelEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return eventBus.addHandler(DoubleClickEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return eventBus.addHandler(ClickEvent.getType(), handler);
    }

    public void removeAllHandlers() {
        eventBus.removeHandlers();
    }
    
    protected void firePostRenderingProcess() {
        if(postRenderingProcess != null)
            postRenderingProcess.execute();
    }
    
    public void addPostRenderingProcess(PostRenderingProcess postRenderingProcess){
        this.postRenderingProcess  = postRenderingProcess;
    }

    public static void drawRoundedRectangle(Context2d context2d, double x, double y, double height, double width, double curve) {
        drawRoundedRectangle(context2d, x, y, height, width, curve, CssColor.make(255, 255, 255), CssColor.make(255, 255, 255));
    }

    public static void drawRoundedRectangle(Context2d context2d, double x, double y, double height, double width, double curve, CssColor fillStyle, CssColor strokeStyle){

        context2d.save();
        context2d.beginPath();
        FillStrokeStyle savedStyle = context2d.getFillStyle();
        context2d.moveTo(x+curve, y);
        context2d.lineTo(x+width-curve, y);
        context2d.quadraticCurveTo(x+width, y, x+width, y+curve);
        context2d.lineTo(x+width, y+height-curve);
        context2d.quadraticCurveTo(x+width, y+height, x+width-curve, y+height);
        context2d.lineTo(x+curve, y+height);
        context2d.quadraticCurveTo(x, y+height, x, y+height-curve);
        context2d.lineTo(x, y+curve);
        context2d.quadraticCurveTo(x, y, x+curve, y);
        context2d.closePath();
        context2d.clip();
        context2d.setFillStyle(fillStyle);
        context2d.setStrokeStyle(strokeStyle);
        context2d.fillRect(x, y, width, height);
        context2d.restore();
        context2d.setFillStyle(savedStyle);
    }
}
