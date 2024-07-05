package csi.client.gwt.viz.graph.controlbar;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Created by Patrick on 4/30/2014.
 */
public class StepGraphPlayer extends AbstractGraphControlBarActivity {
    public StepGraphPlayer(GraphControlBar graphControlBar) {
        super(graphControlBar);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
    	double percent = controlBar.getModel().getCurrentPercent();
        controlBar.getView().setScrubberPercent(percent);
    }
}
