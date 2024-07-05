package csi.client.gwt.dataview;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public interface CancellableScheduleCommand extends ScheduledCommand {

    
    @Override
    public void execute();
    
    public void cancel();

}
