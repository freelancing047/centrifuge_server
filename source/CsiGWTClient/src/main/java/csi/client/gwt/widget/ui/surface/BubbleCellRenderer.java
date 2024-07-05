package csi.client.gwt.widget.ui.surface;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.i18n.client.NumberFormat;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.shared.core.color.ColorUtil;

class BubbleCellRenderer extends CellRenderer {
    private static final int RADI_SELECTION_RATIO = 4;
    private static double MIN_SELECTION_WIDTH = 2;

    protected static CssColor SELECTION_COLOR = CssColor.make(255, 135, 10);
    private static final boolean SHADOW_ENABLED = false;
    private static final double CLICK_DETECTION_BUFFER_ZONE_SIZE = 2.0;
    private static final double LIGHTEN_COLOR_LUE_DELTA = 0.3;
    private static final int SHADOW_DISABLE_CUTOFF = 150;
    private static final double GLOBAL_ALPHA = 0.6;
    private static final double SHADOW_OFFSET_PERCENTAGE = 5.0;
    private static final double SHADOW_OFFSET_PERCENTAGE_HALF = SHADOW_OFFSET_PERCENTAGE / 2;
    private static final String SHADOW_COLOR = "#999";
    private static final int SHADOW_BLUR = 10;
    private static final String KEY_ZOOM_LEVEL = "zoomLevel";
    private static final double TWO_PI = Math.PI * 2;
    private static NumberFormat valueFormat = NumberFormat.getDecimalFormat();

    @Override
    public void render(MatrixModel model, Context2d ctx, double cellValue, boolean isSelected) {
        double currentRadius = model.getScaledBubbleRadius(cellValue);
        //FIXME: this getScaledBubbleRadius should always return a positive number
        currentRadius = Math.abs(currentRadius);


        String color = renderBubble(model, ctx, cellValue, currentRadius);
        if (model.isShowValue()) {
            renderValue(ctx, cellValue, color);
        }
        if (isSelected) {
            renderSelectionDecoration(ctx, currentRadius);
        }
    }

    private String renderBubble(MatrixModel model, Context2d ctx, double cellValue, double currentRadius) {
        String color = model.getColor(cellValue);
        ctx.setFillStyle(color);

        ctx.beginPath();
        ctx.arc(0, 0, Math.abs(currentRadius), 0, TWO_PI, true);
        ctx.closePath();
        ctx.fill();
        return color;
    }

    private void renderValue(Context2d ctx, double cellValue, String color) {
        ColorUtil.HSL hsl = ColorUtil.toHSL(color);
        hsl = ColorUtil.getContrastingGrayScale(hsl);
        ctx.setFillStyle(CssColor.make(ColorUtil.toColorString(hsl)));
        ctx.fillText(valueFormat.format(cellValue), 0, 0, 1);
    }

    private void renderSelectionDecoration(Context2d ctx, double currentRadius) {
        ctx.setStrokeStyle(SELECTION_COLOR);
        ctx.beginPath();

        double lineWidth = currentRadius / RADI_SELECTION_RATIO;
        if (Double.compare(lineWidth, MIN_SELECTION_WIDTH) < 0) {
            lineWidth = MIN_SELECTION_WIDTH;
        }
        Double radius = Math.abs(currentRadius - lineWidth / 2);
        if (Double.compare(radius, MIN_SELECTION_WIDTH) < 0) {
            radius = MIN_SELECTION_WIDTH / 2;
        }

        ctx.arc(0, 0, radius, 0, TWO_PI, true);
        ctx.setLineWidth(lineWidth);

        //increase for selection visibility
        ctx.setGlobalAlpha(GLOBAL_ALPHA + .15);
        ctx.stroke();
    }
}
