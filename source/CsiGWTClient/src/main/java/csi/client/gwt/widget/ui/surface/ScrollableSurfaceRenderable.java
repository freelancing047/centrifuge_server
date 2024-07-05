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
import com.google.gwt.event.dom.client.ClickEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface ScrollableSurfaceRenderable {

    public class BBox {

        int x1, y1, x2, y2;

        public BBox() {
        }

        public BBox(int x1, int y1, int x2, int y2) {
            super();
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public BBox(double x1, double y1, double x2, double y2) {
            this.x1 = (int) Math.floor(x1);
            this.y1 = (int) Math.floor(y1);
            this.x2 = (int) Math.ceil(x2);
            this.y2 = (int) Math.ceil(y2);
        }

        public int getX1() {
            return x1;
        }

        public void setX1(int x1) {
            this.x1 = x1;
        }

        public int getY1() {
            return y1;
        }

        public void setY1(int y1) {
            this.y1 = y1;
        }

        public int getX2() {
            return x2;
        }

        public void setX2(int x2) {
            this.x2 = x2;
        }

        public int getY2() {
            return y2;
        }

        public void setY2(int y2) {
            this.y2 = y2;
        }

        public static BBox withCornerAndDimensions(double x, double y, double width, double height) {
            BBox box = new BBox();
            box.setX1((int) Math.floor(x));
            box.setY1((int) Math.floor(y));
            box.setX2(box.getX1() + (int) Math.ceil(width));
            box.setY2(box.getY1() + (int) Math.ceil(height));
            return box;
        }
    }
    
    /**
     * @return true if the visual element rendered can overlap with others (like a bubble chart), false otherwise.
     */
    public boolean isOverlappingVisual();

    /**
     * @return Index of this cell in the x-ordinal axis.
     */
    public int getX();

    /**
     * @return Index of this cell in the y-ordinal axis.
     */
    public int getY();
    
    /**
     * @return The value that this renderable represents.
     */
    public double getValue();

    /**
     * Render self at location
     * @param cx center x
     * @param cy center y
     * @param ctx Drawing context. This is the visible rendering for the surface. 
     * @param drawContext Additional information for rendering.
     * @return Bounding box for this renderable. 
     */
    public BBox render(double cx, double cy, Context2d ctx, DrawContext drawContext);

    /**
     * 
     * @param x
     * @param y
     * @return true if the coordinates are withing this renderable's region.
     */
    public boolean isHitTest(int x, int y);

    /**
     * Indicates that mouse click was performed on this renderable.
     * @param event Mouse click
     */
    public void onClick(ClickEvent event);

    /**
     * Called when a rectangular selection region is defined.
     * @param x left x
     * @param y top y 
     * @param width width of the rectangle.
     * @param height height of the rectangle.
     */
    public void selectionRectangle(int x, int y, int width, int height);


    public boolean isInRange(int x, int y, int width, int height);

    /**
     * Deselects items within that rect
     * @param x left x
     * @param y top y
     * @param width width of the rectangle.
     * @param height height of the rectangle.
     */
    public void deselectionRectangle(int x, int y, int width, int height);

    /**
     * This call is made once to any instance of the renderable before a redraw of the renderable's canvas to set 
     * canvas options that apply globally to all the renderables in the collection passed to the Scrollable surface.
     * @param ctx drawing context reference
     * @param drawContext Additional information for rendering.
     */
    public void prepareContext(Context2d ctx, DrawContext drawContext);
    
    /**
     * @return true if this renderable is selected.
     */
    public boolean isSelected();
    
    /**
     * @param value true to mark as selected, false otherwise.
     */
    public void setSelected(boolean value);
    
}
