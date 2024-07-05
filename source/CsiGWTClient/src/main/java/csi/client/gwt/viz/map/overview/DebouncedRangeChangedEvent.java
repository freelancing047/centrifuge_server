package csi.client.gwt.viz.map.overview;

import com.google.gwt.core.client.Scheduler;

import java.util.Date;

class DebouncedRangeChangedEvent implements Scheduler.RepeatingCommand {

    private OverviewPresenter overviewPresenter;
    private long refreshAt;
    private boolean cancelled = false;
    private int nextSize = -1;
    private boolean zoomIn;

    DebouncedRangeChangedEvent(OverviewPresenter overviewPresenter, int size, boolean zoomIn) {
        this.overviewPresenter = overviewPresenter;
        nextSize = size;
        this.zoomIn = zoomIn;
        resetTimer();
    }

    public void cancel() {
        cancelled = true;
    }

    @Override
    public boolean execute() {
        long time = new Date().getTime();
        if (!cancelled && time > refreshAt) {
            if(nextSize < 0){
                overviewPresenter.guessFireRangeChangedEvent();
            } else if(zoomIn) {
                overviewPresenter.virtualZoomIn(nextSize);
            } else {
//                overviewPresenter.virtualZoomOut(nextSize);
            }
            this.cancel();
            return false;
        } else if(cancelled){
            return false;
        }else {
            return true;
        }
    }

    private void resetTimer() {
        refreshAt = new Date().getTime() + OverviewPresenter.RANGE_CHANGED_MAX_DELAY_MILLIS;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
