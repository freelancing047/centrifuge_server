package csi.client.gwt.viz.timeline.view.drawing;

import java.util.List;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.EventBus;

import csi.client.gwt.viz.timeline.events.OverviewChangeEvent;
import csi.client.gwt.viz.timeline.events.ZoomEvent;
import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.scheduler.CancelRepeatingCommand;
import csi.client.gwt.viz.timeline.view.TimelineView;
import csi.client.gwt.viz.timeline.view.drawing.layer.ComplexLayer;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Rectangle;

public abstract class TimelineOverview<T extends OverviewRenderable> extends DrawingPanel {

    private static final int PANNING_BUFFER = 2;
    private ComplexLayer backgroundLayer;
    protected ComplexLayer eventLayer;
    private Layer sliderLayer;
    private TimeScale overviewTimeScale;
    private TimelineSliderRenderable renderable;
    private Rectangle backgroundRectangle;
    private CssColor backgroundColor = CssColor.make(240, 240, 240);
    private boolean changed = false;
    private EventBus eventBus;
    private DebouncedZoomEvent scrollCommand = new DebouncedZoomEvent();
    private static final int ZOOM_HANDLER_DELAY_MILLIS = 1000;
    private static final int DRAG_TIMEOUT = 5000;
    private boolean mouseOut = false;
    private CancelRepeatingCommand command;
    private TimelineView timelineView;

    public TimelineOverview() {

        getElement().getStyle().setCursor(Cursor.POINTER);
        backgroundLayer = new ComplexLayer();
        sliderLayer = new Layer();
        eventLayer = new ComplexLayer();
        addLayer(backgroundLayer);
        addLayer(eventLayer);
        addLayer(sliderLayer);
        this.bringToFront(sliderLayer);
        resetBackground();

        this.addHandler(mouseWheelHandler, MouseWheelEvent.getType());
    }

    private MouseDownHandler mouseDownHandler = new MouseDownHandler() {

        @Override
        public void onMouseDown(MouseDownEvent event) {

            if (command != null) {
                command.cancel();
            }
            int x = event.getX();
            int thumbWidth = renderable.getThumbWidth();
            int startX = renderable.getStartX();
            int endX = renderable.getEndX();

            eventLayer.stop();

            if (x <= startX - PANNING_BUFFER && x >= startX - thumbWidth) {

                getElement().getStyle().setCursor(Cursor.COL_RESIZE);
                // Left thumb mouse down
                renderable.setLeftDragging(true);
                renderable.setPanning(false);
            } else if (x >= endX + PANNING_BUFFER && x <= endX + thumbWidth) {

                getElement().getStyle().setCursor(Cursor.COL_RESIZE);
                // Right thumb click
                renderable.setRightDragging(true);
                renderable.setPanning(false);
            } else if (x < endX + thumbWidth && x > startX - thumbWidth) {

                getElement().getStyle().setCursor(Cursor.COL_RESIZE);
                renderable.setPanning(true);
                renderable.setDragging(false);
            } else {

                getElement().getStyle().setCursor(Cursor.AUTO);
                renderable.setDragging(false);
                renderable.setPanning(false);
            }

            eventLayer.start();
        }
    };

    private MouseOverHandler mouseOverHandler = new MouseOverHandler() {

        @Override
        public void onMouseOver(MouseOverEvent event) {
            if(overviewTimeScale == null) {
                return;
            }
            
            List<Axis> overviewAxes = Axis.allRelevant(overviewTimeScale.getInterval(), 0, 0, getEventBus());
            for (Axis axis : overviewAxes) {
                axis.setTimeScale(overviewTimeScale);
            }
            timelineView.getAxes().clear();
            timelineView.getAxes().addAll(overviewAxes);
            mouseOut = false;
            timelineView.renderFooter();
        }
    };

