package csi.client.gwt.viz.timeline.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;
import csi.client.gwt.viz.timeline.view.drawing.DetailedEventRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TooltipRenderable;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

public class TooltipEvent extends BaseCsiEvent<TooltipEventHandler> {


    public static final GwtEvent.Type<TooltipEventHandler> type = new GwtEvent.Type<TooltipEventHandler>();
    
    private TooltipRenderable tooltip;
    private Layer layer;
    private Renderable renderable;
    private int height, width;
    
    public TooltipEvent(Renderable eventRenderable) {
        this.setRenderable(eventRenderable);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<TooltipEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(TooltipEventHandler handler) {
       handler.createTooltip(this);
    }

    public Renderable getRenderable() {
        return renderable;
    }

    public void setRenderable(Renderable renderable) {
        this.renderable = renderable;
    }

    public TooltipRenderable getTooltip() {
        return tooltip;
    }

    public void setTooltip(TooltipRenderable tooltip) {
        this.tooltip = tooltip;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


}
