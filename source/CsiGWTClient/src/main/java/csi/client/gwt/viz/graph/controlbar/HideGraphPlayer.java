package csi.client.gwt.viz.graph.controlbar;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class HideGraphPlayer extends AbstractGraphControlBarActivity {
    public HideGraphPlayer(GraphControlBar graphControlBar) {
        super(graphControlBar);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        GraphControlBarView view = controlBar.getView();
        view.bind(this);
        view.setVisible(false);
    }
}
