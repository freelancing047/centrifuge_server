/**
 * Sencha GXT 3.0.4 - Sencha for GWT
 * Copyright(c) 2007-2013, Sencha, Inc.
 * licensing@sencha.com
 *
 * http://www.sencha.com/products/gxt/license/
 */
package csi.client.gwt.worksheet.layout.window;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.Style.HideMode;
import com.sencha.gxt.core.client.dom.Layer;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.core.client.util.BaseEventPreview;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.core.client.util.Rectangle;
import com.sencha.gxt.core.client.util.Size;
import com.sencha.gxt.fx.client.DragCancelEvent;
import com.sencha.gxt.fx.client.DragEndEvent;
import com.sencha.gxt.fx.client.DragHandler;
import com.sencha.gxt.fx.client.DragMoveEvent;
import com.sencha.gxt.fx.client.DragStartEvent;
import com.sencha.gxt.fx.client.Draggable;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ModalPanel;
import com.sencha.gxt.widget.core.client.Resizable;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.ActivateEvent;
import com.sencha.gxt.widget.core.client.event.ActivateEvent.ActivateHandler;
import com.sencha.gxt.widget.core.client.event.ActivateEvent.HasActivateHandlers;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent;
import com.sencha.gxt.widget.core.client.event.DeactivateEvent;
import com.sencha.gxt.widget.core.client.event.DeactivateEvent.DeactivateHandler;
import com.sencha.gxt.widget.core.client.event.DeactivateEvent.HasDeactivateHandlers;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.MaximizeEvent;
import com.sencha.gxt.widget.core.client.event.MaximizeEvent.HasMaximizeHandlers;
import com.sencha.gxt.widget.core.client.event.MaximizeEvent.MaximizeHandler;
import com.sencha.gxt.widget.core.client.event.MinimizeEvent;
import com.sencha.gxt.widget.core.client.event.MinimizeEvent.HasMinimizeHandlers;
import com.sencha.gxt.widget.core.client.event.MinimizeEvent.MinimizeHandler;
import com.sencha.gxt.widget.core.client.event.ResizeEndEvent;
import com.sencha.gxt.widget.core.client.event.ResizeEndEvent.ResizeEndHandler;
import com.sencha.gxt.widget.core.client.event.ResizeStartEvent;
import com.sencha.gxt.widget.core.client.event.ResizeStartEvent.ResizeStartHandler;
import com.sencha.gxt.widget.core.client.event.RestoreEvent;
import com.sencha.gxt.widget.core.client.event.RestoreEvent.HasRestoreHandlers;
import com.sencha.gxt.widget.core.client.event.RestoreEvent.RestoreHandler;
import com.sencha.gxt.widget.core.client.event.ShowEvent;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.shared.core.Constants;

/**
 *  * NOTE: CSI CHANGES -
 * This class has been copied from Sencha code-base (Window) AS-IS to allow for customization of private members and 
 * methods. 
 * The following modifications have been made:
 * -start modification list-
 *
 *
 * -end modification list-
 *
 * A specialized content panel intended for use as an application window.
 *
 * <p />
 * Code snippet:
 *
 * <pre>
 Window w = new Window();
 w.setHeading("Product Information");
 w.setModal(true);
 w.setPixelSize(600, 400);
 w.setMaximizable(true);
 w.setToolTip("The GXT product page...");
 w.setWidget(new Frame("http://www.sencha.com/products/gxt"));
 w.show();
 * </pre>
 */
