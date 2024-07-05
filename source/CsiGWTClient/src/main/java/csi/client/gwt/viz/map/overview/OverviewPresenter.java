package csi.client.gwt.viz.map.overview;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HandlesAllMouseEvents;
import csi.client.gwt.WebMain;
import csi.client.gwt.viz.map.overview.range.Range;
import csi.client.gwt.viz.map.overview.range.RangeCalculator;
import csi.client.gwt.viz.map.overview.view.OverviewView;
import csi.client.gwt.viz.map.overview.view.OverviewViewWidget;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.timeline.scheduler.CancelRepeatingCommand;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.service.api.MapActionsServiceProtocol;

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
    static final int RANGED_CHANGED_ZOOM_HANDLER_DELAY_MILLIS = 300;
    static final int DRAG_TIMEOUT = 5000;
    static final int RANGE_CHANGED_MAX_DELAY_MILLIS = 200;
    private static final double ZOOM_FACTOR = .3;
    public Range range = Range.EMPTY_RANGE;
    DebouncedRangeChangedEvent rangeChangedEvent = null;
    private boolean doubleCheck = false;
    private OverviewView overviewView;
    private MapPresenter mapPresenter;
    private int totalCategoryCount;
    private int individualBinCount;
    private DragState dragState = DragState.NOT_DRAGGING;
    private Integer viewPortMax = null;
    private List<Integer> fullCategoryOverviewInfo;
    private Integer limit = null;
    private CancelRepeatingCommand command;
    private int addLeftAmount = 0;
    private int addRightAmount = 0;
    private int startPosition = 0;
    private int endPosition = 0;
    private OverviewMouseHandler overviewMouseHandler;
    private boolean debounceRangeUpdate = false;

    public OverviewPresenter(MapPresenter mapPresenter) {
        this.overviewView = new OverviewViewWidget(this);
        this.mapPresenter = mapPresenter;
    }

    public void build(int width) {
        addHandlersToView();
    }

    public boolean isLoaded() {
        boolean overviewViewNotNull = overviewView != null;
        boolean overviewMouseHandlerNotNull = overviewMouseHandler != null;
        return overviewViewNotNull && overviewMouseHandlerNotNull;
    }

/*
    public void setScrollRange(Range range){
        setScrollRange(range, true);
    }
*/

    public void reset() {
        maybeUpdateRange(new Range(0, totalCategoryCount - 1));
        //            overviewState.reset();
//        viewPortMax = null;
//        renderState(true);
        //fireRangeChangedEvent();
    }

    public void resetWithoutRender() {
        calculateRangeValues(new Range(0, totalCategoryCount - 1));
    }

    private boolean calculateRangeValues(Range _range) {
        this.range = _range;
        if (range.getStartIndex() < 0 || range.getEndIndex() > this.totalCategoryCount - 1 || range.getDifference() < 0) {
            return true;
        }
        double binSize = getBinSize();
        startPosition = (int) (range.getStartIndex() * binSize);//position at start of bin
        endPosition = (int) ((range.getEndIndex() * binSize) + binSize);//position at end of bin
        return false;
    }

    private int lastSize = -1;

    public boolean resizeWidth(int desiredWidth, boolean fireEvents) {
        if (desiredWidth > 0 && isLoaded()) {
//            int width = desiredWidth;//RangeCalculator.calculateWidthOfOverviewContent(totalCategoryCount, desiredWidth);
            //FIXME:?
//            overviewState.scaleToNewWidth(width);
            if (overviewView != null && lastSize != desiredWidth) {
                lastSize = desiredWidth;
                ((OverviewViewWidget) overviewView).setWidth(desiredWidth);
            }

            adjustBuckets();
            renderState(false);

            if (this.limit != null) {
                viewPortMax = null;
                initViewPortMax(0, this.limit - 1, false);
            }

            if (mapPresenter.isOverviewScrollingCreated()) {
                doUpdateRange(range);
            }
            return true;
        } else {
            return false;
        }
    }

    public void setScrollRange(int min, int max, boolean fireEvents) {
        Range range = new Range(min, max);
        if (!isLoaded()) {
            return;
        }
        if (range.getStartIndex() < 0 || range.getEndIndex() > this.totalCategoryCount || range.getDifference() < 0) {
            return;
        }
        int startIndex = range.getStartIndex();
        int endIndex = range.getEndIndex();
        double binSize = getBinSize();
        int startPosition = (int) (startIndex * binSize);
        int endPosition = (int) ((endIndex * binSize) + binSize);
        if (!fireEvents || viewPortMax == null) {
            viewPortMax = endPosition - startPosition;
            this.limit = range.getEndIndex() - range.getStartIndex() + 1;
        }
        this.range = range;
        //FIXME:
        maybeUpdateRange(new Range(startIndex, endIndex));
    }

