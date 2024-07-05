package csi.client.gwt.viz.timeline.view.drawing;

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

import csi.client.gwt.viz.timeline.events.RangeSelectionEvent;
import csi.client.gwt.viz.timeline.events.ScrollEvent;
import csi.client.gwt.viz.timeline.events.SelectionChangeEvent;
import csi.client.gwt.viz.timeline.events.TimeScaleChangeEvent;
import csi.client.gwt.viz.timeline.events.TooltipEvent;
import csi.client.gwt.viz.timeline.events.TrackChangeEvent;
import csi.client.gwt.viz.timeline.events.TrackSelectionEvent;
import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.BandingRenderable;
import csi.client.gwt.viz.timeline.model.DetailedEventProxy;
import csi.client.gwt.viz.timeline.scheduler.CancelRepeatingCommand;
import csi.client.gwt.viz.timeline.view.drawing.layer.ComplexLayer;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Rectangle;
import csi.client.gwt.widget.drawing.Renderable;

public class DetailFrame extends TimelineFrame{

    private ComplexLayer eventLayer;
    private ComplexLayer trackNameLayer;
    private ComplexLayer trackLayer;

	private boolean mouseIsDown = false;
	
	private Point firstMouse;

    private TimelineTrackRenderable lastHighlight;
    private DetailedEventRenderable currentlyTooltipped;
    protected DebouncedScrollEvent scrollCommand = new DebouncedScrollEvent();

    private String trackName;
	private CssColor currentColor;

