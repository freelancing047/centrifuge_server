package csi.client.gwt.viz.timeline.presenters;

import java.util.List;
import java.util.Set;

import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.shared.core.visualization.timeline.CommonTrack;

public class TimelineEmptyPresenter implements TimelineChildPresenter{

    @Override
    public void cancel() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clearSelection() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void selectText(String text) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resetZoom() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void zoomIn() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void panToNextEvent() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void adjustGroups(boolean refresh) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public boolean hasSelection() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getSearchHitCount(String text) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void updateOverview() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public TimeScale getTimeScale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateTimeScale() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void zoom() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTimeScale(TimeScale timeScale) {
        // TODO Auto-generated method stub
        
    }
/*
    @Override
    public void selectByText(String text, boolean removeExistingSelection) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deselectByText(String text) {
        // TODO Auto-generated method stub
        
    }*/

    @Override
    public void scroll() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resetState() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resize() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateColor(String colorValue, List<String> itemOrderList) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void select(TimelineEventSelection selection) {
        // TODO Auto-generated method stub
        
    }

    public void clear() {
        resetState();
        setTimeScale(null);
    }

    @Override
    public void renderFooter() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean hasItems() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void selectAll() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void tracksChanged() {
        // TODO Auto-generated method stub
        
    }


}
