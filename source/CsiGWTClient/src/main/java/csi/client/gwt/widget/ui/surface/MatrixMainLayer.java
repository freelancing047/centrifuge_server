package csi.client.gwt.widget.ui.surface;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.client.gwt.viz.matrix.MatrixView;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Renderable;

class MatrixMainLayer extends Layer {
    private CSIContext2d.CanvasTransform mct;

    MatrixMainLayer(MatrixView matrixView) {
        this.matrixView = matrixView;
    }

    @Override
    public void render() {
        matrixView.renderColorScaleHoverMarker();
        Canvas canvas = getCanvas();
        Context2d ctx = canvas.getContext2d();
        buildTransforms();
        canvas.setCoordinateSpaceWidth(getDrawingPanel().getCanvas().getCoordinateSpaceWidth());
        canvas.setCoordinateSpaceHeight(getDrawingPanel().getCanvas().getCoordinateSpaceHeight());
        ctx.clearRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
/*        MatrixModel model = matrixView.getModel();
        {
            ctx.beginPath();
            ctx.setFillStyle(CssColor.make(0,0,100));
            CSIContext2d.CanvasTransform t = getAxisMask();
            ctx.rect(t.getX(0,0), t.getY(0, 0), t.getX(getWidth(), getHeight()) - t.getX(0, 0), t.getY(getWidth(),getHeight()) - t.getY(0, 0));
//            ctx.rect(mainCanvasTransform.getX(model.getX(), model.getY()), mainCanvasTransform.getY(model.getX(), model.getY()), mainCanvasTransform.getX(model.getWidth(), model.getHeight()) - mainCanvasTransform.getX(model.getX(), model.getY()), mainCanvasTransform.getY(model.getWidth(), model.getHeight()) - mainCanvasTransform.getY(model.getX(), model.getY()));
            ctx.closePath();
            ctx.fill();
        }
        {
            ctx.beginPath();
            ctx.setFillStyle(CssColor.make(100,0,0));
            CSIContext2d.CanvasTransform t = getSurfacePaddingMask();
            ctx.rect(t.getX(0,0), t.getY(0, 0), t.getX(getWidth(), getHeight()) - t.getX(0, 0), t.getY(getWidth(),getHeight()) - t.getY(0, 0));
            ctx.closePath();
            ctx.fill();
        }
        {
            ctx.beginPath();
            ctx.setFillStyle(CssColor.make(0,100,0));
            CSIContext2d.CanvasTransform mainCanvasTransform = getMainCanvasTransform();
            ctx.rect(mainCanvasTransform.getX(model.getX(), model.getY()), mainCanvasTransform.getY(model.getX(), model.getY()), mainCanvasTransform.getX(model.getWidth()+model.getX(), model.getHeight()+model.getY()) - mainCanvasTransform.getX(model.getX(), model.getY()), mainCanvasTransform.getY(model.getWidth()+model.getX(), model.getY()+model.getHeight()) - mainCanvasTransform.getY(model.getX(), model.getY()));
            ctx.closePath();
            ctx.fill();
        }
        ctx.setFillStyle(CssColor.make(0, 0, 0));
//        ctx.setFillStyle(CssColor.make(0,255,0));
//        ctx.clearRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());*/
        ctx.save();
        {
            ctx.save();
            CSIContext2d.CanvasTransform mask = getAxisMask();
            mask.invert();
            ctx.setStrokeStyle("2px solid black");
            ctx.beginPath();
            ctx.moveTo(83,0);
            ctx.lineTo(83,getHeight()-83);
            ctx.lineTo(getWidth(),getHeight()-83);
            ctx.stroke();
            ctx.restore();
        }
        for (Renderable thing : things) {
            ctx.save();
            thing.render(ctx);
            ctx.restore();
        }
        ctx.restore();
    }

    private MatrixView matrixView;

    public void buildTransforms() {
        buildMainCanvasTransform();
    }

    public void buildMainCanvasTransform() {
        CSIContext2d.CanvasTransform c = new CSIContext2d.CanvasTransform();
        CSIContext2d.CanvasTransform axisMask = getAxisMask();

        MatrixModel model = matrixView.getModel();

        CSIContext2d.CanvasTransform surfacePaddingMask = getSurfacePaddingMask();
        //transform to category space
        CSIContext2d.CanvasTransform c2 = new CSIContext2d.CanvasTransform();
        //(width,height) in pixels -> (width,height) categories
        c2.scale((double) getWidth() / (double) (model.getWidth()), (double) getHeight() / (double) (model.getHeight()));
        c2.translate(-model.getX(), -model.getY());
        c.multiply(axisMask);
        c.multiply(surfacePaddingMask);
        c.multiply(c2);
        mct = c;
    }

    public CSIContext2d.CanvasTransform getSurfacePaddingMask() {
        CSIContext2d.CanvasTransform c = new CSIContext2d.CanvasTransform();
        {//mask for padding
            double padX, padY;
//            if(matrixView.getModel().getCategoryX().size() == 1 || matrixView.getModel().getCategoryY().size() == 1){
//                if(matrixView.getModel().getCategoryX().size() == 1){
//                    padX = matrixView.getOffsetWidth()/2;
//                }else{
//                    padX = matrixView.getModel().getViewportPad();
//                }
//
//                if(matrixView.getModel().getCategoryY().size() == 1){
//                    padY = matrixView.getOffsetHeight()/2;
//                }else{
//                    padY = matrixView.getModel().getViewportPad();
//                }
//            }else {
            double viewportPad = matrixView.getModel().getViewportPad();
            viewportPad = Math.min(viewportPad, Math.min(getHeight(),getWidth()) * .1);
            padX = viewportPad;
                padY = viewportPad;
//            }
            //(pad,pad)->(0,0)
            c.translate(padX, padY);
            //(width+2pad,height+2pad)->(width,height)
            c.scale((getWidth() - 2 * padX) / (double) getWidth(), (getHeight() - 2 * padY) / (double) getHeight());
        }
        return c;
    }

    public CSIContext2d.CanvasTransform getAxisMask() {
        CSIContext2d.CanvasTransform c = new CSIContext2d.CanvasTransform();

//        if(matrixView.getModel().getCategoryX().size() == 1 || matrixView.getModel().getCategoryY().size() == 1){
//            {
//                c.translate(0, getHeight());
//
//            }
//        }else {

            {//mask for Axis
//        (0,height)->(0,0)
                c.translate(0, getHeight());
//        (0,-height)->(0,height)
                c.scale(1, -1);
//        (83,83)->(0,0)
                c.translate(83, 83);
//        (width-83,height-83)->(width,height)
                c.scale((getWidth() - 83) / (double) getWidth(), (getHeight() - 83) / (double) getHeight());
            }
//        }
        return c;
    }


    public CSIContext2d.CanvasTransform getMainCanvasTransform() {

        return mct;
    }
}
