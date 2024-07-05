package csi.client.gwt.viz.graph.tab.player;

import java.util.Date;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Tab;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import csi.client.gwt.WebMain;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.controlbar.GraphControlBarModel;
import csi.client.gwt.viz.graph.events.GraphEvent;
import csi.client.gwt.viz.graph.events.GraphEventHandler;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.service.api.GraphTimePlayerServiceProtocol;

public class TimePlayer {

	public void fireEvent(Event event) {
		eventBus.fireEvent(event);

	}

	public void addEventHandler(EventHandler eventHandler, Event.Type eventType){
		eventBus.addHandler(eventType, eventHandler);
	}

	public void setHideNonVisibleItems(boolean b) {
		settings.setHideNonVisibleItems(b);
	}

	private final class OnGraphLoad extends GraphEventHandler {

		@Override
		public void onGraphEvent(GraphEvent event) {
			GraphPlayerSettings playerSettings = graph.getPlayerSettings();
			if (playerSettings == null) {
				playerSettings = new GraphPlayerSettings();
				graph.setPlayerSettings(playerSettings);
			}
			settings = new TimePlayerSettingsProxy(TimePlayer.this, playerSettings);
			activityManager.setActivity(new InitTimePlayer(TimePlayer.this));
		}
	}

	private TimePlayerAction action;
	private boolean active;
	private CSIActivityManager activityManager;
	private TimePlayerActivityMapper activityMapper;
	private EventBus eventBus;
	private Graph graph;
	private PlaceController placeController;
	private boolean playing;
	private TimePlayerSettings settings;
	private TimePlayerView view;
	private GraphPlayerSettings gps;

	public TimePlayer(final Graph graph) {
		this.graph = graph;
		view = new TimePlayerTab(this);


		eventBus = new SimpleEventBus();
		placeController = new PlaceController(eventBus);
		activityMapper = new TimePlayerActivityMapper(this);
		activityManager = new CSIActivityManager(activityMapper, eventBus);
		if (graph.isLoaded()) {
			(new OnGraphLoad()).onGraphEvent(null);
		} else {
			graph.addGraphEventHandler(GraphEvents.GRAPH_LOAD_COMPLETE, new OnGraphLoad());
		}
	}

	String getDataviewUuid() {
		return graph.getDataviewUuid();
	}

	GraphSurface getGraphSurface() {
		return graph.getGraphSurface();
	}

	String getGraphUuid() {
		return graph.getUuid();
	}

	DataModelDef getModelDef() {
		return graph.getDataview().getDataView().getMeta().getModelDef();
	}

	public TimePlayerSettings getSettings() {
		return settings;
	}

	public Tab getTab() {
		return view.getTab();
	}

	public TimePlayerView getView() {
		return view;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isPlaying() {
		return playing;
	}

	public void play() {
	        activityManager.setActivity(new PlayTimePlayer(this));
	}

	public void reset() {
		activityManager.setActivity(new ResetTimePlayer(this));
	}
	
	public void resetRange(Date start, Date end) {
	    activityManager.setActivity(new ResetTimePlayerRange(this, start, end));
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setPlaying(boolean isPlaying) {
		playing = isPlaying;
	}

	public void step() {
	    if(active)
		activityManager.setActivity(new StepTimePlayer(this));
	    else
	        stop();
	}

	public void stop() {
		activityManager.setActivity(new StopTimePlayer(this));
	}

	public void mimicStop(){
		stop();
		reset();
		graph.getGraphControlBarAsGraphControlBar().scrubToPercent(0);
		graph.getTimePlayer().fireEvent(new StopEvent());
	}

	public void seekTo(final Date date) {
		{
			final VortexFuture<Object> future = WebMain.injector.getVortex().createFuture();
			try {
				
				if(this.active){
					future.execute(GraphTimePlayerServiceProtocol.class).seek(getGraphUuid(),date.getTime());
				} else {

					VortexFuture<List<String>> activateFuture = WebMain.injector.getVortex().createFuture();
					activateFuture.addEventHandler(new AbstractVortexEventHandler<List<String>>(){

						@Override
						public void onSuccess(List<String> result) {

						    if(result != null && result.size() > 0){
    			                setActive(true);
    							try {
    								future.execute(GraphTimePlayerServiceProtocol.class).seek(getGraphUuid(),date.getTime());
    							} catch (CentrifugeException e) {
    
    							}
						    } else {
                                setActive(false);
						        stop();
						    }
							
						}

						@Override
						public boolean onError(Throwable t) {
							stop();
                            setActive(false);
							return false;
						}
					}
							);
					activateFuture.execute(GraphTimePlayerServiceProtocol.class).activatePlayer("", getGraphUuid(), gps);
				}
			} catch (Exception e) {

                stop();
			}
			getGraphSurface().refresh(future);
		}

	}

	public void seekTo(final double newRelativePosition, final GraphControlBarModel model) {
		{
			final VortexFuture<List<String>> future = WebMain.injector.getVortex().createFuture();
			future.addEventHandler(new AbstractVortexEventHandler<List<String>>(){

				@Override
				public void onSuccess(List<String> result) {
					Date currentDate = new Date(Long.parseLong(result.get(0)));
					view.setCurrentTime(currentDate);
					model.setCurrentTime(currentDate);
					model.invalidatePreviousTime();
					int currentStep = (int)(settings.getTotalSteps() * newRelativePosition);
					if(currentStep > 0){
						//currentStep--;
					}
					if(currentStep > settings.getTotalSteps()){
						currentStep = settings.getTotalSteps();
					}
					settings.setCurrentStep(currentStep);
				}

			});
			try {
				if(this.active){
					future.execute(GraphTimePlayerServiceProtocol.class).seekPosition(getGraphUuid(), (int)(settings.getTotalSteps() * newRelativePosition +.5));
				} else {

					VortexFuture<List<String>> activateFuture = WebMain.injector.getVortex().createFuture();
					activateFuture.addEventHandler(new AbstractVortexEventHandler<List<String>>(){

						@Override
						public void onSuccess(List<String> result) {

						    if(result != null && result.size() > 0){
    			                setActive(true);
    							try {
    								future.execute(GraphTimePlayerServiceProtocol.class).seekPosition(getGraphUuid(), (int)(settings.getTotalSteps() * newRelativePosition +.5));
    							} catch (CentrifugeException e) {
    
    							}
						    } else {
						        setActive(false);
						        stop();
						    }
							
						}

						@Override
						public boolean onError(Throwable t) {
                            stop();
                            setActive(false);
							return false;
						}

					}
							);
					activateFuture.execute(GraphTimePlayerServiceProtocol.class).activatePlayer("", getGraphUuid(), gps);
				}
			} catch (CentrifugeException e) {

                setActive(false);
                stop();
			}
			getGraphSurface().refresh(future);
		}
	}

	public void setGraphPlayerSettings(GraphPlayerSettings gps) {
		this.gps = gps;
	}
}