/*
    public void setRange(Range range){
        setRange(range, true);
    }
*/

    private double getBinSize() {
        return RangeCalculator.createBinSize(overviewView.getWidth(), totalCategoryCount);
    }

    public void initViewPortMax(int startIndex, int endIndex, boolean force) {
        if (viewPortMax == null || force) {
            double binSize = getBinSize();
            int startPosition = (int) (startIndex * binSize);
            int endPosition = (int) ((endIndex * binSize) + binSize);
            viewPortMax = endPosition - startPosition;
            this.limit = endIndex - startIndex + 1;
        }
    }

    private void setRange(Range range, boolean fireEvents) {
        if (!isDebounceRangeUpdate()) {
            maybeUpdateRange(range);
//            renderState(false);
            if (fireEvents) {
            }
        }
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

    public int getWidth() {
        return overviewView.getWidth();
    }

    public int getIndividualBinCount() {
        return individualBinCount;
    }

    public void setIndividualBinCount(int individualBinCount) {
        this.individualBinCount = individualBinCount;
    }

    void zoomIn() {
        int nextRange = (int) (range.getDifference() * (1 - (ZOOM_FACTOR)));
        nextRange = Math.max(1, Math.min(nextRange, range.getDifference() - 2));
        virtualZoomIn(nextRange);

//            rangeChangedEvent = new DebouncedRangeChangedEvent(this, nextRange, true);
//            Scheduler.get().scheduleFixedDelay(rangeChangedEvent, RANGED_CHANGED_ZOOM_HANDLER_DELAY_MILLIS);
        //FIXME:
//        int offset = overviewState.zoomIn();
        renderState(true);
    }

    void zoomOut() {
        int newSize = (int) Math.ceil((double) (range.getDifference()) / (1 - ZOOM_FACTOR));
        if (newSize <= 0) {
            newSize = 1;
        }
        if (newSize > limit) {
            newSize = limit;
        }
        int start = this.range.getStartIndex();
        int end = this.range.getEndIndex();
        start = Math.min(start - 1, start - (newSize / 2));
        end = Math.max(end + 1, end + (newSize / 2));
        if (start < 0) {
            start = 0;
        }
        if (end > totalCategoryCount - 1) {
            end = totalCategoryCount - 1;
        }
        if (start > end) {
            start = end;
        }

        maybeUpdateRange(new Range(start, end));
        //            rangeChangedEvent = new DebouncedRangeChangedEvent(this, nextRange, false);

//        Scheduler.get().scheduleFixedDelay(rangeChangedEvent, RANGED_CHANGED_ZOOM_HANDLER_DELAY_MILLIS);

        //FIXME:
//        OverviewState tempState = overviewState.copy();
//        offset = tempState.zoomOut(viewPortMax);
/*        Range tempRange;

        int offset = 0;
        tempRange = determineNextZoomOutRange(offset, limit, range, totalCategoryCount);

        offset = tempRange.getDifference();
        offset = (int) Math.ceil((double) (range.getDifference()) / (1.0D - ZOOM_FACTOR));
        offset += 2;
//FIXME:

        if (getPositionDifference() <= 1) {
            if (false) {
                int nextRange = (int) Math.ceil((double) (range.getDifference()) / (1.0D - ZOOM_FACTOR));
                nextRange += 2;

                if (nextRange <= offset) {
//                    return 0;
                }
            }
            //FIXME:
//        overviewState = tempState;
        }*/
        renderState(true);
    }

    private void pan(int panAmount) {
        int deltaX = panAmount;
        int max = this.totalCategoryCount;
        if (endPosition + deltaX > getBinSize() * max) {
            deltaX = (int) (getBinSize() * max - endPosition);
        }
        if (startPosition + deltaX < 0) {
            deltaX = -startPosition;
        }
        endPosition += deltaX;
        startPosition += deltaX;
        renderState(false);
    }

    private Range adjustRange(Range range, int targetIndexBreadth) {
        int startIndex = range.getStartIndex();
        int endIndex = range.getEndIndex();
        while (endIndex - startIndex < targetIndexBreadth) {
            if (endIndex < totalCategoryCount) {
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

    private void modifyPositionByOffset(int position, int offset) {
        int newStart = position - offset;
        int newEnd = position + offset;
        Range range = new Range(newStart, newEnd);
        setRange(range, false);
    }

    private void verifyBounds() {
        verifyEndPosition();
        verifyStartPosition();
    }

    private void verifyStartPosition() {
        //FIXME:
/*        if (startPosition < 0) {
            startPosition = 0;
        }
        if (startPosition >= endPosition && startPosition > 0) {
            startPosition = endPosition - 1;
        }*/
    }

    private void verifyEndPosition() {
        //FIXME:
/*
        if(endPosition > width){
            endPosition = width;
        }
        if (endPosition <= startPosition) {
            endPosition = startPosition + 1;
        }
*/
    }

    private void center(int center) {
        int position = (getStartPosition() + getEndPosition()) / 2;
        int offset = getPositionDifference() / 2;
        modifyPositionByOffset(position, offset);
        renderState(false);
    }

    private void renderState(boolean updateTips) {
        if (!updateTips) {
            overviewView.render(dragState, null, null);
            return;
        }
        VortexFuture<String> future = WebMain.injector.getVortex().createFuture();
        future.execute(MapActionsServiceProtocol.class).getStartAndEnd(mapPresenter.getVisualizationDef().getUuid(), mapPresenter.getDataViewUuid());
        future.addEventHandler(new AbstractVortexEventHandler<String>() {
            @Override
            public void onSuccess(String result) {
                String s = "";
                String s1 = "";
                try {
                    String[] split = result.split("\n");
                    s1 = split[1];
                    s = split[0];
                } catch (Exception ignored) {
                }
                if (s == null || s1 == null) {
                    Scheduler.get().scheduleFixedDelay(() -> {
                        renderState(true);
                        return false;
                    },1000);
                    overviewView.render(dragState, "", "");
                }else{
                    overviewView.render(dragState, s, s1);
                }
            }
        });
    }

    private void adjustBuckets() {
        int width = overviewView.getWidth();
        List<Integer> adjustedCategories = RangeCalculator.adjustCategoryDataToSize(fullCategoryOverviewInfo, width);
        overviewView.setCategoryData(adjustedCategories);
    }

    private void addHandlersToView() {
        overviewMouseHandler = new OverviewMouseHandler(this);
        HandlesAllMouseEvents mouseHandler = overviewMouseHandler;
        overviewView.getAddLeft().addClickHandler(createAddLeftHandler());
        overviewView.getMoveLeft().addClickHandler(createMoveLeftHandler());
        overviewView.getAddRight().addClickHandler(createAddRightHandler());
        overviewView.getMoveRight().addClickHandler(createMoveRightHandler());
        overviewView.addMouseOutHandler(mouseHandler);
        overviewView.addMouseWheelHandler(mouseHandler);
        overviewView.addMouseDownHandler(mouseHandler);
        overviewView.addMouseUpHandler(mouseHandler);
        overviewView.addMouseMoveHandler(mouseHandler);
    }

    private ClickHandler createMoveRightHandler() {
        return event -> {
            if (!isDebounceRangeUpdate()) {
                int newEnd = Math.min(totalCategoryCount - 1, range.getEndIndex() + Math.max(1, range.getDifference() - 1));
                int newStart = Math.min(totalCategoryCount - range.getDifference(), newEnd - (range.getDifference() - 1));
                maybeUpdateRange(new Range(newStart, newEnd));
            }
        };
    }

    private ClickHandler createAddRightHandler() {
        return event -> {
            if (!isDebounceRangeUpdate()) {
                if (addRightAmount == 0) {
                    addRightAmount = Math.max(1, range.getDifference());
                }
                int addAmount = addRightAmount;
                int newEnd = Math.min(totalCategoryCount - 1, range.getEndIndex() + addRightAmount);
                maybeUpdateRange(new Range(range.getStartIndex(), newEnd));
                addRightAmount = addAmount;
            }
        };
    }

    private ClickHandler createMoveLeftHandler() {
        return event -> {
            if (!isDebounceRangeUpdate()) {
                int newStart = Math.max(0, range.getStartIndex() - range.getDifference());
                int newEnd = Math.max(range.getDifference() - 1, newStart + range.getDifference() - 1);
                maybeUpdateRange(new Range(newStart, newEnd));
            }
        };
    }

    private ClickHandler createAddLeftHandler() {
        return event -> {
            if (!isDebounceRangeUpdate()) {
                if (addLeftAmount == 0) {
                    addLeftAmount = Math.max(1, range.getDifference());
                }
                int addAmount = addLeftAmount;
                int newStart = Math.max(0, range.getStartIndex() - addLeftAmount);
                maybeUpdateRange(new Range(newStart, range.getEndIndex()));
                addLeftAmount = addAmount;
            }
        };
    }

    private void constrainedFireRangeChangedEvent(Range range) {
        Range rangeFromState = createRangeFromState();
        int targetBreadth = range.getDifference();
        if (rangeFromState.getDifference() != targetBreadth) {
            rangeFromState = adjustRange(rangeFromState, targetBreadth);
        }
        maybeUpdateRange(rangeFromState);
    }

    void guessFireRangeChangedEvent() {
        Range range = createRangeFromState();
        maybeUpdateRange(range);
    }

    private Range createRangeFromState() {
        Range range = inferRange();
        if (this.viewPortMax != null) {
            if (getEndPosition() - getStartPosition() >= this.viewPortMax) {
                range = adjustRange(range, limit - 1);
            }
        }
        if (range.getDifference() > limit - 1) {
            range = adjustRange(range, limit - 1);
        }
        return range;
    }

    void virtualZoomIn(int newSize) {
        if (newSize <= 0) {
            newSize = 1;
        }
        double offset = (range.getDifference() - newSize) / 2D;
        int start = range.getStartIndex();
        int end = range.getEndIndex();
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
        maybeUpdateRange(new Range(start, end));
    }

    private void maybeUpdateRange(Range range) {
        if (isLoaded() && mapPresenter.isOverviewScrollingCreated()) {
            doUpdateRange(range);
        }
    }

    public void doUpdateRange(Range _range) {
        if (calculateRangeValues(_range)) return;
        addLeftAmount = 0;
        addRightAmount = 0;
        mapPresenter.showProgressIndicator();
        setDebounceRangeUpdate(true);
        Scheduler.get().scheduleDeferred(() -> {
            resetDragState();
            int requestStart = Math.min(range.getStartIndex(), range.getEndIndex());
            int requestEnd = Math.max(range.getStartIndex(), range.getEndIndex());
            WebMain.injector.getVortex().execute((Boolean rangeMap) -> {
                if (rangeMap) {
                    mapPresenter.getView().rangeChange();
                    renderState(true);
                }
                mapPresenter.hideProgressIndicator();
            }, MapActionsServiceProtocol.class).updateRange(mapPresenter.getDataViewUuid(), mapPresenter.getUuid(), requestStart, requestEnd);
        });
        renderState(true);
    }

    private DoubleClickHandler createDoubleClickHandler() {
        return event -> {
            if (!isDebounceRangeUpdate()) {
                center(event.getRelativeX(event.getRelativeElement()));
            }
            //fireRangeChangedEvent();
        };
    }

    private ClickHandler createSingleClickHandler() {
        return event -> {
            if (!isDebounceRangeUpdate()) {
            }
        };
    }

    void highlightDraggable(int mouseLocation) {
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

    void selectFromOverview(int relativeX, boolean deselect) {
        switch (calculateDragState(relativeX)) {
            case START_BAR: {
                VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
                future.execute(MapActionsServiceProtocol.class).selectFirstTrack(deselect, mapPresenter.getVisualizationDef().getUuid(), mapPresenter.getDataViewUuid());
                future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        mapPresenter.getView().rangeUpdate();
                    }
                });
            }
            break;
            case END_BAR: {
                VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
                future.execute(MapActionsServiceProtocol.class).selectLastTrack(deselect, mapPresenter.getVisualizationDef().getUuid(), mapPresenter.getDataViewUuid());
                future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        mapPresenter.getView().rangeUpdate();
                    }
                });
            }
            break;
            case BOTH_BARS:
            case NOT_DRAGGING:
                break;
        }
    }

    void resetOverviewFromDrag() {
        if (dragState != DragState.NOT_DRAGGING) {
            dragState = DragState.NOT_DRAGGING;
            //fixme:
//            setRange(lastStart, lastEnd);
            getOverviewView().setCursor(Cursor.POINTER);
            renderState(false);
            // add the single click handler;
            Scheduler.get().scheduleDeferred(() -> overviewView.addClickHandler(createSingleClickHandler()));
        }
    }

    void stopDragging() {
        getOverviewView().setCursor(Style.Cursor.POINTER);
        Range range = inferRange();

        //fixme:
//            setStartPosition(startPosition);
        //fixme:
//            setEndPosition(endPosition);
//            Range range = RangeCalculator.calculateRange(overviewView, totalCategoryCount);
        if (mapPresenter.isOverviewScrollingCreated()) {
            doUpdateRange(range);
        }
    }

    private Range inferRange() {
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

    void adjustDragState(int mouseDownLocation) {
        DragState dragState = calculateDragState(mouseDownLocation);
        this.dragState = dragState;
        switch (this.dragState) {
            case START_BAR:
            case END_BAR:
            case BOTH_BARS:
                overviewView.setCursor(Style.Cursor.COL_RESIZE);
                break;
            case NOT_DRAGGING:
                //FIXME: logic
                if (range.getDifference() <= limit) {
                    constrainedFireRangeChangedEvent(range);
                } else {
                    Range range = inferRange();
                    int difference = this.range.getDifference();
                    range = adjustRange(range, difference);
                    maybeUpdateRange(range);
                }
                break;
        }
    }

    DragState calculateDragState(int mouseDownLocation) {
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

    int getPositionDifference() {
        return getEndPosition() - getStartPosition();
    }

    private boolean isDraggingStartBar(int mouseDownLocation) {
        return mouseDownLocation >= getStartPosition() && mouseDownLocation < getStartPosition() + OverviewView.DRAG_BAR_WIDTH - 2;
    }

    private boolean isDraggingEndBar(int mouseDownLocation) {
        return mouseDownLocation > getEndPosition() + OverviewView.DRAG_BAR_WIDTH + 2 && mouseDownLocation <= getEndPosition() + (OverviewView.DRAG_BAR_WIDTH * 2);
    }

    private boolean isDraggingCenter(int mouseDownLocation) {
        return mouseDownLocation >= getStartPosition() && mouseDownLocation <= getEndPosition() + OverviewView.DRAG_BAR_WIDTH + 2;
    }

    public void setRange(int newStartPosition, int newEndPosition) {
        maybeUpdateRange(new Range(newStartPosition, newEndPosition));
    }

    public boolean isRangeEmpty() {
        return range.equals(Range.EMPTY_RANGE);
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    private void resetDragState() {
        resetOverviewFromDrag();
    }

    public MapPresenter getMapPresenter() {
        return mapPresenter;
    }

    DragState getDragState() {
        return dragState;
    }

    public CancelRepeatingCommand getCommand() {
        return command;
    }

    public void setCommand(CancelRepeatingCommand command) {
        this.command = command;
    }

    private void mapRenderComplete() {
        if (!doubleCheck) {
            setDebounceRangeUpdate(false);
            return;
        }
        doubleCheck = false;
        //extra safeguard
        Scheduler.get().scheduleFixedDelay(() -> {
            mapRenderComplete();
            return false;
        }, 500);
    }

    public void drag(int deltaX) {
//        if (!isDebounceRangeUpdate()) {
//            if (getCommand() != null) {
//                getCommand().cancel();
//            }

//            deltaX = (int) (deltaX / getBinSize());

        switch (getDragState()) {
            case START_BAR:
                panStart(deltaX);
                break;
            case END_BAR:
                panEnd(deltaX);
                break;
            case BOTH_BARS:
                pan(deltaX);
                break;
            default:
                break;
        }

//        }
    }

    private void panEnd(int deltaX) {
        int max = this.totalCategoryCount;
        if (endPosition + deltaX > getBinSize() * max) {
            deltaX = max - endPosition;
        }
        if (endPosition + deltaX <= startPosition + getBinSize()) {
            deltaX = (int) (startPosition + getBinSize() - endPosition);
        }
        endPosition += deltaX;
        renderState(false);
    }

    private void panStart(int deltaX) {
        if (startPosition + deltaX < 0) {
            deltaX = -startPosition;
        }
        if (startPosition + deltaX > endPosition - getBinSize()) {
            deltaX = (int) (endPosition - getBinSize() - startPosition);
        }
        startPosition += deltaX;
        renderState(false);
    }

    private boolean isDebounceRangeUpdate() {
        return debounceRangeUpdate;
    }

    public void setDebounceRangeUpdate(boolean debounceRangeUpdate) {
        this.debounceRangeUpdate = debounceRangeUpdate;
    }

    void handleClick(int clickLoc) {
/*        if (dragState != DragState.NOT_DRAGGING) {
            return;
        }*/

        if (clickLoc > getEndPosition() || clickLoc < getStartPosition()) {
            int clickCategory = (int) (clickLoc / getBinSize());
            int oldCenter = (range.getStartIndex() + range.getEndIndex()) / 2;
            int deltaX = clickCategory - oldCenter;
            pan((int) (deltaX * getBinSize()));
            stopDragging();
//            maybeUpdateRange(new Range((int) (startPosition/getBinSize()),(int) (endPosition/getBinSize())));
//            int i = (range.getDifference()) / 2;
//            int start = clickCategory - i;
//            int end = start + range.getDifference()-1;
//            if (start < 0) {
//                start = 0;
//            }
//            if (start > totalCategoryCount-1) {
//                start = totalCategoryCount - 1;
//            }
//
//            if (end < start) {
//                end = start;
//            }
//
//            if (end > totalCategoryCount-1) {
//                end = totalCategoryCount - 1;
//                start = end - range.getDifference()-1;
//            }
//            maybeUpdateRange(new Range(start,end));
//            trigger = true;
        }
//                }

//        if (trigger) {
//            stopDragging();
//            renderState(true);
//                        fireRangeChangedEvent();
//        }

    }
}
