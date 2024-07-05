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
package csi.client.gwt.widget.ui;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Rectangle;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class RectangularDragger {

    private boolean flag = false;

    public class MouseMoveData {

        private int startX, startY, deltaFromStartX, deltaFromStartY, deltaFromLastX, deltaFromLastY;

        /**
         *
         * @param startX x start
         * @param startY y start
         * @param deltaFromStartX change from start to last
         * @param deltaFromStartY change from start to last
         * @param deltaFromLastX   ??
         * @param deltaFromLastY   ??
         */
        MouseMoveData(int startX, int startY, int deltaFromStartX, int deltaFromStartY, int deltaFromLastX,
                int deltaFromLastY) {
            super();
            this.startX = startX;
            this.startY = startY;
            this.deltaFromStartX = deltaFromStartX;
            this.deltaFromStartY = deltaFromStartY;

            this.deltaFromLastX = deltaFromLastX;
            this.deltaFromLastY = deltaFromLastY;
        }

        public int getStartX() {
            return startX;
        }

        public int getStartY() {
            return startY;
        }

        public int getDeltaFromStartX() {
            return deltaFromStartX;
        }

        public int getDeltaFromStartY() {
            return deltaFromStartY;
        }

        public int getDeltaFromLastX() {
            return deltaFromLastX;
        }

        public int getDeltaFromLastY() {
            return deltaFromLastY;
        }

        public int getStopX(){
            return  getStartX() + getDeltaFromStartX();
        }

        public int getStopY(){
            return getStartY() + getDeltaFromStartY();
        }


        /**
         * @return Returns a rectangle spec that accounts for negative deltas
         */
        public Rectangle getRectangleFromStart() {
            int x = getStartX();
            if (getDeltaFromStartX() < 0) {
                x += getDeltaFromStartX();
            }
            int y = getStartY();
            if (getDeltaFromStartY() < 0) {
                y += getDeltaFromStartY();
            }
            return new Rectangle(x, y, Math.abs(getDeltaFromStartX()), Math.abs(getDeltaFromStartY()));
        }
    }

    public interface RectangularDraggerCallback {

        /**
         * Called when the mouse is clicked down.
         * @param event
         * @param mx Location of mouse down relative to widget.
         * @param my Location of mouse down relative to widget.
         */
        public void onMouseDown(MouseDownEvent event, int mx, int my);

        /**
         * @param data Mouse movement data
         */
        public void onMouseMove(MouseMoveData data);

        /**
         * @param click true if this was only a click (i.e., there was no mouse movement or the mouse up happened 
         * on top of the exact point where mouse-down occurred).
         * @param data Mouse movement data
         */
        public void onMouseUp(boolean click, MouseMoveData data);
    }

    private Widget widget;
    private RectangularDraggerCallback callback;

    private HandlerRegistration globalMouseMoveHandler, globalMouseUpHandler;
    private int clientX, clientY, lastX, lastY;

    public RectangularDragger(Widget widget, RectangularDraggerCallback callback) {
        super();
        this.widget = widget;
        this.callback = callback;
        addHandlers();
    }

    private void addHandlers() {
        widget.addDomHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                handleMouseDown(event);
            }
        }, MouseDownEvent.getType());
    }

    protected void handleMouseDown(MouseDownEvent event) {
        event.preventDefault();

        if(event.getNativeButton() != 1) {
            return;
        }
        if (!flag) {
            flag = true;
            clientX = event.getRelativeX(widget.getElement());
            clientY = event.getRelativeY(widget.getElement());
            lastX = clientX;
            lastY = clientY;
            callback.onMouseDown(event, clientX, clientY);

            globalMouseMoveHandler = widget.addDomHandler(new MouseMoveHandler() {

                @Override
                public void onMouseMove(MouseMoveEvent e) {
                    e.preventDefault();
                    callback.onMouseMove(getMouseMoveData(e));
                }
            }, MouseMoveEvent.getType());

            globalMouseUpHandler = widget.addDomHandler(new MouseUpHandler() {

                @Override
                public void onMouseUp(MouseUpEvent e) {
                    if (e.getNativeButton() != 1) {
                        return;
                    }
                    globalMouseUpHandler.removeHandler();
                    globalMouseMoveHandler.removeHandler();
                    callback.onMouseUp(lastX == clientX && lastY == clientY, getMouseMoveData(e));
                    flag = false;
                }
            }, MouseUpEvent.getType());

            widget.addDomHandler(new MouseOutHandler() {

                @Override
                public void onMouseOut(MouseOutEvent event) {
//            globalMouseUpHandler.removeHandler();
//            globalMouseMoveHandler.removeHandler();

                }
            }, MouseOutEvent.getType());
        }
    }

    protected MouseMoveData getMouseMoveData(MouseEvent<?> e) {

        int x = e.getRelativeX(widget.getElement());
        int y = e.getRelativeY(widget.getElement());

        int dx = x - clientX;
        int dy = y - clientY;
        int ix = x - lastX;
        int iy = y - lastY;
        lastX = x;
        lastY = y;


        /*if(widget instanceof Canvas){
            Canvas can = (Canvas) widget;
            int height = can.getCoordinateSpaceHeight();
            int width = can.getCoordinateSpaceWidth();
//            Native.log("Canvas coordiante width " + width + " height" + height);
//            Native.logXY( dx, ix, dy, iy);
        }*/

        return new MouseMoveData(clientX, clientY, dx, dy, ix, iy);
    }

}
