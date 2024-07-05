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

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.BaseEventPreview;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * This component must be positioned within a LayoutPanel. It accepts one child panel that it reveals on mouse-over
 * and can support mouse click based close (if there was a qualifying click within the component) or mouse-out based
 * close.
 * @author Centrifuge Systems, Inc.
 *
 */
public class SlidingAccessPanel extends LayoutPanel {

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static final String PIN_TOOLTIP = i18n.slidingAccessPanelPinTooltip(); //$NON-NLS-1$
	private static final String UNPIN_TOOLTIP = i18n.slidingAccessPanelUnpinTooltip(); //$NON-NLS-1$
	private Button pinButton;
    private Widget widgetToAdjust;

    public enum Orientation {
        LEFT, RIGHT;
    }

    public enum State {
        CONCEALED, TRANSITION, REVEALED
    }

    ;

    private BaseEventPreview vizBarPreview;
    private Widget widgetToSlide;
    private boolean iconClicked;
    private State state = State.CONCEALED;
    private int revealWidth, childWidth;
    private boolean pinned;
    private HandlerRegistration mouseOutHandler;
    private Orientation orientation;

    private Timer slideCloseTimer = new Timer() {

        @Override
        public void run() {
            if (!iconClicked && state == State.REVEALED) {
                concealVizBar();
                vizBarPreview.remove();
            }
        }
    };
    ;

    @UiConstructor
    public SlidingAccessPanel(int revealWidth, Orientation orientation) {
        super();
        this.revealWidth = revealWidth;
        this.orientation = orientation;

        InlineLabel label = orientation == Orientation.LEFT ? new InlineLabel(">") : new InlineLabel("<"); //$NON-NLS-1$ //$NON-NLS-2$
        add(label);
        setWidgetTopHeight(label, 48.0, Unit.PCT, 12, Unit.PX);
        if (orientation == Orientation.LEFT) {
            setWidgetRightWidth(label, -2, Unit.PX, revealWidth, Unit.PX);
        } else {
            setWidgetLeftWidth(label, 0, Unit.PX, revealWidth, Unit.PX);
        }
    }

