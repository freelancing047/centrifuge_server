package csi.client.gwt.viz.chart.overview;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.*;
import csi.client.gwt.viz.chart.overview.range.*;
import csi.client.gwt.viz.chart.overview.view.OverviewView;
import csi.client.gwt.viz.chart.overview.view.OverviewViewWidget;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.timeline.scheduler.CancelRepeatingCommand;

import java.util.Date;
import java.util.List;


/**
 * Logic for zoom, pan, center, and resize of the overview component.
 * Maps the overview state to categories.
 *
 * @author Centrifuge Systems, Inc.
 */
public class OverviewPresenter {

    //The width refers to the slider width, not necessarily the view width.
    public static final int DEFAULT_OVERVIEW_WIDTH = 600;

    private static final int RANGED_CHANGED_ZOOM_HANDLER_DELAY_MILLIS = 300;

    private static final int DRAG_TIMEOUT = 5000;
    private static final int RANGE_CHANGED_MAX_DELAY_MILLIS = 200;
    private static final int NOT_DRAGGING = -1;
    public Range categoryRange = Range.EMPTY_RANGE;
    DebouncedRangeChangedEvent rangeChangedEvent = null;
    private OverviewState overviewState;
    private OverviewView overviewView;
    private ChartPresenter chartPresenter;
    private int totalCategoryCount;
    private int individualBinCount;
    private int startPosition = 0;
    private int endPosition = 0;
    private DragState dragState = DragState.NOT_DRAGGING;
    private int mouseDownLocation = NOT_DRAGGING;
    private RangeChangedEventHandlerCollection rangeChangedEventHandlers = new RangeChangedEventHandlerCollection();
    private boolean pagingEnabled = false;
    private Integer viewPortMax = null;
    private int lastStart = 0;
    private int lastEnd = 1;
    private List<Integer> fullCategoryOverviewInfo;

    private Integer limit = null;

    private CancelRepeatingCommand command;

    public OverviewPresenter(ChartPresenter chartPresenter) {
        this.overviewView = new OverviewViewWidget(this);
        this.chartPresenter = chartPresenter;
    }

    private static Range determineNextZoomOutRange(int newSize, int limit, Range categoryRange, int totalCategoryCount) {
        if (newSize <= 0) {
            newSize = 1;
        }

        if (newSize > limit - 1) {
            newSize = limit - 1;
        }

        int offset = newSize / 2;

        int start = categoryRange.getStartIndex();
        int end = categoryRange.getEndIndex();

        start -= offset;
        if (start < 0) {
            start = 0;
        }
        if (start > totalCategoryCount) {
            start = totalCategoryCount - 1;
        }

        end = start + newSize;

        if (end > totalCategoryCount) {
            end = totalCategoryCount - 1;
        }


        Range range = new Range(start, end);
        return range;
    }

    private static native int internetExplorerWorkaroundForMouseWheel(NativeEvent evt) /*-{
        if (typeof evt.wheelDelta == "undefined") {
            return 0;
        }
        return Math.round(-evt.wheelDelta / 40) || 0;
    }-*/;

    public void build(int width) {
        this.overviewState = new OverviewState(width-80);

        addHandlersToView();
    }

    public boolean isLoaded() {
        return overviewState != null && overviewView != null;
    }

    public void invisibleReset() {
        invalidateCurrentRange();
        if (null != overviewState) {

            overviewState.reset();
        }
        viewPortMax = null;
        //fireRangeChangedEvent();
    }

    public void reset() {
        invalidateCurrentRange();
        if (null != overviewState) {

            overviewState.reset();
        }
        viewPortMax = null;
        renderState();
        //fireRangeChangedEvent();
    }

