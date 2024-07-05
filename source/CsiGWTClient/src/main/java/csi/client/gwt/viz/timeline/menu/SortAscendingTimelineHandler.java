package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.server.common.model.SortOrder;

public class SortAscendingTimelineHandler extends AbstractTimelineMenuEventHandler {

    public SortAscendingTimelineHandler(TimelinePresenter presenter, TimelineMenuManager menuManager) {
        super(presenter, menuManager);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {

        TimelinePresenter presenter = getPresenter();
        presenter.doSort(SortOrder.ASC);
        
    }

}
