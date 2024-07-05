package csi.client.gwt.viz.graph.menu;

import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.filter.FilterSettingsHandler;
import csi.client.gwt.viz.shared.menu.*;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

public class GraphMenuManager extends AbstractMenuManager<Graph> {
    private static final String TABLE_BROADCAST_MENUS = "broadcast.relgraph";//NON-NLS
    private static final String TABLE_LINKUP_MENUS = "linkup.relgraph";//NON-NLS
    private boolean registrationDone = false;

    public GraphMenuManager(Graph graph) {
        super(graph);
        registerMenuManager(TABLE_BROADCAST_MENUS, new BroadcastMenuManager<>(graph));
        registerMenuManager(TABLE_LINKUP_MENUS, new LinkupMenuManager<>(graph));
    }

	@Override
	public void registerPreloadMenus(boolean limitedMenu) {
        register(MenuKey.LOAD, new LoadHandler(getPresenter(), this));
        if (!limitedMenu) {
            register(MenuKey.DELETE, new DeleteMenuHandler<Graph, AbstractMenuManager<Graph>>(getPresenter(), this));
        }

        register(MenuKey.SETTINGS, new SettingsHandler(getPresenter(), this));
        register(MenuKey.FILTERS, new FilterSettingsHandler<>(getPresenter(), this));
	}