    public void resizeWidth(int desiredWidth, boolean fireEvents) {
        if (desiredWidth > 0 && isLoaded()) {
            int width = desiredWidth;//RangeCalculator.calculateWidthOfOverviewContent(totalCategoryCount, desiredWidth);
            overviewState.scaleToNewWidth(width - 60);

            adjustBuckets(overviewState);
            renderState();

            if (this.limit != null) {
                viewPortMax = null;
                initViewPortMax(0, this.limit - 1, false);
            }

            //Fire this here because sometimes the range changes due to rounding to a whole number for pixels.
            if (fireEvents && categoryRange != Range.EMPTY_RANGE) {
                forceFireRangeChangedEvent(categoryRange);
            }
//                fireRangeChangedEvent();
        }

    }

    public void setScrollRange(Range range) {
        setScrollRange(range, true);
    }

    public void setScrollRange(Range range, boolean fireEvents) {
        if (!isLoaded()) {
            return;
        }
        if (range.getStartIndex() < 0 || range.getEndIndex() > this.totalCategoryCount || range.getDifference() < 0) {
            return;
        }
        int startIndex = range.getStartIndex();
        int endIndex = range.getEndIndex();

        double binSize = RangeCalculator.createBinSize(overviewState.getWidth(), totalCategoryCount);
        int startPosition = (int) (startIndex * binSize);
        int endPosition = (int) ((endIndex * binSize) + binSize);

        if (!fireEvents || viewPortMax == null) {
            viewPortMax = endPosition - startPosition;
            this.limit = range.getEndIndex() - range.getStartIndex() + 1;
        }

        this.categoryRange = range;
        overviewState.setRange(startPosition, endPosition);
        renderState();
        if (fireEvents) {
            fireRangeChangedEvent(range);
        }
    }

    public void initViewPortMax(int startIndex, int endIndex, boolean force) {

        if (viewPortMax == null || force) {
            double binSize = RangeCalculator.createBinSize(overviewState.getWidth(), totalCategoryCount);
            int startPosition = (int) (startIndex * binSize);
            int endPosition = (int) ((endIndex * binSize) + binSize);

            viewPortMax = endPosition - startPosition;
            this.limit = endIndex - startIndex + 1;
        }
    }

    public void setRange(Range range) {
        setRange(range, true);
    }

    public void setRange(Range range, boolean fireEvents) {
        if (range.getStartIndex() < 0 || range.getEndIndex() > this.totalCategoryCount || range.getDifference() < 0) {
            return;
        }

        double binSize = RangeCalculator.createBinSize(overviewState.getWidth(), totalCategoryCount);
        int start = (int) (range.getStartIndex() * binSize);
        int end = (int) ((range.getEndIndex() * binSize) + binSize);

        overviewState.setRange(start, end);
        renderState();

        if (fireEvents)
            fireRangeChangedEvent(range);
    }

    public void setCategoryData(int count, List<Integer> values, int width, boolean fireEvents) {
        if (count > 0) {
            this.totalCategoryCount = count;
        } else {
            this.totalCategoryCount = values.size();
        }
        this.fullCategoryOverviewInfo = values;
        overviewView.setCategoryData(values);
        resizeWidth(width, fireEvents);
    }

    public OverviewView getOverviewView() {
        return overviewView;
    }

    public void addRangeChangedEventHandler(RangeChangedEventHandler handler) {
        rangeChangedEventHandlers.addHandler(handler);
    }

    public int getWidth() {
        return overviewState.getWidth();
    }

    public int getIndividualBinCount() {
        return individualBinCount;
    }

    public void setIndividualBinCount(int individualBinCount) {
        this.individualBinCount = individualBinCount;
    }

    public Range getCategoryRange() {
        return categoryRange;
    }

    public void invalidateCurrentRange() {
        categoryRange = Range.EMPTY_RANGE;
    }

    private int zoomIn() {
        int offset = overviewState.zoomIn();
        renderState();
        return offset;
    }

