package csi.client.gwt.widget.ui.surface;

import com.google.common.collect.Sets;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.i18n.client.NumberFormat;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.client.gwt.viz.matrix.MatrixView;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.service.api.MatrixActionsServiceProtocol;
import csi.shared.core.visualization.matrix.Cell;
import csi.shared.core.visualization.matrix.MatrixDataRequest;
import csi.shared.core.visualization.matrix.MatrixWrapper;

import java.util.HashSet;
import java.util.List;

public abstract class AbstractMatrixAxis extends BaseRenderable {
    private static final String PX_HELVETICA = "px Helvetica";
    private static final String WHITE = "white";
    static final int FONT_SIZE = 12;
    static final int MIN_FONT_SIZE = 12;

    MatrixView view;
    double label_size = 24;

    private final HashSet<Cell> cells = Sets.newHashSet();
    private double width = 1000;
    private double height = 1000;

    public AbstractMatrixAxis(MatrixView view) {
        this.view = view;

        this.addMouseMoveHandler(getMouseMoveHandler());
        this.addClickHandler(getClickHandler());
    }

    protected abstract ClickHandler getClickHandler();

    protected abstract MouseMoveHandler getMouseMoveHandler();


    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    Layer layer;

    @Override
    public void bind(Layer layer) {
        this.layer = layer;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    public void setCells(List<Cell> _cells) {
        cells.clear();
        cells.addAll(_cells);
    }

    public void setWidth(int width) {
        this.width = width;

    }

    public void setHeight(int height) {
        this.height = height;
    }


    String fitLabel(String lbl, Context2d ctx, double maxWidth) {

        boolean adj = false;
        int i = 0;
        while (ctx.measureText(lbl).getWidth() > maxWidth && FONT_SIZE - i > MIN_FONT_SIZE) {
            ctx.setFont((FONT_SIZE - ++i) + "pt Arial Narrow");
        }
        if (ctx.measureText(lbl).getWidth() > maxWidth) {
            while (ctx.measureText(lbl + "..").getWidth() > maxWidth) {
                lbl = lbl.substring(0, lbl.length() - 1);
                adj = true;
            }
        }
        return adj ? lbl + ".." : lbl;
    }


    MatrixModel getModel() {
        return view.getModel();
    }

    double getFontSize() {
        return FONT_SIZE;
    }


    void renderAxisTitle(Context2d ctx) {
        ctx.save();
        int titleFontSize = 15;
        ctx.setFont(titleFontSize + PX_HELVETICA);
        ctx.setStrokeStyle(CssColor.make(WHITE));
        ctx.setLineWidth(4);
        ctx.setTextAlign(Context2d.TextAlign.CENTER);
        ctx.setTextBaseline(Context2d.TextBaseline.MIDDLE);

        ctx.setLineJoin(Context2d.LineJoin.MITER);
        ctx.setMiterLimit(2);//NOTE: makes title rendering prettier
        String composedName = getTitle();
        positionTitle(ctx);
        ctx.strokeText(composedName, 0, 0);
        ctx.fillText(composedName, 0, 0);
        ctx.restore();
    }


    CSIContext2d.CanvasTransform getTransformForEvents() {
        CSIContext2d.CanvasTransform mct = ((MatrixMainLayer) getLayer()).getMainCanvasTransform();
        CSIContext2d.CanvasTransform copy = mct.copy();
        copy.invert();
        return copy;
    }

    protected void requestSelection(MatrixDataRequest axisSelectionRequest) {
        Vortex vortex = view.getVortex();
        VortexFuture<MatrixWrapper> vortexFuture = vortex.createFuture();

        vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
            @Override
            public void onSuccess(MatrixWrapper result) {

                result.getData().getCells().forEach(cell -> {
                        getModel().selectCell(cell);
                });

                view.setLoadingIndicator(false);
                view.refresh();
            }
        });

        view.setLoadingIndicator(true);
        vortexFuture.execute(MatrixActionsServiceProtocol.class).getCellsInRegionForSelection(axisSelectionRequest);
    }

    public CSIContext2d.CanvasTransform getMainCanvasTransform() {
        return ((MatrixMainLayer)layer).getMainCanvasTransform();
    }

    public String getFont() {
        return getFontSize() + "px Arial Narrow";
    }

    abstract String getTitle();

    abstract void positionTitle(Context2d ctx);

    int getLayerHeight() {
        return getLayer().getHeight();
    }

    int getLayerWidth() {
        return getLayer().getWidth();
    }
}