    @Override
    public void registerMenus(boolean limitedMenu) {
    	if (registrationDone) {
//    		super.showMenus(limitedMenu);
    		
	        enable(MenuKey.EXPORT);
	        //register(MenuKey.PUBLISH, new PublishMenuHandler<Graph, AbstractMenuManager<Graph>>(getPresenter(), this));
	
	        if (!limitedMenu) {
	            enable(MenuKey.COPY);
	            enable(MenuKey.MOVE);
	        }
	        enable(MenuKey.HIDE_LEGEND);
	
	        enable(MenuKey.HIDE_ANNOTATION);
	
	        enable(MenuKey.RESET_LEGEND);
	        enable(MenuKey.BUNDLE);
	        enable(MenuKey.UNBUNDLE);
	        enable(MenuKey.COMPUTE_SNA_METRICS);
	        enable(MenuKey.SAVE);
	        enable(MenuKey.SELECT_ALL);
	        enable(MenuKey.SELECT_NEIGHBORS);
	        enable(MenuKey.INVERT_SELECTION);
	        enable(MenuKey.DESELECT_ALL);
	        enable(MenuKey.HIDE_SELECTION);
//	        enable(MenuKey.UNHIDE_SELECTION);
	        enable(MenuKey.UNHIDE_ALL);
	        //        register(MenuKey.REMOVE_SELECTED_NODES, new RemoveSelectedNodesHandler(getPresenter(), this));
	        enable(MenuKey.CLEAR_MERGE_HIGHLIGHTS);
	        enable(MenuKey.DELETE_PLUNKED);
	        enable(MenuKey.FORCE_DIRECTED);
	        enable(MenuKey.RADIAL);
	        enable(MenuKey.CIRCULAR);
	        enable(MenuKey.LINEAR_HIERARCHY);
	        enable(MenuKey.CENTRIFUGE);
	        enable(MenuKey.SCRAMBLE_AND_PLACE);
	        enable(MenuKey.GRID);
	        enable(MenuKey.APPLY_FORCE);
	        enable(MenuKey.TOGGLE_TOOLTIP_ANCHORS_ALWAYS);
	        enable(MenuKey.TOGGLE_TOOLTIP_ANCHORS_HOVER);
	        if (!limitedMenu) {
                enable(MenuKey.SPINOFF);
                enable(MenuKey.SPAWN);
	        }
	        enable(MenuKey.CREATE_SELECTION_FILTER);
//	        initCheckboxes2();
    	} else {
	        super.registerMenus(limitedMenu);
	
	        register(MenuKey.EXPORT, new VizExportMenuHandler<Graph, AbstractMenuManager<Graph>>(getPresenter(), this));
	        //register(MenuKey.PUBLISH, new PublishMenuHandler<Graph, AbstractMenuManager<Graph>>(getPresenter(), this));
	
	        if (!limitedMenu) {
	            register(MenuKey.COPY, new CopyHandler<>(getPresenter(), this));
	            register(MenuKey.MOVE, new MoveHandler<>(getPresenter(), this));
	        }
	        //        register(MenuKey.NODES_LIST, new NodesListHandler(getPresenter(), this));
	        //        register(MenuKey.LINKS_LIST, new LinksListHandler(getPresenter(), this));
	        //        register(MenuKey.GRAPH_SEARCH, new GraphSearchHandler(getPresenter(), this));
	        //        register(MenuKey.TIME_PLAYER, new TimePlayerHandler(getPresenter(), this));
	        register(MenuKey.HIDE_LEGEND, new HideLegendHandler(getPresenter(), this));
	        register(MenuKey.SHOW_LEGEND, new ShowLegendHandler(getPresenter(), this));
	        hide(MenuKey.SHOW_LEGEND);
	
	        register(MenuKey.HIDE_ANNOTATION, new HideAnnotationHandler(getPresenter(), this));
	        register(MenuKey.SHOW_ANNOTATION, new ShowAnnotationHandler(getPresenter(), this));
	        hide(MenuKey.HIDE_ANNOTATION);
	
	        register(MenuKey.RESET_LEGEND, new ResetLegendHandler(getPresenter(), this));
	        //        register(MenuKey.FIND_PATHS, new FindPathsHandler(getPresenter(), this));
	        register(MenuKey.BUNDLE, new BundleHandler(getPresenter(), this));
	        register(MenuKey.UNBUNDLE, new UnbundleHandler(getPresenter(), this));
	        register(MenuKey.QUICK_UNBUNDLE, new QuickUnbundleHandler(getPresenter(), this));



	        //        register(MenuItem.TRANSPARENCY, new TransparencyHandler(getPresenter(), this));
	        //        register(MenuKey.APPEARANCE_EDITOR, new AppearanceEditorHandler(getPresenter(), this));
	        register(MenuKey.COMPUTE_SNA_METRICS, new ComputeSNAMetricsHandler(getPresenter(), this));
	        register(MenuKey.SAVE, new SaveHandler(getPresenter(), this));
	        register(MenuKey.SELECT_ALL, new SelectAllHandler(getPresenter(), this));
			register(MenuKey.DESELECT_ALL, new DeselectAllHandler(getPresenter(), this));
			register(MenuKey.SELECT_NEIGHBORS, new SelectNeighborsHandler(getPresenter(), this));
			register(MenuKey.INVERT_SELECTION, new InvertSelectionHandler(getPresenter(), this));
	        register(MenuKey.HIDE_SELECTION, new HideSelectionHandler(getPresenter(), this));
//	        register(MenuKey.UNHIDE_SELECTION, new UnhideSelectionHandler(getPresenter(), this));
            register(MenuKey.UNHIDE_ALL, new UnhideAllHandler(getPresenter(), this));
            register(MenuKey.SHOW_LINKUP_HIGLIGHTS, new ShowLinkupHighlights(getPresenter(), this));
            //register(MenuKey.HIDE_LINKUP_HIGLIGHTS, new HideLinkupHighlights(getPresenter(), this));
	        
	        //        register(MenuKey.REMOVE_SELECTED_NODES, new RemoveSelectedNodesHandler(getPresenter(), this));
	        register(MenuKey.CLEAR_MERGE_HIGHLIGHTS, new ClearMergeHighlightsHandler(getPresenter(), this));
	        register(MenuKey.DELETE_PLUNKED, new DeleteAllPlunkedItemHandler(getPresenter(), this));
	        register(MenuKey.FORCE_DIRECTED, new ForceDirectedHandler(getPresenter(), this));
	        register(MenuKey.RADIAL, new RadialHandler(getPresenter(), this));
	        register(MenuKey.CIRCULAR, new CircularHandler(getPresenter(), this));
	        register(MenuKey.LINEAR_HIERARCHY, new LinearHierarchyHandler(getPresenter(), this));
	        register(MenuKey.CENTRIFUGE, new CentrifugeHandler(getPresenter(), this));
//	        register(MenuKey.SCRAMBLE_AND_PLACE, new ScrambleAndPlaceHandler(getPresenter(), this));
	        register(MenuKey.GRID, new GridHandler(getPresenter(), this));
	        register(MenuKey.APPLY_FORCE, new ApplyForceHandler(getPresenter(), this));
	        register(MenuKey.TOGGLE_TOOLTIP_ANCHORS_ALWAYS, new TooltipLineToggleHandler(MenuKey.TOGGLE_TOOLTIP_ANCHORS_ALWAYS, getPresenter(), this));
	        register(MenuKey.TOGGLE_TOOLTIP_ANCHORS_HOVER, new TooltipLineToggleHandler(MenuKey.TOGGLE_TOOLTIP_ANCHORS_HOVER, getPresenter(), this));
	        if (!limitedMenu) {
                register(MenuKey.SPINOFF, new GraphSpinoffHandler(getPresenter(), this));
                register(MenuKey.SPAWN, new GraphSpawnTableHandler(getPresenter(), this));
	        }
	        register(MenuKey.CREATE_SELECTION_FILTER, new CreateSelectionFilterHandler(getPresenter(), this));
	        
	        initCheckboxes();

	        registrationDone = true;
    	}
    }

