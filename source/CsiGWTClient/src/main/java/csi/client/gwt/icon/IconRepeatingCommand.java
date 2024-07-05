package csi.client.gwt.icon;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;

public abstract class IconRepeatingCommand implements RepeatingCommand {

    protected double scrollRatio = 0;
    protected double maxScrollPosition = 0;
    protected boolean cancelled = false;

    public IconRepeatingCommand(double scrollRatio, double maxScrollPosition){
        this.scrollRatio = scrollRatio;
        this.maxScrollPosition = maxScrollPosition;
    }
    
    @Override
    public abstract boolean execute();

    public double getScrollRatio() {
        return scrollRatio;
    }

    public void setScrollRatio(double scrollRatio) {
        this.scrollRatio = scrollRatio;
    }

    public double getMaxScrollPosition() {
        return maxScrollPosition;
    }

    public void setMaxScrollPosition(double maxScrollPosition) {
        this.maxScrollPosition = maxScrollPosition;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
