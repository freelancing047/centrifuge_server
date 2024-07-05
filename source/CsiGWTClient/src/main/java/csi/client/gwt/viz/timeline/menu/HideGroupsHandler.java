package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;

public class HideGroupsHandler extends AbstractTimelineMenuEventHandler {

    public HideGroupsHandler(TimelinePresenter presenter, TimelineMenuManager menuManager) {
        super(presenter, menuManager);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().hideGroups();
    }

}
