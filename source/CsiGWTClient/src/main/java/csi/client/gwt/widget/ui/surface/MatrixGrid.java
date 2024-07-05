package csi.client.gwt.widget.ui.surface;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

public class MatrixGrid implements Renderable {
    private Layer layer;
    private MatrixModel model;

    public MatrixGrid(MatrixModel model) {
        this.model = model;
    }

    @Override
    public void render(Context2d ctx) {
        ctx.setStrokeStyle(CssColor.make(50, 50, 50));
        ctx.setLineWidth(10.0);
        int xCats = model.getCategoryX().size();
        int yCats = model.getCategoryY().size();
        {
            ctx.save();
            double a = 1;
            try {
                a = (layer.getWidth()) / (double) (xCats - 1);
            } catch (Exception ignore) {
            }
            double d = 1;
            try {
                d = (layer.getHeight()) / (double) (yCats - 1);
            } catch (Exception ignore) {
            }
            //Change coordinates from pixels to categories
            ctx.scale(a, -d);
            //Moves 0,0 from top left to bottom left
            ctx.translate(0, -(yCats-1));


            /*double alpha = 0.3 + z * 0.7;
            if (alpha > 1.0) {
                alpha = 1.0;
            }

            ctx.setGlobalAlpha(alpha);*/

            // X axis
            {
                for (int i = 0; i < xCats; i++) {
                    ctx.beginPath();
                    ctx.moveTo(i,0);
                    ctx.lineTo(i,yCats);
                    ctx.closePath();
//                    ctx.stroke();
                }
            }

            // Y axis
            {
                for (int i = 0; i < yCats; i++) {
                    ctx.beginPath();
                    ctx.moveTo(0,i);
                    ctx.lineTo(xCats,i);
                    ctx.closePath();
//                    ctx.stroke();
                }
            }
        }
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
        return false;
    }
}
