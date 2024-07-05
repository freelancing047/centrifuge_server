package csi.client.gwt.viz.map.overview;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import csi.client.gwt.viz.timeline.scheduler.CancelRepeatingCommand;

class OverviewMouseHandler extends HandlesAllMouseEvents {
    private OverviewPresenter overviewPresenter;
    private int mouseDownLocation;
    private int mouseX;
    private int mouseY;
    private int originalX;
    private int originalY;
    private boolean mousePressed;
    private boolean dragging;


    public OverviewMouseHandler(OverviewPresenter overviewPresenter) {
        this.overviewPresenter = overviewPresenter;
    }

    @Override
    public void onMouseWheel(MouseWheelEvent mouseWheelEvent) {
            if (overviewPresenter.rangeChangedEvent != null) {
                if (!overviewPresenter.rangeChangedEvent.isCancelled() && overviewPresenter.getPositionDifference() <= 2) {
                    return;
                }
                overviewPresenter.rangeChangedEvent.cancel();
            }
            int deltaY = getDeltaY(mouseWheelEvent);
            if (deltaY < 0) {
                overviewPresenter.zoomIn();
            } else if(deltaY>0) {
                overviewPresenter.zoomOut();
            }
    }

    @Override
    public void onMouseUp(MouseUpEvent mouseUpEvent) {
        mousePressed = false;
        if (dragging) {
            overviewPresenter.stopDragging();
            overviewPresenter.stopDragging();
            dragging = false;
        }
        else if (mouseUpEvent.isControlKeyDown() && overviewPresenter.getDragState()==DragState.NOT_DRAGGING) {
            overviewPresenter.selectFromOverview(mouseUpEvent.getRelativeX(overviewPresenter.getOverviewView().getEventReferenceWidget().getElement()),mouseUpEvent.isShiftKeyDown());
            return;
        }
        else {
            overviewPresenter.handleClick(mouseUpEvent.getRelativeX(overviewPresenter.getOverviewView().getEventReferenceWidget().getElement()));
        }
    }

    @Override
    public void onMouseOver(MouseOverEvent mouseOverEvent) {

    }

    @Override
    public void onMouseOut(MouseOutEvent mouseOutEvent) {
        if (overviewPresenter.getCommand() != null) {
            overviewPresenter.getCommand().cancel();
        }

        if (overviewPresenter.getDragState() == DragState.NOT_DRAGGING) {
            overviewPresenter.getOverviewView().removeHighlights();
        }

        overviewPresenter.setCommand(new CancelRepeatingCommand() {

            @Override
            public boolean execute() {
                if (!isCancel())
                    overviewPresenter.resetOverviewFromDrag();
                return false;
            }
        });

        Scheduler.get().scheduleFixedDelay(overviewPresenter.getCommand(), OverviewPresenter.DRAG_TIMEOUT);
    }


    @Override
    public void onMouseMove(MouseMoveEvent event) {
        final int oldX = mouseX;
        event.preventDefault();
        final int oldY = mouseY;
        mouseX = event.getX();
        mouseY = event.getY();

        if (!dragging) {// should not update cursor during dragging
            // updating cursor is complex logic. so it is handled by presenter
            if ((mouseX != originalX || mouseY != originalY) && mousePressed) {
                dragging = true;
                overviewPresenter.adjustDragState(event.getRelativeX(overviewPresenter.getOverviewView().getEventReferenceWidget().getElement()));
            }
            else{
                overviewPresenter.calculateDragState(event.getRelativeX(overviewPresenter.getOverviewView().getEventReferenceWidget().getElement()));
            }
        } else {
            // drag behavior is complex logic. so it is handled by presenter
            if (mouseX - oldX != 0) {
                overviewPresenter.drag((mouseX - oldX));
            }
        }
        overviewPresenter.highlightDraggable(event.getRelativeX(overviewPresenter.getOverviewView().getEventReferenceWidget().getElement()));
    }



    @Override
    public void onMouseDown(MouseDownEvent mouseDownEvent) {
        mouseDownEvent.preventDefault();
        if (mouseDownEvent.getNativeButton() == NativeEvent.BUTTON_LEFT) {

            mousePressed = true;
            originalX = mouseDownEvent.getX();
            originalY = mouseDownEvent.getY();
        }
    }




    int getDeltaY(MouseWheelEvent event) {
        int deltaY = event.getDeltaY();
        if (deltaY == 0)
            deltaY = internetExplorerWorkaroundForMouseWheel(event.getNativeEvent());
        return deltaY;
    }

    private static native int internetExplorerWorkaroundForMouseWheel(NativeEvent evt) /*-{
        if (typeof evt.wheelDelta == "undefined") {
            return 0;
        }
        return Math.round(-evt.wheelDelta / 40) || 0;
    }-*/;

}
