package csi.client.gwt.widget.ui.surface;

import com.google.gwt.canvas.dom.client.Context2d;
import csi.client.gwt.viz.matrix.MatrixView;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.shared.core.util.Native;

public class MatrixPositionIndicator extends BaseRenderable{
    private final MatrixView matrixView;
    private final DrawingPanel mainCanvas;
    private Layer layer;

    public MatrixPositionIndicator(MatrixView matrixView, DrawingPanel mainCanvas) {

        this.matrixView = matrixView;
        this.mainCanvas = mainCanvas;
    }

    @Override
    public void render(Context2d ctx) {
//        Native.log("Max X " + axisX.getMaxIndex() );
//        Native.log("Max Y " + axisY.getMaxIndex() );
/*

        ctx.save();
        ctx.setGlobalAlpha(.8);
        ctx.setFillStyle(MatrixSurface.GRAY);

//        Context2d grid = gridCanvas.getContext2d();

        ctx.clearRect(0, 0, layer.getWidth(), 10);
//        grid.clearRect(0, 0, canvasWidth, 10);


        ctx.clearRect(layer.getWidth() - 10, 0, 10, layer.getHeight());
//        grid.clearRect(canvasWidth - 10, 0, 10, canvasHeight);

        ctx.save();
        ctx.setGlobalAlpha(1);
        ctx.setFillStyle("black");
        ctx.fillRect(0, 10, layer.getWidth() - 10, 1);
        ctx.fillRect(layer.getWidth()- 10, 10, 1, layer.getHeight());

        ctx.restore();
//        ctx.fillRect(canvasWidth-10,  10, canvasWidth, 1 );
*/


       /* // Horizontal position
        {
            if (matrixView.getModel().isSummary()) {
                double multiplier = (double) layer.getWidth() / matrixView.getModel().getMatrixDataResponse().getMaxX();
                double end = axisX.getDataEnd() - axisX.getDataStart();
                double summaryS = axisX.getDataStart() * multiplier;
                double summaryE = end * multiplier;
                ctx.fillRect(summaryS, 0, summaryE, 10);
            } else {
                int xOffset = 0;
                if (!matrixView.getModel().getMatrixDataResponse().isFullMatrix()) {
                    double multiplier = (double) canvasWidth / model.getMatrixDataResponse().getMaxX();
                    double end = axisX.getDataEnd() - axisX.getDataStart();
                    double summaryS = axisX.getDataStart() * multiplier;
                    xOffset = (int) summaryS;
                }
                int x = (int) (axisX.getCurrentExtent().getStartIndex() / (double) axisX.getMaxIndex() * canvasWidth);
                int w = (int) ((axisX.getCurrentExtent().getEndIndex() - axisX.getCurrentExtent().getStartIndex() + 1) / (double) axisX.getMaxIndex() * canvasWidth);
                if (w < 5) {
                    w = 5;
                }

//                new BBox(x + xOffset , 0, w, 10)
                ctx.fillRect(x + xOffset, 0, w, 10);
        }
            }*/
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
