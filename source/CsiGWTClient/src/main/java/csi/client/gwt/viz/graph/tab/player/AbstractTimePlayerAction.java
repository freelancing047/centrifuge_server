package csi.client.gwt.viz.graph.tab.player;

import java.util.Date;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.CsiMap;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerSpeed;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;
import csi.server.common.model.visualization.graph.TimePlayerUnit;
import csi.server.common.service.api.GraphTimePlayerServiceProtocol;

public abstract class AbstractTimePlayerAction implements TimePlayerAction {

    protected TimePlayer timePlayer;

    public AbstractTimePlayerAction(TimePlayer timePlayer) {
        this.timePlayer = timePlayer;
        timePlayer.getView().bind(this);
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void resetPlayerSettings() {
        timePlayer.reset();
    }
    
    @Override
    public void resetPlayerRange() {
        timePlayer.resetRange(timePlayer.getSettings().getStartTime(), timePlayer.getSettings().getEndTime());
    }

    @Override
    public void setCurrentTime(Date date) {
        timePlayer.seekTo(date);
    }

    @Override
    public void setDuration(int duration) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setDuration(duration);
    }

    @Override
    public void setDurationUnit(TimePlayerUnit unit) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setDurationUnit(unit);
    }

    @Override
    public void setEndField(FieldDef value) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setEndField(value);
    }

    @Override
    public void setEndTime(Date date) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setEndTime(date);
        resetPlayerRange();
    }

    @Override
    public void setFrameSize(int frameSize) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setFrameSize(frameSize);
    }

    @Override
    public void setFrameSizeUnit(TimePlayerUnit unit) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setFrameSizeUnit(unit);

    }

    @Override
    public void setPlaybackMode(TimePlayerPlaybackMode mode) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setPlaybackMode(mode);
    }

    @Override
    public void setSpeed(TimePlayerSpeed speed) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setSpeed(speed);

    }

    @Override
    public void setStartField(FieldDef value) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setStartField(value);
        // need to update start time
        VortexFuture<CsiMap<String, String>> validVF = WebMain.injector.getVortex().createFuture();
        String endField = (settings.getEndField() != null) ? settings.getEndField().getLocalId() : null;
        String startField = (settings.getStartField() != null) ? settings.getStartField().getLocalId() : null;
        validVF.execute(GraphTimePlayerServiceProtocol.class).getEndPoints(timePlayer.getDataviewUuid(),
                timePlayer.getGraphUuid(), startField, endField, settings.getDuration(), settings.getDurationUnit(), null, null);
        validVF.addEventHandler(new AbstractVortexEventHandler<CsiMap<String, String>>() {

            @Override
            public void onSuccess(CsiMap<String, String> result) {
                final TimePlayerView view = timePlayer.getView();
                if (result.size() > 1) {

                    Date startTime = new Date(Long.parseLong((String) result.get("min")));//NON-NLS
                    Date endTime = new Date(Long.parseLong((String) result.get("max")));//NON-NLS
                    int totalSteps = Integer.parseInt(result.get("count"));

                    view.setStartTime(startTime);
                    view.setEndTime(endTime);
                    view.setPlaybackMin(startTime);
                    view.setPlaybackMax(endTime);
                    settings.setStartTime(startTime);
                    settings.setEndTime(endTime);
                    settings.setTotalSteps(totalSteps);
                }
            }
        });
    }

    @Override
    public void setStartTime(Date date) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setStartTime(date);
        resetPlayerRange();
    }

    @Override
    public void setStepMode(TimePlayerStepMode stepMode) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setStepMode(stepMode);
    }

    @Override
    public void setStepSize(int stepSize) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setStepSize(stepSize);
    }

    @Override
    public void setStepSizeUnit(TimePlayerUnit unit) {
        final TimePlayerSettings settings = timePlayer.getSettings();
        settings.setStepSizeUnit(unit);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
    }

    ;

    @Override
    public void startPlayer() {
        timePlayer.play();
    }

    @Override
    public void stopPlayer() {
        timePlayer.stop();
    }
}