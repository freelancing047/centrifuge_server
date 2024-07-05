package csi.client.gwt.viz.timeline.view.measured.drawing;

import com.google.common.base.Optional;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.touch.client.Point;
import csi.client.gwt.WebMain;
import csi.client.gwt.viz.timeline.events.RangeSelectionEvent;
import csi.client.gwt.viz.timeline.events.ScrollEvent;
import csi.client.gwt.viz.timeline.events.TimeScaleChangeEvent;
import csi.client.gwt.viz.timeline.events.TooltipEvent;
import csi.client.gwt.viz.timeline.events.TrackDrillEvent;
import csi.client.gwt.viz.timeline.model.Axis;
import csi.client.gwt.viz.timeline.model.BandingRenderable;
import csi.client.gwt.viz.timeline.model.measured.MeasuredTrackProxy;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.viz.timeline.scheduler.CancelRepeatingCommand;
import csi.client.gwt.viz.timeline.view.drawing.DetailedEventRenderable;
import csi.client.gwt.viz.timeline.view.drawing.ScrollbarRenderable;
import csi.client.gwt.viz.timeline.view.drawing.TimelineFrame;
import csi.client.gwt.viz.timeline.view.drawing.TimelineTrackRenderable;
import csi.client.gwt.viz.timeline.view.drawing.ZoomRectangle;
import csi.client.gwt.viz.timeline.view.drawing.layer.ComplexLayer;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Rectangle;
import csi.client.gwt.widget.drawing.Renderable;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.server.common.service.api.ChronosActionsServiceProtocol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeasureFrame extends TimelineFrame{

    private ComplexLayer trackLayer;

	private Point firstMouse;

    private MeasuredTrackRenderable lastHighlight;

	private CssColor currentColor;
    private long lastRender = 0 ;
	private DrawingPanel zoomDrawingPanel;
	private TimelinePresenter timelinePresenter;

	public MeasureFrame(){
        trackLayer = new ComplexLayer();
        bandingLayer = new ComplexLayer();
		trackLayer.setEventPassingEnabled(false);
		backgroundLayer = new Layer();
		zoomLayer = new Layer();
		tooltipLayer = new Layer();
		scrollbarLayer = new Layer();

		addLayer(backgroundLayer);
		addLayer(bandingLayer);
		addLayer(trackLayer);
//		addLayer(measureLayer);
		addLayer(tooltipLayer);
//		addLayer(zoomLayer);


		//We set the background late so that there is a chance for the TimelinePanel to set height & width
		Scheduler.get().scheduleDeferred(new ScheduledCommand(){

			@Override
			public void execute() {

				setCurrentBackgroundColor(CssColor.make(255, 255, 255));
			}});

//		this.addHandler(mouseWheelHandler, MouseWheelEvent.getType());
	}

	// TODO
	private final MouseMoveHandler hoverHandler = new MouseMoveHandler(){

        @Override
        public void onMouseMove(MouseMoveEvent event) {
			outHoverHandler.onMouseOut(null);

            if(mouseIsDown){
                return;
            }

//            Optional<Renderable> optional = trackLayer.retrieveRenderableAtEvent(event);
//            if(lastHighlight != null) {
//                lastHighlight.setHighlight(false);
//            }
//            if(optional.isPresent()){
//                MeasuredTrackRenderable track = (MeasuredTrackRenderable) optional.get();
//                track.setHighlight(true);
//                track.setSelectHover(event.isControlKeyDown());
//                lastHighlight = track;
//                lastHighlight.setHighlight(true);
//                MeasureFrame.this.render();
//            }
//            
        }

    };

    private final MouseOutHandler outHoverHandler = new MouseOutHandler(){


        @Override
        public void onMouseOut(MouseOutEvent event) {
            if(lastHighlight != null) {
				lastHighlight.setHighlight(false);
				lastHighlight.setSelectHover(false);
				lastHighlight = null;
				MeasureFrame.this.render();
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
	
	/*private final MouseWheelHandler mouseWheelHandler = new MouseWheelHandler(){

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

	};*/



	private final MouseUpHandler endZoomHandler = new MouseUpHandler(){

		@Override
		public void onMouseUp(MouseUpEvent event) {

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
			    Optional<Renderable> optional = trackLayer.retrieveRenderableAtEvent(event);
                if(optional.isPresent()){
                    MeasuredTrackRenderable track = (MeasuredTrackRenderable) optional.get();

					if(event.isControlKeyDown()){
						if (timelinePresenter != null) {
							VortexFuture<TimelineEventSelection> future = WebMain.injector.getVortex().createFuture();
							future.addEventHandler(new AbstractVortexEventHandler<TimelineEventSelection>() {
								@Override
								public void onSuccess(TimelineEventSelection result) {
									timelinePresenter.applySelection(result);
								}
							});
							future.execute(ChronosActionsServiceProtocol.class).doServerTrackSelection(timelinePresenter.getUuid(),
                                    timelinePresenter.getVisualizationDef().getSelection(), !event.isShiftKeyDown(), track.getTrack().getLabel());
						}

					}else {
                            drillToTrack((track).getTrack());
                    }
                    //getEventBus().fireEvent(new TrackChangeEvent(track.getTrackModel()));
                }
				return;
			}
			else
			{
				if(event.isControlKeyDown() && !event.isShiftKeyDown()){
					getEventBus().fireEvent(new RangeSelectionEvent(a, b, true, null));

				} else if(event.isControlKeyDown() && event.isShiftKeyDown()){
					getEventBus().fireEvent(new RangeSelectionEvent(a, b, false, null));

				}else {
					TimeScaleChangeEvent timeScaleChangeEvent = new TimeScaleChangeEvent(a, b);
					getEventBus().fireEvent(timeScaleChangeEvent);
				}
			}

		}


	};


    private void drillToTrack(MeasuredTrackProxy track) {
        getEventBus().fireEvent(new TrackDrillEvent(track.getTrack().getName()));
    }

	private MouseMoveHandler drawZoomHandler = new MouseMoveHandler(){


        @Override
		public void onMouseMove(MouseMoveEvent event) {
			if(mouseIsDown){
				if(event.isControlKeyDown()){
					drawZoomRectangle((int) firstMouse.getX(), event.getX(), CssColor.make(255,133,10));
				} else {
					drawZoomRectangle((int) firstMouse.getX(), event.getX(), CssColor.make(128,189,242));
				}
			} else {

    			Optional<Renderable> hitTest = getTrackLayer().hitTest(event.getX(), event.getY());
                if(hitTest.isPresent()){
                    Renderable renderable = hitTest.get();
                    if(lastHighlight != null) {
                        lastHighlight.setHighlight(false);
                    }
                    if(renderable instanceof MeasuredTrackRenderable){
                        lastHighlight  = ((MeasuredTrackRenderable) renderable);
                        lastHighlight.setHighlight(true);


                        //TODO: render a single guy, Re-render the whole panel for now
                        MeasureFrame.this.render();
                    }
                }
			}

		}};

		private final ClickHandler clickHandler = new ClickHandler(){

            public void onClick(ClickEvent event){
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


		public void setTracks(List<MeasuredTrackRenderable> tracks){
		    for(Renderable renderable: trackLayer.getRenderables()){
                if(renderable instanceof TimelineTrackRenderable){
                    ((TimelineTrackRenderable) renderable).deregisterHandlers();
                }
            }
			trackLayer.removeAll();
			bindTracks(tracks);
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


/*
//		private void addHandlers(final DetailedEventRenderable eventRenderable) {
//			List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
//
//			handlers.add(eventRenderable.addClickHandler(clickHandler));
//			handlers.add(eventRenderable.addDoubleClickHandler(doubleClickHandler));
//			handlers.add(eventRenderable.addMouseUpHandler(endZoomHandler));
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
//			                //currentlyTooltipped = null;
//			            }
//						createTooltip(eventRenderable);
//						MeasureFrame.this.render();
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
//						MeasureFrame.this.render();
//					}
//				}}));
//
//			eventRenderable.setHandlers(handlers);
//		}
*/

		private void addHandlers(final MeasuredTrackRenderable track) {
            List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
            handlers.add(track.addClickHandler(clickHandler));
            handlers.add(track.addMouseDownHandler(startZoomHandler));
            handlers.add(track.addMouseUpHandler(endZoomHandler));
            handlers.add(track.addMouseMoveHandler(drawZoomHandler));
            track.setHandlers(handlers);
        }

		private void bindTracks(List<MeasuredTrackRenderable> tracks) {
			for(MeasuredTrackRenderable track: tracks){
				trackLayer.addItem(track);
				addHandlers(track);
			}
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
			zoomDrawingPanel.render();
		}

		public void setTrackLayer(ComplexLayer trackLayer) {
			this.trackLayer = trackLayer;
		}


		public void render(int bottom) {
			render();
    	}

		public void render() {
		    super.render();
		    //TODO: want to render once and stop layers, but can't because render is on scheduler
			zoomDrawingPanel.render();
		}

        private void fireScroll(boolean up) {

            getEventBus().fireEvent(new ScrollEvent(up));
        }

	public void setZoomDrawingPanel(DrawingPanel zoomDrawingPanel) {
		this.zoomDrawingPanel = zoomDrawingPanel;
		zoomDrawingPanel.addLayer(zoomLayer);
	}

	public void setTimelinePresenter(TimelinePresenter timelinePresenter) {
		this.timelinePresenter = timelinePresenter;
	}

	public TimelinePresenter getTimelinePresenter() {
		return timelinePresenter;
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
            trackLayer.clear();
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
}
