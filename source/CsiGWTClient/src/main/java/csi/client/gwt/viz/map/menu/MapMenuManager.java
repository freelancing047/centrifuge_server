package csi.client.gwt.viz.map.menu;

import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.menu.*;

public class MapMenuManager extends AbstractMenuManager<MapPresenter> {
    private static final String MAP_LINKUP_MENUS = "linkup.mapviz";
    private static final String MAP_BROADCAST_MENUS = "broadcast.mapviz";
    private boolean registrationDone = false;
    private MapPresenter mapPresenter;

    public MapMenuManager(MapPresenter presenter) {
        super(presenter);
        mapPresenter = presenter;
        registerMenuManager(MAP_LINKUP_MENUS, new LinkupMenuManager<>(presenter));
        registerMenuManager(MAP_BROADCAST_MENUS, new BroadcastMenuManager<>(presenter));
    }

    @Override
    public void registerPreloadMenus(boolean limitedMenu) {
        register(MenuKey.LOAD, new LoadMenuHandler<MapPresenter, AbstractMenuManager<MapPresenter>>(getPresenter(), this));

        if (!limitedMenu) {
            register(MenuKey.DELETE, new DeleteMenuHandler<MapPresenter, AbstractMenuManager<MapPresenter>>(getPresenter(), this));
        }

        register(MenuKey.SETTINGS, new MapSettingsHandler(getPresenter(), this));
        register(MenuKey.FILTERS, new MapFilterSettingsHandler(getPresenter(), this));

        if (!limitedMenu) {
            register(MenuKey.SPINOFF, new MapSpinOffHandler(getPresenter(), this));
            register(MenuKey.SPAWN, new MapSpawnTableHandler(getPresenter(), this));
        }
        register(MenuKey.EXPORT, new VizExportMenuHandler(getPresenter(), this));
    }

    @Override
    public void registerMenus(boolean limitedMenu) {
        if (registrationDone) {
            enable(MenuKey.SAVE);

            if (!limitedMenu) {
                enable(MenuKey.COPY);
                enable(MenuKey.MOVE);
            }

            enable(MenuKey.HIDE_LEGEND);
            enable(MenuKey.RESET_LEGEND);
            enable(MenuKey.HIDE_MULTITYPE_DECORATOR);

            enable(MenuKey.SELECT_ALL);
            enable(MenuKey.DESELECT_ALL);

            enable(MenuKey.CREATE_SELECTION_FILTER);
        } else {
            super.registerMenus(limitedMenu);

            register(MenuKey.SAVE, new SaveMenuHandler(getPresenter(), this));

            if (!limitedMenu) {
                register(MenuKey.COPY, new CopyHandler<>(getPresenter(), this));
                register(MenuKey.MOVE, new MoveHandler<>(getPresenter(), this));
            }

            register(MenuKey.HIDE_LEGEND, new HideMapLegendHandler(getPresenter(), this));
            hide(MenuKey.HIDE_LEGEND);
            register(MenuKey.MAP_METRICS, new AbstractMapMenuEventHandler(getPresenter(), this) {
                @Override
                public void onMenuEvent(CsiMenuEvent event) {
                    if (getPresenter().isViewLoaded()) {
                        getPresenter().showMetrics();
                    }
                }
            });

            register(MenuKey.SHOW_LEGEND, new ShowMapLegendHandler(getPresenter(), this));
            hide(MenuKey.SHOW_LEGEND);
            register(MenuKey.RESET_LEGEND, new ResetMapLegendHandler(getPresenter(), this));
            hide(MenuKey.RESET_LEGEND);
            register(MenuKey.HIDE_MULTITYPE_DECORATOR, new HideMultitypeDecoratorHandler(getPresenter(), this));
            hide(MenuKey.HIDE_MULTITYPE_DECORATOR);
            register(MenuKey.SHOW_MULTITYPE_DECORATOR, new ShowMultitypeDecoratorHandler(getPresenter(), this));
            hide(MenuKey.SHOW_MULTITYPE_DECORATOR);
            register(MenuKey.SHOW_LINKUP_HIGLIGHTS, new ShowLinkupHighlights(getPresenter(), this));
            hide(MenuKey.SHOW_LINKUP_HIGLIGHTS);
            register(MenuKey.CLEAR_MERGE_HIGHLIGHTS, new ClearMergeHighlightsHandler(getPresenter(), this));
            hide(MenuKey.CLEAR_MERGE_HIGHLIGHTS);

            register(MenuKey.SELECT_ALL, new SelectAllHandler(getPresenter(), this));
            register(MenuKey.DESELECT_ALL, new DeselectAllHandler(getPresenter(), this));

            register(MenuKey.CREATE_SELECTION_FILTER, new MapCreateSelectionFilterHandler(getPresenter(), this));

            initCheckboxes();

            Widget widget = getPresenter().getChrome().getMenu().getMenuWidget(MenuKey.TOOLS);
            CsiDropdown topLevel = (CsiDropdown) widget;
            topLevel.addClickHandler(event -> {
                if (mapPresenter.isViewLoaded()) {
                    mapPresenter.gatherMapToolsInfo();
                }
            });
            registrationDone = true;
        }
    }

    private void initCheckboxes() {
        getPresenter().getChrome().getMenu().checkedMenuItem(MenuKey.HIDE_LEGEND);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.SHOW_LEGEND);
        getPresenter().getChrome().getMenu().checkedMenuItem(MenuKey.HIDE_MULTITYPE_DECORATOR);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.SHOW_MULTITYPE_DECORATOR);
    }

}
