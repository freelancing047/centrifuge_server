package csi.client.gwt.viz.graph.tab.player;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.util.FieldDefUtils.SortOrder;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;

public class InitTimePlayer extends AbstractTimePlayerAction {

    private List<FieldDef> tempFieldDefs;

    public InitTimePlayer(TimePlayer timePlayer) {
        super(timePlayer);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        initializeFieldDefComboboxes();
        final TimePlayerSettings settings = timePlayer.getSettings();
        final TimePlayerView view = timePlayer.getView();
        view.enablePlay(true);
        view.enableReset(false);
        view.enableStop(false);
        view.setPlaybackMode(settings.getPlaybackMode());
        view.setPlaybackSpeed(settings.getSpeed());
        view.setStartField(settings.getStartField());
        view.setEndField(settings.getEndField());
        view.setDuration(settings.getDuration());
        view.setDurationUnit(settings.getDurationUnit());
        view.setStepSize(settings.getStepSize());
        view.setStepSizeUnit(settings.getStepSizeUnit());
        view.setStartTime(settings.getStartTime());
        view.setCurrentTime();
        view.setEndTime(settings.getEndTime());
        view.setStepMode(settings.getStepMode());
        view.setFramSize(settings.getFrameSize());
        view.setFrameSizeUnit(settings.getFrameSizeUnit());
        view.setPlaybackMin(settings.getPlaybackMin());
        view.setPlaybackMax(settings.getPlaybackMax());
        view.setNonVisibleItems(settings.hideNonVisibleItems());

        if (settings.getStartField() == null) {
            if (!tempFieldDefs.isEmpty()) {
                setStartField(tempFieldDefs.get(0));
            }
        } else {
            setStartField(settings.getStartField());
        }
    }

    private void initializeFieldDefComboboxes() {
        final List<FieldDef> fieldDefs = FieldDefUtils.getSortedNonStaticFields(timePlayer.getModelDef(), SortOrder.ALPHABETIC);
        tempFieldDefs = Lists.newArrayList();
        for (FieldDef fieldDef : fieldDefs) {
            CsiDataType type = fieldDef.getValueType();
            if (type.isTemporal()) {
                tempFieldDefs.add(fieldDef);
            }
        }
        final TimePlayerView view = timePlayer.getView();
        view.setFieldDefs(tempFieldDefs);
    }
}
