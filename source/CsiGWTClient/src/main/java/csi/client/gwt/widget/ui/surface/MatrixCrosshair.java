package csi.client.gwt.widget.ui.surface;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.Scheduler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.client.gwt.viz.matrix.MatrixView;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;
import csi.server.common.model.visualization.matrix.MatrixType;
import csi.shared.core.visualization.matrix.AxisLabel;
import csi.shared.core.visualization.matrix.Cell;

import java.util.*;

public class MatrixCrosshair extends BaseRenderable {
    private static final int FONT_SIZE = 15;
    private static final String TOOLTIP_FONT = "10px sans-serif";

    private MatrixMainLayer layer;
    private MatrixView matrixView;
    private Cell hoverCell = null;
    private long start = 0;
    private double ease = 200;
    private CssColor bubbleBackgroundColor =CssColor.make("#FFF");
    private CssColor tooltipBackgroundColor = CssColor.make("#FFF");
    private CssColor tooltipBorderColor = CssColor.make("#333");
    private Scheduler.RepeatingCommand cmd;
    private CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private Canvas crossHairContext = Canvas.createIfSupported();

    public MatrixCrosshair(MatrixView matrixView) {

        this.matrixView = matrixView;
    }

    @Override
    public void render(Context2d ctx) {

        CSIContext2d.CanvasTransform t = layer.getMainCanvasTransform();
        Cell hoverCell = matrixView.getHoverCell();
        if (hoverCell == null) {
            hoverCell = new Cell();
            hoverCell.setX(matrixView.getMouseX());
            hoverCell.setY(matrixView.getMouseY());
        }
        MatrixModel model = matrixView.getModel();

        if (layer instanceof MatrixMainLayer) {
            if (matrixView.getHoverCell() != null && matrixView.validateCellVisible(matrixView.getHoverCell())) {
                renderItemHighlight(ctx, t, hoverCell, false);
            } else {
                this.hoverCell = null;
            }
            renderXHair(ctx, t, hoverCell, model);
            renderYHair(ctx, t, hoverCell, model);
        }
    }

    public void renderItemHighlight(Context2d ctx, CSIContext2d.CanvasTransform t, Cell hoverCell, boolean ifHovered) {
        if (this.hoverCell != hoverCell) {
            start = new Date().getTime();
            this.hoverCell = hoverCell;
        }

        ctx.save();
        ctx.beginPath();
        double radius = Math.abs(matrixView.getModel().getScaledBubbleRadius(hoverCell.getValue().doubleValue()));
        double scale = .55; // todo
        long now = new Date().getTime();
        double v = ease - (now - start);
        if (v > 1 && cmd == null) {

            cmd = new Scheduler.RepeatingCommand() {
                @Override
                public boolean execute() {
                    layer.getDrawingPanel().render();
                    cmd = null;
                    return false;
                }
            };
            Scheduler.get().scheduleFixedDelay(cmd, 10);
        }


        // this should work for both the summary and not
        if(matrixView.getModel().getSettings().getMatrixType() == MatrixType.HEAT_MAP) {
            scale = (.15 * (Math.min(ease, now - start) / ease)) + .4;
            if(matrixView.getModel().isSummary()) {

                ctx.save();
                ctx.beginPath();

                double scaledBubbleRadius = matrixView.getModel().getScaledBubbleRadius(hoverCell.getValue().doubleValue());

                t.moveOrigin(ctx, hoverCell.getX(), hoverCell.getY());
                double dx = Math.max(t.getX(2, 1) - t.getX(1, 1), scaledBubbleRadius);
                double dy = Math.max(t.getY(1, 2) - t.getY(1, 1), scaledBubbleRadius);
                ctx.setStrokeStyle(CssColor.make("rgba(0,0,0," + (Math.min(ease, now - start) / ease) + ")"));

                ctx.beginPath();
                ctx.moveTo(-scale * dx, -scale * dy);
                ctx.lineTo(-scale * dx, scale * dy);
                ctx.lineTo(scale * dx, scale * dy);
                ctx.lineTo(scale * dx, -scale * dy);

                ctx.closePath();

                ctx.setLineWidth(2);
                ctx.stroke();
                if (ifHovered) {
                    ctx.setFillStyle(CssColor.make("black"));
                    ctx.fill();
                }
                ctx.restore();
            } else {
                ctx.save();
                ctx.beginPath();

                t.moveOrigin(ctx, hoverCell.getX(), hoverCell.getY());
                double dx = t.getX(2, 1) - t.getX(1, 1);
                double dy = t.getY(1, 2) - t.getY(1, 1);

                ctx.setStrokeStyle(CssColor.make("rgba(0,0,0," + (Math.min(ease, now - start) / ease) + ")"));
                ctx.beginPath();

                ctx.moveTo(-scale * dx, -scale * dy);
                ctx.lineTo(-scale * dx, scale * dy);
                ctx.lineTo(scale * dx, scale * dy);
                ctx.lineTo(scale * dx, -scale * dy);

                ctx.closePath();

                ctx.setLineWidth(2);
                ctx.stroke();
                if (ifHovered) {
                    ctx.setFillStyle(CssColor.make("black"));
                    ctx.fill();
                }
                ctx.restore();
            }

        } else {
            ctx.setStrokeStyle(CssColor.make("rgba(0,0,0," + (Math.min(ease, now - start) / ease) + ")"));
            radius += (Math.min(radius, 10) * (Math.min(ease, now - start) / ease));
            ctx.arc(t.getX(hoverCell.getX(), hoverCell.getY()), t.getY(hoverCell.getX(), hoverCell.getY()), radius, 0, 2 * Math.PI, true);
            ctx.closePath();
            ctx.setLineWidth(2);
            ctx.stroke();
            if (ifHovered) {
                ctx.setFillStyle(CssColor.make("black"));
                ctx.fill();
            }
            ctx.restore();
        }
    }

