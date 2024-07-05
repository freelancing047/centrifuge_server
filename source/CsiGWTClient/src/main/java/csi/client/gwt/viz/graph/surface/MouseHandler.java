package csi.client.gwt.viz.graph.surface;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Timer;
import csi.client.gwt.WebMain;

// this is part of the view, keeps state of the mouse
public class MouseHandler extends HandlesAllMouseEvents implements ClickHandler, DoubleClickHandler {
    private static final int HOVER_DELAY = WebMain.getClientStartupInfo().getGraphAdvConfig().getTooltips().getDelay_ms();
    private Timer hoverTimer;
    private boolean dragging = false;
    private GraphSurface graphSurface;
    private boolean mousePressed = false;
    private int mouseX;
    private int mouseY;
    private GraphSurface.Presenter NULL_PRESENTER = new AbstractGraphSurfaceActivity() {
    };
    private GraphSurface.Presenter presenter = NULL_PRESENTER;
    private int originalX;
    private int originalY;

    public MouseHandler(GraphSurface graphSurface) {
        this.graphSurface = graphSurface;
    }

    private static native int getDeltaY(NativeEvent event)/*-{
        return event.wheelDelta;
    }-*/;

    @Override
    public void onClick(ClickEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            int x = event.getX();
            int y = event.getY();
            presenter.clickHere(x, y);
        }
    }

    @Override
    public void onDoubleClick(DoubleClickEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            int x = event.getX();
            int y = event.getY();
            presenter.doubleClickHere(x, y);
        }
    }

    @Override
    public void onMouseDown(final MouseDownEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            event.preventDefault();
            mousePressed = true;

            originalX = event.getX();
            originalY = event.getY();
        }
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event) {
        final int oldX = mouseX;
        final int oldY = mouseY;
        mouseX = event.getX();
        mouseY = event.getY();
        if (dragging) {
            // drag behavior is complex logic. so it is handled by presenter
            presenter.drag((mouseX - oldX), (mouseY - oldY));
        } else {// should not update cursor during dragging
            // updating cursor is complex logic. so it is handled by presenter
            if ((mouseX != originalX || mouseY != originalY) && mousePressed) {
                presenter.startDrag(mouseX, mouseY);
                dragging = true;
            } else if (!mousePressed) {
                presenter.updateCursor();
                if (hoverTimer == null) {
                    hoverTimer = new HoverTimer();
                }
                if (oldX != mouseX || oldY != mouseY) {
                    hoverTimer.schedule(HOVER_DELAY);
                }
            }
        }
        boolean ctrl = event.getNativeEvent().getCtrlKey();
        boolean shift = event.getNativeEvent().getShiftKey();
        determineKeyPressed(ctrl, shift);
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        boolean ctrl = event.getNativeEvent().getCtrlKey();
        boolean shift = event.getNativeEvent().getShiftKey();
        determineKeyPressed(ctrl, shift);
        if (hoverTimer != null) {
            hoverTimer.cancel();
        }
    }

    @Override
    public void onMouseOver(MouseOverEvent event) {
        boolean ctrl = event.getNativeEvent().getCtrlKey();
        boolean shift = event.getNativeEvent().getShiftKey();
        determineKeyPressed(ctrl, shift);
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            if (dragging) {
                presenter.stopDrag(mouseX, mouseY);
            }
            mousePressed = false;
            dragging = false;
        }
    }

    @Override
    public void onMouseWheel(MouseWheelEvent event) {
        int deltaY = event.getDeltaY();
        if (deltaY == 0) {
            deltaY = -getDeltaY(event.getNativeEvent());
        }
        if (deltaY > 0) {
            presenter.zoomOut();
        } else if (deltaY < 0) {
            presenter.zoomIn();
        }
    }

    private void determineKeyPressed(boolean ctrl, boolean shift) {
        KeyHandler keyHandler = graphSurface.getKeyHandler();
        keyHandler.setCtrlPressed(ctrl);
        keyHandler.setShiftPressed(shift);
        keyHandler.updateModes();
    }

    public void setPresenter(GraphSurface.Presenter presenter) {
        if (presenter == null) {
            this.presenter = NULL_PRESENTER;
        } else {
            this.presenter = presenter;
        }
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    private class HoverTimer extends Timer {
        @Override
        public void run() {
            presenter.hoverHere(mouseX, mouseY);
            cancel();
        }
    }
}