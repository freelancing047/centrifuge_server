package csi.client.gwt.viz.graph.tab.player;

import java.util.Date;
import java.util.List;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerSpeed;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;
import csi.server.common.model.visualization.graph.TimePlayerUnit;
import csi.server.common.service.api.GraphTimePlayerServiceProtocol;

public class TimePlayerSettingsProxy implements TimePlayerSettings {

    private final class LoadGraphPlayerSettings extends AbstractVortexEventHandler<GraphPlayerSettings> {

        @Override
        public void onSuccess(GraphPlayerSettings result) {
            gps = result;
        }
    }

    // GraphPlayerSettings is the wrapped object.
    private GraphPlayerSettings gps;
    private TimePlayer timePlayer;
	private int totalSteps;
	private int currentStep;

    public TimePlayerSettingsProxy(TimePlayer timePlayer, GraphPlayerSettings playerSettings) {
        this.timePlayer = timePlayer;
        
        if (playerSettings == null) {
            gps = new GraphPlayerSettings();
        } else {
            gps = playerSettings;
        }
        
        this.timePlayer.setGraphPlayerSettings(gps);
    }

    @Override
    public VortexFuture<List<String>> activate() {
        VortexFuture<List<String>> activatePlayerVF = WebMain.injector.getVortex().createFuture();
        activatePlayerVF.addEventHandler(new AbstractVortexEventHandler<List<String>>() {

            @Override
            public void onSuccess(List<String> result) {
                if(result != null && result.size() > 0)
                    timePlayer.setActive(true);
                else
                    stop();
            }
            

            @Override
            public boolean onError(Throwable t) {
                stop();
                
                return false;
            }
        });
        try {
            activatePlayerVF.execute(GraphTimePlayerServiceProtocol.class).activatePlayer(timePlayer.getDataviewUuid(),
                    timePlayer.getGraphUuid(), gps);
        } catch (CentrifugeException e) {

            stop();
        }
        return activatePlayerVF;
    }

    @Override
    public VortexFuture<List<String>> activatePlayer() {
        return activate();
    }

    @Override
    public int getDuration() {
        return gps.durationNumber;
    }

    @Override
    public TimePlayerUnit getDurationUnit() {
        return gps.durationUnit;
    }

    @Override
    public FieldDef getEndField() {
        return gps.endField;
    }

    @Override
    public Date getEndTime() {
        return gps.playbackEnd;
    }

    @Override
    public int getFrameSize() {
        return gps.frameSizeNumber;
    }

    @Override
    public TimePlayerUnit getFrameSizeUnit() {
        return gps.frameSizePeriod;
    }

    @Override
    public TimePlayerPlaybackMode getPlaybackMode() {
        return gps.playbackMode;
    }

    @Override
    public TimePlayerSpeed getSpeed() {
        return TimePlayerSpeed.fromInt(gps.speed_ms);
    }

    @Override
    public FieldDef getStartField() {
        return gps.startField;
    }

    @Override
    public Date getStartTime() {
        return gps.playbackStart;
    }

    @Override
    public TimePlayerStepMode getStepMode() {
        return gps.stepMode;
    }

    @Override
    public int getStepSize() {
        return gps.stepSizeNumber;
    }

    @Override
    public TimePlayerUnit getStepSizeUnit() {
        return gps.stepSizePeriod;
    }

    @Deprecated
    @Override
    public VortexFuture<GraphPlayerSettings> load() {
        VortexFuture<GraphPlayerSettings> timePlayerSettingsVF = WebMain.injector.getVortex().createFuture();
        try {
            timePlayerSettingsVF.execute(GraphTimePlayerServiceProtocol.class).getTimePlayerSettings(
                    timePlayer.getGraphUuid());
        } catch (CentrifugeException e) {
        }
        timePlayerSettingsVF.addEventHandler(new LoadGraphPlayerSettings());
        return timePlayerSettingsVF;
    }

    @Override
    public void setDuration(int duration) {
        gps.durationNumber = duration;
    }

    @Override
    public void setDurationUnit(TimePlayerUnit unit) {
        gps.durationUnit = unit;
    }

    @Override
    public void setEndField(FieldDef endField) {
        gps.endField = endField;
    }

    @Override
    public void setEndTime(Date date) {
        gps.playbackEnd = date;
    }

    @Override
    public void setFrameSize(int frameSize) {
        gps.frameSizeNumber = frameSize;
    }

    @Override
    public void setFrameSizeUnit(TimePlayerUnit unit) {
        gps.frameSizePeriod = unit;
    }

