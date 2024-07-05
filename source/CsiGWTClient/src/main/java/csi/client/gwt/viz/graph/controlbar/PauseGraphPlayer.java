package csi.client.gwt.viz.graph.controlbar;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class PauseGraphPlayer extends AbstractGraphControlBarActivity {
    PauseGraphPlayer(GraphControlBar controlBar) {
        super(controlBar);
    }

    @Override
    public void togglePlaying() {
        controlBar.play();
    }

    @Override
    public void stopPlayer() {
        controlBar.stop();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        GraphControlBarView view = controlBar.getView();
        view.bind(this);
        view.setPlaying(false);
        controlBar.getGraph().getTimePlayer().stop();
    }
}
