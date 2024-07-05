package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.shared.search.Searchable;

public class SearchHideHandler<V extends Visualization, M extends AbstractMenuManager<V>> extends AbstractMenuEventHandler {

    public SearchHideHandler(V presenter, M menuManager) {
        super(presenter, menuManager);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {

        if(getPresenter() instanceof Searchable) {
            ((Searchable)getPresenter()).hideFind();
        }
    }

}