    @UiChild(tagname = "slide", limit = 1)
    public void setSlide(Widget widget, int width, Unit unit) {
        this.childWidth = width;
        this.widgetToSlide = widget;

        add(widgetToSlide);

        if (orientation == Orientation.LEFT) {
            setWidgetLeftWidth(widgetToSlide, -revealWidth, Unit.PX, width, unit);
        } else {
            setWidgetRightWidth(widgetToSlide, -revealWidth, Unit.PX, width, unit);
        }

        vizBarPreview = new BaseEventPreview() {

            @Override
            protected boolean onAutoHide(NativePreviewEvent pe) {
                concealVizBar();
                iconClicked = false;
                return super.onAutoHide(pe);
            }

            @Override
            protected void onPreviewKeyPress(NativePreviewEvent pe) {
                super.onPreviewKeyPress(pe);
                if (pe.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                    concealVizBar();
                    iconClicked = false;
                    vizBarPreview.remove();
                }
            }

            ;
        };
        vizBarPreview.getIgnoreList().add(widgetToSlide.getElement());

        addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                if (state == State.CONCEALED) {
                    revealVizBar();
                    vizBarPreview.add();
                } else {
                    slideCloseTimer.cancel();
                }
            }
        }, MouseOverEvent.getType());

        setupMouseOutHandler();
    }

    private void setupMouseOutHandler() {
        mouseOutHandler = addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                slideCloseTimer.schedule(500);
            }
        }, MouseOutEvent.getType());
    }

    public void revealVizBar() {
        final LayoutPanel parent = (LayoutPanel) getParent();
        Animation vizBarRevealer = new Animation() {

            @Override
            protected void onStart() {
                super.onStart();
                state = State.TRANSITION;
            }

            @Override
            protected void onUpdate(double progress) {
                if (orientation == Orientation.LEFT) {
                    parent.setWidgetLeftWidth(SlidingAccessPanel.this,
                            (int) ((progress - 1) * (childWidth - revealWidth)), Unit.PX, childWidth, Unit.PX);
                } else {
                    parent.setWidgetRightWidth(SlidingAccessPanel.this,
                            (int) ((progress - 1) * (childWidth - revealWidth)), Unit.PX, childWidth, Unit.PX);
                }
            }

            @Override
            protected void onComplete() {
                super.onComplete();
                state = State.REVEALED;
            }
        };
        vizBarRevealer.run(250);
        widgetToSlide.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
        if (orientation == Orientation.LEFT) {
            getWidgetContainerElement(widgetToSlide).<XElement>cast().setLeft(0);
        } else {
            setWidgetRightWidth(widgetToSlide, 0, Unit.PX, childWidth, Unit.PX);
        }
    }

    public void concealVizBar() {
        final LayoutPanel parent = (LayoutPanel) getParent();
        Animation vizBarConcealer = new Animation() {

            @Override
            protected void onStart() {
                super.onStart();
                state = State.TRANSITION;
            }

            @Override
            protected void onUpdate(double progress) {
                if (orientation == Orientation.LEFT) {
                    parent.setWidgetLeftWidth(SlidingAccessPanel.this, (int) (-progress * (childWidth - revealWidth)),
                            Unit.PX, childWidth, Unit.PX);
                } else {
                    parent.setWidgetRightWidth(SlidingAccessPanel.this, (int) (-progress * (childWidth - revealWidth)),
                            Unit.PX, childWidth, Unit.PX);
                }
            }

            @Override
            protected void onComplete() {
                super.onComplete();
                if (orientation == Orientation.LEFT) {
                    setWidgetLeftWidth(widgetToSlide, -revealWidth, Unit.PX, childWidth, Unit.PX);
                } else {
                    setWidgetRightWidth(widgetToSlide, -revealWidth, Unit.PX, childWidth, Unit.PX);
                }
                state = State.CONCEALED;
            }
        };
        vizBarConcealer.run(250);
    }

    /**
     * @param widget Widget which on click should cause the slider to not automatically collapse but instead collapse
     * on Escape key or mouse click elsewhere.
     */
    public void autoHideDeferOnClick(Widget widget) {
        widget.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                iconClicked = true;
            }
        }, ClickEvent.getType());
    }

    /**
     * @param pin The button (in toggle mode) that controls pinning the panel.
     * @param widgetToAdjust The widget to shrink when this panel is pinned).
     */
    public void setPinControl(final Button pin, final Widget widgetToAdjust) {
        pinButton = pin;
        this.widgetToAdjust = widgetToAdjust;
        vizBarPreview.getIgnoreList().add(pin.getElement());
        pin.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

            	determineTooltip(pin);
                setPinned(!pinned);

            }

			
        });
        determineTooltip(pin);
    }
    
    private void determineTooltip(final Button pin) {
		if(!pinned){
    		pin.setTitle(UNPIN_TOOLTIP);
    	} else {
    		pin.setTitle(PIN_TOOLTIP);
    	}
	}

    public void setState(State state) {
        this.state = state;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        if (pinned) {
            vizBarPreview.remove();
            LayoutPanel parent = (LayoutPanel) getParent();

            if (orientation == Orientation.LEFT) {
                setWidgetLeftWidth(widgetToSlide, 0, Unit.PX, childWidth, Unit.PX);
                parent.setWidgetLeftWidth(SlidingAccessPanel.this, 0, Unit.PX, childWidth, Unit.PX);
                parent.setWidgetLeftRight(widgetToAdjust, childWidth, Unit.PX, 0, Unit.PX);
            } else {
                setWidgetRightWidth(widgetToSlide, 0, Unit.PX, childWidth, Unit.PX);
                parent.setWidgetRightWidth(SlidingAccessPanel.this, 0, Unit.PX, childWidth, Unit.PX);
                parent.setWidgetLeftRight(widgetToAdjust, 0, Unit.PX, childWidth, Unit.PX);
            }
            mouseOutHandler.removeHandler();
        } else {
            vizBarPreview.add();
            concealVizBar();
            vizBarPreview.remove();
            setupMouseOutHandler();
            LayoutPanel parent = (LayoutPanel) getParent();
            if (orientation == Orientation.LEFT) {
                parent.setWidgetLeftRight(widgetToAdjust, revealWidth, Unit.PX, 0, Unit.PX);
            } else {
                parent.setWidgetLeftRight(widgetToAdjust, 0, Unit.PX, revealWidth, Unit.PX);
            }
        }

    }
}