    public void renderYHair(Context2d ctx, CSIContext2d.CanvasTransform t, Cell hoverCell, MatrixModel model) {
        ctx.save();

        Context2d ctx1 = ctx;
        crossHairContext.setSize(layer.getWidth() + "px", layer.getHeight() + "px");
        crossHairContext.setCoordinateSpaceWidth(layer.getCanvas().getCoordinateSpaceWidth());
        crossHairContext.setCoordinateSpaceHeight(layer.getCanvas().getCoordinateSpaceHeight());
        ctx = crossHairContext.getContext2d();
        ctx.clearRect(0, 0, crossHairContext.getCoordinateSpaceWidth(), crossHairContext.getCoordinateSpaceHeight());

        List<AxisLabel> categoryY = model.getCategoryY();
        int hy = hoverCell.getY();

        if (hy >= model.getY() && hy <= model.getHeight()-1 + model.getY()) {
            AxisLabel cat = null;
            //TODO: I can do better
            for (AxisLabel axisLabel : categoryY) {
                if (axisLabel.getOrdinalPosition() == hy) {
                    cat = axisLabel;
                }
            }
            if (cat != null) {
                ctx.beginPath();
                double x1 = t.getX(model.getX(), hy);
                double y = t.getY(model.getX(), hy);
                double x2 = t.getX(model.getX() + model.getWidth() - 1, hy);
                ctx.moveTo(x1 - model.getViewportPad() - 5, y);
                ctx.lineTo(layer.getWidth(), y);
                ctx.closePath();
                ctx.stroke();

                ctx.setFont(FONT_SIZE + "pt Arial Narrow");
                String label = cat.getLabel();
                //CEN-5101 always draw axis label
                // don't draw the axis label if we dn't have space for it
                if(ctx.measureText(label).getWidth() > matrixView.getOffsetHeight()){
                    label = fitLabel(label, ctx, layer.getHeight()-2*FONT_SIZE);
                }
                drawYCallout(ctx, x1, y, label);
                if (!matrixView.getModel().isSummary()) {
                    if (layer.getDrawingPanel().isOnYAxis()) {
                        drawYLabelTooltip(ctx, y, cat);
                        layer.getDrawingPanel().setOnYAxis(false);
                    }
                }
            }
            if (this.hoverCell != null) {
                ctx.setGlobalCompositeOperation(Context2d.Composite.DESTINATION_OUT);

                renderItemHighlight(ctx, t, hoverCell, true);
                ctx.setGlobalCompositeOperation(Context2d.Composite.SOURCE_OVER);
            }
            ctx1.drawImage(crossHairContext.getCanvasElement(), 0, 0);
        }
    }

