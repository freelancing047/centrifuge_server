package csi.client.gwt.viz.timeline.view.drawing;

import java.util.Date;
import java.util.List;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
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
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.touch.client.Point;

import csi.client.gwt.viz.timeline.events.ScrollEvent;
import csi.client.gwt.viz.timeline.events.TooltipEvent;
import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.BandingRenderable;
import csi.client.gwt.viz.timeline.scheduler.CancelRepeatingCommand;
import csi.client.gwt.viz.timeline.view.drawing.layer.ComplexLayer;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Rectangle;
import csi.client.gwt.widget.drawing.Renderable;

public abstract class TimelineFrame extends DrawingPanel{

    protected static final int DRAG_SENSITIVITY = 4;
    protected static final int TOOLTIP_DELAY = 1200;
    protected ComplexLayer bandingLayer;
    protected Layer backgroundLayer;
    protected Layer zoomLayer;
    protected Layer tooltipLayer;
    protected Layer scrollbarLayer;

    protected static final int ZOOM_HANDLER_DELAY_MILLIS = 100;
    protected static final int SCROLL_HANDLER_DELAY_MILLIS = 10;

    protected boolean mouseIsDown = false;
	
    protected Point firstMouse;
    ;
    private EventBus eventBus;
    private Renderable currentlyTooltipped;
	protected Rectangle backgroundRectangle;
    protected ZoomRectangle zoomRectangle;
	protected CancelRepeatingCommand tooltipCommand;
    protected DebouncedScrollEvent scrollCommand = new DebouncedScrollEvent();

	private CssColor currentColor;

	public TimelineFrame(){
        bandingLayer = new ComplexLayer();
		backgroundLayer = new Layer();
		zoomLayer = new Layer();
		tooltipLayer = new Layer();
		scrollbarLayer = new Layer();
		
		addLayer(backgroundLayer);
		addLayer(bandingLayer);
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
		
		//this.addHandler(mouseWheelHandler, MouseWheelEvent.getType());
	}

	// TODO
	private final MouseMoveHandler hoverHandler = new MouseMoveHandler(){

        @Override
        public void onMouseMove(MouseMoveEvent event) {
//			outHoverHandler.onMouseOut(null);

            if(mouseIsDown){
                return;
            }

//            Optional<Renderable> optional = trackLayer.retrieveRenderableAtEvent(event);
//            if(optional.isPresent()){
//                TimelineTrackRenderable track = (TimelineTrackRenderable) optional.get();
//                track.setHighlight(true);
//                track.setSelectHover(event.isControlKeyDown());
//                lastHighlight = track;
//                TimelineFrame.this.render();
//            }
            
        }
        
    };
    
//    private final MouseOutHandler outHoverHandler = new MouseOutHandler(){
//
//
//        @Override
//        public void onMouseOut(MouseOutEvent event) {
//            if(lastHighlight != null) {
//				lastHighlight.setHighlight(false);
//				lastHighlight.setSelectHover(false);
//				lastHighlight = null;
//				TimelineFrame.this.render();
//			}
//
//        }
//        
//    };
    
    protected abstract MouseDownHandler getStartZoomHandler();

//	private final MouseDownHandler startZoomHandler = new MouseDownHandler(){
//
//		@Override
//		public void onMouseDown(MouseDownEvent event) {
//
//			// was this a right-click or ctrl-click? ignore.
//			//			if (e.isPopupTrigger())
//			//				return;
//
//			// TODO: might have to move this to the renderable
//			//			for (Mouseover o:objectLocations)
//			//			{
//			//				if (o.contains(event.getX(), event.getY()) && o.thing instanceof Interval)
//			//				{
//			//					moveTime((Interval)o.thing);
//			//					return;
//			//				}
//			//			}
//
//			// if not, prepare
//			if(!mouseIsDown){
//				firstMouse = new Point(event.getX(), event.getY());
//				mouseIsDown=true;
//			}
//			//repaint();
//		}
//	};
	
//	private final MouseWheelHandler mouseWheelHandler = new MouseWheelHandler(){
//
//		@Override
//		public void onMouseWheel(MouseWheelEvent event) {
//			int deltaY = getDeltaY(event);
//            if (deltaY < 0) {
//            	scrollCommand.setUp(true);
//            } else {
//                scrollCommand.setUp(false);
//            }
//            scrollCommand.resetTimer();
//			Scheduler.get().scheduleFixedDelay(scrollCommand, ZOOM_HANDLER_DELAY_MILLIS);
//		}
//
//	};

