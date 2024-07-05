package csi.client.gwt.viz.graph.tab.player;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;

public class StepTimePlayer extends AbstractTimePlayerAction {

    private final class onStepSuccess extends AbstractVortexEventHandler<List<String>> {

        @Override
        public void onSuccess(List<String> result) {
            if (timePlayer.isPlaying()) {
                long date = Long.parseLong(result.get(0));
                Date currentTime = new Date(date);
                timePlayer.fireEvent(new StepEvent(currentTime));
                timePlayer.getView().setCurrentTime(currentTime);
                // TODO: might want to tighten up this logic...
                if (!timePlayer.getSettings().getEndTime().after(currentTime)) {
                    timePlayer.stop();
                } else {
                    Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                        @Override
                        public boolean execute() {
                            if (timePlayer.isPlaying()) {
                                timePlayer.step();
                            }
                            return false;
                        }
                    }, timePlayer.getSettings().getSpeed().getDelay());
                }
            }
        }
    }

    public StepTimePlayer(TimePlayer timePlayer) {
        super(timePlayer);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        VortexFuture<List<String>> stepPlayerVF = timePlayer.getSettings().step();
        stepPlayerVF.addEventHandler(new onStepSuccess());
        timePlayer.getGraphSurface().refresh(stepPlayerVF);
    }
}
