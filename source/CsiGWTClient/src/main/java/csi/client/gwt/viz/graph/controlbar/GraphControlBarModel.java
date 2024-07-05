package csi.client.gwt.viz.graph.controlbar;

import java.util.Date;

import csi.client.gwt.viz.graph.tab.player.TimePlayerSettings;
import csi.server.common.model.visualization.graph.TimePlayerConstants.TimePlayerStepMode;

public class GraphControlBarModel {

    private final GraphControlBar controlBar;
    private boolean initialized;
    private long startTime;
    private Date currentTime;
    private Date previousTime;
    
    GraphControlBarModel(GraphControlBar controlBar) {
        this.controlBar = controlBar;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public Date getCurrentTime() {
        return currentTime;
    }
    
    private Date getPreviousTime(){
    	return previousTime;
    }
    
    public void invalidatePreviousTime(){
    	previousTime = null;
    }

    public double getCurrentPercent() {
        TimePlayerSettings settings = controlBar.getGraph().getTimePlayer().getSettings();
        
        long now = getCurrentTime().getTime();
        Date newTime = getCurrentTime();
        Date time = getPreviousTime();
        if(time != null){
	        if(newTime.compareTo(time) != 0){
	        	settings.incrementStep();
	        }
        }

        previousTime = new Date(now);
        if(settings.getStepMode() == TimePlayerStepMode.PERCENTAGE){
        	return settings.getPercentSize();
        }
        
        Date startDate = settings.getStartTime();
        if(startDate == null){
            return 0;
        }
        long startTime = startDate.getTime();
        Date endDate = settings.getEndTime();
        if (endDate == null) {
            return 0;
        }
        long endTime = endDate.getTime();
        return ((double)(now-startTime) /(double)(endTime-startTime));
    }

    public void setCurrentPercent(double percent) {
        if(percent<0){
            percent =0;
        }
        if (percent>1) {
            percent=1;
        }
        TimePlayerSettings settings = controlBar.getGraph().getTimePlayer().getSettings();
        Date startDate = settings.getStartTime();
        if(startDate == null){
            return;
        }
        long startTime = startDate.getTime();
        Date endDate = settings.getEndTime();
        if (endDate == null) {
            return;
        }
        long endTime = endDate.getTime();
        if(settings.getStepMode() != TimePlayerStepMode.PERCENTAGE){
        	currentTime = new Date((long) ((endTime-startTime)*percent+startTime));
        }
    }
}