    protected int getDeltaY(MouseWheelEvent event) {
        int deltaY = event.getDeltaY();
        if(deltaY == 0)
            deltaY = internetExplorerWorkaroundForMouseWheel(event.getNativeEvent());
        return deltaY;
    }

    protected static native int internetExplorerWorkaroundForMouseWheel(NativeEvent evt) /*-{
        if (typeof evt.wheelDelta == "undefined") {
            return 0;
        }
        return Math.round(-evt.wheelDelta / 40) || 0;
    }-*/;
    

//
//	private final MouseUpHandler endZoomHandler = new MouseUpHandler(){
//
//		@Override
//		public void onMouseUp(MouseUpEvent event) {
//		    startComplexLayers();
//
//			if(zoomRectangle != null){
//				zoomLayer.remove(zoomRectangle);
//			}
//
//			if (!mouseIsDown){ // clicked on a date label.
//				return;
//			}
//
//			mouseIsDown=false;
//
//			// this is dark magic
//			int a=(int) Math.min(event.getX(), firstMouse.getX());
//			int b=(int) Math.max(event.getX(), firstMouse.getX());
//			if (b-a<16) // a click rather than a drag;
//			{
////			    Optional<Renderable> optional = trackLayer.retrieveRenderableAtEvent(event);
////                if(optional.isPresent()){
////					TimelineTrackRenderable track = (TimelineTrackRenderable) optional.get();
////
////					if(event.isControlKeyDown() && !event.isShiftKeyDown()){
////						track.getTrackModel().getEvents().forEach((e) -> e.setSelected(true));
////
////					}else if(event.isControlKeyDown() && event.isShiftKeyDown()){
////						track.getTrackModel().getEvents().forEach((e) -> e.setSelected(false));
////					}else {
////						track.toggleCollapse();
////					}
////
////                    getEventBus().fireEvent(new TrackChangeEvent(track.getTrackModel()));
////                }
//				return;
//			}
//			else
//			{
//				if(event.isControlKeyDown() && !event.isShiftKeyDown()){
//					getEventBus().fireEvent(new RangeSelectionEvent(a, b, true));
//
//				} else if(event.isControlKeyDown() && event.isShiftKeyDown()){
//					getEventBus().fireEvent(new RangeSelectionEvent(a, b, false));
//
//				}else {
//					TimeScaleChangeEvent timeScaleChangeEvent = new TimeScaleChangeEvent(a, b);
//					getEventBus().fireEvent(timeScaleChangeEvent);
//				}
//			}
//			startComplexLayers();
//
//		}
//
//	};

    public void stopComplexLayers() {
        for(Layer layer: getLayers()){
            if(layer instanceof ComplexLayer){
                ((ComplexLayer) layer).stop();
            }
        }
    }
    
    public void startComplexLayers() {
        for(Layer layer: getLayers()){
            if(layer instanceof ComplexLayer){
                ((ComplexLayer) layer).stop();
            }
        }
    }

	private MouseMoveHandler drawZoomHandler = new MouseMoveHandler(){

		@Override
		public void onMouseMove(MouseMoveEvent event) {
			if(mouseIsDown){
			    stopComplexLayers();
				if(event.isControlKeyDown()){
					drawZoomRectangle((int) firstMouse.getX(), event.getX(), CssColor.make(255,133,10));
				} else {
					drawZoomRectangle((int) firstMouse.getX(), event.getX(), CssColor.make(128,189,242));
				}
			}
		}};

		private final ClickHandler clickHandler = new ClickHandler(){

			public void onClick(ClickEvent event){
//				//event.getSource()
//				Optional<Renderable> hitTest = getEventLayer().hitTest(event.getX(), event.getY());
//				if(hitTest.isPresent()){
//					Renderable renderable = hitTest.get();
//					if(renderable instanceof EventRenderable){
//						EventRenderable eventRenderable = ((EventRenderable) renderable);
//						if(event.isControlKeyDown())
//							eventRenderable.setSelected(!eventRenderable.isSelected());
//
//
//						//TODO: render a single guy, Re-render the whole panel for now
//						TimelineFrame.this.render();
//					}
//				}
//				// Stop event, prevent highlighting of canvas
//				event.stopPropagation();
			}
		};

		private DoubleClickHandler doubleClickHandler = new DoubleClickHandler(){

			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				// Stop event, helps prevent highlighting of canvas
				event.stopPropagation();
			}
		};


