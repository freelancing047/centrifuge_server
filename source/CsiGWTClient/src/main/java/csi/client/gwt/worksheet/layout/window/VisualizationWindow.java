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
package csi.client.gwt.worksheet.layout.window;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.core.client.util.Rectangle;
import com.sencha.gxt.core.client.util.Size;
import com.sencha.gxt.fx.client.DragEndEvent;
import com.sencha.gxt.fx.client.DragStartEvent;
import com.sencha.gxt.fx.client.Draggable;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.event.*;
import com.sencha.gxt.widget.core.client.event.ActivateEvent.ActivateHandler;
import com.sencha.gxt.widget.core.client.event.DeactivateEvent.DeactivateHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.ApplicationBanner;
import csi.client.gwt.mainapp.SecurityBanner;
import csi.client.gwt.util.GenericCallback;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.viz.shared.chrome.panel.VizPanelFrameProvider;
import csi.client.gwt.widget.ui.ResizeableAbsolutePanel;
import csi.client.gwt.worksheet.layout.window.appearance.VisualizationWindowAppearance;
import csi.client.gwt.worksheet.layout.window.events.ExpandFromMinimizeEvent;
import csi.client.gwt.worksheet.layout.window.events.ExpandFromMinimizeEventHandler;
import csi.shared.core.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class VisualizationWindow extends WindowBase implements RequiresResize, VizPanelFrameProvider {


    public static final int OFF_SCREEN_PROTECTION = 10000;

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final String RESTORE_TOOLTIP = i18n.visualizationWindowRestoreTooltip(); //$NON-NLS-1$
    private final String FULLSCREEN_TOOLTIP = i18n.visualizationWindowFullscreenTooltip(); //$NON-NLS-1$
    private static final int DIM_MINIMIZED = 20;
    private static final int DRAG_WIDTH = 6;

    private static final int MAX_HEIGHT = 4000;
    private static final int MAX_WIDTH = 4000;
    private static final int MIN_HEIGHT = 300;
    private static final int MIN_WIDTH = 370;

    private VizPanel visualizationPanel;
    private ResizeableAbsolutePanel visualizationContainer;

    private boolean minimized;
    private int minimizedYPosition;

    private boolean fullScreen;
    private List<ToolButton> otherToolButtons = new ArrayList<ToolButton>();
    private List<ToolButton> otherVisibleToolButtons = new ArrayList<ToolButton>();
    private Button fullScreenButton, restoreFullScreenButton;
    private FlowPanel fullScreenContainer;
    private HandlerRegistration browserWindowResizeHandler;
    //private FullScreenAppearance fullScreenAppearance = new FullScreenAppearance();

    private boolean fullScreenRestoreMaximizedState;
    private Point fullScreenRestorePos, minimizeRestorePos;
    private Size fullScreenRestoreSize, minimizeRestoreSize;

    private GenericCallback<Void, Void> minimizeBeforeCallback, minimizeAfterCallback;
    private boolean inFocus = false;
    private final Draggable d;

    @Override
    protected void onDragStart(DragStartEvent de) {
        if(isFullScreen()) {
            return;
        } else {
            super.onDragStart(de);
            getVisualizationPanel().hideMenu();
        }
    }

    @Override
    protected void onDragEnd(DragEndEvent de) {
        if (isFullScreen()) {
            return;
        } else {
            super.onDragEnd(de);
            getVisualizationPanel().showMenu();
        }
    }

    public VisualizationWindow(ResizeableAbsolutePanel panel) {
        super(new VisualizationWindowAppearance());
        init();
        visualizationContainer = panel;
        panel.add(this);
        // This is required to get the window to realign in maximized state.
        this.setContainer(panel.getElement());

        d = getDraggable();
//        Draggable d = getDraggable(visualizationPanel.getNavBar());
        d.setUseProxy(true);
        d.setMoveAfterProxyDrag(false);
        d.setContainer(panel);
    }

    private void init() {
        setMaximizable(true);
        setMinimizable(true);
        setClosable(false);
        setDraggable(true);
        setShadow(false);
        setMonitorWindowResize(false);
        setConstrain(false);
        getResizable().setMaxHeight(MAX_HEIGHT);
        getResizable().setMinHeight(MIN_HEIGHT);
        getResizable().setMaxWidth(MAX_WIDTH);
        getResizable().setMinWidth(MIN_WIDTH);


        addActivateHandler(new ActivateHandler<WindowBase>() {

            @Override
            public void onActivate(ActivateEvent<WindowBase> event) {
                getHeader().addStyleName(Constants.UIConstants.Styles.WINDOW_LAYOUT_ACTIVATED);
            }
        });
        addDeactivateHandler(new DeactivateHandler<WindowBase>() {

            @Override
            public void onDeactivate(DeactivateEvent<WindowBase> event) {
                getHeader().removeStyleName(Constants.UIConstants.Styles.WINDOW_LAYOUT_ACTIVATED);
            }
        });

        getHeader().addDomHandler(new DoubleClickHandler() {

            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                handleDoubleClick();
            }
        }, DoubleClickEvent.getType());
    }

    private void handleDoubleClick() {
        visualizationPanel.hideMenu();
        if(isFullScreen()){
            restoreFullScreen(); // or just return?
        } else if (isMaximized()) {
            restore();
        } else {
            maximize();
        }
    }

    @Override
    protected void initTools() {
        super.initTools();
        // Capture references to minimize and maximize buttons (those are private and otherwise inaccessible)
        for (Widget widget : getHeader().getTools()) {
            if (widget instanceof ToolButton) {
                otherToolButtons.add((ToolButton) widget);
            }
        }
//        fullScreenButton = fullScreenAppearance.getFullScreenButton();
//        restoreFullScreenButton = fullScreenAppearance.getRestoreFullScreenButton();
        fullScreenButton = new Button();
        fullScreenButton.setSize(ButtonSize.SMALL);
        fullScreenButton.setIcon(IconType.RESIZE_FULL);
        fullScreenButton.setTitle(FULLSCREEN_TOOLTIP);
        fullScreenButton.addStyleName(Constants.UIConstants.Styles.WINDOW_LAYOUT_HEADER_BUTTON);
        getHeader().addTool(fullScreenButton);
        restoreFullScreenButton = new Button();
        restoreFullScreenButton.setSize(ButtonSize.SMALL);
        restoreFullScreenButton.setIcon(IconType.RESIZE_SMALL);
        restoreFullScreenButton.setTitle(RESTORE_TOOLTIP);
        restoreFullScreenButton.addStyleName(Constants.UIConstants.Styles.WINDOW_LAYOUT_HEADER_BUTTON);
        getHeader().addTool(restoreFullScreenButton);
        restoreFullScreenButton.setVisible(false);

        fullScreenButton.addClickHandler(event -> fullScreen());
        restoreFullScreenButton.addClickHandler(event -> restoreFullScreen());
//        fullScreenButton.addSelectHandler(new SelectHandler() {
//
//            @Override
//            public void onSelect(SelectEvent event) {
//                fullScreen();
//            }
//        });
//        restoreFullScreenButton.addSelectHandler(new SelectHandler() {
//
//            @Override
//            public void onSelect(SelectEvent event) {
//                restoreFullScreen();
//            }
//        });
    }

    public void restoreFullScreen() {
        visualizationPanel.hideMenu();
        if (fullScreen) {

            fullScreen = false;
            browserWindowResizeHandler.removeHandler();

            VisualizationWindow.this.removeFromParent();
            visualizationContainer.setVisible(true);
            this.setContainer(visualizationContainer.getElement());
            visualizationContainer.add(this);
            fitContainer();
            if (fullScreenRestoreMaximizedState) {
                maximize();
            } else {
                if(fullScreenRestorePos != null) {
                    setPosition(Style.Position.ABSOLUTE, fullScreenRestorePos.getX(), fullScreenRestorePos.getY());
                }
                if(fullScreenRestoreSize != null){
                    setPixelSize(fullScreenRestoreSize.getWidth(), fullScreenRestoreSize.getHeight());
                }
                d.setEnabled(true);
                getDraggable(visualizationPanel.getNavBar()).setEnabled(true);
                getResizable().setEnabled(true);
                fullScreenRestorePos = null;
            }
            fullScreenButton.setVisible(true);
            restoreFullScreenButton.setVisible(false);
            showMaxRestoreButtons();

            if (null != fullScreenContainer) {
                fullScreenContainer.removeFromParent();
                fullScreenContainer = null;
            }
            for (ToolButton button : otherVisibleToolButtons) {
                button.setVisible(true);
            }
            otherVisibleToolButtons.clear();

            VizPanel panel = getVisualizationPanel();

            if(panel != null) {
                getVisualizationPanel().enablePopoutButton(true);
            }
        }
        visualizationPanel.showNameRow();
    }

    private void fullScreen() {

        if (!fullScreen) {

            fullScreen = true;
            visualizationPanel.getVizName().getElement().getStyle().setLineHeight(16, Style.Unit.PX);

            // Capture current configuration
            fullScreenRestoreMaximizedState = isMaximized();
            if (!isMaximized()) {
                fullScreenRestorePos = getElement().getPosition(true);
                fullScreenRestoreSize = getElement().getSize();
            }
            fullScreenContainer = new DivWidget(Constants.UIConstants.Styles.WINDOW_FULL_SCREEN_CONTAINER);
            fullScreenContainer.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
            RootPanel.get().add(fullScreenContainer);

            removeFromParent();
            fullScreenContainer.add(this);
            setContainer(fullScreenContainer.getElement());
            fullScreenFit();
            browserWindowResizeHandler = com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {

                @Override
                public void onResize(ResizeEvent event) {
                    VisualizationWindow.this.onResize();
                }
            });
            fullScreenButton.setVisible(false);
            restoreFullScreenButton.setVisible(true);
            hideMaxRestoreButtons();

            for (ToolButton button : otherToolButtons) {
                if (button.isVisible()) {
                    button.setVisible(false);
                    otherVisibleToolButtons.add(button);
                }
            }

            visualizationContainer.setVisible(false);

            VizPanel panel = getVisualizationPanel();
            if(panel != null) {
                getVisualizationPanel().enablePopoutButton(false);
            }
            visualizationPanel.showMenu();
            visualizationPanel.getNavBar().getElement().getStyle().setTop(0, Style.Unit.PX);
        }
    }
    public VizPanel getVisualizationPanel() {
        return visualizationPanel;
    }

    public void setVisualizationPanel(VizPanel visualizationPanel) {
        this.visualizationPanel = visualizationPanel;
        setWidget(visualizationPanel);
        Draggable draggable = getDraggable(visualizationPanel.getNavBar());
        draggable.setContainer(visualizationContainer);

        visualizationPanel.getNavBar().addBitlessDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                handleDoubleClick();
            }
        }, DoubleClickEvent.getType());
    }

    public void setMinimizedYPosition(int minimizedYPosition) {
        this.minimizedYPosition = minimizedYPosition;
    }

    public void setMinimizeBeforeCallback(GenericCallback<Void, Void> minimizeBeforeCallback) {
        this.minimizeBeforeCallback = minimizeBeforeCallback;
    }

    public void setMinimizeAfterCallback(GenericCallback<Void, Void> minimizeAfterCallback) {
        this.minimizeAfterCallback = minimizeAfterCallback;
    }

    public HandlerRegistration addExpandFromMinimizeEventHandler(ExpandFromMinimizeEventHandler handler) {
        return addHandler(handler, ExpandFromMinimizeEvent.type);
    }

    /**
     * The window in "minimized" state actually is of the same size as its last state but off-screen. The animation 
     * is made to pop-out as if its size is increasing but the visualization is detached during the animation so as to
     * not kill the system with visualization resize calls.
     */
    public void expandFromMinimize(boolean animated) {
        setMinimized(false);
        visualizationPanel.hideMenu();
        visualizationPanel.removeFromParent();

        // NOTE: In the init method, the panel is restored at the end of the expand operation.
        final Point current = getElement().getPosition(true);

        current.setX(current.getX()+OFF_SCREEN_PROTECTION);
        current.setY(current.getY()+OFF_SCREEN_PROTECTION);
        if (animated) {
            Animation animation = new Animation() {

                @Override
                protected void onUpdate(double progress) {
                    int newX = current.getX() + (int) ((minimizeRestorePos.getX() - current.getX()) * progress);
                    int newY = current.getY() + (int) ((minimizeRestorePos.getY() - current.getY()) * progress);
                    setPosition(newX, newY);
                    int newWidth = DIM_MINIMIZED + (int) ((minimizeRestoreSize.getWidth() - DIM_MINIMIZED) * progress);
                    int newHeight = DIM_MINIMIZED
                            + (int) ((minimizeRestoreSize.getHeight() - DIM_MINIMIZED) * progress);
                    setPixelSize(newWidth, newHeight);
                }

                @Override
                protected void onComplete() {
                    super.onComplete();
                    fireEvent(new ExpandFromMinimizeEvent());
                    setWidget(visualizationPanel);
                    visualizationPanel.showNameRow();
                }
            };
            animation.run(300);
        } else {
            setPosition(Style.Position.ABSOLUTE, minimizeRestorePos.getX(), minimizeRestorePos.getY());
            setPixelSize(minimizeRestoreSize.getWidth(), minimizeRestoreSize.getHeight());
            fireEvent(new ExpandFromMinimizeEvent());
            setWidget(visualizationPanel);
        }

    }

    @Override
    /**
     * This is being overridden because the computation of the "body" of the window tries to account for the frame size
     * and uses the frame size for the default "appearance." Our custom appearance has different dimensions and thus we
     * need to adjust for it.
     * @return
     */
    protected Size getFrameSize() {
        return new Size(14, 14);
    }

    @Override
    public void onResize() {
        if (isVisible()) {
            if (fullScreen) {
                fullScreenFit();
            } else if (isMinimized()) {
                minimize();
            } else if (isMaximized()) {
                fitContainer();
            }
        }
    }

    @Override
    public void onStartResize(ResizeStartEvent re) {
        visualizationPanel.hideMenu();
        super.onStartResize(re);
    }

    @Override
    public void onEndResize(ResizeEndEvent re) {
        super.onEndResize(re);
        visualizationPanel.showNameRow();
    }
    @Override
    protected void onWindowResize(int width, int height) {
        // Noop - we use the resize from the container for regular mode (non-maximized or full-screen) because the
        // browser window resize event fires before the container is sized and so the window doesn't size properly.
        // In full-screen mode, having this take effect will cause the negative offsets to be reset to 0.
    }

    @Override
    public void fitContainer() {
        minimized = false;
        visualizationPanel.hideMenu();
        super.fitContainer();
        visualizationPanel.showNameRow();
    }

    public void fullScreenFit() {
        Rectangle bounds = getContainer().<XElement> cast().getBounds();
        setPosition(bounds.getX() - DRAG_WIDTH, bounds.getY() - DRAG_WIDTH + SecurityBanner.getHeight() + ApplicationBanner.getFullScreenHeight());
        setPixelSize(bounds.getWidth() + 2 * DRAG_WIDTH, bounds.getHeight() + 2 * (DRAG_WIDTH - SecurityBanner.getHeight() + ApplicationBanner.getFullScreenHeight()));
    }

    @Override
    public void setName(String name) {
        getHeader().setText(name);
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
        event.stopPropagation();
    }

    @Override
    @UiChild
    public void addButton(Widget widget) {
        getHeader().insertTool(widget, 0);
    }

    @Override
    public boolean isInFocus() {
        return inFocus;
    }

    @Override
    public void setInFocus(boolean isFocused) {
        inFocus = isFocused;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    public void setMinimizeRestorePos(Point minimizeRestorePos) {
        this.minimizeRestorePos = minimizeRestorePos;
    }

    public void setMinimizeRestoreSize(Size minimizeRestoreSize) {
        this.minimizeRestoreSize = minimizeRestoreSize;
    }

    @Override
    public void minimize() {
        // We don't want to fire the minimize event before the window has reached its minimized state. This
        // method is called immediately on clicking the minimize button and the super-class implementation fires the
        // MinimizeEvent. So we override this and call the fire event after completion of minimization animation.
        if (minimizeBeforeCallback != null) {
            minimizeBeforeCallback.onCallback(null);
        }
        final int dx = 0 - getElement().getSize().getWidth() - 10;
        minimize(dx, minimizedYPosition);
    }

    /**
     * @param dx The destination x coordinate to end on
     * @param dy The destination y coordinate to end on
     */
    private void minimize(final int dx, final int dy) {

        //If the window is fullscreen, and we attempt to minimize, this will leave an overlay on the Root,
        //merely removing the overlay doesn't appear to be enough. Forcing an exit from fullScreen in the short-term.
        visualizationPanel.hideMenu();
        if(fullScreen) {
            restoreFullScreen();
        }

        // Temporarily remove the visualization from the parent so that it is not deluged with resize requests.
        visualizationPanel.removeFromParent();

        if (!minimized) {
            minimizeRestorePos = getElement().getPosition(true);
            minimizeRestoreSize = getElement().getSize();
        }

        Animation animation = new Animation() {

            @Override
            protected void onUpdate(double progress) {
                int newX = minimizeRestorePos.getX() + (int) ((dx - minimizeRestorePos.getX()) * progress);
                int newY = minimizeRestorePos.getY() + (int) ((dy - minimizeRestorePos.getY()) * progress);
                setPosition(newX, newY);
                int newWidth = DIM_MINIMIZED + (int) ((DIM_MINIMIZED - minimizeRestoreSize.getWidth()) * progress);
                int newHeight = DIM_MINIMIZED + (int) ((DIM_MINIMIZED - minimizeRestoreSize.getHeight()) * progress);
                setPixelSize(newWidth, newHeight);
            }

            @Override
            protected void onComplete() {
                if (minimizeAfterCallback != null) {
                    minimizeAfterCallback.onCallback(null);
                }
                fireEvent(new MinimizeEvent());
                // restore size (now that viz is off screen).
                setPosition(dx - OFF_SCREEN_PROTECTION, dy - OFF_SCREEN_PROTECTION);
                setPixelSize(minimizeRestoreSize.getWidth(), minimizeRestoreSize.getHeight());
                setWidget(visualizationPanel);
            }
        };
        if (!minimized) {
            animation.run(300);
        } else if (minimized) {
            animation.run(0);
        }

        setMinimized(true);
    }

    @Override
    public void restore() {
        // In the case where on first load, a window is maximized, the restorePos and size are null. Pressing the
        // restore button will leave the window at the same size. Since we can't directly affect the restorePos &
        // restoreSize (they are private members), we go about figuring the situation indirectly.
        visualizationPanel.hideMenu();
        boolean tempFullScreen = fullScreen;
        if(fullScreen){
            //Must exit fullScreen first, because VisualizationWindow is in a different dom node currently.
            restoreFullScreen();
        }
        Point p1 = getElement().getPosition(true);
        Size s1 = getElement().getSize();
        super.restore();
        setPosition(Style.Position.ABSOLUTE);
        Point p2 = getElement().getPosition(true);
        Size s2 = getElement().getSize();

        if(!tempFullScreen && fullScreenRestorePos != null){
            // First check if we are restoring from a maximized state that was called after fullscreen,
            // Therefore the maximized state in WindowBase has the incorrect restore values
            setPosition(Style.Position.ABSOLUTE, fullScreenRestorePos.getX(), fullScreenRestorePos.getY());
            setPixelSize(fullScreenRestoreSize.getWidth(), fullScreenRestoreSize.getHeight());
            getDraggable().setEnabled(true);
            getResizable().setEnabled(true);
            //Go back to relying on regular restore
            fullScreenRestorePos = null;
        } else if (tempFullScreen){
            //if restoring from a maximized state, that is also a fullscreen, we must stay in full screen.
            //Record previous fullScreenStates
            Point tempRestorePos = fullScreenRestorePos;
            Size tempRestoreSize = fullScreenRestoreSize;
            //This overwrites the restore state, so must use temps as new settings
            fullScreen();
            fullScreenRestorePos = tempRestorePos;
            fullScreenRestoreSize = tempRestoreSize;
        } else if (p1.equals(p2) && s1.equals(s2)) {
            // Restore didn't have any effect. Call center.
            center();
            fireEvent(new ResizeEndEvent(this, null));
        } else {
            // A third case is where the window has a width and height greater than the current viewport. In that case
            // we center it as well.
            if (s2.getWidth() + 40 > com.google.gwt.user.client.Window.getClientWidth()
                    && s2.getHeight() + 40 > com.google.gwt.user.client.Window.getClientHeight()) {
                center();
                fireEvent(new ResizeEndEvent(this, null));
            }
        }
        visualizationPanel.showNameRow();
    }

    public void ensureVisibleHeader() {
        Point p = getElement().getPosition(true);
        Size z = getElement().getSize();
        if (p.getY() < 0 || p.getY() > getContainer().getOffsetHeight() || p.getX() < -z.getWidth()
                || p.getX() > getContainer().getOffsetWidth()) {
            center();
        }
    }

    @Override
    public void maximize(){
        //If this is not set, the restoreFullScreen() does  not take it into account
        //when exiting fullscreen.
        visualizationPanel.hideMenu();
        fullScreenRestoreMaximizedState = true;
        super.maximize();
        visualizationPanel.showNameRow();

    }

    @Override
    public void center() {
        setPixelSize(getContainer().getOffsetWidth() / 2, getContainer().getOffsetHeight() / 2);
        super.center();
    }

    /**
     * @param fireEvent true to fire the restore event, false to disable the event.
     */
    public void restore(boolean fireEvent) {
        if (!fireEvent) {
            disableEvents();
        }
        if (isMaximized()) {
            restore();
        }
        if (!fireEvent) {
            enableEvents();
        }
    }

    public int getHeight(){
        try{
            return Integer.parseInt(this.height.replaceAll("px", ""));
        } catch(Exception e){
            return MIN_HEIGHT;
        }
    }

    public int getWidth(){
        try{
            return Integer.parseInt(this.width.replaceAll("px", ""));
        } catch(Exception e){
            return MIN_WIDTH;
        }
    }

    @Override
    public void setPixelSize(int width, int height) {
        // TODO Auto-generated method stub
        super.setPixelSize(width, height);
    }

    public boolean isFullScreen() {
        return fullScreen;
    }
}