    private int zoomOut(int viewPortMax) {
        int offset;
        OverviewState tempState = overviewState.copy();
        offset = tempState.zoomOut(viewPortMax);
        Range tempRange = determineNextZoomOutRange(offset, limit, categoryRange, totalCategoryCount);
        offset = (int) Math.ceil((double) (categoryRange.getDifference()) / (1.0D - OverviewState.ZOOM_FACTOR));
        offset += 2;
        if (overviewState.getPositionDifference() <= 1) {
            int nextRange = (int) Math.ceil((double) (tempRange.getDifference()) / (1.0D - OverviewState.ZOOM_FACTOR));
            nextRange += 2;
            if (nextRange <= offset) {
                return 0;
            }
        }
        overviewState = tempState;
        renderState();
        return offset;
    }

    private void pan(int panAmount) {
        overviewState.pan(panAmount);
        renderState();

    }

    private Range adjustRange(Range range, int targetIndexBreadth) {

        int startIndex = range.getStartIndex();
        int endIndex = range.getEndIndex();

        while (endIndex - startIndex < targetIndexBreadth) {
            if (endIndex + 1 < totalCategoryCount) {
                endIndex++;
            }

            if (endIndex == totalCategoryCount) {
                if (startIndex > 0) {
                    startIndex--;
                } else {
                    break;
                }
            }

            if (endIndex - startIndex == targetIndexBreadth) {
                break;
            }

            if (startIndex > 0) {
                startIndex--;
            }

            if (startIndex == 0) {
                if (endIndex < totalCategoryCount) {
                    endIndex++;
                } else {
                    break;
                }
            }
        }

        Range adjustedRange = new Range(startIndex, endIndex);

        while (adjustedRange.getDifference() - targetIndexBreadth > 1) {
            adjustedRange = new Range(++startIndex, --endIndex);
        }

        if (adjustedRange.getDifference() - targetIndexBreadth == 1) {
            adjustedRange = new Range(startIndex, --endIndex);
        }

        return adjustedRange;

    }

    private void moveStart(int amount) {
        overviewState.moveStart(amount);
        renderState();
    }

    private void moveEnd(int amount) {
        overviewState.moveEnd(amount);
        renderState();
    }

    private void center(int center) {
        overviewState.center(center);
        renderState();
    }

    private void renderState() {
        if (overviewState != null)
            overviewView.render(overviewState, dragState);
    }

    private void adjustBuckets(OverviewState overviewState) {
        int width = overviewState.getWidth();

        List<Integer> adjustedCategories = RangeCalculator.adjustCategoryDataToSize(fullCategoryOverviewInfo, width);

        overviewView.setCategoryData(adjustedCategories);
    }

    private void addHandlersToView() {
        MouseWheelHandler mouseWheelHandler = createMouseWheelHandler();
        MouseDownHandler mouseDownHandler = createMouseDownHandler();
        MouseUpHandler mouseUpHandler = createMouseUpHandler();
        MouseMoveHandler mouseMoveHandler = createMouseMoveHandler();
//        MouseOutHandler mouseOutHandler = createMouseOutHandler();
//        DoubleClickHandler doubleClickHandler = createDoubleClickHandler();

        overviewView.addMouseOutHandler(event -> {
            if (command != null) {
                command.cancel();
            }

            if (dragState == DragState.NOT_DRAGGING) {
                overviewView.removeHighlights();
            }

            command = new CancelRepeatingCommand() {

                @Override
                public boolean execute() {
                    if (!isCancel())
                        resetOverviewFromDrag();
                    return false;
                }
            };

            Scheduler.get().scheduleFixedDelay(command, DRAG_TIMEOUT);
        });
        overviewView.getMoveLeft().addClickHandler(createMoveLeftHandler());
        overviewView.getMoveRight().addClickHandler(createMoveRightHandler());
        overviewView.addMouseWheelHandler(mouseWheelHandler);
        overviewView.addMouseDownHandler(mouseDownHandler);
        overviewView.addMouseUpHandler(mouseUpHandler);
        overviewView.addMouseMoveHandler(mouseMoveHandler);
        //overviewView.addClickHandler(createSingleClickHandler());
        //overviewView.addMouseOutHandler(mouseOutHandler);
//        overviewView.addDoubleClickHandler(doubleClickHandler);

    }

