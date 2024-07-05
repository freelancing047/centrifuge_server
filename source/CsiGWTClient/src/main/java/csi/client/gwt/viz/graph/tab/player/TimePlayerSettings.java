package csi.client.gwt.viz.graph.tab.player;

import java.util.Date;
import java.util.List;

import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerSpeed;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;
import csi.server.common.model.visualization.graph.TimePlayerUnit;

public interface TimePlayerSettings {

    TimePlayerSpeed getSpeed();

    FieldDef getStartField();

    FieldDef getEndField();

    VortexFuture<List<String>> step();

    VortexFuture<Void> stop();

    VortexFuture<GraphPlayerSettings> load();

    VortexFuture<List<String>> activatePlayer();

    void setStartField(FieldDef startField);

    void setEndField(FieldDef endField);

    void setSpeed(TimePlayerSpeed speed);

    TimePlayerPlaybackMode getPlaybackMode();

    void setPlaybackMode(TimePlayerPlaybackMode mode);

    int getDuration();

    void setDuration(int duration);

    void setDurationUnit(TimePlayerUnit unit);

    TimePlayerUnit getDurationUnit();

    TimePlayerUnit getStepSizeUnit();

    int getStepSize();

    void setStepSizeUnit(TimePlayerUnit unit);

    void setStepSize(int stepSize);

    Date getEndTime();

    Date getStartTime();

    void setStartTime(Date date);

    void setEndTime(Date date);

    TimePlayerStepMode getStepMode();

    void setStepMode(TimePlayerStepMode stepMode);

    VortexFuture<List<String>> activate();

    VortexFuture<Void> stopPlayer();

    TimePlayerUnit getFrameSizeUnit();

    int getFrameSize();

    void setFrameSizeUnit(TimePlayerUnit unit);

    void setFrameSize(int frameSize);

    Date getPlaybackMin();

    Date getPlaybackMax();

    void setHideNonVisibleItems(boolean b);

    boolean hideNonVisibleItems();

	double getPercentSize();
	
	void setTotalSteps(int totalSteps);

	void incrementStep();

	int getTotalSteps();

	void setCurrentStep(int currentStep);
}
