package csi.client.gwt.viz.timeline.view.summary.drawing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Optional;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.touch.client.Point;

import csi.client.gwt.viz.timeline.events.CrumbRemovedEvent;
import csi.client.gwt.viz.timeline.events.RangeSelectionEvent;
import csi.client.gwt.viz.timeline.events.ScrollEvent;
import csi.client.gwt.viz.timeline.events.SelectionChangeEvent;
import csi.client.gwt.viz.timeline.events.TimeScaleChangeEvent;
import csi.client.gwt.viz.timeline.events.TooltipEvent;
import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.BandingRenderable;
import csi.client.gwt.viz.timeline.model.measured.MeasuredTrackProxy;
import csi.client.gwt.viz.timeline.scheduler.CancelRepeatingCommand;
import csi.client.gwt.viz.timeline.view.drawing.DetailedEventRenderable;
import csi.client.gwt.viz.timeline.view.drawing.EventRenderable;
import csi.client.gwt.viz.timeline.view.drawing.ScrollbarRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TimelineFrame;
import csi.client.gwt.viz.timeline.view.drawing.TrackBreadCrumb;
import csi.client.gwt.viz.timeline.view.drawing.ZoomRectangle;
import csi.client.gwt.viz.timeline.view.drawing.layer.ComplexLayer;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Rectangle;
import csi.client.gwt.widget.drawing.Renderable;

public class SummaryFrame extends TimelineFrame{

    private ComplexLayer eventLayer;
    private ComplexLayer detailedEventLayer;
    private ComplexLayer trackNameLayer;

    private Point firstMouse;

    private EventRenderable lastHighlight;

    private String trackName = null;
    private CssColor currentColor;
    private DetailedEventRenderable currentlyTooltipped;

    public SummaryFrame(){
        detailedEventLayer = new ComplexLayer();
        eventLayer = new ComplexLayer();
        trackNameLayer = new ComplexLayer();
        bandingLayer = new ComplexLayer();
        //eventLayer.setEventPassingEnabled(false);
        backgroundLayer = new Layer();
        zoomLayer = new Layer();
        tooltipLayer = new Layer();
        scrollbarLayer = new Layer();

        addLayer(backgroundLayer);
        addLayer(bandingLayer);
        addLayer(trackNameLayer);
        addLayer(eventLayer);
        addLayer(detailedEventLayer);
        addLayer(zoomLayer);
        addLayer(tooltipLayer);
        addLayer(scrollbarLayer);

        this.bringToFront(scrollbarLayer);

        //We set the background late so that there is a chance for the TimelinePanel to set height & width
        Scheduler.get().scheduleDeferred(new ScheduledCommand(){

            @Override
            public void execute() {

                setCurrentBackgroundColor(CssColor.make(255, 255, 255));
            }});

        this.addHandler(mouseWheelHandler, MouseWheelEvent.getType());
    }

    // TODO
    private final MouseMoveHandler hoverHandler = new MouseMoveHandler(){

        @Override
        public void onMouseMove(MouseMoveEvent event) {
            outHoverHandler.onMouseOut(null);

            if(mouseIsDown){
                return;
            }

            Optional<Renderable> optional;
            if(!eventLayer.getRenderables().isEmpty()){
                optional = eventLayer.retrieveRenderableAtEvent(event);
            } else {
                optional = detailedEventLayer.retrieveRenderableAtEvent(event);
            } 
            
            
            if(optional.isPresent()){
                EventRenderable track = (EventRenderable) optional.get();
                track.setHighlight(true);
                //track.setSelectHover(event.isControlKeyDown());
                lastHighlight = track;
                SummaryFrame.this.render();
            } else {
                optional = trackNameLayer.retrieveRenderableAtEvent(event);
                if(optional.isPresent()){
                    TrackBreadCrumb breadCrumb = (TrackBreadCrumb) optional.get();
                    breadCrumb.setHighlight(true);
                    SummaryFrame.this.render();
                }
            }

        }

    };

    private final MouseOutHandler outHoverHandler = new MouseOutHandler(){


        @Override
        public void onMouseOut(MouseOutEvent event) {
            if(lastHighlight != null) {
                lastHighlight.setHighlight(false);
                //lastHighlight.setSelectHover(false);
                lastHighlight = null;
                SummaryFrame.this.render();
            }

        }

    };