    private ClickHandler createMoveRightHandler() {
        return event -> {
            int newEnd = Math.min(totalCategoryCount , categoryRange.getEndIndex() +
                    Math.max(1, categoryRange.getDifference()));
            int newStart = Math.min(totalCategoryCount - categoryRange.getDifference(), newEnd -
                    (categoryRange.getDifference()));
            Range newRange = new Range(newStart, newEnd);
            setRange(newRange);
        };
    }

    private ClickHandler createMoveLeftHandler() {
        return event -> {
            int newStart = Math.max(0, categoryRange.getStartIndex() - categoryRange.getDifference());
            int newEnd = Math.max(categoryRange.getDifference() , newStart + categoryRange.getDifference());
            Range newRange = new Range(newStart, newEnd);
            setRange(newRange);
        };
    }

    private void constrainedFireRangeChangedEvent(OverviewState overviewState, Range range) {
        Range rangeFromState = createRangeFromState(overviewState);
        int targetBreadth = range.getDifference();
        if (rangeFromState.getDifference() != targetBreadth) {
            rangeFromState = adjustRange(rangeFromState, targetBreadth);
        }
        this.overviewState = overviewState;
        fireRangeChangedEvent(rangeFromState);
    }

    private void guessFireRangeChangedEvent(OverviewState overviewState) {
        Range range = createRangeFromState(overviewState);

        this.overviewState = overviewState;
        fireRangeChangedEvent(range);
    }

    private Range createRangeFromState(OverviewState overviewState) {
        Range range = RangeCalculator.calculateRange(overviewState, totalCategoryCount);

        if (this.viewPortMax != null) {
            if (overviewState.getEndPosition() - overviewState.getStartPosition() >= this.viewPortMax) {
                range = adjustRange(range, limit - 1);
            }
        }

        if (range.getDifference() > limit - 1) {
            range = adjustRange(range, limit - 1);
        }
        return range;
    }

    private void virtualZoomIn(int newSize) {
        if (newSize <= 0) {
            newSize = 1;
        }

        int offset = newSize / 2;

        int start = categoryRange.getStartIndex();
        int end = categoryRange.getEndIndex();

        start += offset;

        if (newSize == 1) {
            end = start;
        } else {
            end -= offset;
        }

        if (start < 0) {
            start = 0;
        }
        if (start > totalCategoryCount) {
            start = totalCategoryCount - 1;
        }

        if (end < start) {
            end = start + 1;
        }

        if (end > totalCategoryCount) {
            end = totalCategoryCount - 1;
        }


        fireRangeChangedEvent(new Range(start, end));

    }

    private void virtualZoomOut(int newSize) {
        Range range = determineNextZoomOutRange(newSize, limit, categoryRange, totalCategoryCount);
        fireRangeChangedEvent(range);

    }

    private void fireRangeChangedEvent(Range range) {
        if (!range.equals(categoryRange)) {
            forceFireRangeChangedEvent(range);
        } else {
            resetDragState();
        }
    }

    public void forceFireRangeChangedEvent(Range range) {

        rangeChangedEventHandlers.fireEvent(new RangeChangedEvent(range));

        categoryRange = range;
    }