    private MouseOutHandler mouseOutHandler = new MouseOutHandler() {

        @Override
        public void onMouseOut(MouseOutEvent event) {
            {
                List<Axis> overviewAxes = Axis.allRelevant(timelineView.getPresenter().getCurrentInterval(), 0, 0, getEventBus());
                for (Axis axis : overviewAxes) {
                    axis.setTimeScale(timelineView.getPresenter().getTimeScale());
                }
                timelineView.getAxes().clear();
                timelineView.getAxes().addAll(overviewAxes);
                mouseOut = false;
                timelineView.renderFooter();
            }
            mouseOut = true;
            if (renderable == null) {
                return;
            }
            renderable.setHover(false);
            if (command != null) {
                command.cancel();
            }
            if(renderable.isDragging() || renderable.isPanning()) {
                command = new CancelRepeatingCommand() {
    
                    @Override
                    public boolean execute() {
                        if (!isCancel())
                            resetDrag();
                        return false;
                    }
    
                };
                Scheduler.get().scheduleFixedDelay(command, DRAG_TIMEOUT);
            }

            render();
        }
    };

    private MouseMoveHandler mouseMoveHandler = new MouseMoveHandler() {

        @Override
        public void onMouseMove(MouseMoveEvent event) {
            if(overviewTimeScale == null) {
                return;
            }
            if (renderable == null) {
                return;
            }

            eventLayer.stop();
                renderable.setHover(false);
            if (command != null) {
                command.cancel();
            }
            if (mouseOut && renderable.isDragging()) {
                command = new CancelRepeatingCommand() {

                    @Override
                    public boolean execute() {
                        if (!isCancel())
                            resetDrag();
                        return false;
                    }

                };

                Scheduler.get().scheduleFixedDelay(command, DRAG_TIMEOUT);
            }
            if (renderable == null) {
                eventLayer.start();
                return;
            }
            int thumbOffset = renderable.getThumbWidth() / 2;
            if (renderable.isLeftDragging()) {
                changed = true;
                int newX = event.getX() + thumbOffset;
                if (newX < renderable.getTimeScale().getLow()) {
                    newX = (int) renderable.getTimeScale().getLow();
                } else if (newX >= renderable.getEndX()) {
                    newX = renderable.getEndX() - 1;
                }
                long newTime = renderable.getTimeScale().toTime(newX);
                renderable.getTimelineSlider().setStart(newTime);
                render();
            } else if (renderable.isRightDragging()) {
                changed = true;
                int newX = event.getX() - thumbOffset;
                if (newX > renderable.getTimeScale().getHigh()) {
                    newX = (int) renderable.getTimeScale().getHigh();
                } else if (newX <= renderable.getStartX() + 1) {
                    newX = renderable.getStartX() + 2;
                }
                long newTime = renderable.getTimeScale().toTime(newX);
                renderable.getTimelineSlider().setEnd(newTime);
                render();
            } else if (renderable.isPanning()) {
                List<Axis> overviewAxes = Axis.allRelevant(overviewTimeScale.getInterval(), 0, 0, getEventBus());
                for (Axis axis : overviewAxes) {
                    axis.setTimeScale(overviewTimeScale);
                }
                timelineView.getAxes().clear();
                timelineView.getAxes().addAll(overviewAxes);
                mouseOut = false;
                timelineView.renderFooter();
                changed = true;

                int width = renderable.getEndX() - renderable.getStartX();

                int x = event.getX();
                int newStart = x - width / 2;
                int newEnd = x + width - width / 2;

                if (newStart < renderable.getTimeScale().getLow()) {

                    int difference = Math.abs((int) (newStart - renderable.getTimeScale().getLow()));
                    newStart = (int) renderable.getTimeScale().getLow();

                    // +1 needed to compensate for rounding error???
                    newEnd = newEnd + difference + 1;

                } else if (newEnd > renderable.getTimeScale().getHigh()) {

                    int difference = Math.abs((int) (newEnd - renderable.getTimeScale().getHigh()));
                    newEnd = (int) renderable.getTimeScale().getHigh();

                    // +1 needed to compensate for rounding error???
                    newStart = newStart - difference + 1;

                }

                long startTime = renderable.getTimeScale().toTime(newStart);
                renderable.getTimelineSlider().setStart(startTime);
                long endTime = renderable.getTimeScale().toTime(newEnd);
                renderable.getTimelineSlider().setEnd(endTime);

                render();

            } else {
                int x = event.getX();
                int thumbWidth = renderable.getThumbWidth();
                int startX = renderable.getStartX();
                int endX = renderable.getEndX();
                if (x <= startX - PANNING_BUFFER && x >= startX - thumbWidth) {
                    renderable.setHoverLeft(true);
                } else if (x >= endX + PANNING_BUFFER && x <= endX + thumbWidth) {
                    renderable.setHoverRight(true);
                } else if (x < endX + thumbWidth && x > startX - thumbWidth) {
                    renderable.setHoverCenter(true);
                } else {
                }
                render();
            }
            

            eventLayer.start();

        }

    };

