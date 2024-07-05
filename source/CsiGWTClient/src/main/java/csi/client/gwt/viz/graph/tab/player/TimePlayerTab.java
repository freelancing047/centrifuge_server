package csi.client.gwt.viz.graph.tab.player;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Legend;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.datetimepicker.client.ui.DateTimeBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.tab.GraphTab;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.graph.TimePlayerConstants;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerPlaybackMode;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerSpeed;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;
import csi.server.common.model.visualization.graph.TimePlayerUnit;

public class TimePlayerTab implements TimePlayerView {

    private static final String myDateFormatString = "yyyy-mm-dd";  //TODO: These i18n ones don't work properly... DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).getPattern();
    private static final String myTimeFormatString = "h:mm:ss.SSS a";
    private static final DateTimeFormat myTimeFormat = DateTimeFormat.getFormat(myTimeFormatString);
    private static final DateTimeFormat myDateTimeFormat = DateTimeFormat.getFormat(myDateFormatString + " "
            + myTimeFormatString);
    private static final DateTimeFormat myDateFormat = DateTimeFormat.getFormat(myDateFormatString + " "
            + myTimeFormatString);
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    @UiField
    RadioButton playbackModeTimeSpanRB;
    @UiField
    RadioButton playbackModeCumulativeRB;
    @UiField
    RadioButton playbackModeFixedTimeSpanRB;
    @UiField
    ControlGroup endFieldControlGroup;
    @UiField
    DateTimeBox currentDateBoxAppended;
    @UiField
    DateTimeBox endDateBoxAppended;
    @UiField(provided = true)
    FieldDefComboBox endFieldDefCB;
    @UiField
    RadioButton fastSpeedRB;
    @UiField
    RadioButton moderateSpeedRB;
    @UiField
    Button playButton;
    @UiField
    Button resetButton;
    @UiField
    RadioButton slowSpeedRB;
    @UiField
    DateTimeBox startDateBoxAppended;
    @UiField(provided = true)
    FieldDefComboBox startFieldDefCB;
    @UiField
    Button stopButton;
    @UiField
    GraphTab tab;
    @UiField
    StringComboBox stepSizeListBox;
    @UiField
    TextBox durationTextBox;
    @UiField
    StringComboBox durationListBox;
    @UiField
    ControlGroup stepSizeControlGroup;
    @UiField
    ControlGroup durationControlGroup;
    @UiField
    ControlGroup frameSizeControlGroup;
    @UiField
    TextBox stepSizeTextBox;
    @UiField
    ProgressBar progressBar;
    @UiField
    TextBox startTextBox;
    @UiField
    TextBox currentTextBox;
    @UiField
    TextBox endTextBox;
    @UiField
    RadioButton relativeStepModeRB;
    @UiField
    RadioButton absoluteStepModeRB;
    @UiField
    RadioButton percentageStepModeRB;
    @UiField
    TextBox frameSizeTextBox;
    @UiField
    StringComboBox frameSizeListBox;
    @UiField
    CheckBox nonVisibleItems;
    @UiField
    Legend timeRangeLegend;
    @UiField
    Legend timeWindowLegend;
    @UiField
    Legend timeControlsLegend;
    @UiField
    Legend eventDefinitionLegend;
    private TimePlayerAction action = NullTimePlayerAction.get();
    private TimePlayer timePlayer;

