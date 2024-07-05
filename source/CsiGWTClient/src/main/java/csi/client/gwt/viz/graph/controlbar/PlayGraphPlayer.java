package csi.client.gwt.viz.graph.controlbar;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.tab.player.TimePlayer;

public class PlayGraphPlayer extends AbstractGraphControlBarActivity {

    public PlayGraphPlayer(GraphControlBarImpl graphControlBar) {
        super(graphControlBar);
    }

    @Override
    public void togglePlaying() {
        controlBar.pause();
    }

    @Override
    public void stopPlayer() {
        controlBar.stop();

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        GraphControlBarView view = controlBar.getView();
        view.bind(this);
        view.setPlaying(true);
        Graph graph = controlBar.getGraph();
        TimePlayer timePlayer = graph.getTimePlayer();
        controlBar.getView().ensureScrubberTransition();
        timePlayer.play();
    }
}