    String fitLabel(String lbl, Context2d ctx, double maxWidth) {

        boolean adj = false;
        int i = 0;
        ctx.setFont(FONT_SIZE + "pt Arial Narrow");
        if (ctx.measureText(lbl).getWidth() > maxWidth) {
            while (ctx.measureText(lbl + "..").getWidth() > maxWidth) {
                lbl = lbl.substring(0, lbl.length() - 1);
                adj = true;
            }
        }
        return adj ? lbl + "…" : lbl;
    }

    private void drawYCallout(Context2d ctx, double x1, double y, String label) {
        label = matrixView.getModel().formatAxisLabel(label, MatrixModel.Axis.Y);
        double textLength = ctx.measureText(label).getWidth();

        double viewportPad = -(layer.getAxisMask().getX(0, 0) - layer.getMainCanvasTransform().getX(matrixView.getModel().getX(), matrixView.getModel().getY()));
        if (x1 >= textLength + 7 + viewportPad) {
            drawRoundedRectangle(ctx, x1 - textLength - 7 - viewportPad, y - FONT_SIZE / 2 - 5, FONT_SIZE + 10, textLength + 10, FONT_SIZE / 4, bubbleBackgroundColor, bubbleBackgroundColor);
            ctx.setLineWidth(.5);
            ctx.stroke();
            ctx.setFillStyle(CssColor.make("#133c55"));
            ctx.fillText(label, x1 - textLength - viewportPad - 2, y + FONT_SIZE / 2);
        } else {
            if (y + textLength - 10 > 0) {

                int height = layer.getHeight();
                if (y - textLength / 2 < 30) {
                    ctx.translate(x1, textLength / 2);
                } else if (y + textLength / 2 > height-30) {
                    ctx.translate(x1, layer.getHeight() - textLength / 2 - 15);
                } else {
                    ctx.translate(x1, y);
                }

                drawRoundedRectangle(ctx, -(FONT_SIZE + 10) - viewportPad, -textLength / 2, textLength + 10, FONT_SIZE + 10, FONT_SIZE / 4, bubbleBackgroundColor, bubbleBackgroundColor);
                ctx.setLineWidth(.5);
                ctx.stroke();
                ctx.setTextAlign(Context2d.TextAlign.CENTER);
                ctx.rotate(-Math.PI / 2);
                ctx.setFillStyle(CssColor.make("#333"));
                ctx.fillText(label, -5, -FONT_SIZE / 2.0 - viewportPad + 3);
            }
        }
        ctx.restore();
    }

    public void renderXHair(Context2d ctx, CSIContext2d.CanvasTransform t, Cell hoverCell, MatrixModel model) {

        Context2d ctx1 = ctx;
        crossHairContext.setSize(layer.getWidth() + "px", layer.getHeight() + "px");
        crossHairContext.setCoordinateSpaceWidth(layer.getCanvas().getCoordinateSpaceWidth());
        crossHairContext.setCoordinateSpaceHeight(layer.getCanvas().getCoordinateSpaceHeight());
        ctx = crossHairContext.getContext2d();
        ctx.clearRect(0, 0, crossHairContext.getCoordinateSpaceWidth(), crossHairContext.getCoordinateSpaceHeight());
        List<AxisLabel> categoryX = model.getCategoryX();
        int hx = hoverCell.getX();
        if (hx >= model.getX() && hx <= model.getWidth() + model.getX()) {
            ctx.setStrokeStyle(CssColor.make("rgb(0,0,0)")); //$NON-NLS-1$}
            ctx.setLineWidth(.5);
            AxisLabel cat = null;
            //TODO: I can do better... something like categoryX.get((hx-model.getX())/binSize)
            for (AxisLabel x : categoryX) {
                if (x.getOrdinalPosition() == hx) {
                    cat = x;
                }
            }
            if (cat != null) {
                ctx.save();
                ctx.beginPath();
                double x = t.getX(hx, model.getY());
                double y1 = t.getY(hx, model.getY());
                double y2 = t.getY(hx, model.getY() + model.getHeight() - 1);
                ctx.moveTo(x, y1 - 5 + model.getViewportPad());
                ctx.lineTo(x, 0);
                ctx.closePath();
                ctx.stroke();
                ctx.setFont(FONT_SIZE + "pt Arial Narrow");
                String label = cat.getLabel();
                double textLength = ctx.measureText(label).getWidth();
                //CEN-5101 always draw axis label
                if (ctx.measureText(label).getWidth() > matrixView.getOffsetHeight()) {
                    label = fitLabel(label, ctx, layer.getWidth() - 83 - FONT_SIZE);
                }
                drawXCallout(ctx, x, y1, label);
                if (!matrixView.getModel().isSummary()) {
                    if (layer.getDrawingPanel().isOnXAxis()) {
                        drawXLabelTooltip(ctx, x, cat);
                        layer.getDrawingPanel().setOnXAxis(false);
                    }
                }
            }
            if (this.hoverCell != null) {
                ctx.setGlobalCompositeOperation(Context2d.Composite.DESTINATION_OUT);

                renderItemHighlight(ctx, t, hoverCell, true);
                ctx.setGlobalCompositeOperation(Context2d.Composite.SOURCE_OVER);
            }
            ctx1.drawImage(crossHairContext.getCanvasElement(), 0, 0);
        }
    }