public class WindowBase extends ContentPanel implements HasActivateHandlers<WindowBase>,
        HasDeactivateHandlers<WindowBase>, HasMaximizeHandlers, HasMinimizeHandlers, HasRestoreHandlers {


    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static final String MAXIMIZED = "maximized"; //$NON-NLS-1$
	private static final String X_WINDOW_DRAGGABLE = "x-window-draggable"; //$NON-NLS-1$
	private static final String UL = "ul"; //$NON-NLS-1$
	private static final String POSITION = "position"; //$NON-NLS-1$
	private static final String STATIC = "static"; //$NON-NLS-1$
	private static final String AUTO = "auto"; //$NON-NLS-1$
	private static final String HIDDEN = "hidden"; //$NON-NLS-1$
	private static final String OVERFLOW = "overflow"; //$NON-NLS-1$
	private static final String TRUE = Boolean.TRUE.toString();
	private static final String HIDE_FOCUS = "hideFocus"; //$NON-NLS-1$
	private final String MINIMIZE_TOOLTIP = i18n.windowBaseMinimizeTooltip(); //$NON-NLS-1$
    private final String RESTORE_TOOLTIP = i18n.windowBaseResotreTooltip(); //$NON-NLS-1$
    private final String MAXIMIZE_TOOLTIP = i18n.windowBaseMaximizeTooltip(); //$NON-NLS-1$

    @SuppressWarnings("javadoc")
    public interface WindowAppearance extends ContentPanelAppearance {

        String ghostClass();
    }

    @SuppressWarnings("javadoc")
    public interface WindowMessages {

        String close();

        String move();

        String moveWindowDescription();

        String resize();

        String resizeWindowDescription();

    }

    protected class DefaultWindowMessages implements WindowMessages {

        public String close() {
            return DefaultMessages.getMessages().messageBox_close();
        }

        public String move() {
            return DefaultMessages.getMessages().window_ariaMove();
        }

        public String moveWindowDescription() {
            return DefaultMessages.getMessages().window_ariaMoveDescription();
        }

        public String resize() {
            return DefaultMessages.getMessages().window_ariaResize();
        }

        public String resizeWindowDescription() {
            return DefaultMessages.getMessages().window_ariaResizeDescription();
        }

    }

    private class ResizeHandler implements ResizeStartHandler, ResizeEndHandler {

        @Override
        public void onResizeEnd(final ResizeEndEvent event) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    onEndResize(event);
                }
            });
        }

        @Override
        public void onResizeStart(ResizeStartEvent event) {
            onStartResize(event);
        }
    }

    protected Draggable dragger;
    protected WindowBaseManager manager;

    protected Button restoreBtn, maxBtn, minBtn;
    protected int ariaMoveResizeDistance = 5;
    protected boolean removeFromParentOnHide = true;

    private boolean closable = true;
    private WindowMessages windowMessages;
    private boolean constrain = true;
    private Widget focusWidget;
    private boolean maximizable;
    private int minHeight = 100;
    private boolean minimizable;
    private int minWidth = 200;
    private boolean modal;
    private boolean blinkModal = false;
    private boolean onEsc = true;
    private boolean resizable = true;
    private Layer ghost;
    private boolean maximized;
    private ModalPanel modalPanel;
    private Resizable resizer;
    private Point restorePos;
    private Size restoreSize, viewportSize;
    private boolean draggable = true;
    private boolean positioned;
    private boolean autoHide;
    private BaseEventPreview eventPreview;
    private boolean resizing;
    private XElement container;
    private Boolean restoreShadow;
    private Boolean restoreWindowScrolling;
    private HandlerRegistration modalPreview;
    private boolean dragging;
    private ResizeHandler resizeHandler;
    private DragHandler dragHandler = new DragHandler() {

        public void onDragCancel(DragCancelEvent event) {
            WindowBase.this.onDragCancel(event);
        };

        public void onDragEnd(DragEndEvent event) {
            WindowBase.this.onDragEnd(event);
        };

        public void onDragMove(DragMoveEvent event) {
            WindowBase.this.onDragMove(event);
        };

        public void onDragStart(DragStartEvent event) {
            WindowBase.this.onDragStart(event);
        };
    };

    private TextButton hideButton;
    private final WindowAppearance appearance;

    /**
     * Creates a new window with the specified appearance.
     *
     * @param appearance the window appearance
     */
    public WindowBase(WindowAppearance appearance) {
        super(appearance);
        this.appearance = appearance;
        shim = true;
        hidden = true;
        setShadow(true);
        setDraggable(true);

        setMonitorWindowResize(true);

        forceLayoutOnResize = true;

        getElement().makePositionable(true);

        eventPreview = new BaseEventPreview() {

            @Override
            protected boolean onAutoHide(NativePreviewEvent ce) {
                if (autoHide) {
                    if (resizing) {
                        return false;
                    }
                    hide();
                    return true;
                }
                return false;
            }

            @Override
            protected void onPreviewKeyPress(NativePreviewEvent pe) {
                onKeyPress(pe.getNativeEvent().<Event> cast());
            }

        };
        eventPreview.getIgnoreList().add(getElement());

        sinkEvents(Event.ONMOUSEDOWN | Event.ONKEYPRESS);

        getElement().setTabIndex(0);
        getElement().setAttribute(HIDE_FOCUS, TRUE);
    }

    @Override
    public HandlerRegistration addActivateHandler(ActivateHandler<WindowBase> handler) {
        return addHandler(handler, ActivateEvent.getType());
    }

    @Override
    public HandlerRegistration addDeactivateHandler(DeactivateHandler<WindowBase> handler) {
        return addHandler(handler, DeactivateEvent.getType());
    }

    @Override
    public HandlerRegistration addMaximizeHandler(MaximizeHandler handler) {
        return addHandler(handler, MaximizeEvent.getType());
    }

    @Override
    public HandlerRegistration addMinimizeHandler(MinimizeHandler handler) {
        return addHandler(handler, MinimizeEvent.getType());
    }

    @Override
    public HandlerRegistration addRestoreHandler(RestoreHandler handler) {
        return addHandler(handler, RestoreEvent.getType());
    }

    /**
     * Aligns the window to the specified element. Should only be called when the
     * window is visible.
     *
     * @param elem the element to align to.
     * @param alignment the position to align to (see {@link XElement#alignTo} for
     *          more details)
     * @param offsets the offsets
     */
    public void alignTo(Element elem, AnchorAlignment alignment, int[] offsets) {
        Point p = getElement().getAlignToXY(elem, alignment, offsets[0], offsets[1]);
        setPagePosition(p.getX(), p.getY());
    }

    /**
     * Centers the window in the viewport. Should only be called when the window
     * is visible.
     */
    public void center() {
        Point p = getElement().getAlignToXY(Document.get().getBody(),
                new AnchorAlignment(Anchor.CENTER, Anchor.CENTER), 0,0);
        setPagePosition(p.getX(), p.getY());
    }

    /**
     * Focus the window. If a focusWidget is set, it will receive focus, otherwise
     * the window itself will receive focus.
     */
    @Override
    public void focus() {
        Scheduler.get().scheduleFinally(new ScheduledCommand() {

            @Override
            public void execute() {
                doFocus();
            }
        });
    }

    /**
     * Returns true if the window is constrained.
     *
     * @return the constrain state
     */
    public boolean getConstrain() {
        return constrain;
    }

    /**
     * Returns the windows's container element.
     *
     * @return the container element or null if not specified
     */
    public Element getContainer() {
        return container;
    }

    /**
     * Returns the window's draggable instance.
     *
     * @return the draggable instance
     */
    public Draggable getDraggable() {
        if (dragger == null && draggable) {
            dragger = new Draggable(this, header);
            dragger.setConstrainClient(getConstrain());
            dragger.setSizeProxyToSource(false);
            dragger.addDragHandler(dragHandler);
        }
        return dragger;
    }

    public Draggable getDraggable(Widget dragWidget) {
            Draggable dragme= null;
        if (dragWidget != null && draggable) {
            dragme = new Draggable(this, dragWidget);
            dragme.setConstrainClient(getConstrain());
            dragme.setSizeProxyToSource(false);
            dragme.addDragHandler(dragHandler);
        }

        return dragme;
    }

    /**
     * Returns the focus widget.
     *
     * @return the focus widget
     */
    public Widget getFocusWidget() {
        return focusWidget;
    }

    /**
     * Returns the button the was clicked to initiate the hide.
     *
     * @return the hide button or null
     */
    public TextButton getHideButton() {
        return hideButton;
    }

    /**
     * Returns the minimum height.
     *
     * @return the minimum height
     */
    public int getMinHeight() {
        return minHeight;
    }

    /**
     * Returns the minimum width.
     *
     * @return the minimum width
     */
    public int getMinWidth() {
        return minWidth;
    }

    /**
     * Returns the window's resizable instance.
     *
     * @return the resizable
     */
    public Resizable getResizable() {
        if (resizer == null && resizable) {
            resizeHandler = new ResizeHandler();

            resizer = new Resizable(this);
            resizer.setMinWidth(getMinWidth());
            resizer.setMinHeight(getMinHeight());
            resizer.addResizeStartHandler(resizeHandler);
            resizer.addResizeEndHandler(resizeHandler);
        }
        return resizer;
    }

    @Override
    public void hide() {
        hide(null);
    }

    /**
     * Hides the window.
     *
     * @param buttonPressed the button that was pressed or null
     */
    public void hide(TextButton buttonPressed) {
        if (hidden || !fireCancellableEvent(new BeforeHideEvent())) {
            return;
        }

        hideButton = buttonPressed;

        if (dragger != null) {
            dragger.cancelDrag();
        }

        hidden = true;

        if (!maximized) {
            restoreSize = getElement().getSize();
            restorePos = new Point(getElement().getLeft(), getElement().getTop());
            viewportSize = XDOM.getViewportSize();
        }

        if (modalPreview != null) {
            modalPreview.removeHandler();
            modalPreview = null;
        }

        onHide();
        manager.unregister(this);
        if (removeFromParentOnHide) {
            removeFromParent();
        }

        if (modalPanel != null) {
            ModalPanel.push(modalPanel);
            modalPanel = null;
        }

        eventPreview.remove();
        notifyHide();

        if (restoreWindowScrolling != null) {
            Document.get().enableScrolling(restoreWindowScrolling.booleanValue());
        }

        fireEvent(new HideEvent());
        // fireEvent(Events.Hide, new WindowEvent(this, buttonPressed));
    }

    /**
     * Returns true if auto hide is enabled.
     *
     * @return the auto hide state
     */
    public boolean isAutoHide() {
        return autoHide;
    }

    /**
     * Returns true if modal blinking is enabled.
     *
     * @return the blink modal state
     */
    public boolean isBlinkModal() {
        return blinkModal;
    }

    /**
     * Returns true if the window is closable.
     *
     * @return the closable state
     */
    public boolean isClosable() {
        return closable;
    }

    /**
     * Returns true if the panel is draggable.
     *
     * @return the draggable state
     */
    public boolean isDraggable() {
        return draggable;
    }

    /**
     * Returns true if window maximizing is enabled.
     *
     * @return the maximizable state
     */
    public boolean isMaximizable() {
        return maximizable;
    }

    /**
     * Returns true if the window is maximized.
     *
     * @return the plain style state
     */
    public boolean isMaximized() {
        return maximized;
    }

    /**
     * Returns true if window minimizing is enabled.
     *
     * @return the minimizable state
     */
    public boolean isMinimizable() {
        return minimizable;
    }

    /**
     * Returns true if modal behavior is enabled.
     *
     * @return the modal state
     */
    public boolean isModal() {
        return modal;
    }

    /**
     * Returns true if the window is closed when the esc key is pressed.
     *
     * @return the on esc state
     */
    public boolean isOnEsc() {
        return onEsc;
    }

    /**
     * Returns true if window resizing is enabled.
     *
     * @return the resizable state
     */
    public boolean isResizable() {
        return resizable;
    }

    /**
     * Fits the window within its current container and automatically replaces the
     * 'maximize' tool button with the 'restore' tool button.
     */
    public void maximize() {
        if (!maximized) {
            restoreSize = getElement().getSize();
            restorePos = getElement().getPosition(true);
            restoreShadow = getShadow();
            if (container == null) {
                String bodyOverflow = com.google.gwt.dom.client.Document.get().isCSS1Compat() ? Document.get()
                        .getDocumentElement().getStyle().getProperty(OVERFLOW) : Document.get().getBody().getStyle()
                        .getProperty(OVERFLOW);
                if (!HIDDEN.equals(bodyOverflow)) {
                    restoreWindowScrolling = true;
                }
                com.google.gwt.dom.client.Document.get().enableScrolling(false);
            }
            maximized = true;
            addStyleDependentName(MAXIMIZED);
            header.removeStyleName(X_WINDOW_DRAGGABLE);
            if (layer != null) {
                layer.disableShadow();
            }
            boolean cacheSizesRestore = cacheSizes;
            cacheSizes = false;
            fitContainer();
            cacheSizes = cacheSizesRestore;

            if (maximizable) {
                maxBtn.setVisible(false);
                restoreBtn.setVisible(true);
            }
            if (draggable) {
                dragger.setEnabled(false);
            }
            if (resizable) {
                resizer.setEnabled(false);
            }

            fireEvent(new MaximizeEvent());
        } else {
            fitContainer();
        }
    }

    protected void hideMaxRestoreButtons() {

        maxBtn.setVisible(false);
        restoreBtn.setVisible(false);
    }

    protected void showMaxRestoreButtons() {

        if (maximizable) {
            if (maximized) {
                maxBtn.setVisible(false);
                restoreBtn.setVisible(true);
            } else {
                maxBtn.setVisible(true);
                restoreBtn.setVisible(false);
            }
        }
    }

    /**
     * Placeholder method for minimizing the window. By default, this method
     * simply fires the minimize event since the behavior of minimizing a window
     * is application-specific. To implement custom minimize behavior, either the
     * minimize event can be handled or this method can be overridden.
     */
    public void minimize() {
        fireEvent(new MinimizeEvent());
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONMOUSEDOWN:
                // dont bring to front on clicks where active is model as active window
                // may have just been opened from this click event
            	bringToFrontIfNotActive();

                break;
        }
    }

	public void bringToFrontIfNotActive() {
		Widget a = manager.getActive();
		if (a instanceof WindowBase) {
		    WindowBase active = (WindowBase) a;
		    if (active != null && active != this && !active.isModal()) {
		        manager.bringToFront(this);
		    }
		}
	}

    /**
     * Restores a maximized window back to its original size and position prior to
     * being maximized and also replaces the 'restore' tool button with the
     * 'maximize' tool button.
     */
    public void restore() {
        if (maximized) {
            getElement().removeClassName("x-window-maximized"); //$NON-NLS-1$
            if (maximizable) {
                restoreBtn.setVisible(false);
                maxBtn.setVisible(true);
            }
            if (restoreShadow != null && restoreShadow.booleanValue() && layer != null) {
                layer.enableShadow();
                restoreShadow = null;
            }
            if (draggable) {
                dragger.setEnabled(true);
            }
            if (resizable) {
                resizer.setEnabled(true);
            }
            header.addStyleName(X_WINDOW_DRAGGABLE);
            if (restorePos != null) {
                setPosition(restorePos.getX(), restorePos.getY());

                boolean cacheSizesRestore = cacheSizes;
                cacheSizes = false;
                setPixelSize(restoreSize.getWidth(), restoreSize.getHeight());
                cacheSizes = cacheSizesRestore;
            }
            if (container == null && restoreWindowScrolling != null) {
                com.google.gwt.dom.client.Document.get().enableScrolling(restoreWindowScrolling.booleanValue());
                restoreWindowScrolling = null;
            }
            maximized = false;
            fireEvent(new RestoreEvent());
        }
    }

    /**
     * Makes this the active window by showing its shadow, or deactivates it by
     * hiding its shadow. This method also fires the activate or deactivate event
     * depending on which action occurred.
     *
     * @param active true to make the window active
     */
    public void setActive(boolean active) {
        if (active) {
            if (isVisible()) {
                eventPreview.push();
                if (!maximized && layer != null) {
                    if (getShadow()) {
                        layer.enableShadow();
                    }
                    layer.sync(true);
                }
                if (modal && modalPanel == null) {
                    modalPanel = ModalPanel.pop();
                    modalPanel.setBlink(blinkModal);
                    modalPanel.show(this);
                }
            }
            fireEvent(new ActivateEvent<WindowBase>(this));
        } else {
            if (modalPanel != null) {
                ModalPanel.push(modalPanel);
                modalPanel = null;
            }
            hideShadow();
            fireEvent(new DeactivateEvent<WindowBase>(this));
        }
    }

    /**
     * True to hide the window when the user clicks outside of the window's bounds
     * (defaults to false, pre-render).
     *
     * @param autoHide true for auto hide
     */
    public void setAutoHide(boolean autoHide) {
        this.autoHide = autoHide;
    }

    /**
     * True to blink the window when the user clicks outside of the windows bounds
     * (defaults to false). Only applies window model = true.
     *
     * @param blinkModal true to blink
     */
    public void setBlinkModal(boolean blinkModal) {
        this.blinkModal = blinkModal;
        if (modalPanel != null) {
            modalPanel.setBlink(blinkModal);
        }
    }

    /**
     * True to display the 'close' tool button and allow the user to close the
     * window, false to hide the button and disallow closing the window (default
     * to true).
     *
     * @param closable true to enable closing
     */
    public void setClosable(boolean closable) {
        this.closable = closable;
    }

    /**
     * True to constrain the window to the {@link XDOM#getViewportSize()}, false
     * to allow it to fall outside of the Viewport (defaults to true).
     *
     * @param constrain true to constrain, otherwise false
     */
    public void setConstrain(boolean constrain) {
        this.constrain = constrain;
        if (dragger != null) {
            dragger.setConstrainClient(constrain);
        }
    }

    /**
     * Sets the container element to be used to size and position the window when
     * maximized.
     *
     * @param container the container element
     */
    public void setContainer(Element container) {
        this.container = container.<XElement> cast();
    }

    /**
     * True to enable dragging of this Panel (defaults to false).
     *
     * @param draggable the draggable to state
     */
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
        if (draggable) {
            header.addStyleName(X_WINDOW_DRAGGABLE);
            getDraggable();
        } else if (dragger != null) {
            dragger.release();
            dragger = null;
            header.removeStyleName(X_WINDOW_DRAGGABLE);
        }
    }

    /**
     * Widget to be given focus when the window is focused).
     *
     * @param focusWidget the focus widget
     */
    public void setFocusWidget(Widget focusWidget) {
        this.focusWidget = focusWidget;
    }

    /**
     * True to display the 'maximize' tool button and allow the user to maximize
     * the window, false to hide the button and disallow maximizing the window
     * (defaults to false). Note that when a window is maximized, the tool button
     * will automatically change to a 'restore' button with the appropriate
     * behavior already built-in that will restore the window to its previous
     * size.
     *
     * @param maximizable the maximizable state
     */
    public void setMaximizable(boolean maximizable) {
        this.maximizable = maximizable;
    }

    /**
     * The minimum height in pixels allowed for this window (defaults to 100).
     * Only applies when resizable = true.
     *
     * @param minHeight the min height
     */
    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
        if (resizer != null) {
            resizer.setMinHeight(minHeight);
        }
    }

    /**
     * True to display the 'minimize' tool button and allow the user to minimize
     * the window, false to hide the button and disallow minimizing the window
     * (defaults to false). Note that this button provides no implementation --
     * the behavior of minimizing a window is implementation-specific, so the
     * minimize event must be handled and a custom minimize behavior implemented
     * for this option to be useful.
     *
     * @param minimizable true to enabled minimizing
     */
    public void setMinimizable(boolean minimizable) {
        this.minimizable = minimizable;
    }

    /**
     * The minimum width in pixels allowed for this window (defaults to 200). Only
     * applies when resizable = true.
     *
     * @param minWidth the minimum height
     */
    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
        if (resizer != null) {
            resizer.setMinWidth(minWidth);
        }
    }

    /**
     * True to make the window modal and mask everything behind it when displayed,
     * false to display it without restricting access to other UI elements
     * (defaults to false).
     *
     * @param modal true for modal
     */
    public void setModal(boolean modal) {
        this.modal = modal;
    }

    /**
     * True to close the window when the escape key is pressed (defaults to true).
     * Only applies when {@link #setCollapsible(boolean)} is true.
     *
     * @param onEsc true to close window on escape key press
     */
    public void setOnEsc(boolean onEsc) {
        this.onEsc = onEsc;
    }

    @Override
    public void setPagePosition(int x, int y) {
        super.setPagePosition(x, y);
        positioned = true;
    }

    @Override
    public void setPosition(int left, int top) {
        super.setPosition(left, top);
        positioned = true;
    }

    public void setPosition(Style.Position position, int left, int top) {
        super.getElement().getStyle().setPosition(position);
        this.setPosition(left, top);
    }

    public void setPosition(Style.Position position) {
        super.getElement().getStyle().setPosition(position);
    }

    /**
     * True to allow user resizing at each edge and corner of the window, false to
     * disable resizing (defaults to true).
     *
     * @param resizable true to enabled resizing
     */
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
        if (resizable) {
            getResizable();
        } else if (resizer != null) {
            resizer.release();
            resizer = null;
        }
    }

    /**
     * Sets the window messages.
     *
     * @param windowMessages the window messages
     */
    public void setWindowMessages(WindowMessages windowMessages) {
        this.windowMessages = windowMessages;
    }

    /**
     * Sets the z-index for the window. A larger value will cause the window to
     * appear over windows with smaller values.
     *
     * @param zIndex the z-index (stacking order) of the window
     */
    public void setZIndex(int zIndex) {
        getElement().setZIndex(zIndex);
        if (ghost != null) {
            ghost.getElement().setZIndex(zIndex);
        }
        if (modalPanel != null) {
            modalPanel.getElement().setZIndex(zIndex - 9);
        }
    }

    /**
     * Shows the window, rendering it first if necessary, or activates it and
     * brings it to front if hidden.
     */
    @Override
    public void show() {
        if (!hidden || !fireCancellableEvent(new BeforeShowEvent())) {
            return;
        }
        // remove hide style, else layout fails
        removeStyleName(getHideMode().value());
        // addStyleName(HideMode.OFFSETS.value());
        if (!isAttached()) {
            RootPanel.get().add(this);
        }

        getElement().makePositionable(true);
        onShow();
        manager.register(this);

        afterShow();
        notifyShow();
    }

    /**
     * Sends this window to the back of (lower z-index than) any other visible
     * windows.
     */
    public void toBack() {
        manager.sendToBack(this);
    }

    /**
     * Brings this window to the front of any other visible windows.
     */
    public void toFront() {
        manager.bringToFront(this);
    }

    protected void afterShow() {
        hidden = false;

        if (restorePos != null) {
            if (XDOM.getViewportSize().equals(viewportSize)) {
                setPosition(restorePos.getX(), restorePos.getY());
                if (restoreSize != null) {
                    setPixelSize(restoreSize.getWidth(), restoreSize.getHeight());
                }
            } else {
                restorePos = null;
                restoreSize = null;
                viewportSize = null;
                positioned = false;
            }
        }
        if (restoreWindowScrolling != null) {
            com.google.gwt.dom.client.Document.get().enableScrolling(false);
        }

        int h = getOffsetHeight();
        int w = getOffsetWidth();
        boolean autoHeight = isAutoHeight();
        boolean autoWidth = isAutoWidth();

        //TODO assuming there was an issue with old ie - look more into this. IG
//        if ((GXT.isIE6() || GXT.isIE7()) && autoWidth) {
//            setWidth(minWidth);
//        }

        h = getOffsetHeight();

        if (h < minHeight && w < minWidth) {
            setPixelSize(minWidth, minHeight);
        } else if (h < minHeight) {
            setHeight(minHeight);
        } else if (w < minWidth) {
            setWidth(minWidth);
        }

        if (autoHeight) {
            height = null;
        }
        if (autoWidth) {
            width = null;
        }

        // not positioned, then center
        if (!positioned) {
            getElement().center(true);
        }

        getElement().updateZIndex(0);
        if (modal) {
            modalPreview = Event.addNativePreviewHandler(new NativePreviewHandler() {

                public void onPreviewNativeEvent(NativePreviewEvent event) {
                    if (Element.is(event.getNativeEvent().getEventTarget())) {
                        XElement target = event.getNativeEvent().getEventTarget().<XElement> cast();

                        String tag = target.getTagName();
                        // ignore html and body because of frames
                        if (!resizing
                                && !dragging
                                && !tag.equalsIgnoreCase("html") //$NON-NLS-1$
                                && !tag.equalsIgnoreCase("body") //$NON-NLS-1$
                                && event.getTypeInt() != Event.ONLOAD
                                && manager.getActive() == WindowBase.this
                                && (modalPanel == null || (modalPanel != null && !modalPanel.getElement().isOrHasChild(
                                target))) && !WindowBase.this.getElement().isOrHasChild(target)
                                && target.findParent("." + CommonStyles.get().ignore(), -1) == null) { //$NON-NLS-1$
                        }
                    }
                }
            });
        }

        // missing cursor workaround
        if (GXT.isGecko()) {
            XElement e = getElement().selectNode("." + getStylePrimaryName() + "-bwrap"); //$NON-NLS-1$ //$NON-NLS-2$
            if (e != null) {
                e.getStyle().setProperty(OVERFLOW, AUTO);
                e.getStyle().setProperty(POSITION, STATIC);
            }
        }

        eventPreview.add();

        if (maximized) {
            maximize();
        }

        removeStyleName(HideMode.OFFSETS.value());
        fireEvent(new ShowEvent());
        toFront();
    }

    protected Layer createGhost() {
        XElement div = DOM.createDiv().<XElement> cast();
        Layer l = new Layer(div);
        if (shim && GXT.isUseShims()) {
            l.enableShim();
        }
        l.getElement().setClassName(appearance.ghostClass());
        if (header != null) {
            div.appendChild(getElement().getFirstChild().cloneNode(true));
        }
        l.getElement().appendChild(DOM.createElement(UL));
        return l;
    }

    protected void doFocus() {
        if (focusWidget != null) {
            if (focusWidget instanceof Component) {
                ((Component) focusWidget).focus();
            } else {
                focusWidget.getElement().focus();
            }
        } else {
            WindowBase.super.focus();
        }
    }

    protected void fitContainer() {
        if (container != null) {
            Rectangle bounds = container.getBounds();
            setPagePosition(bounds.getX(), bounds.getY());
            setPixelSize(bounds.getWidth(), bounds.getHeight());
        } else {
            setPosition(0, 0);
            setPixelSize(XDOM.getViewportWidth(), XDOM.getViewportHeight());
        }
    }

    protected ModalPanel getModalPanel() {
        return modalPanel;
    }

    /**
     * Returns the window messages. The default implementation provides for
     * translatable messages using a resource file.
     *
     * @return the window messages
     */
    protected WindowMessages getWindowMessages() {
        if (windowMessages == null) {
            windowMessages = new DefaultWindowMessages();
        }
        return windowMessages;
    }

    protected Layer ghost() {
        Layer g = createGhost();
        g.getElement().setVisibility(false);
        Rectangle box = getElement().getBounds(false);
        g.getElement().setBounds(box, true);
        int h = appearance.getBodyWrap(getElement()).getOffsetHeight();
        g.getElement().getChild(1).<XElement> cast().setHeight(h - 1, true);
        return g;
    }

    @Override
    protected void initTools() {
        super.initTools();

        if (minimizable) {
            minBtn = new Button();
            minBtn.setSize(ButtonSize.SMALL);
            minBtn.setIcon(IconType.MINUS);
            minBtn.setTitle(MINIMIZE_TOOLTIP);
            minBtn.addStyleName(Constants.UIConstants.Styles.WINDOW_LAYOUT_HEADER_BUTTON);
            minBtn.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    minimize();
                }

            });
            header.addTool(minBtn);
        }

        if (maximizable) {
            maxBtn = new Button();
            maxBtn.setSize(ButtonSize.SMALL);
            maxBtn.setIcon(IconType.PLUS);
            maxBtn.setTitle(MAXIMIZE_TOOLTIP);
            maxBtn.addStyleName(Constants.UIConstants.Styles.WINDOW_LAYOUT_HEADER_BUTTON);
            maxBtn.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {

                    maximize();
                }
            });
            header.addTool(maxBtn);

            restoreBtn = new Button();
            restoreBtn.setTitle(RESTORE_TOOLTIP);
            restoreBtn.setSize(ButtonSize.SMALL);
            restoreBtn.setIcon(IconType.RESIZE_SMALL);
            restoreBtn.addStyleName(Constants.UIConstants.Styles.WINDOW_LAYOUT_HEADER_BUTTON);
            restoreBtn.setVisible(false);
            restoreBtn.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    restore();
                }
            });
            header.addTool(restoreBtn);
        }

    }

    @Override
    protected void onAfterFirstAttach() {
        super.onAfterFirstAttach();

        setResizable(resizable);

        if (manager == null) {
            manager = WindowBaseManager.get();
        }

        if (modal || maximizable || constrain) {
            monitorWindowResize = true;
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (eventPreview != null) {
            eventPreview.remove();
        }
    }

    protected void onDragCancel(DragCancelEvent event) {
        dragging = false;
        unghost(null);

        if (layer != null && getShadow()) {
            layer.enableShadow();
        }
        focus();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                if (WindowBase.this.eventPreview != null && WindowBase.this.ghost != null) {
                    WindowBase.this.eventPreview.getIgnoreList().remove(WindowBase.this.ghost.getElement());
                }
            }
        });
    }

    protected void onDragEnd(DragEndEvent de) {
        dragging = false;
        unghost(de);

        restorePos = getElement().getPosition(true);
        positioned = true;

        if (layer != null && getShadow()) {
            layer.enableShadow();
        }
        focus();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                if (WindowBase.this.eventPreview != null && WindowBase.this.ghost != null) {
                    WindowBase.this.eventPreview.getIgnoreList().remove(WindowBase.this.ghost.getElement());
                }
            }
        });
    }

    protected void onDragMove(DragMoveEvent de) {

    }

    protected void onDragStart(DragStartEvent de) {
        dragging = true;
        hideShadow();
        ghost = ghost();
        if (eventPreview != null && ghost != null) {
            eventPreview.getIgnoreList().add(ghost.getElement());
        }
        showWindow(false);
        Draggable d = de.getSource();
        d.setProxy(ghost.getElement());
    }

    protected void onEndResize(ResizeEndEvent re) {
        resizing = false;
    }

    protected void onKeyPress(Event we) {
        int keyCode = we.getKeyCode();

        boolean t = getElement().isOrHasChild(we.getEventTarget().<Element> cast());
        boolean key = true;
        if (key && closable && onEsc && keyCode == KeyCodes.KEY_ESCAPE && t) {
            hide();
        }

    }

    protected void onStartResize(ResizeStartEvent re) {
        resizing = true;
    }

    @Override
    protected void onWindowResize(int width, int height) {
        if (isVisible()) {
            if (maximized) {
                fitContainer();
            } else {
                if (constrain) {
                    Point p = getElement().adjustForConstraints(getElement().getPosition(false));
                    setPagePosition(p.getX(), p.getY());
                }
            }
        }
    }

    protected void showWindow(boolean show) {
        if (show) {
            onShow();
        } else {
            onHide();
        }
    }

    protected void unghost(DragEndEvent de) {
        showWindow(true);
        if (de != null) {
            setPagePosition(de.getX(), de.getY());
        }
    }
}