//		public void setTracks(List<TimelineTrackRenderable> tracks){
//		    for(Renderable renderable: trackLayer.getRenderables()){
//                if(renderable instanceof TimelineTrackRenderable){
//                    ((TimelineTrackRenderable) renderable).deregisterHandlers();
//                }
//            }
//			trackLayer.removeAll();
//			bindTracks(tracks);
//		}

//		public void setEvents(List<EventRenderable> events){
//			//Deregister old handlers in case these are the same event object(usually are)
//			for(Renderable renderable: eventLayer.getRenderables()){
//				if(renderable instanceof EventRenderable){
//					((EventRenderable) renderable).deregisterHandlers();
//				}
//			}
//			eventLayer.removeAll();
//			bindEvents(events);
//		}
//
//		private void bindEvents(List<EventRenderable> events) {
//			for(EventRenderable event: events){
//				eventLayer.addItem(event);
//				addHandlers(event);
//			}
//		}
//		
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


//		private void addHandlers(final EventRenderable eventRenderable) {
//			List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
//			
//			handlers.add(eventRenderable.addClickHandler(clickHandler));
//			handlers.add(eventRenderable.addDoubleClickHandler(doubleClickHandler));
//			//handlers.add(eventRenderable.addMouseUpHandler(endZoomHandler));
//
//
//			handlers.add(eventRenderable.addMouseOverHandler(new MouseOverHandler(){
//				@Override
//				public void onMouseOver(MouseOverEvent event) {
//					if(mouseIsDown){
//						//Do something here? draw rectangle?
//						//Currently prevents bad behavior when drag-zooming
//					} else {
//						eventRenderable.setHighlight(true);
//						if(tooltipLayer.getRenderables() == null || tooltipLayer.getRenderables().isEmpty()){
//			                currentlyTooltipped = null;
//			            }
//						createTooltip(eventRenderable);
//						TimelineFrame.this.render();
//					}
//				}
//
//                }));
//
//			handlers.add(eventRenderable.addMouseMoveHandler(drawZoomHandler));
//			handlers.add(eventRenderable.addMouseOutHandler(new MouseOutHandler(){
//				@Override
//				public void onMouseOut(MouseOutEvent event) {
//					eventRenderable.setHighlight(false);
//					if(mouseIsDown){
//						//Do something here?  draw rectangle?
//						//Currently prevents bad behavior when drag-zooming
//					} else {
//						TimelineFrame.this.render();
//					}
//				}}));
//			
//			eventRenderable.setHandlers(handlers);
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
			backgroundRectangle.addMouseDownHandler(getStartZoomHandler());
			backgroundRectangle.addMouseMoveHandler(hoverHandler);
			backgroundRectangle.addMouseUpHandler(getEndZoomHandler());
			backgroundRectangle.addMouseMoveHandler(getDrawZoomHandler());
			backgroundRectangle.setFillStyle(color);
		}
				
		public void createTooltip(final Renderable renderable) {
            
		    
		    if(this.currentlyTooltipped == null || this.currentlyTooltipped != renderable){
    		    this.currentlyTooltipped = renderable;
    
    		    
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
                        TooltipEvent event = new TooltipEvent(renderable);
                        event.setHeight(height);
                        event.setWidth(width);
                        event.setLayer(tooltipLayer);
                        getEventBus().fireEvent(event);
                        return false;
                    }};
    		    
    		    Scheduler.get().scheduleFixedDelay(tooltipCommand, TOOLTIP_DELAY);
		    }

        }
		

        protected abstract MouseUpHandler getEndZoomHandler();
        protected abstract MouseMoveHandler getDrawZoomHandler();

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
			zoomRectangle.addMouseUpHandler(getEndZoomHandler());
			zoomRectangle.addMouseMoveHandler(getDrawZoomHandler());
			this.render();
		}



        public void render(int bottom) {
			render();
			

		}
		
		public void render() {
		    //startLayers();
		    if(tooltipLayer.getRenderables() == null || tooltipLayer.getRenderables().isEmpty()){
                currentlyTooltipped = null;
            }
		    super.render();
		    //TODO: want to render once and stop layers, but can't because render is on scheduler
		    //stopLayers();
		}
		
		
        private void fireScroll(boolean up) {
		    
            getEventBus().fireEvent(new ScrollEvent(up));
        }

        public DebouncedScrollEvent getScrollCommand() {
            return scrollCommand;
        }
		
		protected class DebouncedScrollEvent implements Scheduler.RepeatingCommand {

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
                refreshAt = new Date().getTime() + SCROLL_HANDLER_DELAY_MILLIS;
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
            for(Layer layer: getLayers()){
                layer.clear();
            }
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

       
}
