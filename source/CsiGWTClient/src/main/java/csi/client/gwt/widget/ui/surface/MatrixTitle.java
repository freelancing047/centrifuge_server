package csi.client.gwt.widget.ui.surface;

import com.google.gwt.canvas.dom.client.Context2d;
import csi.client.gwt.viz.matrix.MatrixView;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;

class MatrixTitle extends BaseRenderable {

    private Layer layer;
    private MatrixView matrixView;
    private DrawingPanel mainCanvas;

    public MatrixTitle(MatrixView matrixView, DrawingPanel mainCanvas) {
        this.matrixView = matrixView;
        this.mainCanvas = mainCanvas;
    }

    @Override
    public void render(Context2d ctx) {
        ctx.save();
        ctx.setGlobalAlpha(0.7);
        ctx.setFillStyle("black");
        ctx.setFont(15 + "px Helvetica"); //$NON-NLS-1$
        ctx.setTextAlign(Context2d.TextAlign.CENTER);
        ctx.setTextBaseline(Context2d.TextBaseline.BOTTOM);
        ctx.fillText(matrixView.getMatrixTitle(), mainCanvas.getOffsetWidth() / 2.0, 20);
        ctx.restore();
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
