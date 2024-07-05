package csi.client.gwt.viz.graph.surface.tooltip;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;

public abstract class TooltipCloseCommand implements RepeatingCommand {

    private String tooltipId = "";

    public TooltipCloseCommand(String string){
        this.tooltipId  = string;
    }
    

    public String getTooltipId(){
        return tooltipId;
    }
}
