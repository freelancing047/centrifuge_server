package csi.client.gwt.viz.graph.tab.player;

import java.util.Date;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Tab;

import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerSpeed;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;
import csi.server.common.model.visualization.graph.TimePlayerUnit;

public interface TimePlayerView {

    void enablePlay(boolean enabled);

    void enableReset(boolean enabled);

    void enableStop(boolean enabled);

    Tab getTab();

    void bind(TimePlayerAction timePlayerAction);

    void setFieldDefs(List<FieldDef> fieldDefs);

    void setPlaybackMode(TimePlayerPlaybackMode playbackMode);

    void setPlaybackSpeed(TimePlayerSpeed speed);

    void setStartField(FieldDef startField);

    void setEndField(FieldDef endField);

    void setDuration(int duration);

    void setDurationUnit(TimePlayerUnit unit);

    void setStepSize(int stepSize);

    void setStepSizeUnit(TimePlayerUnit stepSizeUnit);

    void setStartTime(Date startTime);

    void setCurrentTime(Date startTime);

    void setEndTime(Date endTime);

    void setStepMode(TimePlayerStepMode stepMode);

    void setFramSize(int frameSize);

    void setFrameSizeUnit(TimePlayerUnit frameSizeUnit);

    void setPlaybackMin(Date playbackMin);

    void setPlaybackMax(Date playbackMax);

    void setNonVisibleItems(boolean value);

    void setCurrentTime();
}
