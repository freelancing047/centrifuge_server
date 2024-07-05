package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;

public class ExpandAllHandler extends AbstractTimelineMenuEventHandler {

    public ExpandAllHandler(TimelinePresenter presenter, TimelineMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().collapseGroups(false);
    }

}