	public DetailFrame(){
		eventLayer = new ComplexLayer();
        trackLayer = new ComplexLayer();
        bandingLayer = new ComplexLayer();
        trackNameLayer = new ComplexLayer();
		trackLayer.setEventPassingEnabled(false);
		backgroundLayer = new Layer();
		zoomLayer = new Layer();
		tooltipLayer = new Layer();
		scrollbarLayer = new Layer();
		
		addLayer(backgroundLayer);
		addLayer(bandingLayer);
		addLayer(trackLayer);
        addLayer(trackNameLayer);
		addLayer(eventLayer);
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

            Optional<Renderable> optional = trackLayer.retrieveRenderableAtEvent(event);
            if(optional.isPresent()){
                TimelineTrackRenderable track = (TimelineTrackRenderable) optional.get();
                track.setHighlight(true);
                track.setSelectHover(event.isControlKeyDown());
                lastHighlight = track;
                DetailFrame.this.render();
            }
            
        }
        
    };
    
    private final MouseOutHandler outHoverHandler = new MouseOutHandler(){


        @Override
        public void onMouseOut(MouseOutEvent event) {
            if(lastHighlight != null) {
				lastHighlight.setHighlight(false);
				lastHighlight.setSelectHover(false);
				lastHighlight = null;
				DetailFrame.this.render();
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
            	scrollCommand.setUp(true);
            } else {
                scrollCommand.setUp(false);
            }
            scrollCommand.resetTimer();
			Scheduler.get().scheduleFixedDelay(scrollCommand, SCROLL_HANDLER_DELAY_MILLIS);
		}

	};



	private final MouseUpHandler endZoomHandler = new MouseUpHandler(){

		@Override
		public void onMouseUp(MouseUpEvent event) {
		    eventLayer.start();

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
			boolean shiftKeyDown = event.isShiftKeyDown();
            if (b-a<16) // a click rather than a drag;
			{
			    Optional<Renderable> optional = trackLayer.retrieveRenderableAtEvent(event);
                if(optional.isPresent()){
					TimelineTrackRenderable track = (TimelineTrackRenderable) optional.get();

					if(event.isControlKeyDown() ) {
                        List<Integer> ids = new ArrayList<Integer>();
                        for(DetailedEventProxy detailedEventProxy: track.getTrackModel().getEvents()) {
                            ids.add(detailedEventProxy.getEvent().getEventDefinitionId());
                            detailedEventProxy.setSelected(!shiftKeyDown);
                        }
                        
    	                getEventBus().fireEvent(new TrackSelectionEvent(ids, !shiftKeyDown));
    	                
					}else {
						track.toggleCollapse();
					}

                    getEventBus().fireEvent(new TrackChangeEvent(track.getTrackModel()));
                }
				return;
			}
			else
			{
				if(event.isControlKeyDown() && !shiftKeyDown){
					getEventBus().fireEvent(new RangeSelectionEvent(a, b, true, trackName));

				} else if(event.isControlKeyDown() && shiftKeyDown){
					getEventBus().fireEvent(new RangeSelectionEvent(a, b, false, trackName));

				}else {
					TimeScaleChangeEvent timeScaleChangeEvent = new TimeScaleChangeEvent(a, b);
					getEventBus().fireEvent(timeScaleChangeEvent);
				}
			}
			eventLayer.start();

		}

	};

	private MouseMoveHandler drawZoomHandler = new MouseMoveHandler(){

		@Override
		public void onMouseMove(MouseMoveEvent event) {
			if(mouseIsDown){
			    eventLayer.stop();
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
				Optional<Renderable> hitTest = getEventLayer().hitTest(event.getX(), event.getY());
				if(hitTest.isPresent()){
					Renderable renderable = hitTest.get();
					if(renderable instanceof DetailedEventRenderable){
						DetailedEventRenderable eventRenderable = ((DetailedEventRenderable) renderable);
						if(event.isControlKeyDown()){
							eventRenderable.setSelected(!eventRenderable.isSelected());

	                        getEventBus().fireEvent(new SelectionChangeEvent(eventRenderable));
						}

						//TODO: render a single guy, Re-render the whole panel for now
						DetailFrame.this.render();
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


		public void setTracks(List<TimelineTrackRenderable> tracks){
		    for(Renderable renderable: trackLayer.getRenderables()){
                if(renderable instanceof TimelineTrackRenderable){
                    ((TimelineTrackRenderable) renderable).deregisterHandlers();
                }
            }
			trackLayer.removeAll();
			bindTracks(tracks);
		}

		public void setEvents(List<DetailedEventRenderable> events){
			//Deregister old handlers in case these are the same event object(usually are)
			for(Renderable renderable: eventLayer.getRenderables()){
				if(renderable instanceof DetailedEventRenderable){
					((DetailedEventRenderable) renderable).deregisterHandlers();
				}
			}
			eventLayer.removeAll();
			bindEvents(events);
		}

		private void bindEvents(List<DetailedEventRenderable> events) {
			for(DetailedEventRenderable event: events){
				eventLayer.addItem(event);
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


		private void addHandlers(final DetailedEventRenderable eventRenderable) {
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
						if(tooltipLayer.getRenderables() == null || tooltipLayer.getRenderables().isEmpty()){
			                currentlyTooltipped = null;
			            }
						createTooltip(eventRenderable);
						DetailFrame.this.render();
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
						DetailFrame.this.render();
					}
				}}));
			
			eventRenderable.setHandlers(handlers);
		}

		
		private void bindTracks(List<TimelineTrackRenderable> tracks) {
			for(TimelineTrackRenderable track: tracks){
				trackLayer.addItem(track);
				
			}
		}

		public Layer getEventLayer() {
			return eventLayer;
		}

		public void setEventLayer(ComplexLayer eventLayer) {
			this.eventLayer = eventLayer;
		}

		public Layer getTrackLayer() {
			return trackLayer;
		}
		
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
            
		    
		    if(this.currentlyTooltipped == null || eventRenderable.getEvent().getEvent().getEventDefinitionId() != this.currentlyTooltipped.getEvent().getEvent().getEventDefinitionId()){
    		    this.currentlyTooltipped = eventRenderable;
    
    		    
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
			this.trackLayer = trackLayer;
		}


		public void render(int bottom) {
			render();
		}
		
		public void render() {
		    startLayers();
		    if(tooltipLayer.getRenderables() == null || tooltipLayer.getRenderables().isEmpty()){
                currentlyTooltipped = null;
            }
		    //drawTrackName();
		    super.render();
		    //TODO: want to render once and stop layers, but can't because render is on scheduler
		    //stopLayers();
		}
		
		
		private void stopLayers() {
		    eventLayer.stop();
            trackLayer.stop();
            bandingLayer.stop();
            trackNameLayer.stop();
        }

        private void startLayers() {
            eventLayer.start();
            trackLayer.start();
            bandingLayer.start();
            trackNameLayer.start();
        }
        
        public void drawTrackName(){
			 /*   if (trackNameRenderable != null) {

				//Height fail-safes
				int height = 10000;
				if(this.getOffsetHeight() > 200){
					height = this.getOffsetHeight();
				}


				trackNameRenderable = new TrackBreadCrumb();//height - 50);

				trackNameRenderable.setLabel(trackName);
				//trackNameRenderable.setFillStyle(color);
				trackNameLayer.addItem(trackNameRenderable);
	//            trackNameRenderable.addMouseMoveHandler(new MouseMoveHandler(){
	//
	//                @Override
	//                public void onMouseMove(MouseMoveEvent event) {
	//
	//                }});
				trackNameRenderable.addMouseDownHandler(startZoomHandler);
				trackNameRenderable.addMouseUpHandler(endZoomHandler);

				}

				if(trackName == null){
					trackNameLayer.setVisible(false);
				} else {
					trackNameLayer.setVisible(true);
				}

				this.render();*/
            
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
            trackLayer.clear();
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
            // TODO Auto-generated method stub
            return startZoomHandler;
        }

        @Override
        protected MouseUpHandler getEndZoomHandler() {
            // TODO Auto-generated method stub
            return endZoomHandler;
        }

        @Override
        protected MouseMoveHandler getDrawZoomHandler() {
            // TODO Auto-generated method stub
            return drawZoomHandler;
        }

        public void setTrackName(String trackName) {
            this.trackName = trackName;
        }
}
