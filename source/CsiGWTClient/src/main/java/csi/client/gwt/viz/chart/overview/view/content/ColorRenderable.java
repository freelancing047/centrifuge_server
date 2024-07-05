package csi.client.gwt.viz.chart.overview.view.content;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.shared.HandlerRegistration;

import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;

public class ColorRenderable extends BaseRenderable {

    private Layer layer;
    
    private double x;
    private double width;
    private String cssColor;


    private final static int BAR_HEIGHT = 15;
    
    public ColorRenderable(double d, double widgetWidth, String cssColor){
        this.x = d;
        this.width = widgetWidth;
        this.cssColor = cssColor;
    }

    @Override
    public void render(Context2d context2d) {
        context2d.save();
        context2d.setFillStyle(cssColor);
        context2d.fillRect(x, 2, x+width, BAR_HEIGHT);
        context2d.fill();
        context2d.restore();
        
        firePostRenderingProcess();
    }

    

    @Override
    public boolean hitTest(double x, double y) {
        return false;
    }

    @Override
    public void bind(Layer layer) {
        this.layer = layer;
    }

    @Override
    public boolean isDirty() {
        // TODO Auto-generated method stub
        return false;
    }

}
