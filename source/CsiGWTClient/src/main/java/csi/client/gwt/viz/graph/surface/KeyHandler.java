package csi.client.gwt.viz.graph.surface;

import com.google.gwt.event.dom.client.*;
import csi.client.gwt.viz.graph.surface.GraphSurface.ClickMode;
import csi.client.gwt.viz.graph.surface.GraphSurface.DragMode;
import csi.client.gwt.viz.graph.surface.GraphSurface.Presenter;

class KeyHandler extends HandlesAllKeyEvents {
    private boolean ctrlPressed;
    private boolean shiftPressed;
    private GraphSurface graphSurface;

    KeyHandler(GraphSurface graphSurface) {
        this.graphSurface = graphSurface;
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        int keyCode = event.getNativeKeyCode();
        switch (keyCode) {
            case KeyCodes.KEY_CTRL:
                ctrlPressed = true;
                updateModes();
                break;
            case KeyCodes.KEY_SHIFT:
                shiftPressed = true;
                updateModes();
                break;
            case KeyCodes.KEY_ESCAPE:
                graphSurface.show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {
        // empty
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
        int keyCode = event.getNativeKeyCode();
        switch (keyCode) {
            case KeyCodes.KEY_CTRL:
                ctrlPressed = false;
                updateModes();
                break;
            case KeyCodes.KEY_SHIFT:
                shiftPressed = false;
                updateModes();
                break;
            default:
                break;
        }
    }

    void updateModes() {
        setClickMode();
        setDragMode();
        // the cursor may need to change
        Presenter presenter = graphSurface.getPresenter();
        if (presenter != null) {
            presenter.updateCursor();
        }
    }

    private void setClickMode() {
        if (ctrlPressed) {
            graphSurface.setClickMode(ClickMode.SELECT);
        } else {
            graphSurface.setClickMode(ClickMode.NONE);
        }
    }

    private void setDragMode() {
        if (ctrlPressed && shiftPressed) {
            graphSurface.setDragMode(DragMode.DESELECT);
            return;
        }
        if (shiftPressed) {
            graphSurface.setDragMode(DragMode.ZOOM);
            return;
        }
        if (ctrlPressed) {
            graphSurface.setDragMode(DragMode.SELECT);
            return;
        }
        graphSurface.setDragMode(DragMode.DEFAULT);
    }

    void setShiftPressed(boolean shiftPressed) {
        this.shiftPressed = shiftPressed;
    }

    void setCtrlPressed(boolean ctrlPressed) {
        this.ctrlPressed = ctrlPressed;
    }
}
