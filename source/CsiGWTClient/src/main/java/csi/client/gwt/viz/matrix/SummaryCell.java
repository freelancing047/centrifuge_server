package csi.client.gwt.viz.matrix;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.sencha.gxt.widget.core.client.info.Info;
import csi.client.gwt.widget.ui.surface.DrawContext;
import csi.client.gwt.widget.ui.surface.ScrollableSurfaceRenderable;
import csi.shared.core.visualization.matrix.Cell;

/**
 * Created by Ivan on 10/9/2017.
 */

// GIVE SUMMARY CELL AN ID TO RESOLVE ON THE SERVER, otherwise we are sending long ass arrays back and forth.
public class SummaryCell extends MatrixRenderable {
    protected static CssColor SELECTION_COLOR = CssColor.make(255,135,10);
    private static final int RADI_SELECTION_RATIO = 4;
    private static final double CLICK_DETECTION_BUFFER_ZONE_SIZE = 4;
    private double currentRadius;
    private static final double TWO_PI = Math.PI * 2;
    private static  double MIN_SELECTION_WIDTH = 3;
    private static final String KEY_ZOOM_LEVEL = "zoomLevel";
    private MatrixPresenter presenter;
    private static final double GLOBAL_ALPHA = 0.5;

    public SummaryCell(boolean showValue, MatrixPresenter mP) {
        super(showValue);
        presenter = mP;

    }

    @Override
    public boolean isOverlappingVisual() {
        return false;
    }


    @SuppressWarnings("unused")
    @Override
    public void prepareContext(Context2d ctx, DrawContext drawContext) {
        super.prepareContext(ctx, drawContext);

        ctx.setGlobalAlpha(GLOBAL_ALPHA);

        if (isShowValue()) {
            double h = drawContext.getCategoryHeight();
            if (h < 20) {
                ctx.setFont("6pt Helvetica");
            } else {
                ctx.setFont("8pt Helvetica");
            }
            ctx.setTextAlign(Context2d.TextAlign.CENTER);
            ctx.setTextBaseline(Context2d.TextBaseline.MIDDLE);
        }

        // Setup zoom level to ensure we are not zooming in too much.
        double minRadius = getModel().getScaledBubbleRadius(getModel().getMetrics().getMinValue()) * drawContext.getZoomLevel();
        if (minRadius < 1.0) {
            drawContext.put(KEY_ZOOM_LEVEL, 1.0 / getModel().getScaledBubbleRadius(getModel().getMetrics().getMinValue()));
        } else {
            drawContext.put(KEY_ZOOM_LEVEL, drawContext.getZoomLevel());
        }
    }

    @Override
    //TOD: still need to add proper sizing
    public BBox render(double cx, double cy, Context2d ctx, DrawContext drawContext) {
        super.render(cx, cy);
        currentRadius = getModel().getScaledBubbleRadius(getValue()) * drawContext.<Double> get(KEY_ZOOM_LEVEL);

        //hackyhack
        if(currentRadius < (getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMin()/2)){
            currentRadius = getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMin()/2;
        }


        if(currentRadius < getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMin()){
            currentRadius=getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMin();
        }

        ctx.save();
        String color = getColorForBubble();

        ctx.beginPath();
        ctx.setLineWidth(2);
        if(isSelected()){
            double lineWidth = currentRadius/RADI_SELECTION_RATIO;
            if(lineWidth < MIN_SELECTION_WIDTH){
                lineWidth = MIN_SELECTION_WIDTH;
            }
            ctx.setFillStyle(SELECTION_COLOR);
            ctx.setStrokeStyle(SELECTION_COLOR);
            ctx.arc(cx, cy, Math.abs(currentRadius - lineWidth), 0, TWO_PI, true);
        }else{
            ctx.setFillStyle(CssColor.make(color));
            ctx.setStrokeStyle(CssColor.make(color));
            ctx.arc(cx, cy, Math.abs(currentRadius), 0, TWO_PI, true);
        }

        ctx.stroke();
        ctx.restore();


        if(isSelected()){
            ctx.setStrokeStyle(SELECTION_COLOR);
            ctx.beginPath();

            double lineWidth = currentRadius/RADI_SELECTION_RATIO;
            if(Double.compare(lineWidth, MIN_SELECTION_WIDTH) < 0){
                lineWidth = MIN_SELECTION_WIDTH;
            }
            Double radius = Math.abs(currentRadius - lineWidth/2);
            if(Double.compare(radius, MIN_SELECTION_WIDTH) < 0){
                radius = MIN_SELECTION_WIDTH/2;
            }

            ctx.arc(cx, cy,radius, 0, TWO_PI, true);
            ctx.setLineWidth(lineWidth);

            //increase for selection visibility
            ctx.setGlobalAlpha(GLOBAL_ALPHA+.15);
            ctx.stroke();
//            ctx.restore();
        }

        double x1 = cx - currentRadius;
        double y1 = cy - currentRadius;
        double x2 = cx + currentRadius;
        double y2 = cy + currentRadius;
        return new BBox(x1, y1, x2, y2);
    }



    @Override
    public boolean isHitTest(int x, int y) {
        double dx = x - getCx();
        double dy = y - getCy();
        // We add to the value to account for dithering and the fact that mouse coordinates as int are not as
        // precise.
        return Math.sqrt(dx * dx + dy * dy) < currentRadius + CLICK_DETECTION_BUFFER_ZONE_SIZE;
    }


    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
    }

}
