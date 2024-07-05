package csi.client.gwt.viz.graph;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.vortex.VortexFuture;

public class NullableRepeatingCommand implements Scheduler.RepeatingCommand {

    private boolean isRepeat = true;
    private GraphImpl graph;
    private VortexFuture<Void> vortexFuture;
    private ClickHandler cancelHandler;

    public NullableRepeatingCommand(GraphImpl graph, VortexFuture<Void> vortexFuture, ClickHandler cancelHandler) {
        this.graph = graph;
        this.vortexFuture = vortexFuture;
        this.cancelHandler = cancelHandler;
    }

    @Override
    public boolean execute() {
        if (!graph.isHideProgressRequested() && isRepeat) {
            graph.createProgressIndicator(vortexFuture, cancelHandler);
        }
        return false;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

}
