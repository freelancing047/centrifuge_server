package csi.client.gwt.viz.graph.tab.player;

import java.util.Date;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerSpeed;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;
import csi.server.common.model.visualization.graph.TimePlayerUnit;

public final class NullTimePlayerAction implements TimePlayerAction {

    @Override
    public void setFrameSizeUnit(TimePlayerUnit unit) {
    }

    @Override
    public void setFrameSize(int frameSize) {
    }

    private static final NullTimePlayerAction instance = new NullTimePlayerAction();

    private NullTimePlayerAction() {
    }

    @Override
    public void setStepMode(TimePlayerStepMode stepMode) {
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void setDurationUnit(TimePlayerUnit valueOf) {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
    }

    @Override
    public void stopPlayer() {
    }

    @Override
    public void setStartField(FieldDef value) {
    }

    @Override
    public void setSpeed(TimePlayerSpeed slow) {
    }
    
    @Override
    public void resetPlayerSettings() {
    }

    @Override
    public void startPlayer() {
    }

    @Override
    public void setEndField(FieldDef value) {
    }

    public static NullTimePlayerAction get() {
        return instance;
    }

    @Override
    public void setPlaybackMode(TimePlayerPlaybackMode timeSpan) {

    }

    @Override
    public void setDuration(int duration_raw) {
    }

    @Override
    public void setStepSize(int stepSize) {
    }

    @Override
    public void setStepSizeUnit(TimePlayerUnit valueOf) {
    }

    @Override
    public void setCurrentTime(Date date) {
    }

    @Override
    public void setEndTime(Date date) {
    }

    @Override
    public void setStartTime(Date vdate) {
    }

    @Override
    public void resetPlayerRange() {
    }
}
