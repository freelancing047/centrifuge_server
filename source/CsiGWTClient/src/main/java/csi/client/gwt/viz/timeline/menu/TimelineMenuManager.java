/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.viz.map.menu.AbstractMapMenuEventHandler;
import csi.client.gwt.viz.shared.filter.FilterSettingsHandler;
import csi.client.gwt.viz.shared.menu.*;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TimelineMenuManager extends AbstractMenuManager<TimelinePresenter> {

    private static final String TIMELINE_LINKUP_MENUS = "linkup.timelineviz";
    private static final String TIMELINE_BROADCAST_MENUS = "broadcast.timelineviz";

    public TimelineMenuManager(TimelinePresenter presenter) {
        super(presenter);
        registerMenuManager(TIMELINE_LINKUP_MENUS, new LinkupMenuManager<TimelinePresenter>(presenter));
        registerMenuManager(TIMELINE_BROADCAST_MENUS, new BroadcastMenuManager<TimelinePresenter>(presenter));
    }

	@Override
	public void registerPreloadMenus(boolean limitedMenu) {
        register(MenuKey.LOAD, new LoadMenuHandler<TimelinePresenter, AbstractMenuManager<TimelinePresenter>>(getPresenter(), this));
        if(!limitedMenu)
        	register(MenuKey.DELETE, new DeleteMenuHandler<TimelinePresenter, AbstractMenuManager<TimelinePresenter>>(getPresenter(), this));
        register(MenuKey.SETTINGS, new TimelineSettingsHandler(getPresenter(), this));
        register(MenuKey.FILTERS, new FilterSettingsHandler<TimelinePresenter>(getPresenter(), this));
	}

    @Override
    public void registerMenus(boolean limitedMenu) {
        super.registerMenus(limitedMenu);

        register(MenuKey.EXPORT, new VizExportMenuHandler<TimelinePresenter, AbstractMenuManager<TimelinePresenter>>(getPresenter(), this));
        //register(MenuKey.PUBLISH, new PublishMenuHandler<TimelinePresenter, AbstractMenuManager<TimelinePresenter>>(getPresenter(), this));
        register(MenuKey.SAVE, new TimelineSaveMenuHandler<TimelinePresenter, AbstractMenuManager<TimelinePresenter>>(getPresenter(), this));

//        //register(MenuKey.DOWNLOAD_IMAGE, new DownloadAsImageHandler(getPresenter(), this));
//
        if(!limitedMenu){
	        register(MenuKey.COPY, new CopyHandler<TimelinePresenter>(getPresenter(), this));
	        register(MenuKey.MOVE, new MoveHandler<TimelinePresenter>(getPresenter(), this));
        }
        register(MenuKey.SELECT_ALL, new SelectAllHandler(getPresenter(), this));
        register(MenuKey.DESELECT_ALL, new DeselectAllHandler(getPresenter(), this));
        register(MenuKey.COLLAPSE_ALL, new CollapseAllHandler(getPresenter(), this));
        register(MenuKey.EXPAND_ALL, new ExpandAllHandler(getPresenter(), this));
        register(MenuKey.ZOOM_OUT, new ZoomOutHandler(getPresenter(), this));
        register(MenuKey.ZOOM_IN, new ZoomInHandler(getPresenter(), this));
        register(MenuKey.SORT_ASC, new SortAscendingTimelineHandler(getPresenter(), this));
        register(MenuKey.SORT_DSC, new SortDescendingTimelineHandler(getPresenter(), this));

        register(MenuKey.HIDE_LEGEND, new HideLegendHandler(getPresenter(), this));
        register(MenuKey.SHOW_LEGEND, new ShowLegendHandler(getPresenter(), this));

        register(MenuKey.RESET_LEGEND, new ResetLegendHandler(getPresenter(), this));
        register(MenuKey.SHOW_GROUPS, new ShowGroupsHandler(getPresenter(), this));
        register(MenuKey.HIDE_GROUPS, new HideGroupsHandler(getPresenter(), this));
        register(MenuKey.RESET_GROUPS, new ResetGroupsHandler(getPresenter(), this));
        register(MenuKey.SHOW_SEARCH, new SearchShowHandler<TimelinePresenter, TimelineMenuManager>(getPresenter(), this));
        register(MenuKey.HIDE_SEARCH, new SearchHideHandler<TimelinePresenter, TimelineMenuManager>(getPresenter(), this));
//
        register(MenuKey.TIMELINE_METRICS, new AbstractTimelineMenuEventHandler(getPresenter(), this) {
            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                if(getPresenter().isViewLoaded()) {
                    getPresenter().showMetrics();
                }

            }
        });

        if(!limitedMenu) {
            register(MenuKey.SPINOFF, new TimelineSpinoffHandler(getPresenter(), this));
            register(MenuKey.SPAWN, new TimelineSpawnTableHandler(getPresenter(), this));
        }
        register(MenuKey.CREATE_SELECTION_FILTER, new CreateSelectionFilterHandler(getPresenter(), this));

        getPresenter().getChrome().getMenu().checkedMenuItem(MenuKey.HIDE_GROUPS);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.SHOW_GROUPS);
        getPresenter().getChrome().getMenu().checkedMenuItem(MenuKey.HIDE_LEGEND);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.SHOW_LEGEND);

        getPresenter().getChrome().getMenu().checkedMenuItem(MenuKey.HIDE_SEARCH);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.SHOW_SEARCH);
        getPresenter().getChrome().getMenu().hide(MenuKey.SORT_ASC);
    }

}
