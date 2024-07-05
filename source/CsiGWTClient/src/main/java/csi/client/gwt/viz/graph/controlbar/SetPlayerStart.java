package csi.client.gwt.viz.graph.controlbar;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SetPlayerStart extends AbstractGraphControlBarActivity {
    public SetPlayerStart(GraphControlBar graphControlBar) {
        super(graphControlBar);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
//        TimePlayerSettings settings = controlBar.getGraph().getTimePlayer().getSettings();
//        settings.setStartTime(new Date(controlBar.getModel().getStartTime()));
    }
}