    private void initCheckboxes() {
		getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.FORCE_DIRECTED);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.RADIAL);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.CIRCULAR);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.LINEAR_HIERARCHY);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.CENTRIFUGE);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.SCRAMBLE_AND_PLACE);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.GRID);
        
        Graph graph = getPresenter();
        RelGraphViewDef vizDef = (RelGraphViewDef)graph.getVisualizationDef();
        graph.getModel().checkLayout(vizDef.getLayout());

        getPresenter().getChrome().getMenu().checkedMenuItem(MenuKey.HIDE_ANNOTATION);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.SHOW_ANNOTATION);

        getPresenter().getChrome().getMenu().checkedMenuItem(MenuKey.HIDE_LEGEND);
        getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.SHOW_LEGEND);
    }
    
    @Override
    public void hideMenus(boolean limitedMenu) {
        super.hideMenus(limitedMenu);

        hide(MenuKey.EXPORT);
        if (!limitedMenu) {
        	hide(MenuKey.COPY);
        	hide(MenuKey.MOVE);
        }
        hide(MenuKey.HIDE_LEGEND);
        hide(MenuKey.SHOW_LEGEND);

        hide(MenuKey.HIDE_ANNOTATION);
        hide(MenuKey.HIDE_ANNOTATION);

        hide(MenuKey.RESET_LEGEND);
        hide(MenuKey.BUNDLE);
        hide(MenuKey.UNBUNDLE);
        hide(MenuKey.COMPUTE_SNA_METRICS);
        hide(MenuKey.SAVE);
        hide(MenuKey.SELECT_ALL);
        hide(MenuKey.SELECT_NEIGHBORS);
        hide(MenuKey.INVERT_SELECTION);
        hide(MenuKey.DESELECT_ALL);
        hide(MenuKey.HIDE_SELECTION);
        hide(MenuKey.UNHIDE_SELECTION);
        hide(MenuKey.UNHIDE_ALL);
        hide(MenuKey.CLEAR_MERGE_HIGHLIGHTS);
        hide(MenuKey.DELETE_PLUNKED);
        hide(MenuKey.FORCE_DIRECTED);
        hide(MenuKey.RADIAL);
        hide(MenuKey.CIRCULAR);
        hide(MenuKey.LINEAR_HIERARCHY);
        hide(MenuKey.CENTRIFUGE);
        hide(MenuKey.SCRAMBLE_AND_PLACE);
        hide(MenuKey.GRID);
        hide(MenuKey.APPLY_FORCE);
        hide(MenuKey.TOGGLE_TOOLTIP_ANCHORS_ALWAYS);
        hide(MenuKey.TOGGLE_TOOLTIP_ANCHORS_HOVER);
        if (!limitedMenu) {
            hide(MenuKey.SPINOFF);
            hide(MenuKey.SPAWN);
        }
        hide(MenuKey.CREATE_SELECTION_FILTER);
//        initCheckboxes2();
    }
}
