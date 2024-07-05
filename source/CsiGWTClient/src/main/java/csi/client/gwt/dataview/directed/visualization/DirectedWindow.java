package csi.client.gwt.dataview.directed.visualization;

import java.util.ArrayList;
import java.util.List;

import com.emitrom.lienzo.client.core.util.Console;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.core.client.util.Rectangle;
import com.sencha.gxt.core.client.util.Size;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.event.ActivateEvent;
import com.sencha.gxt.widget.core.client.event.ActivateEvent.ActivateHandler;
import com.sencha.gxt.widget.core.client.event.DeactivateEvent;
import com.sencha.gxt.widget.core.client.event.DeactivateEvent.DeactivateHandler;

import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.viz.shared.chrome.panel.VizPanelFrameProvider;
import csi.client.gwt.widget.ui.ResizeableAbsolutePanel;
import csi.client.gwt.worksheet.layout.window.WindowBase;
import csi.client.gwt.worksheet.layout.window.appearance.VisualizationWindowAppearance;
import csi.client.gwt.worksheet.layout.window.events.ExpandFromMinimizeEvent;
import csi.client.gwt.worksheet.layout.window.events.ExpandFromMinimizeEventHandler;
import csi.shared.core.Constants;

public class DirectedWindow extends WindowBase implements RequiresResize, VizPanelFrameProvider {
    private static final int DRAG_WIDTH = 6;

    private VizPanel visualizationPanel;
    private ResizeableAbsolutePanel visualizationContainer;

    private boolean inFocus = false;
    private boolean fullScreen;
    private List<ToolButton> otherToolButtons = new ArrayList<ToolButton>();

    public DirectedWindow(ResizeableAbsolutePanel panel) {
        super(new VisualizationWindowAppearance());
        init();
        visualizationContainer = panel;
        visualizationContainer.add(this);
        // This is required to get the window to realign in maximized state.
        this.setContainer(visualizationContainer.getElement());

    }

    private void init() {
        setMaximizable(true);
        setMinimizable(false);
        setClosable(false);
        setDraggable(false);
        setShadow(false);
        setMonitorWindowResize(false);
        setConstrain(false);
        setResizable(false);


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
                maximize();
            }
        }, DoubleClickEvent.getType());
    }
    @Override
    public void maximize() {
        visualizationPanel.hideMenu();
        Console.log("HID MENU");
        if (isMaximized()) {
            setZIndex(0);
            restore();
        } else {
            setZIndex(XDOM.getTopZIndex(1));
            super.maximize();
        }
        Console.log("FINISHED MAXIMIZE");
        visualizationPanel.showNameRow();
        Console.log("SHOWING NAME ROW");
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
        
    }


    public VizPanel getVisualizationPanel() {
        return visualizationPanel;
    }

    public void setVisualizationPanel(VizPanel visualizationPanel) {
        this.visualizationPanel = visualizationPanel;
        getHeader().setText(visualizationPanel.getVisualization().getName());
        setWidget(visualizationPanel);
    }


    public HandlerRegistration addExpandFromMinimizeEventHandler(ExpandFromMinimizeEventHandler handler) {
        return addHandler(handler, ExpandFromMinimizeEvent.type);
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
            } else if (isMaximized()) {
                fitContainer();
            }
        }
    }

    @Override
    protected void onWindowResize(int width, int height) {
        // Noop - we use the resize from the container for regular mode (non-maximized or full-screen) because the
        // browser window resize event fires before the container is sized and so the window doesn't size properly.
        // In full-screen mode, having this take effect will cause the negative offsets to be reset to 0.
    }

    public void fullScreenFit() {
        Rectangle bounds = getContainer().<XElement> cast().getBounds();
        setPosition(bounds.getX() - DRAG_WIDTH, bounds.getY() - DRAG_WIDTH);
        setPixelSize(bounds.getWidth() + 2 * DRAG_WIDTH, bounds.getHeight() + 2 * DRAG_WIDTH);
    }

    @Override
    public void setName(String name) {
        getHeader().setText(name);
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


    public void ensureVisibleHeader() {
        Point p = getElement().getPosition(true);
        Size z = getElement().getSize();
        if (p.getY() < 0 || p.getY() > getContainer().getOffsetHeight() || p.getX() < -z.getWidth()
                || p.getX() > getContainer().getOffsetWidth()) {
            center();
        }
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

    public boolean isFullScreen() {
        return fullScreen;
    }
}
