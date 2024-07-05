package csi.client.gwt.viz.timeline.presenters;

import java.util.List;
import java.util.Set;

import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.shared.core.visualization.timeline.CommonTrack;

public interface TimelineChildPresenter {

    void cancel();

    //void updateSelection(TimelineEventSelection selection);

    void clearSelection();

    void selectText(String text);

    void resetZoom(); 

    void zoomIn();

    void panToNextEvent();

    void adjustGroups(boolean overviewRefresh);

    void selectAll();

    boolean hasSelection();

    int getSearchHitCount(String text);

    void updateOverview();

    TimeScale getTimeScale();

    void updateTimeScale();

    void zoom();

    void setTimeScale(TimeScale timeScale);

    void scroll();
    
    void resetState();

    void resize();

    void updateColor(String colorValue, List<String> itemOrderList);

    void select(TimelineEventSelection selection);

    void renderFooter();

    boolean hasItems();

    void tracksChanged();

}
