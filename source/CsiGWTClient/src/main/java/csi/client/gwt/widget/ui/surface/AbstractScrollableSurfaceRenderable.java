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
package csi.client.gwt.widget.ui.surface;

import com.google.gwt.canvas.dom.client.Context2d;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractScrollableSurfaceRenderable implements ScrollableSurfaceRenderable {

    private double cx, cy;

    public void render(double cx, double cy) {
        this.cx = cx;
        this.cy = cy;
    }

    /**
     * @return returns true if the center is within the rectangle.
     */
    public boolean isInRect(int x, int y, int width, int height) {
        return x <= cx && cx <= x + width && y <= cy && cy <= y + height;
    }

    public double getCx() {
        return cx;
    }

    public double getCy() {
        return cy;
    }

    public void toggleSelection() {
        setSelected(!isSelected());
    }

    @Override
    public void selectionRectangle(int x, int y, int width, int height) {
        if (isInRect(x, y, width, height)) {
            setSelected(true);
        }
    }

    public boolean isInRange(int x, int y, int width, int height){
        if (isInRect(x, y, width, height)) {
            return true;
        }
        return false;
    }

    public void deselectionRectangle(int x, int y, int width, int height){
        if (isInRect(x, y, width, height)) {
            setSelected(false);
        }
    }

    
    @Override
    public void prepareContext(Context2d ctx, DrawContext drawContext) {
        // noop
    }
}