    private final MouseDownHandler startZoomHandler = new MouseDownHandler(){

        @Override
        public void onMouseDown(MouseDownEvent event) {

            // was this a right-click or ctrl-click? ignore.
            //			if (e.isPopupTrigger())
            //				return;

            // TODO: might have to move this to the renderable
            //			for (Mouseover o:objectLocations)
            //			{
            //				if (o.contains(event.getX(), event.getY()) && o.thing instanceof Interval)
            //				{
            //					moveTime((Interval)o.thing);
            //					return;
            //				}
            //			}

            // if not, prepare
            if(!mouseIsDown){
                firstMouse = new Point(event.getX(), event.getY());
                mouseIsDown=true;
            }
            //repaint();
        }
    };

    private final MouseWheelHandler mouseWheelHandler = new MouseWheelHandler(){

        @Override
        public void onMouseWheel(MouseWheelEvent event) {
            int deltaY = getDeltaY(event);
            if (deltaY < 0) {
                getScrollCommand().setUp(true);
            } else {
                getScrollCommand().setUp(false);
            }
            getScrollCommand().resetTimer();
            Scheduler.get().scheduleFixedDelay(getScrollCommand(), ZOOM_HANDLER_DELAY_MILLIS);
        }

    };



    private final MouseUpHandler endZoomHandler = new MouseUpHandler(){

        @Override
        public void onMouseUp(MouseUpEvent event) {
            startLayers();

            if(zoomRectangle != null){
                zoomLayer.remove(zoomRectangle);
            }

            if (!mouseIsDown){ // clicked on a date label.
                return;
            }

            mouseIsDown=false;

            // this is dark magic
            int a=(int) Math.min(event.getX(), firstMouse.getX());
            int b=(int) Math.max(event.getX(), firstMouse.getX());
            if (b-a<DRAG_SENSITIVITY) // a click rather than a drag;
            {
                Optional<Renderable> optional;
                if(!eventLayer.getRenderables().isEmpty()){
                    optional = eventLayer.retrieveRenderableAtEvent(event);
                } else {
                    optional = detailedEventLayer.retrieveRenderableAtEvent(event);
                }
                if(optional.isPresent()){
                    //MeasuredTrackRenderable track = (MeasuredTrackRenderable) optional.get();

                    if(event.isControlKeyDown() && !event.isShiftKeyDown()){
                        //no-op no selection

                    }else if(event.isControlKeyDown() && event.isShiftKeyDown()){
                        //no-op no selection);
                    }else {
                        //drillToTrack(track.getTrack());
                    }

                    //getEventBus().fireEvent(new TrackChangeEvent(track.getTrackModel()));
                } else {
                    optional = trackNameLayer.retrieveRenderableAtEvent(event);
                    if(optional.isPresent()){
                        TrackBreadCrumb crumb = (TrackBreadCrumb) optional.get();

                        getEventBus().fireEvent(new CrumbRemovedEvent());
                    }
                }
                return;
            }
            else
            {
                if(event.isControlKeyDown() && !event.isShiftKeyDown()){
                    getEventBus().fireEvent(new RangeSelectionEvent(a, b, true, trackName));

                } else if(event.isControlKeyDown() && event.isShiftKeyDown()){
                    getEventBus().fireEvent(new RangeSelectionEvent(a, b, false, trackName));

                }else {
                    TimeScaleChangeEvent timeScaleChangeEvent = new TimeScaleChangeEvent(a, b);
                    getEventBus().fireEvent(timeScaleChangeEvent);
                }
            }
            startLayers();

        }

        private void drillToTrack(MeasuredTrackProxy track) {
            // TODO Auto-generated method stub

        }

    };

    private MouseMoveHandler drawZoomHandler = new MouseMoveHandler(){

        @Override
        public void onMouseMove(MouseMoveEvent event) {
            if(mouseIsDown){
                stopLayers();
                if(event.isControlKeyDown()){
                    drawZoomRectangle((int) firstMouse.getX(), event.getX(), CssColor.make(255,133,10));
                } else {
                    drawZoomRectangle((int) firstMouse.getX(), event.getX(), CssColor.make(128,189,242));
                }
            }
        }};

