package csi.client.gwt.viz.timeline.presenters;

import csi.client.gwt.viz.timeline.view.TimelineView;

public abstract class AbstractTimelineChildPresenter {

    public abstract TimelineView getView();

    public void renderFooter() {
        getView().renderFooter();
    }


}
