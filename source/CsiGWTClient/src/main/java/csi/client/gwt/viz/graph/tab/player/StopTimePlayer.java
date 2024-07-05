package csi.client.gwt.viz.graph.tab.player;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class StopTimePlayer extends AbstractTimePlayerAction {

    public StopTimePlayer(TimePlayer timePlayer) {
        super(timePlayer);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        TimePlayerView view = timePlayer.getView();
        view.enablePlay(true);
        view.enableStop(false);
        view.enableReset(true);
        timePlayer.setPlaying(false);
        timePlayer.fireEvent(new StopEvent());
        timePlayer.getGraphSurface().getGraph().hideProgressIndicator();
    }
}