    public TimePlayerTab(final TimePlayer timePlayer) {
        this.timePlayer = timePlayer;
        startFieldDefCB = new FieldDefComboBox();
        endFieldDefCB = new FieldDefComboBox();
        uiBinder.createAndBindUi(this);
        initializeLegends();
        for (TimePlayerUnit unit : TimePlayerUnit.timeUnitsSmallToLarge()) {
            stepSizeListBox.getStore().add(unit.toString());
            durationListBox.getStore().add(unit.toString());
            frameSizeListBox.getStore().add(unit.toString());
        }
        startDateBoxAppended.setFormat(myDateFormatString);
        startDateBoxAppended.setAutoClose(true);
        
        currentDateBoxAppended.setFormat(myDateFormatString);
        currentDateBoxAppended.setAutoClose(true);
        
        endDateBoxAppended.setFormat(myDateFormatString);
        endDateBoxAppended.setAutoClose(true);
                
        // startDateBoxAppended.setMaxView(ViewMode.DECADE);
        // startDateBoxAppended.setStartView(ViewMode.MONTH);
        tab.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                timePlayer.getGraphSurface().getGraph().showControlBar(true);
            }
        });
    }

    private void initializeLegends() {
        timeRangeLegend.getElement().setInnerHTML(CentrifugeConstantsLocator.get().timePlayerTab_timeRange());
        eventDefinitionLegend.getElement().setInnerHTML(CentrifugeConstantsLocator.get().timePlayerTabEventDefinition());
        timeWindowLegend.getElement().setInnerHTML(CentrifugeConstantsLocator.get().timeplayerTab_timeWindow());
        timeControlsLegend.getElement().setInnerHTML(CentrifugeConstantsLocator.get().timePlayerTab_timeControls());
    }

    @Override
    public void setPlaybackMode(TimePlayerPlaybackMode playbackMode) {
        switch (playbackMode) {
            case CUMULATIVE:
                playbackModeCumulativeRB.setValue(true);
                endFieldDefCB.setEnabled(false);
                durationTextBox.setEnabled(false);
                durationListBox.setEnabled(false);
                break;
            case DYNAMIC_TIME_SPAN:
                playbackModeTimeSpanRB.setValue(true);
                endFieldDefCB.setEnabled(true);
                endFieldControlGroup.setVisible(true);
                durationTextBox.setEnabled(false);
                durationListBox.setEnabled(false);
                break;
            case FIXED_TIME_SPAN:
                playbackModeTimeSpanRB.setValue(true);
                endFieldDefCB.setEnabled(false);
                durationControlGroup.setVisible(true);
                durationTextBox.setEnabled(true);
                durationListBox.setEnabled(true);
            default:
                playbackModeCumulativeRB.setValue(false);
                playbackModeTimeSpanRB.setValue(false);
                endFieldDefCB.setEnabled(false);
                durationTextBox.setEnabled(false);
                durationListBox.setEnabled(false);
                break;
        }
    }

    @UiHandler("playbackModeFixedTimeSpanRB")
    void onPlaybackModeFixedTimeSpan(ClickEvent event) {
            stopAndResetPlayer();
            action.setPlaybackMode(TimePlayerPlaybackMode.FIXED_TIME_SPAN);
            endFieldDefCB.setEnabled(false);
            durationControlGroup.setVisible(true);
            durationTextBox.setEnabled(true);
            durationListBox.setEnabled(true);
            endFieldControlGroup.setVisible(false);
    }

    @Override
    public void setPlaybackSpeed(TimePlayerSpeed speed) {
        switch (speed) {
            case FAST:
                fastSpeedRB.setValue(true);
                break;
            case MODERATE:
                moderateSpeedRB.setValue(true);
                break;
            case SLOW:
                slowSpeedRB.setValue(true);
                break;
            default:
                fastSpeedRB.setValue(false);
                moderateSpeedRB.setValue(false);
                slowSpeedRB.setValue(false);
                break;
        }
    }

    @Override
    public void setFrameSizeUnit(TimePlayerUnit frameSizeUnit) {
        int index = TimePlayerUnit.timeUnitsSmallToLarge().indexOf(frameSizeUnit);
        frameSizeListBox.setValue(frameSizeUnit.toString(), true);
    }

    @Override
    public void setFramSize(int frameSize) {
        frameSizeTextBox.setValue("" + frameSize);
    }

    @UiHandler("playbackModeCumulativeRB")
    void onPlaybackCumulative(ClickEvent event) {
        stopAndResetPlayer();
        action.setPlaybackMode(TimePlayerPlaybackMode.CUMULATIVE);
        endFieldDefCB.setEnabled(false);
        durationControlGroup.setVisible(false);
        endFieldControlGroup.setVisible(false);
        durationTextBox.setEnabled(false);
        durationListBox.setEnabled(false);
    }

    @UiHandler("playbackModeTimeSpanRB")
    void onPlaybackTimeSpan(ClickEvent event) {
        stopAndResetPlayer();
        action.setPlaybackMode(TimePlayerPlaybackMode.DYNAMIC_TIME_SPAN);
        endFieldDefCB.setEnabled(true);
        durationControlGroup.setVisible(false);
        endFieldControlGroup.setVisible(true);
        durationTextBox.setEnabled(false);
        durationListBox.setEnabled(false);
    }

    @Override
    public void setFieldDefs(List<FieldDef> fieldDefs) {
        startFieldDefCB.getStore().clear();
        endFieldDefCB.getStore().clear();
        startFieldDefCB.getStore().addAll(fieldDefs);
        endFieldDefCB.getStore().addAll(fieldDefs);
        startFieldDefCB.setSelectedIndex(0);
    }

    @Override
    public Tab getTab() {
        return tab;
    }

    @UiHandler("endFieldDefCB")
    void onEndField(SelectionEvent<FieldDef> event) {
        stopAndResetPlayer();
        action.setEndField(event.getSelectedItem());
    }

    @UiHandler("fastSpeedRB")
    void onFastSpeedRB(ClickEvent event) {
        stopAndResetPlayer();
        action.setSpeed(TimePlayerConstants.TimePlayerSpeed.FAST);
    }

    @UiHandler("moderateSpeedRB")
    void onModerateSpeedRB(ClickEvent event) {
        action.setSpeed(TimePlayerConstants.TimePlayerSpeed.MODERATE);
    }

    @UiHandler("playButton")
    void onPlay(ClickEvent event) {
        action.startPlayer();
    }

    @UiHandler("resetButton")
    void onReset(ClickEvent event) {
        action.resetPlayerSettings();
    }

    @UiHandler("slowSpeedRB")
    void onSlowSpeed(ValueChangeEvent<Boolean> event) {
        stopAndResetPlayer();
        action.setSpeed(TimePlayerConstants.TimePlayerSpeed.SLOW);
    }

    @UiHandler("startFieldDefCB")
    void onStartField(SelectionEvent<FieldDef> event) {
        action.setStartField(event.getSelectedItem());
    }

    @UiHandler("stopButton")
    void onStop(ClickEvent event) {
        action.stopPlayer();
    }

    @Override
    public void enablePlay(boolean enabled) {
        playButton.setEnabled(enabled);
    }

    @Override
    public void enableReset(boolean enabled) {
        resetButton.setEnabled(enabled);
    }

    @Override
    public void enableStop(boolean enabled) {
        stopButton.setEnabled(enabled);
    }

    @Override
    public void bind(TimePlayerAction timePlayerAction) {
        this.action = timePlayerAction;
    }

    @Override
    public void setStartField(FieldDef startField) {
        startFieldDefCB.setValue(startField);
    }

    @Override
    public void setEndField(FieldDef endField) {
        endFieldDefCB.setValue(endField);
    }

    @Override
    public void setDuration(int duration_ms) {
        durationTextBox.setValue("" + duration_ms);
    }

    @UiHandler("durationTextBox")
    void onDurationChange(ValueChangeEvent<String> event) {
        stopAndResetPlayer();
        int duration = 0;
        try {
            duration = Integer.parseInt(event.getValue());
            action.setDuration(duration);
        } catch (Exception e) {
            durationControlGroup.setType(ControlGroupType.ERROR);
        }
    }

    @UiHandler("stepSizeTextBox")
    void onStepSizeChange(ValueChangeEvent<String> event) {
        stopAndResetPlayer();
        int stepSize = 0;
        try {
            stepSize = Integer.parseInt(event.getValue());
            action.setStepSize(stepSize);
            stepSizeControlGroup.setType(ControlGroupType.NONE);
        } catch (Exception e) {
            stepSizeControlGroup.setType(ControlGroupType.ERROR);
        }
    }

    @UiHandler("frameSizeTextBox")
    void onFrameSizeChange(ValueChangeEvent<String> event) {
        stopAndResetPlayer();
        int frameSize = 0;
        try {
            frameSize = Integer.parseInt(event.getValue());
            action.setFrameSize(frameSize);
            frameSizeControlGroup.setType(ControlGroupType.NONE);
        } catch (Exception e) {
            frameSizeControlGroup.setType(ControlGroupType.ERROR);
        }
    }

    @Override
    public void setDurationUnit(TimePlayerUnit unit) {
        checkNotNull(unit);
        durationListBox.setValue(unit.toString(), true);
    }

    @UiHandler("durationListBox")
    void onDurationUnitChange(SelectionEvent<String> event) {
        stopAndResetPlayer();
        action.setDurationUnit(TimePlayerUnit.valueOf(event.getSelectedItem()));
    }

    @UiHandler("frameSizeListBox")
    void onFrameSizeUnitChange(SelectionEvent<String> event) {
        stopAndResetPlayer();
        action.setFrameSizeUnit(TimePlayerUnit.valueOf(event.getSelectedItem()));
    }

    @UiHandler("stepSizeListBox")
    void onStepSizeUnitChange(SelectionEvent<String> event) {
        stopAndResetPlayer();
        action.setStepSizeUnit(TimePlayerUnit.valueOf(event.getSelectedItem()));
    }

    @Override
    public void setStepSize(int stepSize) {
        stepSizeTextBox.setValue("" + stepSize);
    }

    @Override
    public void setStepSizeUnit(TimePlayerUnit stepSizeUnit) {
        checkNotNull(stepSizeUnit);
        stepSizeListBox.setValue(stepSizeUnit.toString(), true);
    }

    @Override
    public void setCurrentTime() {
        Date date = null;
        long startTime = 0;
        if (startDateBoxAppended.isAttached() && startDateBoxAppended.getValue() != null) {
            startTime = startDateBoxAppended.getValue().getTime();
            date = new Date();
            date.setTime(startTime);
            currentDateBoxAppended.setValue(date);
        }
        
        
        if (date == null) {
            currentTextBox.setValue("");
        } else {
            currentTextBox.setValue(myTimeFormat.format(date));
        }
        

        progressBar.setPercent(0);
    }
    
    @Override
    public void setCurrentTime(Date date) {
        currentDateBoxAppended.setValue(date);
        if (date == null) {
            currentTextBox.setValue("");
        } else {
            currentTextBox.setValue(myTimeFormat.format(date));
        }
        long startTime = 0;
        if (startDateBoxAppended.getValue() != null) {
            startTime = startDateBoxAppended.getValue().getTime();
        }
        long endTime = 0;
        if (endDateBoxAppended.getValue() != null) {

            endTime = endDateBoxAppended.getValue().getTime();
        }
        int percent = 0;
        if (endTime != startTime) {
            percent = (int) ((100 * (date.getTime() - startTime)) / (endTime - startTime));
        }

        if (percent != 0 && percent != 100 && timePlayer.getSettings().getStepMode() == TimePlayerStepMode.PERCENTAGE) {

            percent = (int) (progressBar.getPercent() + timePlayer.getSettings().getPercentSize() * 100);
        }

        progressBar.setPercent(percent);
    }

    @Override
    public void setEndTime(Date date) {
        endDateBoxAppended.setValue(date);
        if (date == null) {
            endTextBox.setValue("");
        } else {
            endTextBox.setValue(myTimeFormat.format(date));
        }
    }

    @UiHandler("startTextBox")
    void onStartTextBox(ValueChangeEvent<String> event) {
        Date date = startDateBoxAppended.getValue();
        String formattedDate = myDateFormat.format(date);
        Date dateTime = myDateTimeFormat.parse(formattedDate + " " + startTextBox.getValue());
        action.setEndTime(dateTime);
    }

    @UiHandler("currentTextBox")
    void onCurrentTextBox(ValueChangeEvent<String> event) {

    }

    @UiHandler("endTextBox")
    void onEndTextBox(ValueChangeEvent<String> event) {

    }

    @Override
    public void setStartTime(Date date) {
        startDateBoxAppended.setValue(date);
        // timePlayer.showTime();
        if (date != null) {
            startTextBox.setValue(myTimeFormat.format(date));

            // startDateBoxAppended.reconfigure();
        } else {
            startTextBox.setValue("");
        }
    }

    @UiHandler("currentDateBoxAppended")
    void onCurrentTimeChange(ValueChangeEvent<Date> event) {
        action.setCurrentTime(event.getValue());
    }

    @UiHandler("endDateBoxAppended")
    void onEndTimeChange(ValueChangeEvent<Date> event) {
        stopAndResetPlayer();
        action.setEndTime(event.getValue());
    }

    @UiHandler("startDateBoxAppended")
    void onStartTimeChange(ValueChangeEvent<Date> event) {
        stopAndResetPlayer();
        action.setStartTime(event.getValue());
    }

    @Override
    public void setStepMode(TimePlayerStepMode stepMode) {
        switch (stepMode) {
            case ABSOLUTE:
                absoluteStepModeRB.setValue(true);
                stepSizeTextBox.setEnabled(true);
                stepSizeListBox.setEnabled(true);
                frameSizeTextBox.setEnabled(true);
                frameSizeListBox.setEnabled(true);
                break;
            case RELATIVE:
                relativeStepModeRB.setValue(true);
                stepSizeTextBox.setEnabled(false);
                stepSizeListBox.setEnabled(false);
                frameSizeTextBox.setEnabled(true);
                frameSizeListBox.setEnabled(true);
                break;
            case PERCENTAGE:
                percentageStepModeRB.setValue(true);
                stepSizeTextBox.setEnabled(false);
                stepSizeListBox.setEnabled(false);
                frameSizeTextBox.setEnabled(true);
                frameSizeListBox.setEnabled(true);
            default:
                absoluteStepModeRB.setValue(false);
                relativeStepModeRB.setValue(false);
                stepSizeTextBox.setEnabled(false);
                stepSizeListBox.setEnabled(false);
                break;
        }
    }


    private void stopAndResetPlayer(){
        timePlayer.mimicStop();
    }


    @UiHandler("absoluteStepModeRB")
    void onAbsoluteStepModeRB(ClickEvent event) {
        stopAndResetPlayer();
        action.setStepMode(TimePlayerStepMode.ABSOLUTE);
        stepSizeTextBox.setEnabled(true);
        stepSizeListBox.setEnabled(true);
        frameSizeTextBox.setEnabled(true);
    }

    @UiHandler("relativeStepModeRB")
    void onRelativeStepSizeRB(ClickEvent event) {
        stopAndResetPlayer();
        action.setStepMode(TimePlayerStepMode.RELATIVE);
        stepSizeTextBox.setEnabled(false);
        stepSizeListBox.setEnabled(false);
        frameSizeTextBox.setEnabled(true);
    }

    @UiHandler("percentageStepModeRB")
    void onPercentageStepSizeRB(ClickEvent event) {
        stopAndResetPlayer();
        action.setStepMode(TimePlayerStepMode.PERCENTAGE);
        stepSizeTextBox.setEnabled(false);
        stepSizeListBox.setEnabled(false);
        frameSizeTextBox.setEnabled(true);
    }

    @Override
    public void setPlaybackMin(Date playbackMin) {
        if (playbackMin != null) {
            startDateBoxAppended.setStartDate_(playbackMin);
            currentDateBoxAppended.setStartDate_(playbackMin);
            endDateBoxAppended.setStartDate_(playbackMin);
            reconfigueDateTimeBoxes();
        }
    }

    @Override
    public void setPlaybackMax(Date playbackMax) {
        if (playbackMax != null) {
            startDateBoxAppended.setEndDate_(playbackMax);
            currentDateBoxAppended.setEndDate_(playbackMax);
            endDateBoxAppended.setEndDate_(playbackMax);
            reconfigueDateTimeBoxes();
        }
    }

    private void reconfigueDateTimeBoxes() {
//        startDateBoxAppended.reconfigure();
//        currentDateBoxAppended.reconfigure();
//        endDateBoxAppended.reconfigure();

    }

    @UiHandler("nonVisibleItems")
    void onNonVisibleItems(ClickEvent event) {
        timePlayer.setHideNonVisibleItems(nonVisibleItems.getValue());
    }

    @Override
    public void setNonVisibleItems(boolean value) {
        nonVisibleItems.setValue(value);
    }

    interface MyUiBinder extends UiBinder<GraphTab, TimePlayerTab> {
    }
}