    @Override
    public void setPlaybackMode(TimePlayerPlaybackMode mode) {
        gps.playbackMode = mode;
    }

    @Override
    public void setSpeed(TimePlayerSpeed speed) {
        gps.speed_ms = speed.getDelay();
    }

    @Override
    public void setStartField(FieldDef startField) {
        gps.startField = startField;
    }

    @Override
    public void setStartTime(Date date) {
        gps.playbackStart = date;
    }

    @Override
    public void setStepMode(TimePlayerStepMode stepMode) {
        gps.stepMode = stepMode;
    }

    @Override
    public void setStepSize(int stepSize) {
        gps.stepSizeNumber = stepSize;
    }

    @Override
    public void setStepSizeUnit(TimePlayerUnit unit) {
        gps.stepSizePeriod = unit;
    }

    @Override
    public VortexFuture<List<String>> step() {
        VortexFuture<List<String>> stepPlayerVF = WebMain.injector.getVortex().createFuture();
        try {
            stepPlayerVF.execute(GraphTimePlayerServiceProtocol.class).stepPlayer(timePlayer.getGraphUuid());
        } catch (CentrifugeException e) {
        }
        return stepPlayerVF;
    }

    @Override
    public VortexFuture<Void> stop() {
        VortexFuture<Void> stopPlayerVF = WebMain.injector.getVortex().createFuture();
        stopPlayerVF.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {

            	currentStep = 0;
            }
        });
        try {
            stopPlayerVF.execute(GraphTimePlayerServiceProtocol.class).stopPlayer(timePlayer.getGraphUuid());
        } catch (CentrifugeException e) {
        }
        return stopPlayerVF;
    }

    @Override
    public VortexFuture<Void> stopPlayer() {
        VortexFuture<Void> stopPlayerVF = WebMain.injector.getVortex().createFuture();
        stopPlayerVF.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {

            	currentStep = 0;
            }
        });
        try {
            stopPlayerVF.execute(GraphTimePlayerServiceProtocol.class).stopPlayer(timePlayer.getGraphUuid());
        } catch (CentrifugeException e) {
        }
        return stopPlayerVF;
    }

    @Override
    public Date getPlaybackMin() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getPlaybackMax() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setHideNonVisibleItems(boolean b) {
        gps.setHideNonVisibleItems(b);
    }

    @Override
    public boolean hideNonVisibleItems() {
        return gps.hideNonVisibleItems;
    }

	@Override
	public double getPercentSize() {
		return  (double)currentStep/(double)totalSteps;
	}

	@Override
	public void setCurrentStep(int currentStep){
		this.currentStep = currentStep;
	}
	
	@Override
	public void setTotalSteps(int totalSteps) {
		this.totalSteps = totalSteps;
		this.currentStep = 0;
	}
	
	@Override
	public int getTotalSteps(){
		return this.totalSteps;
	}

	
	
	@Override
	public void incrementStep() {
		this.currentStep++;
	}

}

// new Callback<GraphPlayerSettings>() {
//
// @Override
// public void onSuccess(GraphPlayerSettings settings) {
// setSettings(settings);
// /*Keep*/
// // if ((settings.getPlaybackStart() == null) || (settings.getPlaybackEnd() == null)) {
// // return;
// // }
// // Date fromDate = new Date(settings.getPlaybackStart());
// // startDateTimeBoxAppended.setValue(fromDate);
// // currentDateTimeBoxAppended.setValue(fromDate);
// // Date toDate = new Date(settings.getPlaybackEnd());
// // endDateTimeBoxAppended.setValue(toDate);
//
// /*Delete*/
// // playerSlider = new DateSlider(fromDate, toDate,
// // DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_LONG));
// // playerSlider.addValueChangeHandler(new ValueChangeHandler<Integer>() {
// //
// // @Override
// // public void onValueChange(ValueChangeEvent<Integer> event) {
// // if (!isPlaying && isActivated) {
// // try {
// // WebMain.injector.getVortex().execute(new Callback<List<String>>() {
// //
// // @Override
// // public void onSuccess(List<String> settings) {
// // timePlayer.getGraph().getGraphSurface().refresh();
// // }
// // }, GraphTimePlayerServiceProtocol.class)
// // .seek(timePlayer.getGraph().getUuid(), new Long(event.getValue()));
// // } catch (CentrifugeException e) {
// // }
// // }
// // }
// // });
// // playerSliderContainer.add(playerSlider);
// }
// },
