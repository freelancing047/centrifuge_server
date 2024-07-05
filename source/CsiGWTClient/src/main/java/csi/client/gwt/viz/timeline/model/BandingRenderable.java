package csi.client.gwt.viz.timeline.model;

import java.util.Date;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;

import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

public class BandingRenderable implements Renderable {

    private Axis axis;
    private Layer layer;

    public BandingRenderable(Axis axis){
        this.axis = axis;
    }
    
    @Override
    public void render(Context2d context2d) {
        int n = axis.getTics().size();
            context2d.setGlobalAlpha(1);
        for (int i=0; i<n-1; i++)
        {

            long start = axis.getTics().get(i);
            long end = axis.getTics().get(i+1);

            int x0= Math.max(axis.getX(), axis.getTimeScale().toInt(start));
            int x1= axis.getTimeScale().toInt(end);
            if (x0 < 20) {
                continue;
            }
                context2d.setStrokeStyle(CssColor.make(51, 102, 204));


            context2d.setFillStyle(i%2 == 0 ?
                    CssColor.make(254,254,254) : CssColor.make(238,238,238));
            context2d.setLineWidth(.1);
            context2d.strokeRect(x0, 0, 0, layer.getHeight());
//            context2d.strokeRect(x0, 0, x1-x0-1, layer.getHeight());
            //g.fillRect(x0, y, x1-x0-1, height);
            context2d.setStrokeStyle(CssColor.make(255,255,255));

            context2d.beginPath();
//            context2d.moveTo(x1-1, 0);
//            context2d.lineTo(x1-1, 0+layer.getHeight());
//
//            //g.drawLine(x1-1, y, x1-1, y+height);
//
//            context2d.moveTo(x0,0+layer.getHeight());
//            context2d.lineTo(x1,0+layer.getHeight());
            //g.drawLine(x0,y+height,x1,y+height);

            context2d.stroke();
            context2d.closePath();
        }
            context2d.setGlobalAlpha(1);
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