    private void drawXCallout(Context2d ctx, double x, double y1, String label) {
        label = matrixView.getModel().formatAxisLabel(label, MatrixModel.Axis.X);

        double textLength = ctx.measureText(label).getWidth();
        if (x + textLength / 2 > layer.getWidth() - 10) {
            ctx.translate(layer.getWidth() - textLength / 2 - 10, y1);
        } else if(x - textLength/2 < layer.getAxisMask().getX(0, 0)){
            ctx.translate(layer.getAxisMask().getX(0, 0) + textLength/2 + 4, y1);
        }else{
            ctx.translate(x, y1);
        }
        double viewportPad = layer.getAxisMask().getY(0,0)-layer.getMainCanvasTransform().getY(matrixView.getModel().getX(),matrixView.getModel().getY());
        drawRoundedRectangle(ctx, -textLength / 2 - 5, +viewportPad - 2, FONT_SIZE + 10, textLength + 10, FONT_SIZE / 4, bubbleBackgroundColor, bubbleBackgroundColor);
        ctx.setLineWidth(.5);
        ctx.stroke();
        ctx.setFillStyle(CssColor.make("#333"));
        ctx.fillText(label, -textLength / 2, +viewportPad + FONT_SIZE + 2);
        ctx.restore();
    }

    private void drawXLabelTooltip(Context2d ctx, double x, AxisLabel cat) {
        int tooltipPadding = 15;
        int spacingBetweenAxisAndTooltip = 5;
        int spacingBetweenCrossHairAndTooltip = 5;
        int magicNumber85 = 85;
        double magicNumberPoint8 = .8;
        double magicNumberPoint5 = .5;
        int magicNumber4 = 4;

        String[] tooltipLabels = {i18n.matrixTooltipCount(),
                i18n.matrixTooltipMinLabel(),
                i18n.matrixTooltipMax(),
                i18n.matrixTooltipAvg(),
                i18n.matrixTooltipSum()};
        String[] tooltipValues = {Integer.toString(cat.getCount()),
                cat.getMin().toString(),
                Double.toString(cat.getMax()),
                Double.toString(cat.getAvg()),
                Double.toString(cat.getSum())};
        ctx.setFont(TOOLTIP_FONT);
        double longestLabelTextLength = Arrays.stream(tooltipLabels).map(s -> ctx.measureText(s).getWidth()).max(Double::compare).get();
        double longestValueTextLength = Arrays.stream(tooltipValues).map(s -> ctx.measureText(s).getWidth()).max(Double::compare).get();

        double boxWidth = longestValueTextLength + longestLabelTextLength + 2 * tooltipPadding;
        if (x + boxWidth + spacingBetweenCrossHairAndTooltip > layer.getWidth()) {
            x -= boxWidth + 2 * spacingBetweenCrossHairAndTooltip;
        }

        double tooltipBoxUpperBound;
        double tooltipBoxLeftBound;

        {//RenderBox
            ctx.setGlobalAlpha(magicNumberPoint8);
            tooltipBoxUpperBound = layer.getAxisMask().getY(0, 0) - spacingBetweenAxisAndTooltip - magicNumber85;
            tooltipBoxLeftBound = x + spacingBetweenCrossHairAndTooltip;
            drawRoundedRectangle(ctx, tooltipBoxLeftBound, tooltipBoxUpperBound, magicNumber85, boxWidth, magicNumber4, tooltipBackgroundColor, tooltipBorderColor);
            ctx.setLineWidth(magicNumberPoint5);
            ctx.stroke();
        }

        {//RenderText
            ctx.setGlobalAlpha(1.0);
            ctx.setFillStyle(CssColor.make("#333"));
            ctx.translate(tooltipBoxLeftBound + longestLabelTextLength + tooltipPadding, tooltipBoxUpperBound);
            for (int i = 0; i < tooltipLabels.length; i++) {
                ctx.translate(0, FONT_SIZE);
                renderTooltipMetric(ctx, tooltipLabels[i], tooltipValues[i]);
            }
        }
        ctx.restore();
    }

