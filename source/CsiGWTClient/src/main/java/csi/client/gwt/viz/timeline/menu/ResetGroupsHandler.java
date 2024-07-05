package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;

public class ResetGroupsHandler extends AbstractTimelineMenuEventHandler {

    public ResetGroupsHandler(TimelinePresenter presenter, TimelineMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {

        TimelinePresenter presenter = getPresenter();
        presenter.showGroups();
        presenter.resetGroup();
 }

}
