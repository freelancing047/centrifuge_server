package csi.client.gwt.viz.graph.tab.player;

import java.util.Date;

import com.google.gwt.activity.shared.Activity;

import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerSpeed;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;
import csi.server.common.model.visualization.graph.TimePlayerUnit;

public interface TimePlayerAction extends Activity {

    void stopPlayer();

    void setStartField(FieldDef value);

    void setSpeed(TimePlayerSpeed slow);

    void resetPlayerSettings();
    
    void resetPlayerRange();

    void startPlayer();

    void setEndField(FieldDef value);

    void setPlaybackMode(TimePlayerPlaybackMode timeSpan);

    void setDuration(int duration_ms);

    void setDurationUnit(TimePlayerUnit unit);

    void setStepSizeUnit(TimePlayerUnit unit);

    void setStepSize(int stepSize);

    void setStartTime(Date vdate);

    void setEndTime(Date date);

    void setCurrentTime(Date date);

    void setStepMode(TimePlayerStepMode stepMode);

    void setFrameSizeUnit(TimePlayerUnit unit);

    void setFrameSize(int frameSize);

}
