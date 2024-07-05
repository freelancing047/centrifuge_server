package csi.client.gwt.viz.timeline.scheduler;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;

public abstract class CancelRepeatingCommand implements RepeatingCommand {

    boolean cancel = false;
    
    @Override
    public abstract boolean execute();

    public boolean isCancel() {
        return cancel;
    }

    public void cancel(){
        cancel = true;
    }

}
