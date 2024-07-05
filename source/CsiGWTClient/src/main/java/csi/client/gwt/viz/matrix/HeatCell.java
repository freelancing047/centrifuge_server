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

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.i18n.client.NumberFormat;

import csi.client.gwt.widget.ui.surface.DrawContext;
import csi.shared.core.color.ColorUtil;
import csi.shared.core.color.ColorUtil.HSL;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class HeatCell extends MatrixRenderable {

    private static final double SELECTION_RATIO = 5;

    private static final double MIN_SELECTION_WIDTH = .5;

    private static NumberFormat valueFormat = NumberFormat.getDecimalFormat();

    private double width;
    private double height;

    public HeatCell(boolean showValue) {
        super(showValue);
    }
    
    @Override
    public boolean isOverlappingVisual() {
        return false;
    }

    @Override
    public void prepareContext(Context2d ctx, DrawContext drawContext) {
        super.prepareContext(ctx, drawContext);
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
    }

    /**
     * @param cx
     * @param cy
     * @param ctx
     * @param drawContext
     */
    @Override
    public BBox render(double cx, double cy, Context2d ctx, DrawContext drawContext) {
        super.render(cx, cy);

        width = drawContext.getCategoryWidth();
        height = drawContext.getCategoryHeight();
        if (width < 2.0) {
            width = 2.0;
        }
        if (height < 2.0) {
            height = 2.0;
        }

    String color = getColorForBubble();
        ctx.setFillStyle(CssColor.make(color));
        ctx.beginPath();
        
        ctx.rect(cx - width / 2.0, cy - height / 2.0, width, height);
        ctx.fill();
        if (isShowValue()) {
            ctx.beginPath();
            HSL hsl = ColorUtil.toHSL(color);
            hsl = ColorUtil.getContrastingGrayScale(hsl);
            ctx.setFillStyle(CssColor.make(ColorUtil.toColorString(hsl)));
            // Previous x coord  cx+ width / 2.0 - width / 10.0
            // moving to dead center of the cell because user cannot see the numbers is matrix is very small, top right cells will not have number.
            ctx.fillText(valueFormat.format(getValue()), cx, cy,
                    drawContext.getCategoryWidth());
        }
        
        if(isSelected()){
            ctx.setStrokeStyle(SELECTION_COLOR);
            ctx.beginPath();
            
            double lineWidth = height < width ? height/SELECTION_RATIO:width/SELECTION_RATIO;
            
            
            if(lineWidth < MIN_SELECTION_WIDTH){
                lineWidth = MIN_SELECTION_WIDTH;
            }

            ctx.setLineWidth(lineWidth);
            ctx.strokeRect(cx - width / 2.0 + lineWidth/2, cy - height / 2.0 + lineWidth/2, width - lineWidth, height - lineWidth);

            ctx.stroke();
            ctx.restore();
        }
        
        return BBox.withCornerAndDimensions(cx - width/2.0, cy - height / 2.0, width, height);
    }

    @Override
    public boolean isHitTest(int x, int y) {
        double cx = getCx() - (width / 2.0);
        double cy = getCy() - (height / 2.0);
        return cx <= x && x <= cx + width && cy <= y && y <= cy + height;
    }

}
