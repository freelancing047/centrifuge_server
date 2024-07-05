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
package csi.client.gwt.widget.ui.scroll;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.aria.client.OrientationValue;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RequiresResize;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * Uses the browser's native scrollbar to provide arbitrary horizontal or vertical scroll control.
 * @author Centrifuge Systems, Inc.
 *
 */
public class NativeScroller extends FlowPanel implements RequiresResize {

    private OrientationValue orientation;
    private DivWidget internalDiv = new DivWidget();
    private EventBus eventBus = new SimpleEventBus();
    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    // Used to figure out what the max scroll value is. When the vertical scroll bar is positioned to the top
    // programmatically, this flag is set and the corresponding scroll handler's offset value is captured to figure
    // out the inverted "zero" value.

    public NativeScroller(OrientationValue orientation) {
        super();
        this.orientation = orientation;
        switch (orientation) {
            case HORIZONTAL:
                getElement().getStyle().setOverflowX(Overflow.SCROLL);
                internalDiv.setHeight("10px"); //$NON-NLS-1$
                break;
            case VERTICAL:
                getElement().getStyle().setOverflowY(Overflow.SCROLL);
                internalDiv.setWidth("10px"); //$NON-NLS-1$
                break;
        }
        add(internalDiv);

        addDomHandler(new ScrollHandler() {

            @Override
            public void onScroll(ScrollEvent e) {
                handleScroll();
            }

        }, ScrollEvent.getType());
    }

    /**
     * @param maxScrollSize The max value to scroll to. NOTE: This value should necessarily be larger than the 
     * width (or height) of the scroller for the scrollbar to appear.
     */
    public void setMax(int maxScrollSize) {
        switch (orientation) {
            case HORIZONTAL:
                internalDiv.setWidth(maxScrollSize + "px"); //$NON-NLS-1$
                forceScrollEvent();
                break;
            case VERTICAL:
                internalDiv.setHeight(maxScrollSize + "px"); //$NON-NLS-1$
                forceScrollEvent();
                break;
        }
    }

    /**
     * Sets the scrollers to the "zero" mark.
     */
    public void initialize() {
        switch (orientation) {
            case HORIZONTAL:
                this.getElement().setScrollLeft(0);
                break;
            case VERTICAL:
                this.getElement().setScrollTop(internalDiv.getOffsetHeight());
                break;
        }
    }

    private void handleScroll() {
        int delta;
        double ratio;

        switch (orientation) {
            case HORIZONTAL:
                delta = this.getElement().getScrollLeft();
                ratio = delta / (double) internalDiv.getOffsetWidth();
                break;
            case VERTICAL:
                delta = internalDiv.getOffsetHeight() - this.getOffsetHeight() - this.getElement().getScrollTop();
                ratio = delta / (double) internalDiv.getOffsetHeight();
                break;
            default:
                throw new RuntimeException(i18n.nativeScrollerException() + orientation); //$NON-NLS-1$
        } // end switch.

        NativeScrollEvent event = new NativeScrollEvent();
        event.setDelta(delta);
        event.setRatio(ratio);
        eventBus.fireEventFromSource(event, this);
    }

    @Override
    public void onResize() {
        if (orientation == OrientationValue.VERTICAL && internalDiv.getOffsetHeight() > 0) {
            this.getElement().setScrollTop(internalDiv.getOffsetHeight());
        } else if (orientation == OrientationValue.HORIZONTAL) {
            this.getElement().setScrollLeft(0);
        }
    }

    public HandlerRegistration addScrollHandler(NativeScrollEventHandler handler) {
        return eventBus.addHandlerToSource(NativeScrollEvent.type, this, handler);
    }

    public void forceScrollEvent() {
        handleScroll();
    }

    /**
     * @param ratio Ratio to which the scrollbar should be positioned.
     */
    public void setPositionToRatio(double ratio) {
        if (orientation == OrientationValue.HORIZONTAL) {
            this.getElement().setScrollLeft((int) (ratio * internalDiv.getOffsetWidth()));
        } else {
            this.getElement().setScrollTop(
                    internalDiv.getOffsetHeight() - this.getOffsetHeight()
                            - (int) (ratio * internalDiv.getOffsetHeight()));
        }
    }

    public double getRatio() {
        if (orientation == OrientationValue.HORIZONTAL) {
            int delta = this.getElement().getScrollLeft();
            return delta / (double) internalDiv.getOffsetWidth();
        } else {
            int delta = internalDiv.getOffsetHeight() - this.getOffsetHeight() - this.getElement().getScrollTop();
            return delta / (double) internalDiv.getOffsetHeight();
        }
    }

    public void setPositionToRatio(double ratio, double dd) {
        int d = (int) Math.round(dd);
        switch (orientation) {
            case HORIZONTAL:
                this.getElement().setScrollLeft((int) (ratio * internalDiv.getOffsetWidth()) + d);
            case VERTICAL:
                this.getElement().setScrollTop(
                        internalDiv.getOffsetHeight() - this.getOffsetHeight() - (int) (ratio * internalDiv.getOffsetHeight())
                                - d);
                break;
        }
    }
}