    private MouseUpHandler mouseUpHandler = new MouseUpHandler() {

        @Override
        public void onMouseUp(MouseUpEvent event) {

            if (command != null) {
                command.cancel();
            }
            getElement().getStyle().setCursor(Cursor.POINTER);
            renderable.setDragging(false);
            renderable.setPanning(false);
            // render();
            if (!changed) {
                int x = event.getX();

                int endX = renderable.getEndX();
                int startX = renderable.getStartX();

                int difference = endX - startX;
                int offset = (endX - startX)/2;
                
                
                if(x < startX || x > endX) {
                    startX = x - offset;
                    if(startX < renderable.getTimeScale().getLow()) {
                        startX = (int) renderable.getTimeScale().getLow();
                    }
                    endX = startX+difference;
                    
                    if(endX > renderable.getTimeScale().getHigh()) {
                        endX = (int) renderable.getTimeScale().getHigh();
                        startX = endX - difference;
                    }

                    long newEndTime = renderable.getTimeScale().toTime(endX);
                    long newStartTime = renderable.getTimeScale().toTime(startX);

                    renderable.getTimelineSlider().setEnd(newEndTime);
                    renderable.getTimelineSlider().setStart(newStartTime);
                    changed = true;
                    
                }
                
//                if (x < startX) {
//                    
//                    
//                    long newTime = renderable.getTimeScale().toTime(x);
//                    if (x < renderable.getTimeScale().getLow()) {
//                        newTime = (long) renderable.getTimeScale().getStart();
//                    }
//                    renderable.getTimelineSlider().setStart(newTime);
//                    changed = true;
//                } else if (x > endX) {
//                    long newTime = renderable.getTimeScale().toTime(x);
//                    if (x > renderable.getTimeScale().getHigh()) {
//                        newTime = renderable.getTimeScale().getEnd();
//                    }
//                    renderable.getTimelineSlider().setEnd(newTime);
//                    changed = true;
//                }
            }

            if (changed) {
                OverviewChangeEvent changeEvent = new OverviewChangeEvent();
                changeEvent.setStart(renderable.getTimelineSlider().getStart());
                changeEvent.setEnd(renderable.getTimelineSlider().getEnd());
                // changeEvent
                eventBus.fireEvent(changeEvent);
                changed = false;
            }
            eventLayer.start();
        }

    };

    private final MouseWheelHandler mouseWheelHandler = new MouseWheelHandler() {

        @Override
        public void onMouseWheel(MouseWheelEvent event) {
            if(command != null) {
                command.cancel();
            }
            long magnitude = 1;
            if(scrollCommand != null  && ! scrollCommand.isCancel()) {
                magnitude = scrollCommand.getMagnitude();
                scrollCommand.setCancel(true);
                scrollCommand = null;
                magnitude++;
            }
            
            scrollCommand = new DebouncedZoomEvent();
            scrollCommand.setMagnitude(magnitude);
            int deltaY = getDeltaY(event);
            if (deltaY < 0) {
                scrollCommand.setIn(true);
            } else {
                scrollCommand.setIn(false);
            }
            render();
            scrollCommand.setX(event.getX());

            timelineView.getPresenter().preZoom(new ZoomEvent(event.getX(), scrollCommand.isIn(), true, magnitude));
            Scheduler.get().scheduleFixedDelay(scrollCommand, ZOOM_HANDLER_DELAY_MILLIS);
        }

    };

