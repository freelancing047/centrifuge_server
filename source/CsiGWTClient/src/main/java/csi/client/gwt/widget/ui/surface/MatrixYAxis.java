package csi.client.gwt.widget.ui.surface;

import com.google.common.collect.Lists;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.client.gwt.viz.matrix.MatrixView;
import csi.shared.core.visualization.matrix.AxisLabel;
import csi.shared.core.visualization.matrix.MatrixDataRequest;

import java.util.List;

class MatrixYAxis extends AbstractMatrixAxis {

    MatrixYAxis(MatrixView view) {
        super(view);
    }

    @Override
    protected MouseMoveHandler getMouseMoveHandler() {
        return new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if(view.isLoaded()) {
                    double x = event.getX();
                    double y = event.getY();
                    CSIContext2d.CanvasTransform t2 = getTransformForEvents();
                    view.clearHoverCell();

                    int minY = (int) view.getModel().getY();
                    int binCountForAxisY = getModel().getBinCountForAxis((int) getModel().getHeight());

                    int yCat = (int) (t2.getY(x, y) + .5);
                    int yMod = (yCat - minY) % binCountForAxisY;
                    yCat = (yCat - yMod) + binCountForAxisY / 2;

                    view.setMouseXY(-1, yCat);
                    layer.getDrawingPanel().setOnYAxis(true);
                    layer.getDrawingPanel().render();
                }
            }
        };
    }

    public ClickHandler getClickHandler() {
        return event -> {
            if (event.isControlKeyDown()) {
                double x = event.getX();
                double y = event.getY();
                CSIContext2d.CanvasTransform t2 = getTransformForEvents();
                int minY = (int) view.getModel().getY();
                int binCountForAxisY = getModel().getBinCountForAxis((int) getModel().getHeight());

                int yCat = (int) (t2.getY(x, y) + .5);
                int yMod = (yCat - minY) % binCountForAxisY;
                yCat =  (yCat - yMod);

                if(event.isShiftKeyDown()){
                    MatrixDataRequest request = getModel().getAxisSelectionRequest(yCat,MatrixModel.Axis.Y);
                    getModel().getSelectedCells().removeIf(cell -> cell.getY() >= request.getStartY() && cell.getY() <=request.getEndY());
                    view.refresh();
                }else {

                    requestSelectionForRow(yCat);
                }
            }
        };
    }


    private void requestSelectionForRow(int rowToSelect) {
        MatrixDataRequest request = getModel().getAxisSelectionRequest(rowToSelect,MatrixModel.Axis.Y);
        requestSelection(request);
    }

    public void render(Context2d ctx) {
        if(!view.isLoaded()){
            return;
        }
        if (layer instanceof MatrixMainLayer) {
            MatrixMainLayer mml = (MatrixMainLayer) layer;
            CSIContext2d.CanvasTransform t = mml.getMainCanvasTransform();
            CSIContext2d.CanvasTransform axisMaskTransform = mml.getAxisMask();
            String gridLineColor = "rgb(230,230,230)"; //LightGray
            ctx.setStrokeStyle(CssColor.make(gridLineColor)); //$NON-NLS-1$
            ctx.setLineWidth(0.5);

            //FIXME: why do we need this???
            List<AxisLabel> categoryY = Lists.newArrayList();
            for (AxisLabel axisLabel : getModel().getCategoryY()) {
                int pos = axisLabel.getOrdinalPosition();
                if (pos >= getModel().getY() && pos <= getModel().getY() + getModel().getHeight()) {
                    categoryY.add(axisLabel);
                }
            }

            if (!categoryY.isEmpty()) {
                int mod = 1;
                double height = t.getY(getModel().getX(), categoryY.get(0).getOrdinalPosition()) - t.getY(getModel().getX(), categoryY.get(categoryY.size() - 1).getOrdinalPosition());
                if (categoryY.size() > height / label_size) {
                    mod = (int) (categoryY.size() / (double) (height / label_size));
                }
                for (int i = 0; i < categoryY.size(); i++) {
                    if (i % mod != 0) {
                        continue;
                    }
                    AxisLabel cat = categoryY.get(i);
                    int catPos = cat.getOrdinalPosition();
                    double y = t.getY(getModel().getX(), catPos);
                    ctx.save();
                    renderLine(ctx, axisMaskTransform, y);
                    renderLabel(ctx, axisMaskTransform, cat, y);
                    ctx.restore();
                }
            }
            renderAxisTitle(ctx);
        }
    }

    private void renderLabel(Context2d ctx, CSIContext2d.CanvasTransform axisMaskTransform, AxisLabel cat, double y) {
        int padRight = 4; //FIXME: magic numbers (related to tickSize)
        int padLeft = 30; //FIXME: magic numbers
        ctx.closePath();
        ctx.setFont(getFont());
        String label = view.getModel().formatAxisLabel(cat.getLabel(), MatrixModel.Axis.Y);
        label = fitLabel(label, ctx, (83 - padLeft - padRight));
        ctx.translate(axisMaskTransform.getX(0, 0), y + getFontSize() / 2);
        ctx.fillText(label, -ctx.measureText(label).getWidth() - padRight, 0);
        ctx.closePath();
    }

    private void renderLine(Context2d ctx, CSIContext2d.CanvasTransform axisMaskTransform, double y) {
        int tickSize = 5; //FIXME: magic numbers
        CSIContext2d.CanvasTransform mct = getMainCanvasTransform();
        double endX = Math.min(getModel().getX()+getModel().getWidth(),getModel().getMetrics().getAxisXCount()-1);
        double x2 = mct.getX(endX, 0);
        ctx.beginPath();
        ctx.moveTo(axisMaskTransform.getX(0, 0) - tickSize, y);
        ctx.lineTo(x2, y);
        ctx.stroke();
        ctx.closePath();
    }

    @Override
    String getTitle() {
      return getModel().getYAxisTitle();
    }

    void positionTitle(Context2d ctx) {
        ctx.rotate(-Math.PI / 2.0);
        int titlePadLeft = 15;
        ctx.translate(-getLayerHeight() / 2.0, titlePadLeft);
    }


    @Override
    public boolean hitTest(double x, double y) {
        CSIContext2d.CanvasTransform transform = getMainCanvasTransform();
        if(transform == null){
            return false;
        }

        double i = transform.getX(0, 0);

        return x < 83;
    }
}