        private final ClickHandler clickHandler = new ClickHandler(){

            public void onClick(ClickEvent event){
                //event.getSource()
                Optional<Renderable> hitTest;
                if(!eventLayer.getRenderables().isEmpty()){
                    hitTest = eventLayer.retrieveRenderableAtEvent(event);
                } else {
                    hitTest = detailedEventLayer.retrieveRenderableAtEvent(event);
                }
                if(hitTest.isPresent()){
                    Renderable renderable = hitTest.get();
                    if(renderable instanceof DetailedEventRenderable){
                        DetailedEventRenderable eventRenderable = ((DetailedEventRenderable) renderable);
                        if(event.isControlKeyDown()){
                            eventRenderable.setSelected(!eventRenderable.isSelected());

                            getEventBus().fireEvent(new SelectionChangeEvent(eventRenderable));
                        }

                        //TODO: render a single guy, Re-render the whole panel for now
                        SummaryFrame.this.render();
                    } else if(renderable instanceof SummaryEventRenderable){
                        SummaryEventRenderable eventRenderable = ((SummaryEventRenderable) renderable);
                        if(event.isControlKeyDown()){
                            eventRenderable.setSelected(!eventRenderable.isSelected());
                            getEventBus().fireEvent(new SelectionChangeEvent(eventRenderable));
                        }


                        //TODO: render a single guy, Re-render the whole panel for now
                        SummaryFrame.this.render();
                    }
                }
                // Stop event, prevent highlighting of canvas
                event.stopPropagation();
            }
        };