    private int getDeltaY(MouseWheelEvent event) {
        int deltaY = event.getDeltaY();
        if (deltaY == 0)
            deltaY = DetailFrame.internetExplorerWorkaroundForMouseWheel(event.getNativeEvent());
        return deltaY;
    }

    public void addSlider(TimelineSliderRenderable renderable) {
        sliderLayer.clear();
        if (this.renderable != null) {
            this.renderable.removeAllHandlers();
        }
        sliderLayer.addItem(renderable);
        this.renderable = renderable;
        renderable.addMouseDownHandler(mouseDownHandler);
        renderable.addMouseUpHandler(mouseUpHandler);
        renderable.addMouseMoveHandler(mouseMoveHandler);
        renderable.addMouseOverHandler(mouseOverHandler);
        renderable.addMouseWheelHandler(mouseWheelHandler);
        renderable.addMouseOutHandler(mouseOutHandler);
    }

    public void forceStart() {
        eventLayer.start();
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public abstract void setOverviewRenderables(List<T> overviewRenderables);

    public void setTimeScale(TimeScale timeScale) {
        timeScale.setLow(timeScale.getLow() + TimelineSliderRenderable.THUMB_WIDTH);
        timeScale.setHigh(timeScale.getHigh() - TimelineSliderRenderable.THUMB_WIDTH);
        this.overviewTimeScale = timeScale;
    }

    public TimeScale getTimeScale() {
        return this.overviewTimeScale;
    }

    public void updateTimeScale(int start, int end) {

        TimeScale timescale = getTimeScale();
        if (timescale != null)
            timescale.setNumberRange(start + TimelineSliderRenderable.THUMB_WIDTH,
                    end - TimelineSliderRenderable.THUMB_WIDTH);

        resetBackground();
    }

    private void fireZoom(int x, boolean in, long magnitude) {

        getEventBus().fireEvent(new ZoomEvent(x, in, true, magnitude));
    }

    public void setTimelineView(TimelineView timelineView) {
        this.timelineView = timelineView;
    }

    public TimelineView getTimelineView() {
        return timelineView;
    }

    private class DebouncedZoomEvent implements Scheduler.RepeatingCommand {

        private boolean cancel = false;
        private boolean in;
        private int x;
        private long magnitude;

        public DebouncedZoomEvent() {
        }

        public void setMagnitude(long magnitude) {
            this.magnitude = magnitude;
        }

        public long getMagnitude() {
            return this.magnitude;
        }

        @Override
        public boolean execute() {
            if (!cancel) {
                fireZoom(x, in, magnitude);
                setCancel(true);
                return false;
            } else {
                return true;
            }
        }

        public boolean isIn() {
            return in;
        }

        public void setIn(boolean in) {
            this.in = in;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public boolean isCancel() {
            return cancel;
        }

        public void setCancel(boolean cancel) {
            this.cancel = cancel;
        }
    }

    private void resetBackground() {
        if (backgroundRectangle != null) {
            backgroundRectangle.removeAllHandlers();
            backgroundLayer.removeAll();
            backgroundRectangle = null;
        }

        // Height & width fail-safes
        int height = 10000;
        int width = 10000;

        if (this.getOffsetHeight() > 20) {
            height = this.getOffsetHeight();
        }

        if (this.getOffsetWidth() > 200) {
            width = this.getOffsetWidth();
        }

        backgroundRectangle = new Rectangle(0, 0, width, height);

        backgroundLayer.addItem(backgroundRectangle);
        backgroundRectangle.addMouseUpHandler(mouseUpHandler);
        backgroundRectangle.addMouseMoveHandler(mouseMoveHandler);
        backgroundRectangle.addMouseOutHandler(mouseOutHandler);
        backgroundRectangle.addMouseOverHandler(mouseOverHandler);
        backgroundRectangle.addMouseWheelHandler(mouseWheelHandler);
        backgroundRectangle.setFillStyle(backgroundColor);

    }

    private void resetDrag() {
        getElement().getStyle().setCursor(Cursor.POINTER);
        renderable.cancelDrag();
        eventLayer.start();
        render();
    }

    public void reset() {
        eventLayer.clear();
    }

}