    private void renderTooltipMetric(Context2d ctx, String tooltipLabel, String tooltipValue) {
        ctx.setTextAlign(Context2d.TextAlign.RIGHT);
        ctx.fillText(tooltipLabel, 0, 0);

        ctx.setTextAlign(Context2d.TextAlign.LEFT);
        ctx.fillText(tooltipValue,0, 0);
    }

    private void drawYLabelTooltip(Context2d ctx, double y, AxisLabel cat) {
        int tooltipPadding = 15;
        int spacingBetweenAxisAndTooltip = 5;
        int spacingBetweenCrossHairAndTooltip = 5;
        int magicNumber85 = 85;
        double magicNumberPoint8 = .8;
        double magicNumberPoint5 = .5;
        int magicNumber4 = 4;
        double x = layer.getAxisMask().getX(0, 0);

        String[] tooltipLabels = {i18n.matrixTooltipCount(),
                i18n.matrixTooltipMinLabel(),
                i18n.matrixTooltipMax(),
                i18n.matrixTooltipAvg(),
                i18n.matrixTooltipSum()};
        String[] tooltipValues = {Integer.toString(cat.getCount()),
                cat.getMin().toString(),
                Double.toString(cat.getMax()),
                Double.toString(cat.getAvg()),
                Double.toString(cat.getSum())};
        ctx.setFont(TOOLTIP_FONT);
        double longestLabelTextLength = Arrays.stream(tooltipLabels).map(s -> ctx.measureText(s).getWidth()).max(Double::compare).get();
        double longestValueTextLength = Arrays.stream(tooltipValues).map(s -> ctx.measureText(s).getWidth()).max(Double::compare).get();

        double boxWidth = longestValueTextLength + longestLabelTextLength + 2 * tooltipPadding;
        double tooltipBoxUpperBound;
        double tooltipBoxLeftBound;
//        double maxWidth = longestTextLength + 10;
        {//RenderBox
            ctx.setGlobalAlpha(magicNumberPoint8);
            tooltipBoxUpperBound = y+spacingBetweenCrossHairAndTooltip;
            tooltipBoxLeftBound = x + spacingBetweenAxisAndTooltip;
//        drawRoundedRectangle(ctx, xEdge,   y + 10, 85, totalLongestTextLength + 30, FONT_SIZE / 4, tooltipBackgroundColor, tooltipBackgroundColor);
            drawRoundedRectangle(ctx, tooltipBoxLeftBound, y+spacingBetweenCrossHairAndTooltip, magicNumber85, boxWidth, magicNumber4, tooltipBackgroundColor, tooltipBorderColor);
            ctx.setLineWidth(magicNumberPoint5);
            ctx.stroke();
        }

        {//RenderText
            ctx.setGlobalAlpha(1.0);
            ctx.setFillStyle(CssColor.make("#333"));
            ctx.translate(tooltipBoxLeftBound + longestLabelTextLength + tooltipPadding, tooltipBoxUpperBound);
            for (int i = 0; i < tooltipLabels.length; i++) {
                ctx.translate(0, FONT_SIZE);
                renderTooltipMetric(ctx, tooltipLabels[i], tooltipValues[i]);
            }
        }
        ctx.restore();
    }

    @Override
    public boolean hitTest(double x, double y) {
        return false;
    }

    @Override
    public void bind(Layer layer) {
        if (layer instanceof MatrixMainLayer) {
            this.layer = (MatrixMainLayer) layer;
        }
    }

    @Override
    public boolean isDirty() {
        return false;
    }
}
