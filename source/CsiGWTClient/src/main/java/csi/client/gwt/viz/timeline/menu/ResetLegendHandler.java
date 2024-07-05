package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;

public class ResetLegendHandler extends AbstractTimelineMenuEventHandler {

    public ResetLegendHandler(TimelinePresenter presenter, TimelineMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {

        TimelinePresenter presenter = getPresenter();
        presenter.showLegend();
        presenter.resetLegend();
 }

}
