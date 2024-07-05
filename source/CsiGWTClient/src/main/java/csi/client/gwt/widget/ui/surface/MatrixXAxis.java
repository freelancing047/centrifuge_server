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

public class MatrixXAxis extends AbstractMatrixAxis {

    MatrixXAxis(MatrixView view) {
        super(view);
    }


    protected MouseMoveHandler getMouseMoveHandler() {
        return new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if(view.isLoaded()) {
                    double x = event.getX();
                    double y = event.getY();
                    CSIContext2d.CanvasTransform t2 = getTransformForEvents();
                    view.clearHoverCell();

                    int minX = (int) view.getModel().getX();

                    int binCountForAxisX = getModel().getBinCountForAxis((int) getModel().getWidth());
                    int xCat = (int) (t2.getX(x, y) + .5);
                    int xMod = (xCat - minX) % binCountForAxisX;
                    xCat = (xCat - xMod) + binCountForAxisX / 2;
                    view.setMouseXY(xCat, -1);
                    layer.getDrawingPanel().setOnXAxis(true);
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
                int minX = (int) view.getModel().getX();
                int binCountForAxisX = getModel().getBinCountForAxis((int) getModel().getWidth());
                int columnToSelect = (int) (t2.getX(x, y) + .5);
                int xMod = (columnToSelect - minX) % binCountForAxisX;
                columnToSelect = (columnToSelect - xMod);

                columnToSelect = view.getMouseX() - binCountForAxisX / 2;
                if(event.isShiftKeyDown()){
                    MatrixDataRequest request = getModel().getAxisSelectionRequest(columnToSelect, MatrixModel.Axis.X);
                    getModel().getSelectedCells().removeIf(cell -> cell.getX() >= request.getStartX()  && cell.getX() <= request.getEndX()) ;
                    view.refresh();
                }else {
                    requestSelectionForColumn(columnToSelect);
                }
            }
        };
    }

    private void requestSelectionForColumn(int columnToSelect) {
        MatrixDataRequest request = getModel().getAxisSelectionRequest(columnToSelect, MatrixModel.Axis.X);
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
            ctx.setStrokeStyle(CssColor.make("rgb(230,230,230)")); //LightGray
            ctx.setLineWidth(0.5);

            //FIXME: why do we need this???
            List<AxisLabel> categoryX = Lists.newArrayList();
            for (AxisLabel axisLabel : getModel().getCategoryX()) {
                int pos = axisLabel.getOrdinalPosition();
                if (pos >= getModel().getX() && pos <= getModel().getX() + getModel().getWidth()) {
                    categoryX.add(axisLabel);
                }
            }

            if (!categoryX.isEmpty()) {
                int mod = 1;
                int endX = categoryX.get(categoryX.size() - 1).getOrdinalPosition();
                int startX = categoryX.get(0).getOrdinalPosition();
                double width = t.getX(endX, 0) - t.getX(startX, 0);
                if (categoryX.size() > width / label_size) {
                    mod = (int) (categoryX.size() / (width / label_size));
                }
                for (int i = 0; i < categoryX.size(); i++) {
                    if (i % mod != 0) {
                        continue;
                    }
                    AxisLabel cat = categoryX.get(i);
                    int catPos = cat.getOrdinalPosition();
                    double x = t.getX(catPos, getModel().getY());
                    ctx.save();
                    renderLine(ctx, axisMaskTransform, x);
                    renderLabel(ctx, axisMaskTransform, cat, x);
                    ctx.restore();
                }
            }
            renderAxisTitle(ctx);
        }
    }

    private void renderLabel(Context2d ctx, CSIContext2d.CanvasTransform axisMaskTransform, AxisLabel cat, double x) {
        int padRight = 4; //FIXME: magic numbers (related to tickSize)
        int padLeft = 30; //FIXME: magic numbers
        ctx.setFont(getFont());
        String label = view.getModel().formatAxisLabel(cat.getLabel(), MatrixModel.Axis.X);
        label = fitLabel(label, ctx, (83 - padLeft - padRight));
        ctx.translate(x, axisMaskTransform.getY(0, 0));
        ctx.rotate(-Math.PI / 2);
        ctx.fillText(label, -ctx.measureText(label).getWidth() - 4, getFontSize() / 2);
    }

    private void renderLine(Context2d ctx, CSIContext2d.CanvasTransform t1, double x) {
        int tickSize = 5; //FIXME: magic numbers
        CSIContext2d.CanvasTransform mct = getMainCanvasTransform();
        double endY = Math.min(getModel().getY()+getModel().getHeight(), getModel().getMetrics().getAxisYCount()-1);
        double y2 = mct.getY(0, endY);
        ctx.beginPath();
        ctx.moveTo(x, t1.getY(0, 0) + tickSize);
        ctx.lineTo(x, y2);
        ctx.closePath();
        ctx.stroke();
    }

    @Override
    String getTitle() {
        return getModel().getXAxisTitle();
    }

    void positionTitle(Context2d ctx) {
        int titlePadBottom = 15;
        ctx.translate(getLayerWidth() / 2.0, getLayerHeight() - titlePadBottom);
    }

    @Override
    public boolean hitTest(double x, double y) {
        boolean b = y >  layer.getHeight() - 83;
        return b;
    }
}
