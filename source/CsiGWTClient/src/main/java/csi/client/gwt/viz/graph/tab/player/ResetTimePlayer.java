package csi.client.gwt.viz.graph.tab.player;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ResetTimePlayer extends AbstractTimePlayerAction {

    public ResetTimePlayer(TimePlayer timePlayer) {
        super(timePlayer);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        // init();
        TimePlayerView view = timePlayer.getView();
        view.enablePlay(true);
        view.enableStop(false);
        view.enableReset(false);
        timePlayer.setActive(false);
        timePlayer.setPlaying(false);
        TimePlayerSettings settings = timePlayer.getSettings();
        timePlayer.getGraphSurface().refresh(settings.stopPlayer());
    }
}