        private DoubleClickHandler doubleClickHandler = new DoubleClickHandler(){

            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                // Stop event, helps prevent highlighting of canvas
                event.stopPropagation();
            }
        };
        private EventBus eventBus;
        //
        //		}

        public void setEvents(List<SummaryEventRenderable> events){
            //Deregister old handlers in case these are the same event object(usually are)
            for(Renderable renderable: eventLayer.getRenderables()){
                if(renderable instanceof DetailedEventRenderable){
                    ((SummaryEventRenderable) renderable).deregisterHandlers();
                }
            }
            eventLayer.removeAll();
            bindEvents(events);
        }

        public void setDetailedEvents(List<DetailedEventRenderable> events){
            //Deregister old handlers in case these are the same event object(usually are)
            for(Renderable renderable: detailedEventLayer.getRenderables()){
                if(renderable instanceof DetailedEventRenderable){
                    ((DetailedEventRenderable) renderable).deregisterHandlers();
                }
            }
            detailedEventLayer.removeAll();
            bindDetailedEvents(events);
        }

        private void bindEvents(List<SummaryEventRenderable> events) {
            if(events == null){
                return;
            }
            for(SummaryEventRenderable event: events){
                eventLayer.addItem(event);
                addHandlers(event);
            }
        }

        private void bindDetailedEvents(List<DetailedEventRenderable> events) {
            if(events == null){
                return;
            }
            for(DetailedEventRenderable event: events){
                detailedEventLayer.addItem(event);
                addHandlers(event);
            }
        }

        public void setScrollbar(final ScrollbarRenderable scrollbarRenderable){

            for(Renderable renderable: scrollbarLayer.getRenderables()){
                if(renderable instanceof ScrollbarRenderable){
                    ((ScrollbarRenderable) renderable).deregisterHandlers();
                }
            }

            scrollbarLayer.removeAll();
            scrollbarRenderable.addMouseUpHandler(new MouseUpHandler(){

                @Override
                public void onMouseUp(MouseUpEvent event) {

                    boolean fire = scrollbarRenderable.mouseUp(event);

                    if(fire){
                        getEventBus().fireEvent(new ScrollEvent());
                    }
                }});

            scrollbarRenderable.addMouseDownHandler(new MouseDownHandler(){

                @Override
                public void onMouseDown(MouseDownEvent event) {

                    boolean fire = scrollbarRenderable.mouseDown(event);

                    if(fire){
                        getEventBus().fireEvent(new ScrollEvent());
                    }
                }});
            scrollbarRenderable.addMouseMoveHandler(new MouseMoveHandler(){

                @Override
                public void onMouseMove(MouseMoveEvent event) {

                    boolean fire = scrollbarRenderable.mouseMove(event);

                    if(fire){
                        getEventBus().fireEvent(new ScrollEvent());
                    }
                }});

            scrollbarRenderable.addMouseOutHandler(new MouseOutHandler(){

                @Override
                public void onMouseOut(MouseOutEvent event) {
                    boolean fire = scrollbarRenderable.mouseOut(event);

                    if(fire){
                        getEventBus().fireEvent(new ScrollEvent());
                    }
                }});

            scrollbarLayer.addItem(scrollbarRenderable);
        }


        private void addHandlers(final EventRenderable eventRenderable) {
            List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

            handlers.add(eventRenderable.addClickHandler(clickHandler));
            handlers.add(eventRenderable.addDoubleClickHandler(doubleClickHandler));
            handlers.add(eventRenderable.addMouseUpHandler(endZoomHandler));


            handlers.add(eventRenderable.addMouseOverHandler(new MouseOverHandler(){

                @Override
                public void onMouseOver(MouseOverEvent event) {
                    if(mouseIsDown){
                        //Do something here? draw rectangle?
                        //Currently prevents bad behavior when drag-zooming
                    } else {
                        eventRenderable.setHighlight(true);

                        if(eventRenderable instanceof DetailedEventRenderable) {
                            if(tooltipLayer.getRenderables() == null || tooltipLayer.getRenderables().isEmpty()){
                                currentlyTooltipped = null;
                            }
                            createTooltip(eventRenderable);
                            }
                        SummaryFrame.this.render();
                    }
                }

            }));
            
            handlers.add(eventRenderable.addMouseMoveHandler(drawZoomHandler));
            handlers.add(eventRenderable.addMouseOutHandler(new MouseOutHandler(){
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    eventRenderable.setHighlight(false);
                    if(mouseIsDown){
                        //Do something here?  draw rectangle?
                        //Currently prevents bad behavior when drag-zooming
                    } else {
                        SummaryFrame.this.render();
                    }
                }}));

            eventRenderable.setHandlers(handlers);
        }

        //		
        //		private void bindTracks(List<MeasuredTrackRenderable> tracks) {
        //			for(MeasuredTrackRenderable track: tracks){
        //				trackLayer.addItem(track);
        //				
        //			}
        //		}



        public void resetBackground(){
            setCurrentBackgroundColor(currentColor);
        }

        public void setCurrentBackgroundColor(CssColor color) {
            currentColor = color;
            if (backgroundRectangle != null) {
                backgroundLayer.remove(backgroundRectangle);
                backgroundRectangle = null;
            }

            //Height & width fail-safes
            int height = 10000;
            int width = 10000;

            if(this.getOffsetHeight() > 200){
                height = this.getOffsetHeight();
            }

            if(this.getOffsetWidth() > 200){
                width = this.getOffsetWidth();
            }

            backgroundRectangle = new Rectangle(0, 0, width, height);

            backgroundLayer.addItem(backgroundRectangle);
            backgroundRectangle.addMouseDownHandler(startZoomHandler);
            backgroundRectangle.addMouseMoveHandler(hoverHandler);
            backgroundRectangle.addMouseUpHandler(endZoomHandler);
            backgroundRectangle.addMouseMoveHandler(drawZoomHandler);
            backgroundRectangle.setFillStyle(color);
        }

        private void createTooltip(final DetailedEventRenderable eventRenderable) {


            //		    if(this.currentlyTooltipped == null || eventRenderable.getEvent().getEvent().getEventDefinitionId() != this.currentlyTooltipped.getEvent().getEvent().getEventDefinitionId()){
            //    		    this.currentlyTooltipped = eventRenderable;
            //    

            final int height = this.getOffsetHeight();
            final int width = this.getOffsetWidth();

            if(tooltipCommand != null){
                tooltipCommand.cancel();
            }

            if(height == 0 || width == 0){
                return;
            }

            tooltipCommand = new CancelRepeatingCommand(){

                @Override
                public boolean execute() {
                    if(isCancel()){
                        return false;
                    }
                    TooltipEvent event = new TooltipEvent(eventRenderable);
                    event.setHeight(height);
                    event.setWidth(width);
                    event.setLayer(tooltipLayer);
                    getEventBus().fireEvent(event);
                    return false;
                }};

                Scheduler.get().scheduleFixedDelay(tooltipCommand, TOOLTIP_DELAY);
        }

        // }

        public void drawTrackName(){
          /*  if(trackNameRenderable == null){
                trackNameRenderable = new TrackBreadCrumb();//height - 50);
                //trackNameRenderable.setFillStyle(color);
                trackNameLayer.addItem(trackNameRenderable);
                //              trackNameRenderable.addMouseMoveHandler(new MouseMoveHandler(){
                //
                //                  @Override
                //                  public void onMouseMove(MouseMoveEvent event) {
                //                      
                //                  }});
                trackNameRenderable.addMouseDownHandler(startZoomHandler);
                trackNameRenderable.addMouseUpHandler(endZoomHandler);
                trackNameRenderable.addMouseMoveHandler(hoverHandler);
                trackNameRenderable.addMouseOutHandler(new MouseOutHandler(){
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    trackNameRenderable.setHighlight(false);
                    if(mouseIsDown){
                        //Do something here?  draw rectangle?
                        //Currently prevents bad behavior when drag-zooming
                    } else {
                        SummaryFrame.this.render();
                    }
                }});
            }

            if(trackName == null){
                trackNameLayer.setVisible(false);
                trackNameRenderable.setLabel("");
            } else {
                trackNameLayer.setVisible(true);

                trackNameRenderable.setLabel(trackName);
            }*/
            

        }

        public void drawZoomRectangle(int startX,  int endX, CssColor color){
            if (zoomRectangle != null) {
                zoomLayer.remove(zoomRectangle);
                zoomRectangle = null;
            }

            //Height fail-safes
            int height = 10000;
            if(this.getOffsetHeight() > 200){
                height = this.getOffsetHeight();
            }

            int min = Math.min(startX, endX);
            int max = Math.max(startX, endX);

            zoomRectangle = new ZoomRectangle(min, 0, max-min, height);
            zoomRectangle.setFillStyle(color);
            zoomLayer.addItem(zoomRectangle);
            zoomRectangle.addMouseUpHandler(endZoomHandler);
            zoomRectangle.addMouseMoveHandler(drawZoomHandler);
            this.render();
        }

        public void setTrackLayer(ComplexLayer trackLayer) {
            this.eventLayer = trackLayer;
        }


        public void render(int bottom) {
            render();


        }

        public void render() {
            //startLayers();
            if(tooltipLayer.getRenderables() == null || tooltipLayer.getRenderables().isEmpty()){
                //                currentlyTooltipped = null;
            }

            drawTrackName();
            super.render();
            //TODO: want to render once and stop layers, but can't because render is on scheduler
            //stopLayers();
        }


        private void stopLayers() {
            detailedEventLayer.stop();
            eventLayer.stop();
            bandingLayer.start();
        }

        private void startLayers() {
            detailedEventLayer.start();
            eventLayer.start();
            bandingLayer.start();
        }

        private void fireScroll(boolean up) {

            getEventBus().fireEvent(new ScrollEvent(up));
        }



        private class DebouncedScrollEvent implements Scheduler.RepeatingCommand {

            private long refreshAt;
            private boolean up;

            public DebouncedScrollEvent() {
                resetTimer();
            }

            @Override
            public boolean execute() {
                long time = new Date().getTime();
                if (time > refreshAt) {
                    fireScroll(up);
                    return false;
                } else {
                    return true;
                }
            }

            public void resetTimer() {
                refreshAt = new Date().getTime() + ZOOM_HANDLER_DELAY_MILLIS;
            }

            public boolean isUp() {
                return up;
            }

            public void setUp(boolean in) {
                this.up = in;
            }

        }

        public Layer getTooltipLayer() {
            return tooltipLayer;
        }

        public void reset() {
            detailedEventLayer.clear();
            eventLayer.clear();
            tooltipLayer.clear();
            bandingLayer.clear();
        }

        public void setAxes(List<Axis> axes) {
            for(Renderable renderable: bandingLayer.getRenderables()){
                if(renderable instanceof Axis){
                    ((Axis) renderable).deregisterHandlers();
                }
            }
            bandingLayer.removeAll();
            bindAxes(axes);
        }
        private void bindAxes(List<Axis> axes) {
            if(axes == null || axes.size() == 0){
                return;
            }
            Axis axis = axes.get(axes.size()-1);
            bandingLayer.addItem(new BandingRenderable(axis));

        }

        public void setEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
        }

        public EventBus getEventBus(){
            return this.eventBus;
        }

        @Override
        protected MouseDownHandler getStartZoomHandler() {
            return startZoomHandler;
        }

        @Override
        protected MouseUpHandler getEndZoomHandler() {
            return endZoomHandler;
        }

        @Override
        protected MouseMoveHandler getDrawZoomHandler() {
            return drawZoomHandler;
        }

        public void setTrackName(String trackName2) {
            this.trackName = trackName2;
        }
}
