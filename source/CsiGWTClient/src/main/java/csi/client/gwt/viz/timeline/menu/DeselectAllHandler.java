package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;

public class DeselectAllHandler extends  AbstractTimelineMenuEventHandler {


    public DeselectAllHandler(TimelinePresenter table, TimelineMenuManager mgr) {
        super(table, mgr);
    }


    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().deselectAll();
    }
}
