/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.viz.matrix;

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.i18n.client.NumberFormat;

import csi.client.gwt.WebMain;
import csi.client.gwt.widget.ui.surface.DrawContext;
import csi.shared.core.color.ColorUtil;
import csi.shared.core.color.ColorUtil.HSL;
import csi.shared.core.util.Native;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class Bubble extends MatrixRenderable {


    private static final int RADI_SELECTION_RATIO = 4;
    private static  double MIN_SELECTION_WIDTH = 2;


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
    private double currentRadius;

    public Bubble(boolean showValue) {
        super(showValue);

        try{
            MIN_SELECTION_WIDTH = WebMain.getClientStartupInfo().getMatrixMinSelectionRadius();
        }catch (Exception e){
            //nothing
        }
    }

    @Override
    public boolean isOverlappingVisual() {
        return true;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
    }

    @SuppressWarnings("unused")
    @Override
    public void prepareContext(Context2d ctx, DrawContext drawContext) {
        super.prepareContext(ctx, drawContext);
        // Apply shadow
        if (SHADOW_ENABLED && drawContext.getTotalElements() <= SHADOW_DISABLE_CUTOFF) {
            ctx.setShadowColor(SHADOW_COLOR);
            ctx.setShadowBlur(SHADOW_BLUR);
            ctx.setShadowOffsetX(drawContext.getCategoryWidth() / SHADOW_OFFSET_PERCENTAGE);
            ctx.setShadowOffsetY(drawContext.getCategoryHeight() / SHADOW_OFFSET_PERCENTAGE);
        }
        // Alpha
        ctx.setGlobalAlpha(GLOBAL_ALPHA);

        ctx.setLineJoin(Context2d.LineJoin.MITER);
        ctx.setMiterLimit(2);


        if (isShowValue()) {
            double h = drawContext.getCategoryHeight();
            if (h < 20) {
                ctx.setFont("6pt Helvetica");
            } else {
                ctx.setFont("8pt Helvetica");
            }
            ctx.setTextAlign(TextAlign.CENTER);
            ctx.setTextBaseline(TextBaseline.MIDDLE);
        }

        // Setup zoom level to ensure we are not zooming in too much.
        double minRadius = getModel().getScaledBubbleRadius(getModel().getMetrics().getMinValue())
                * drawContext.getZoomLevel();
        if (minRadius < 1.0) {
            drawContext.put(KEY_ZOOM_LEVEL, 1.0 / (getModel().getScaledBubbleRadius(getModel().getMetrics().getMinValue())));
        } else {
            drawContext.put(KEY_ZOOM_LEVEL, drawContext.getZoomLevel());
        }
    }

    @SuppressWarnings("unused")
    @Override
    public BBox render(double cx, double cy, Context2d ctx, DrawContext drawContext) {
        super.render(cx, cy);


//        log("Current zoom level" + drawContext.getZoomLevel());
        currentRadius = getModel().getScaledBubbleRadius(getValue()) * drawContext.<Double> get(KEY_ZOOM_LEVEL);

        if(currentRadius < (getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMin()/2)){
            currentRadius = getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMin()/2;
        }


        if(currentRadius < getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMin()){
            currentRadius=getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMin();
        }
        currentRadius = Math.abs(currentRadius);
        String color = getColorForBubble();
        if (!SHADOW_ENABLED || drawContext.getTotalElements() > SHADOW_DISABLE_CUTOFF) {
            ctx.setFillStyle(color);
        } else {
            double x0 = cx - currentRadius / SHADOW_OFFSET_PERCENTAGE_HALF;
            double y0 = cy - currentRadius / SHADOW_OFFSET_PERCENTAGE_HALF;
            double r0 = currentRadius / SHADOW_OFFSET_PERCENTAGE;
            CanvasGradient gradient = ctx.createRadialGradient(x0, y0, r0, cx, cy, currentRadius);
            gradient.addColorStop(0, lighten(color));
            gradient.addColorStop(1, color);
            ctx.setFillStyle(gradient);
            
        }

        ctx.beginPath();
        if(isSelected()){
            double lineWidth = currentRadius/RADI_SELECTION_RATIO;
            if(lineWidth < MIN_SELECTION_WIDTH){
                lineWidth = MIN_SELECTION_WIDTH;
            }

            ctx.arc(cx, cy, Math.abs(currentRadius - lineWidth), 0, TWO_PI, true);
        } else {
            ctx.arc(cx, cy, Math.abs(currentRadius), 0, TWO_PI, true);
        }
        ctx.fill();
        if (isShowValue()) {
            ctx.beginPath();
            HSL hsl = ColorUtil.toHSL(color);
            hsl = ColorUtil.getContrastingGrayScale(hsl);
            ctx.setFillStyle(CssColor.make(ColorUtil.toColorString(hsl)));
            ctx.fillText(valueFormat.format(getCell().getValue()), cx, cy, drawContext.getCategoryWidth());
        }
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

    private String lighten(String color) {
        HSL hsl = ColorUtil.toHSL(color);
        hsl = new HSL(hsl.getH(), hsl.getS(), hsl.getL() + LIGHTEN_COLOR_LUE_DELTA);
        return ColorUtil.toColorString(hsl);
    }

    @Override
    public boolean isHitTest(int x, int y) {
        double dx = x - getCx();
        double dy = y - getCy();
        // We add to the value to account for dithering and the fact that mouse coordinates as int are not as
        // precise.
        return Math.sqrt(dx * dx + dy * dy) < currentRadius + CLICK_DETECTION_BUFFER_ZONE_SIZE;
    }

}
