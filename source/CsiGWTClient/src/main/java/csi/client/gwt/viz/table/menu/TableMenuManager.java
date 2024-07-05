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
package csi.client.gwt.viz.table.menu;

import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.BroadcastMenuManager;
import csi.client.gwt.viz.shared.menu.CopyHandler;
import csi.client.gwt.viz.shared.menu.CreateSelectionFilterHandler;
import csi.client.gwt.viz.shared.menu.DeleteMenuHandler;
import csi.client.gwt.viz.shared.menu.LinkupMenuManager;
import csi.client.gwt.viz.shared.menu.LoadMenuHandler;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.viz.shared.menu.MoveHandler;
import csi.client.gwt.viz.shared.menu.SaveMenuHandler;
import csi.client.gwt.viz.shared.menu.VizExportMenuHandler;
import csi.client.gwt.viz.table.TablePresenter;
import csi.client.gwt.viz.timeline.menu.SearchHideHandler;
import csi.client.gwt.viz.timeline.menu.SearchShowHandler;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TableMenuManager extends AbstractMenuManager<TablePresenter> {

	private static final String TABLE_BROADCAST_MENUS = "broadcast.tableviz";
	private static final String TABLE_LINKUP_MENUS = "linkup.tableviz";

    public TableMenuManager(TablePresenter tablePresenter) {
        super(tablePresenter);
        registerMenuManager(TABLE_BROADCAST_MENUS, new BroadcastMenuManager<TablePresenter>(tablePresenter));
        registerMenuManager(TABLE_LINKUP_MENUS, new LinkupMenuManager<TablePresenter>(tablePresenter));
    }

	@Override
	public void registerPreloadMenus(boolean limitedMenu) {
        register(MenuKey.LOAD, new LoadMenuHandler<TablePresenter, AbstractMenuManager<TablePresenter>>(getPresenter(), this));
        if(!limitedMenu)
        	register(MenuKey.DELETE, new DeleteMenuHandler<TablePresenter, AbstractMenuManager<TablePresenter>>(getPresenter(), this));
        register(MenuKey.SETTINGS, new SettingsHandler(getPresenter(), this));
        register(MenuKey.FILTERS, new TableFilterSettingsHandler(getPresenter(), this));
	}

    @Override
    public void registerMenus(boolean limitedMenu) {
    	super.registerMenus(limitedMenu);
        register(MenuKey.EXPORT,  new VizExportMenuHandler<TablePresenter, AbstractMenuManager<TablePresenter>>(getPresenter(), this));
        //register(MenuKey.PUBLISH, new PublishMenuHandler<TablePresenter, AbstractMenuManager<TablePresenter>>(getPresenter(), this));
        register(MenuKey.SAVE, new SaveMenuHandler<TablePresenter, AbstractMenuManager<TablePresenter>>(getPresenter(), this));

        if(!limitedMenu){
	        register(MenuKey.COPY, new CopyHandler<TablePresenter>(getPresenter(), this));
	        register(MenuKey.MOVE, new MoveHandler<TablePresenter>(getPresenter(), this));
        }
        register(MenuKey.SELECT_ALL, new SelectAllHandler(getPresenter(), this));
        register(MenuKey.DESELECT_ALL, new DeselectAllHandler(getPresenter(), this));
        register(MenuKey.SHOW_SEARCH, new SearchShowHandler<TablePresenter, TableMenuManager>(getPresenter(), this));
        register(MenuKey.HIDE_SEARCH, new SearchHideHandler<TablePresenter, TableMenuManager>(getPresenter(), this));
        register(MenuKey.COPY_CELLS, new CopyCellsHandler<TablePresenter,TableMenuManager>(getPresenter(), this));
        if(!limitedMenu) {
            register(MenuKey.SPINOFF, new SpinoffHandler(getPresenter(), this));
            register(MenuKey.SPAWN, new SpawnTableHandler(getPresenter(), this));
        }
        register(MenuKey.CREATE_SELECTION_FILTER, new CreateSelectionFilterHandler(getPresenter(), this));
        

        getPresenter().getChrome().getMenu().checkedMenuItem(MenuKey.HIDE_SEARCH);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.SHOW_SEARCH);

        hide(MenuKey.HIDE_SEARCH);
        enable(MenuKey.SHOW_SEARCH);
    }

}
