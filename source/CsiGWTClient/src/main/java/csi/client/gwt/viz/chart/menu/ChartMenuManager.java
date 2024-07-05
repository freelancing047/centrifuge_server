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
package csi.client.gwt.viz.chart.menu;

import java.util.List;

import com.github.gwtbootstrap.client.ui.NavLink;

import com.google.gwt.core.client.Scheduler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.chart.view.ChartMetricsView;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.*;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.MeasureDefinition;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChartMenuManager extends AbstractMenuManager<ChartPresenter> {

    private static final String CHART_LINKUP_MENUS = "linkup.chartviz";
    private static final String CHART_BROADCAST_MENUS = "broadcast.chartviz";

    boolean limited = true;
    public ChartMenuManager(ChartPresenter chartPresenter) {
        super(chartPresenter);
        registerMenuManager(CHART_LINKUP_MENUS, new LinkupMenuManager<ChartPresenter>(chartPresenter));
        registerMenuManager(CHART_BROADCAST_MENUS, new BroadcastMenuManager<ChartPresenter>(chartPresenter));
    }

	@Override
	public void registerPreloadMenus(boolean limitedMenu) {
	    limited = limitedMenu;
        register(MenuKey.LOAD, new LoadMenuHandler<ChartPresenter, AbstractMenuManager<ChartPresenter>>(getPresenter(), this));
        if(!limitedMenu){
        	register(MenuKey.DELETE, new DeleteMenuHandler<ChartPresenter, AbstractMenuManager<ChartPresenter>>(getPresenter(), this));
        }
        register(MenuKey.SETTINGS, new ChartSettingsHandler(getPresenter(), this));
        register(MenuKey.FILTERS, new ChartFilterSettingsHandler(getPresenter(), this));
	}

    @Override
    public void registerMenus(boolean limitedMenu) {
        super.registerMenus(limitedMenu);
        limited = limitedMenu;

        register(MenuKey.EXPORT, new VizExportMenuHandler<ChartPresenter, AbstractMenuManager<ChartPresenter>>(getPresenter(), this));

        register(MenuKey.SAVE, new SaveMenuHandler<ChartPresenter, AbstractMenuManager<ChartPresenter>>(getPresenter(), this));

        if(!limitedMenu){
        	register(MenuKey.MOVE, new MoveHandler<ChartPresenter>(getPresenter(), this));
        	register(MenuKey.COPY, new CopyHandler<ChartPresenter>(getPresenter(), this));

        }

        register(MenuKey.SELECT_ALL, new SelectAllHandler(getPresenter(), this));
        register(MenuKey.DESELECT_ALL, new DeselectAllHandler(getPresenter(), this));
        register(MenuKey.SELECT_DDD, new SelectDDDHandler(getPresenter(), this));

        register(MenuKey.CHART_METRICS, new AbstractChartMenuEventHandler(getPresenter(), this) {
            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                getPresenter().showMetrics();
            }
        });

        register(MenuKey.TOGGLE_SHOW_BREADCRUMBS, new AbstractChartMenuEventHandler(getPresenter(), this){
            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                getPresenter().doToggleBreadcrumb();
            }
        });

        if(!limitedMenu){
            register(MenuKey.SPINOFF, new ChartSpinoffHandler(getPresenter(),this));
            register(MenuKey.SPAWN, new ChartSpawnTableHandler(getPresenter(),this));
        }
        register(MenuKey.CREATE_SELECTION_FILTER, new CreateSelectionFilterHandler<ChartPresenter, ChartMenuManager>(getPresenter(), this));

        register(MenuKey.HIDE_OVERVIEW, new AbstractChartMenuEventHandler(getPresenter(), this) {
            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                getPresenter().setOverviewVisibility(false);
                enable(MenuKey.SHOW_OVERVIEW);
                hide(MenuKey.HIDE_OVERVIEW);
            }
        });

        register(MenuKey.SHOW_OVERVIEW, new AbstractChartMenuEventHandler(getPresenter(), this) {
            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                getPresenter().setOverviewVisibility(true);
                hide(MenuKey.SHOW_OVERVIEW);
                enable(MenuKey.HIDE_OVERVIEW);
            }
        });
        if (getPresenter().getVisualizationDef().getHideOverview()) {
            hide(MenuKey.HIDE_OVERVIEW);
        } else {
            hide(MenuKey.SHOW_OVERVIEW);
        }

        registerQuickSortMenuItems();
    }

    /**
     * Creates, adds a NavLink to the Edit Menu on Viz for each measure that is on the chart.
     */
    private void registerQuickSortMenuItems(){
        register(MenuKey.TOGGLE_SORT_CATEGORY, new CategorySortHandler(getPresenter(), this));

        if(getPresenter().getVisualizationDef().getChartSettings().isUseCountStarForMeasure()){
            register(MenuKey.TOGGLE_SORT_MEASURE, new MeasureSortHandler(getPresenter(), null));
        }else {
            addDynamicMeasuresToMenu();
        }
    }


    /**
     * This will add either a one menu item for *toggle sort by measure* or it will generate a list of measures.
     *
     */
    private void addDynamicMeasuresToMenu(){
        // need for the prefix label for measures
        CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
        ChartSettings chartSets = getPresenter().getVisualizationDef().getChartSettings();
        List<MeasureDefinition> mDefs = chartSets .getMeasureDefinitions();
        CsiMenuNav menu = getPresenter().getChrome().getMenu();

        // Apparently its totally possible to have measures there, but not use them..
        if(!chartSets .isUseCountStarForMeasure()) {
            //if we are not using count start - we don't need the single sort by measure
            menu.hide(MenuKey.TOGGLE_SORT_MEASURE);

            for (MeasureDefinition m : mDefs) {
                NavLink nav = new NavLink(i18n.menuDynamicToggleSortBy(m.getComposedName()));
                nav.addClickHandler(new MeasureSortHandler(getPresenter(), m));
                menu.addMenuItem(MenuKey.EDIT, nav);
            }
        }else{
            menu.enable(MenuKey.TOGGLE_SORT_MEASURE);
        }
    }

    /**
     * rebuilds the dynamic items under Edit MenuKey
     */
    public void updateDynamicMenus(){
        final CsiMenuNav myMenu =  getPresenter().getChrome().getMenu();
        myMenu.removeAllDynamicValues();
        addDynamicMeasuresToMenu();
        LinkupMenuManager<ChartPresenter> mgr = new LinkupMenuManager<ChartPresenter>(getPresenter());
        registerMenuManager(CHART_LINKUP_MENUS, mgr);
        mgr.registerMenus(limited);
    }

}
