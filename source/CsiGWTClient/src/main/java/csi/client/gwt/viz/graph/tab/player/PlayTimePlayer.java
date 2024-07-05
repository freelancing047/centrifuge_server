package csi.client.gwt.viz.graph.tab.player;

import java.util.List;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;

public class PlayTimePlayer extends AbstractTimePlayerAction {

    private final class StepCommand implements RepeatingCommand {

        public boolean stop;

        @Override
        public boolean execute() {
            return timePlayer.isPlaying() && !stop;
        }
    }

    public PlayTimePlayer(TimePlayer timePlayer) {
        super(timePlayer);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        TimePlayerView view = timePlayer.getView();
        view.enablePlay(false);
        view.enableStop(true);
        view.enableReset(false);
        timePlayer.setPlaying(true);
        TimePlayerSettings settings = timePlayer.getSettings();
        if(!timePlayer.isActive()) {
            VortexFuture<List<String>> activateVF = settings.activate();
            activateVF.addEventHandler(new AbstractVortexEventHandler<List<String>>() {

                @Override
                public void onSuccess(List<String> result) {
                    timePlayer.step();
                }
            });
        }else {
            timePlayer.step();
        }
    }

    @Override
    public void onStop() {
        stepCommand.stop = true;
        super.onStop();
    }

    private final StepCommand stepCommand = new StepCommand();

}
