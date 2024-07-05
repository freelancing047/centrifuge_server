package csi.client.gwt.widget.ui.surface;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.client.gwt.viz.matrix.MatrixView;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;

public class MatrixOverview extends BaseRenderable {

    private final YOverview yOverview;
    private final XOverview xOverview;
    //renderScrubber
    private double scrubberSize = 5;
    private String centrifugeBlue = "rgb(33, 104, 147)"; //ScrubberColor
    private CssColor barColor = CssColor.make("rgb(84,84,84)");
    private int barWidth = 2;
    private Layer layer = null;
    private MatrixView matrixView;

    public MatrixOverview(MatrixView matrixView) {
        this.matrixView = matrixView;
        yOverview = new YOverview(matrixView.getModel());
        xOverview = new XOverview(matrixView.getModel());
    }

    @Override
    public void render(Context2d context2d) {
        context2d.closePath();
        if(matrixView.getModel().getMetrics() == null){
            return;
        }

        yOverview.renderPositionIndicator(context2d);
        xOverview.renderPositionIndicator(context2d);
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

    class YOverview {
        MatrixModel model;
        private int padLeft = 83;

        public YOverview(MatrixModel model) {
            this.model = model;
        }

        private void renderPositionIndicator(Context2d ctx) {
            ctx.save();
            double total = model.getMetrics().getAxisYCount();
            CSIContext2d.CanvasTransform mct = getAxisTransform();
//            mct.invert();
            double height = mct.getY(0, 0);
            double startY = model.getY();
            double spacePerCategory = (height / total);

            double startPos = Math.max(((total - (Math.floor(startY) + model.getHeight()+1)) * spacePerCategory), 0);
            double endPos = Math.min((total - Math.ceil(startY)) * spacePerCategory, height);
            renderScrubber(ctx, startPos, endPos);
//            renderBar(ctx, height);
            ctx.restore();
        }

        private void renderBar(Context2d ctx, double height) {
//            ctx.setFillStyle(barColor);
//            ctx.fillRect(padLeft, 0, barWidth, height);
//            ctx.stroke();
        }

        private void renderScrubber(Context2d ctx, double startPos, double endPos) {
            if (endPos - startPos > 0) {
                ctx.setFillStyle(CssColor.make(centrifugeBlue));
                ctx.fillRect(padLeft, startPos, scrubberSize, endPos - startPos);
                ctx.stroke();
            }
        }
    }

    private CSIContext2d.CanvasTransform getAxisTransform() {
        if (layer instanceof MatrixMainLayer) {
            MatrixMainLayer mml = (MatrixMainLayer) this.layer;
            return mml.getAxisMask().copy();
        }
        return null;
    }

    class XOverview {
        MatrixModel model;
        int padLeft = 83;
        int padBottom = 83;

        public XOverview(MatrixModel model) {
            this.model = model;
        }

        private void renderPositionIndicator(Context2d ctx) {
            ctx.save();

            double total = model.getMetrics().getAxisXCount();
            double width = layer.getWidth() - padLeft;

            double startX = model.getX();
            double one = width / total;

            double endPos = Math.min(((Math.floor(startX) + model.getWidth() +1) * one), width) + padLeft;
            double sPos = Math.max(Math.ceil(startX) * one, 0) + padLeft;

            // draw the viewport
            if (endPos - sPos > 0) {
                ctx.setFillStyle(centrifugeBlue);
                ctx.fillRect(sPos, layer.getHeight() - padBottom-scrubberSize, endPos - sPos, scrubberSize);
                ctx.stroke();
            }

            // draw the whole bar
           /* ctx.setFillStyle(barColor);
            ctx.fillRect(padLeft, layer.getHeight() - padBottom-barWidth, layer.getWidth(), barWidth);
            ctx.stroke();*/

            ctx.restore();
        }
    }


}