    private MouseOutHandler createMouseOutHandler() {
        return new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {

                stopDragging();
            }
        };
    }

    private DoubleClickHandler createDoubleClickHandler() {
        return new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                center(event.getRelativeX(event.getRelativeElement()));
                //fireRangeChangedEvent();
            }
        };
    }

    private ClickHandler createSingleClickHandler() {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (dragState != DragState.NOT_DRAGGING) {
                    return;
                }

                boolean trigger = false;

                int clickLoc = event.getRelativeX(event.getRelativeElement());

//                GWT.log("Click Loc: " + clickLoc);
//                GWT.log("Center Point: " + centerPoint);
//                GWT.log("Is within: " + isWithinRange(clickLoc) );
//                GWT.log("End: " + getEndPosition() + " Start: " + getStartPosition() );
//                if(isWithinRange(clickLoc)){
//
//                    if(centerPoint > clickLoc){
//                        overviewState.setStartPosition(clickLoc);
//                        trigger = true;
//                    }
//
//                    if(centerPoint < clickLoc){
//                        overviewState.setEndPosition(clickLoc);
//                        trigger = true;
//                    }
//
//                }else{
                if (clickLoc > getEndPosition()) {
                    overviewState.setEndPosition(clickLoc);
                    trigger = true;
                }

                if (clickLoc < getStartPosition()) {
                    overviewState.setStartPosition(clickLoc);
                    trigger = true;
                }
//                }

                if (trigger) {
                    renderState();
                    //fireRangeChangedEvent();
                }

            }
        };
    }

    private void highlightDraggable(int mouseLocation) {
        DragState dragState = calculateDragState(mouseLocation);
        overviewView.removeHighlights();
        switch (dragState) {
            case START_BAR:
                overviewView.highlightStartBar();
                break;
            case END_BAR:
                overviewView.highlightEndBar();
                break;
            case BOTH_BARS:
                overviewView.highlightCenterBar();
                break;
            case NOT_DRAGGING:

                break;
            default:
                break;

        }

    }

    private MouseMoveHandler createMouseMoveHandler() {
        return new MouseMoveHandler() {


            @Override
            public void onMouseMove(MouseMoveEvent event) {
                event.preventDefault();

                if (event.getRelativeElement() == null) {
                    return;
                }
                int mouseLocation = event.getRelativeX(event.getRelativeElement());
                if (dragState == DragState.NOT_DRAGGING) {
                    highlightDraggable(mouseLocation);
                    return;
                }

                if (command != null) {
                    command.cancel();
                }

                int dragAmount = mouseLocation - mouseDownLocation;
                if (overviewState.validateDrag(dragAmount, dragState, viewPortMax)) {
//                    return;
                    switch (dragState) {
                        case START_BAR:
                            dragAmount = (overviewState.getEndPosition() - overviewState.getStartPosition()) - viewPortMax;
                            mouseLocation = overviewState.getStartPosition() + viewPortMax;
                            break;
                        case END_BAR:
                            dragAmount = viewPortMax - (overviewState.getEndPosition() - overviewState.getStartPosition());
                            mouseLocation = overviewState.getStartPosition() + viewPortMax;
                            break;
                    }

                }

                moveOverviewControl(dragAmount);

                mouseDownLocation = mouseLocation;
            }

            private void moveOverviewControl(int dragAmount) {
                switch (dragState) {
                    case START_BAR:
                        moveStart(dragAmount);
                        break;
                    case END_BAR:
                        moveEnd(dragAmount);
                        break;
                    case BOTH_BARS:
                        pan(dragAmount);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private MouseUpHandler createMouseUpHandler() {
        return new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                stopDragging();
            }
        };
    }

    private void resetOverviewFromDrag() {
        if (dragState != DragState.NOT_DRAGGING) {
            dragState = DragState.NOT_DRAGGING;
            overviewState.setRange(lastStart, lastEnd);
            getOverviewView().setCursor(Cursor.POINTER);
            renderState();
            // add the single click handler;
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    overviewView.addClickHandler(createSingleClickHandler());
                }
            });
        }
    }

    private Range inferRanger() {
        double binSize = getBinSize();
        int start = (int) (getStartPosition() / binSize + .5);
        int end = (int) (getEndPosition() / binSize - .5);
        if (start > totalCategoryCount) {
            start = totalCategoryCount - 1;
        }
        if (end < start) {
            end = start;
        }
        if (end > totalCategoryCount) {
            end = totalCategoryCount - 1;
        }
        return new Range(start, end);
    }

    private double getBinSize() {
        return RangeCalculator.createBinSize(overviewView.getWidth(), totalCategoryCount);
    }

    public void stopDragging() {
        getOverviewView().setCursor(Style.Cursor.POINTER);
        DragState lastDragState = dragState;

        Range range = RangeCalculator.calculateRange(overviewState, totalCategoryCount);
        switch (lastDragState) {
            case BOTH_BARS:
                if (categoryRange.getDifference() <= limit) {
                    constrainedFireRangeChangedEvent(overviewState, categoryRange);
                } else {
                    //This ignores widget position difference for proper pan on virtually zoomed categories
                    int difference = categoryRange.getDifference();
                    range = adjustRange(range, difference);
                    fireRangeChangedEvent(range);
                }
                break;
            case START_BAR:
            case END_BAR:
                guessFireRangeChangedEvent(overviewState);
                break;
            default:
                dragState = DragState.NOT_DRAGGING;
                mouseDownLocation = NOT_DRAGGING;
                break;
        }
        lastStart = overviewState.getStartPosition();
        lastEnd = overviewState.getEndPosition();
        renderState();
    }

    private MouseDownHandler createMouseDownHandler() {
        return new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                // turn off click handler on mouse down...
                overviewView.removeMouseClickHandler();
                mouseDownLocation = event.getRelativeX(event.getRelativeElement());
                event.preventDefault();
                adjustDragState();

            }


        };
    }

    private void adjustDragState() {
        DragState dragState = calculateDragState(mouseDownLocation);
        this.dragState = dragState;
        lastStart = overviewState.getStartPosition();
        lastEnd = overviewState.getEndPosition();
        switch (this.dragState) {
            case START_BAR:
                overviewView.setCursor(Style.Cursor.COL_RESIZE);
                break;
            case END_BAR:
                overviewView.setCursor(Style.Cursor.COL_RESIZE);
                break;
            case BOTH_BARS:
                overviewView.setCursor(Style.Cursor.COL_RESIZE);
                break;
            case NOT_DRAGGING:
                overviewView.setCursor(Style.Cursor.POINTER);
                int currentLocation = overviewState.getStartPosition() + (overviewState.getEndPosition() - overviewState.getStartPosition()) / 2;
                pan(mouseDownLocation - currentLocation - OverviewView.DRAG_BAR_WIDTH);
                mouseDownLocation = NOT_DRAGGING;

                if (categoryRange.getDifference() <= limit) {
                    constrainedFireRangeChangedEvent(overviewState, categoryRange);
                } else {
                    Range range = RangeCalculator.calculateRange(overviewState, totalCategoryCount);
                    int difference = categoryRange.getDifference();
                    range = adjustRange(range, difference);
                    fireRangeChangedEvent(range);
                }
                break;
        }

    }

    public DragState calculateDragState(int mouseDownLocation) {
        DragState dragState = DragState.NOT_DRAGGING;
        if (isDraggingStartBar(mouseDownLocation)) {
            dragState = DragState.START_BAR;
        } else if (isDraggingEndBar(mouseDownLocation)) {
            dragState = DragState.END_BAR;
        } else if (isDraggingCenter(mouseDownLocation)) {
            dragState = DragState.BOTH_BARS;
        } else {
            dragState = DragState.NOT_DRAGGING;
        }
        return dragState;
    }

    private MouseWheelHandler createMouseWheelHandler() {
        return new MouseWheelHandler() {

            @Override
            public void onMouseWheel(MouseWheelEvent event) {
                if (rangeChangedEvent != null) {
                    if (rangeChangedEvent.getOverviewState() != null && !rangeChangedEvent.cancelled && overviewState.getPositionDifference() <= 2) {
                        return;
                    }
                    rangeChangedEvent.cancel();
                }

                int deltaY = getDeltaY(event);
                boolean zoomIn = true;
                int offset = 0;
                if (deltaY < 0) {
                    offset = zoomIn();
                } else {
                    offset = zoomOut(viewPortMax);
                    zoomIn = false;
                }

                if (offset == 0 && zoomIn) {
                    int nextRange = (int) (categoryRange.getDifference() * (1 - (OverviewState.ZOOM_FACTOR)));

                    rangeChangedEvent = new DebouncedRangeChangedEvent(nextRange, zoomIn, null);
                } else if (offset == 0) {
                    int nextRange = 0;
                    if (categoryRange.getDifference() < 1) {
                        nextRange = 2;
                    } else {
                        nextRange = (int) Math.ceil((double) (categoryRange.getDifference()) / (1 - OverviewState.ZOOM_FACTOR));
                        nextRange += 2;
                    }
                    rangeChangedEvent = new DebouncedRangeChangedEvent(nextRange, zoomIn, null);
                } else {
                    rangeChangedEvent = new DebouncedRangeChangedEvent(-1, zoomIn, overviewState.copy());
                }
                Scheduler.get().scheduleFixedDelay(rangeChangedEvent, RANGED_CHANGED_ZOOM_HANDLER_DELAY_MILLIS);
            }
        };
    }

    private int getDeltaY(MouseWheelEvent event) {
        int deltaY = event.getDeltaY();
        if (deltaY == 0)
            deltaY = internetExplorerWorkaroundForMouseWheel(event.getNativeEvent());
        return deltaY;
    }

    private boolean isDraggingStartBar(int mouseDownLocation) {
        return mouseDownLocation >= overviewState.getStartPosition() && mouseDownLocation < overviewState.getStartPosition() + OverviewView.DRAG_BAR_WIDTH +3;
    }

    private boolean isDraggingEndBar(int mouseDownLocation) {
        return mouseDownLocation > overviewState.getEndPosition() + OverviewView.DRAG_BAR_WIDTH && mouseDownLocation <= overviewState.getEndPosition() + (OverviewView.DRAG_BAR_WIDTH * 2);
    }

    private boolean isDraggingCenter(int mouseDownLocation) {
        return mouseDownLocation >= overviewState.getStartPosition() && mouseDownLocation <= overviewState.getEndPosition();
    }

    public int getStartPosition() {
        return overviewState.getStartPosition();
    }

    public int getEndPosition() {
        return overviewState.getEndPosition();
    }

    /**
     * checks if the passed in position is within the selected region
     *
     * @param position - position in question.
     * @return true if the value passed in is within the range, false otherwise.
     */
    private boolean isWithinRange(int position) {
        return (position > getStartPosition() && position < getEndPosition());
    }

    public int getNumberOfCategories() {
        return totalCategoryCount;
    }

    public void resetMax() {
        viewPortMax = null;
        limit = null;
    }

    public void resetDragState() {
        resetOverviewFromDrag();
    }

    private class DebouncedRangeChangedEvent implements Scheduler.RepeatingCommand {

        private long refreshAt;
        private boolean cancelled = false;
        private int nextSize = -1;
        private boolean zoomIn;
        private OverviewState overviewState;

        public DebouncedRangeChangedEvent(int size, boolean zoomIn, OverviewState overviewState) {
            nextSize = size;
            this.overviewState = overviewState;
            this.zoomIn = zoomIn;
            resetTimer();
        }

        public void cancel() {
            cancelled = true;
        }

        @Override
        public boolean execute() {
            long time = new Date().getTime();
            if (!cancelled && time > refreshAt) {
                if (nextSize < 0) {
                    guessFireRangeChangedEvent(overviewState);
                } else if (zoomIn) {
                    virtualZoomIn(nextSize);
                } else {
                    virtualZoomOut(nextSize);
                }
                this.cancel();
                return false;
            } else if (cancelled) {
                return false;
            } else {
                return true;
            }
        }

        public void resetTimer() {
            refreshAt = new Date().getTime() + RANGE_CHANGED_MAX_DELAY_MILLIS;
        }

        public OverviewState getOverviewState() {
            return overviewState;
        }
    }


}
