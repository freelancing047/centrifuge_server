package csi.server.common.model.visualization.graph;

import java.util.Date;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GraphPlayerSettings extends ModelObject {

    @ManyToOne
    public FieldDef startField;

    @ManyToOne
    public FieldDef endField;

    public int durationNumber = 1;

    @Enumerated(EnumType.STRING)
    public TimePlayerUnit durationUnit = TimePlayerUnit.MILLISECOND;

    public int stepSizeNumber = 1;

    @Enumerated(EnumType.STRING)
    public TimePlayerUnit stepSizePeriod = TimePlayerUnit.MILLISECOND;

    @Enumerated(EnumType.STRING)
    public TimePlayerStepMode stepMode = TimePlayerStepMode.PERCENTAGE;

    @Enumerated(EnumType.STRING)
    public TimePlayerPlaybackMode playbackMode = TimePlayerPlaybackMode.CUMULATIVE;

    public boolean hideNonVisibleItems = true;

    public int frameSizeNumber = 1;

    public TimePlayerUnit frameSizePeriod = TimePlayerUnit.YEAR;

    public int speed_ms = 1;

    public Date playbackStart;

    public Date playbackEnd;

    public Date playbackMin;

    public Date playbackMax;

    public GraphPlayerSettings() {
        super();
    }

    @PrePersist
    public void onPersistHandler() {
        if (speed_ms == 0) {
            speed_ms = 1;
        }
    }

    public FieldDef getStartField() {

        return startField;
    }

    public void setStartField(FieldDef startFieldIn) {

        startField = startFieldIn;
    }

    public FieldDef getEndField() {

        return endField;
    }

    public void setEndField(FieldDef endFieldIn) {

        endField = endFieldIn;
    }

    int getDurationNumber() {

        return durationNumber;
    }

    public void setDurationNumber(int durationNumberIn) {

        durationNumber = durationNumberIn;
    }

    TimePlayerUnit getDurationUnit() {

        return durationUnit;
    }

    public void setDurationUnit(TimePlayerUnit durationUnitIn) {

        durationUnit = durationUnitIn;
    }

    int getStepSizeNumber() {

        return stepSizeNumber;
    }

    public void setStepSizeNumber(int stepSizeNumberIn) {

        stepSizeNumber = stepSizeNumberIn;
    }

    TimePlayerUnit getStepSizePeriod() {

        return stepSizePeriod;
    }

    public void setStepSizePeriod(TimePlayerUnit stepSizePeriodIn) {

        stepSizePeriod = stepSizePeriodIn;
    }

    TimePlayerStepMode getStepMode() {

        return stepMode;
    }

    public void setStepMode(TimePlayerStepMode stepModeIn) {

        stepMode = stepModeIn;
    }

    TimePlayerPlaybackMode getPlaybackMode() {

        return playbackMode;
    }

    public void setPlaybackMode(TimePlayerPlaybackMode playbackModeIn) {

        playbackMode = playbackModeIn;
    }

    boolean getHideNonVisibleItems() {

        return hideNonVisibleItems;
    }

    public void setHideNonVisibleItems(boolean hideNonVisibleItemsIn) {

        hideNonVisibleItems = hideNonVisibleItemsIn;
    }

    int getFrameSizeNumber() {

        return frameSizeNumber;
    }

    public void setFrameSizeNumber(int frameSizeNumberIn) {

        frameSizeNumber = frameSizeNumberIn;
    }

    TimePlayerUnit getFrameSizePeriod() {

        return frameSizePeriod;
    }

    public void setFrameSizePeriod(TimePlayerUnit frameSizePeriodIn) {

        frameSizePeriod = frameSizePeriodIn;
    }

    int getSpeed_ms() {

        return speed_ms;
    }

    public void setSpeed_ms(int speed_msIn) {

        speed_ms = speed_msIn;
    }

    Date getPlaybackStart() {

        return playbackEnd;
    }

    public void setPlaybackStart(Date playbackStartIn) {

        playbackStart = playbackStartIn;
    }

    Date getPlaybackEnd() {

        return playbackEnd;
    }

    public void setPlaybackEnd(Date playbackEndIn) {

        playbackEnd = playbackEndIn;
    }

    Date getPlaybackMin() {

        return playbackMin;
    }

    public void setPlaybackMin(Date playbackMinIn) {

        playbackMin = playbackMinIn;
    }

    Date getPlaybackMax() {

        return playbackMax;
    }

    public void setPlaybackMax(Date playbackMaxIn) {

        playbackMax = playbackMaxIn;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModelObject> GraphPlayerSettings clone(Map<String, T> fieldMapIn) {

        GraphPlayerSettings myClone = new GraphPlayerSettings();

        super.cloneComponents(myClone);

        myClone.setStartField((FieldDef) cloneFromOrToMap(fieldMapIn, (T) getStartField(), fieldMapIn));
        myClone.setEndField((FieldDef) cloneFromOrToMap(fieldMapIn, (T) getEndField(), fieldMapIn));
        myClone.setDurationNumber(getDurationNumber());
        myClone.setDurationUnit(getDurationUnit());
        myClone.setStepSizeNumber(getStepSizeNumber());
        myClone.setStepSizePeriod(getStepSizePeriod());
        myClone.setStepMode(getStepMode());
        myClone.setPlaybackMode(getPlaybackMode());
        myClone.setHideNonVisibleItems(getHideNonVisibleItems());
        myClone.setFrameSizeNumber(getFrameSizeNumber());
        myClone.setFrameSizePeriod(getFrameSizePeriod());
        myClone.setSpeed_ms(getSpeed_ms());
        myClone.setPlaybackStart(getPlaybackStart());
        myClone.setPlaybackEnd(getPlaybackEnd());
        myClone.setPlaybackMin(getPlaybackMin());
        myClone.setPlaybackMax(getPlaybackMax());

        return myClone;
    }

    public <T extends ModelObject> GraphPlayerSettings copy(Map<String, T> fieldMapIn) {

        GraphPlayerSettings myCopy = new GraphPlayerSettings();

        super.copyComponents(myCopy);

        myCopy.setStartField(getStartField());
        myCopy.setEndField(getEndField());
        myCopy.setDurationNumber(getDurationNumber());
        myCopy.setDurationUnit(getDurationUnit());
        myCopy.setStepSizeNumber(getStepSizeNumber());
        myCopy.setStepSizePeriod(getStepSizePeriod());
        myCopy.setStepMode(getStepMode());
        myCopy.setPlaybackMode(getPlaybackMode());
        myCopy.setHideNonVisibleItems(getHideNonVisibleItems());
        myCopy.setFrameSizeNumber(getFrameSizeNumber());
        myCopy.setFrameSizePeriod(getFrameSizePeriod());
        myCopy.setSpeed_ms(getSpeed_ms());
        myCopy.setPlaybackStart(getPlaybackStart());
        myCopy.setPlaybackEnd(getPlaybackEnd());
        myCopy.setPlaybackMin(getPlaybackMin());
        myCopy.setPlaybackMax(getPlaybackMax());

        return myCopy;
    }
}
