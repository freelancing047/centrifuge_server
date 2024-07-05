package csi.client.gwt.viz.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dev.util.Pair;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.window.legend.GraphLegend;
import csi.client.gwt.viz.graph.window.legend.InCommonLegendItem;
import csi.client.gwt.viz.graph.window.legend.NewlyAddedLegendItem;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.graph.GraphRequest;
import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.GraphActionServiceProtocol;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;
import csi.shared.core.visualization.graph.GraphLayout;
import csi.shared.gwt.viz.graph.MultiTypeInfo;
import csi.shared.gwt.vortex.CsiPair;

public class GraphProxy implements Graph.Model {

    private static final int DEFAULT_RENDER_THRESHOLD = 5000000;
    private static final int DEFAULT_BACKGROUND_COLOR_INT = 0xF7F7F7;
    private static final String DEFAULT_BACKGROUND_COLOR_STRING = "#F7F7F7"; //NON-NLS
    private static final CssColor DEFAULT_BACKGROUND_COLOR = CssColor.make(DEFAULT_BACKGROUND_COLOR_STRING);
    private static final String CSI_RELGRAPH_BACKGROUND_COLOR = "csi.relgraph.backgroundColor"; //NON-NLS
    private CssColor backgroundColor = DEFAULT_BACKGROUND_COLOR;
    private int backgroundColorInt = DEFAULT_BACKGROUND_COLOR_INT;
    private String backgroundColorString = DEFAULT_BACKGROUND_COLOR_STRING;
    private Graph graph;
    private RelGraphViewDef relGraphViewDef;
    // FIXME: would rather create a static NULL_SELECTION_MODEL
    private SelectionModel selectionModel = new SelectionModel();
    // This is a session variable with scope of a single visualization
    private boolean loadAfterSave = true;

    private VortexEventHandler<SelectionModel> setSelectionModel = new AbstractVortexEventHandler<SelectionModel>() {

        @Override
        public boolean onError(Throwable t) {
            // TODO: should i set the selection model to null?
            return true;
        }

        @Override
        public void onSuccess(SelectionModel result) {
            selectionModel = result;
            // fire selection changed event.
            graph.fireEvent(GraphEvents.SELECTION_CHANGED);
        }
    };

    @Override
    public void setPlayerSettings(GraphPlayerSettings playerSettings) {
        relGraphViewDef.setPlayerSettings(playerSettings);
    }


    public GraphProxy(Graph graph, VisualizationDef vizDef) {
        checkNotNull(graph);
        checkNotNull(vizDef);
        this.graph = graph;
        if (vizDef instanceof RelGraphViewDef) {
            relGraphViewDef = (RelGraphViewDef) vizDef;
            setBackgroundColor();
        }
    }

    @Override
    public VortexFuture<List<CsiMap<String, String>>> applyLayout(GraphLayout layout) {
        VortexFuture<List<CsiMap<String, String>>> future = WebMain.injector.getVortex().createFuture();
        try {

            ((RelGraphViewDef)graph.getVisualizationDef()).setLayout(layout);
            graph.saveSettings(false);
            graph.getModel().saveSettings(false);
            checkLayout(layout);
            
            String asyncParam = "true"; //NON-NLS // at the moment this isn't used...
            String value = layout.toString();
            String action = ""; // at the moment this isn't used...
            String x = "0";// at the moment this isn't used...
            String y = "0";// at the moment this isn't used...
            String componentIDParam = "";// at the moment this isn't used...
            future.execute(GraphActionServiceProtocol.class).componentLayoutAction(graph.getUuid(), componentIDParam,
                    x, y, action, value, asyncParam);
            

            graph.getMenuManager().hide(MenuKey.HIDE_ANNOTATION);
            graph.getMenuManager().enable(MenuKey.SHOW_ANNOTATION);

            
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        return future;
    }

    @Override
	public void applyLayoutBeforeLoad(GraphLayout layout) {
		((RelGraphViewDef) graph.getVisualizationDef()).setLayout(layout);
        graph.saveSettings(false);
        graph.getModel().saveSettings(false);
        VortexFuture<List<CsiMap<String, String>>> future = WebMain.injector.getVortex().createFuture();
        try {
        	future.execute(GraphActionServiceProtocol.class).clearGraphBeforeLoad(graph.getUuid(), layout.toString());
        } catch (Exception e) {
        }
	}

    @Override
    public VortexFuture<List<CsiMap<String, String>>> applyLayout(GraphLayout layout, int iterations) {
        VortexFuture<List<CsiMap<String, String>>> future = WebMain.injector.getVortex().createFuture();
        try {
            String asyncParam = "true"; //NON-NLS // at the moment this isn't used...
            String value = layout.toString();
            String action = ""+iterations; // at the moment this isn't used...
            String x = "0";// at the moment this isn't used...
            String y = "0";// at the moment this isn't used...
            String componentIDParam = "";// at the moment this isn't used...
            future.execute(GraphActionServiceProtocol.class).componentLayoutAction(graph.getUuid(), componentIDParam,
                    x, y, action, value, asyncParam);

            
            future.addEventHandler(noOpeventHandler);

            graph.getMenuManager().hide(MenuKey.HIDE_ANNOTATION);
            graph.getMenuManager().enable(MenuKey.SHOW_ANNOTATION);


        } catch (Exception e) {
        }
        return future;
    }


    public void checkLayout(GraphLayout layout) {

        if(graph == null || graph.getMenuManager() == null){
            return;
        }
        graph.getMenuManager().unCheckItem(MenuKey.SCRAMBLE_AND_PLACE);
        graph.getMenuManager().unCheckItem(MenuKey.CENTRIFUGE);
        graph.getMenuManager().unCheckItem(MenuKey.CIRCULAR);
        graph.getMenuManager().unCheckItem(MenuKey.FORCE_DIRECTED);
        graph.getMenuManager().unCheckItem(MenuKey.LINEAR_HIERARCHY);
        graph.getMenuManager().unCheckItem(MenuKey.RADIAL);
        graph.getMenuManager().unCheckItem(MenuKey.GRID);
        
        if(layout == null){
            graph.getMenuManager().checkItem(MenuKey.FORCE_DIRECTED);
            return;
        }
        switch(layout){
        case scramble:
            graph.getMenuManager().checkItem(MenuKey.SCRAMBLE_AND_PLACE);
            break;
        case centrifuge:
            graph.getMenuManager().checkItem(MenuKey.CENTRIFUGE);
            break;
        case circular:
            graph.getMenuManager().checkItem(MenuKey.CIRCULAR);
            break;
        case forceDirected:
            graph.getMenuManager().checkItem(MenuKey.FORCE_DIRECTED);
            break;
        case treeNodeLink:
            graph.getMenuManager().checkItem(MenuKey.LINEAR_HIERARCHY);
            break;
        case treeRadial:
            graph.getMenuManager().checkItem(MenuKey.RADIAL);
            break;
        case grid:
            graph.getMenuManager().checkItem(MenuKey.GRID);
            break;
        }
    }

    @Override
    public VortexFuture<Void> clearMergeHighlights() {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).clearMergeHighlights(graph.getUuid());
        } catch (Exception e) {
        }

        vortexFuture.addEventHandler(noOpeventHandler);
        return vortexFuture;
    }

    @Override
    public VortexFuture<SelectionModel> clearSelection() {
        VortexFuture<SelectionModel> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).clearSelection(selectionModel.id, graph.getUuid());
        } catch (Exception e) {
        }
        vortexFuture.addEventHandler(setSelectionModel);
        return vortexFuture;
    }

    @Override
    public VortexFuture<FindItemDTO> findItemAt(int x, int y, boolean all) {
        VortexFuture<FindItemDTO> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).gwtFindItem2(x, y, graph.getUuid(), all);
        } catch (Exception e) {
        }

        vortexFuture.addEventHandler(noOpeventHandler);
        return vortexFuture;
    }
    @Override
    public VortexFuture<FindItemDTO> getItem(FindItemDTO item) {
        VortexFuture<FindItemDTO> vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.execute(GraphActionServiceProtocol.class).getItem(graph.getUuid(), item, true);
        return vortexFuture;
    }
    
    @Override
    public VortexFuture<MultiTypeInfo> findItemTypes(int x, int y) {
        VortexFuture<MultiTypeInfo> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).gwtFindItemTypes(x, y, graph.getUuid());
        } catch (Exception e) {
        }

        vortexFuture.addEventHandler(noOpeventHandler);
        return vortexFuture;
    }

    @Override
    public CssColor getBackgroundColor() {
        setBackgroundColor();
        return backgroundColor;
    }

    @Override
    public int getBackgroundColorInt() {
        setBackgroundColor();
        return backgroundColorInt;
        // Color color = ClientColorHelper.get().makeFromString(backgroundColorString);
        // return color.getIntColor();
    }

    @Override
    public Collection<? extends Integer> getSelectLinks() {
        if (selectionModel == null) {
            // TODO: Log this case;
            return Lists.newArrayList();
        }
        return selectionModel.links;
    }

    @Override
    public Collection<? extends Integer> getSelectNodes() {
        if (selectionModel == null) {
            // TODO: Log this case;
            return Lists.newArrayList();
        }
        return selectionModel.nodes;
    }

    @Override
    public VortexFuture<SelectionModel> invertSelection() {
        VortexFuture<SelectionModel> vortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).invertSelection(selectionModel.id, graph.getUuid());
        } catch (CentrifugeException e) {
        }
        vortexFuture.addEventHandler(setSelectionModel);
        return vortexFuture;
    }

    @Override
    public VortexFuture<SelectionModel> selectAll() {
        VortexFuture<SelectionModel> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).selectAll(selectionModel.id, graph.getUuid());
        } catch (CentrifugeException e) {
        }
        vortexFuture.addEventHandler(setSelectionModel);
        return vortexFuture;
    }

    @Override
    public VortexFuture<SelectionModel> selectPoint(int x, int y) {
        // NOTE: the method takes a integers for a server call that accepts doubles. However these doubles are cast to integers on the other side, so this seems more transparent
        final VortexFuture<SelectionModel> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).selectionByPoint(graph.getUuid(), (double) x,
                    (double) y, false);
        } catch (CentrifugeException e) {
        }
        vortexFuture.addEventHandler(setSelectionModel);
        return vortexFuture;
    }

    @Override
    public VortexFuture<SelectionModel> selectRegion(int x1, int y1, int x2, int y2) {
        VortexFuture<SelectionModel> vortexFuture = WebMain.injector.getVortex().createFuture();
        boolean resetSelection = false; // never reset selection while dragging??

        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).selectRegion(graph.getUuid(), (double) x1,
                    (double) y1, (double) x2, (double) y2, null, resetSelection);
        } catch (Exception e) {
        }
        vortexFuture.addEventHandler(setSelectionModel);
        return vortexFuture;
    }
    
    @Override
    public VortexFuture<SelectionModel> deselectRegion(int x1, int y1, int x2, int y2) {
        VortexFuture<SelectionModel> vortexFuture = WebMain.injector.getVortex().createFuture();
        boolean resetSelection = false; // never reset selection while dragging??

        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).deselectRegion(graph.getUuid(), (double) x1,
                    (double) y1, (double) x2, (double) y2, null, resetSelection);
        } catch (Exception e) {
        }
        vortexFuture.addEventHandler(setSelectionModel);
        return vortexFuture;
    }

    @Override
    public void setBackgroundColor(int color) {
        if(color == 16777215){
            color = 16711422;
        }
        backgroundColorInt = color;
        relGraphViewDef.setPropertyValue(CSI_RELGRAPH_BACKGROUND_COLOR, backgroundColorInt + ""); // push into server model
        setBackgroundColor(); // update proxy model
    }

    /*@Override
    public void setBackgroundColor(int color) {
        if(color == 16711422){
            color = 16777215;
        }
        backgroundColorInt = color;
        relGraphViewDef.setPropertyValue(CSI_RELGRAPH_BACKGROUND_COLOR, backgroundColorInt + ""); // push into server model
        setBackgroundColor(); // update proxy model
    }

    private void setBackgroundColor() {
        if ((relGraphViewDef == null) || (relGraphViewDef.getSettings() == null)) {
            return;
        }
        String myColorString = relGraphViewDef.getSettings().getPropertiesMap().get(CSI_RELGRAPH_BACKGROUND_COLOR);
        if ((null != myColorString) && (0 < myColorString.length())) {

            int color = Integer.parseInt(myColorString);
            if(color == 16711422){
                color = 16777215;
            }
            if(backgroundColorInt == color) {
                return; //optimization
            }
            Color myColor = ClientColorHelper.get().make(color);
            backgroundColorString = myColor.toString();
            backgroundColorInt = color;
            backgroundColor = CssColor.make(backgroundColorString);
        } else {
            relGraphViewDef.setPropertyValue(CSI_RELGRAPH_BACKGROUND_COLOR, backgroundColorInt + ""); // push default background color into server model
        }
    }
transformItemPointToScreen
    */

    private void setBackgroundColor() {
        if ((relGraphViewDef == null) || (relGraphViewDef.getSettings() == null)) {
            return;
        }
        String myColorString = relGraphViewDef.getSettings().getPropertiesMap().get(CSI_RELGRAPH_BACKGROUND_COLOR);
        if ((null != myColorString) && (0 < myColorString.length())) {

            int color = Integer.parseInt(myColorString);
            if(color == 16777215){
                color = 16711422;
            }
            if(backgroundColorInt == color) {
                return; //optimization
            }
            Color myColor = ClientColorHelper.get().make(color);
            backgroundColorString = myColor.toString();
            backgroundColorInt = color;
            backgroundColor = CssColor.make(backgroundColorString);
        } else {
            relGraphViewDef.setPropertyValue(CSI_RELGRAPH_BACKGROUND_COLOR, backgroundColorInt + ""); // push default background color into server model
        }
    }

    private void setRelGraphViewDef(RelGraphViewDef relGraphViewDef) {
        this.relGraphViewDef = relGraphViewDef;
    }

    @Override
    public VortexFuture<Void> unhideAll() {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).unhideAll(graph.getDataviewUuid(), graph.getUuid());
        } catch (Exception e) {
        }
        return vortexFuture;
    }
    
    @Override
    public VortexFuture<CsiPair<Boolean, Boolean>> showLinkupHighlights() {
        VortexFuture<CsiPair<Boolean, Boolean>> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).showLastLinkupHighlights(graph.getUuid(), graph.getDataviewUuid());
        } catch (Exception e) {
        }
        
        
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<CsiPair<Boolean, Boolean>>() {

            @Override
            public void onSuccess(CsiPair<Boolean, Boolean> result) {
                //update legend
                GraphLegend legend = graph.getGraphLegend();
                legend.load();
                
            }
        });
        return vortexFuture;
    }

    @Override
    public VortexFuture<Void> computeSNA() {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            String metric = "";// Does not appear to be used...
            graph.showProgressIndicator(vortexFuture, null);
            vortexFuture.execute(GraphActionServiceProtocol.class).computeSNA(graph.getUuid(), metric);
            vortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>(){

                @Override
                public void onSuccess(Void result) {
                    graph.hideProgressIndicator();
                }

            @Override
            public boolean onError(Throwable t) {
                graph.hideProgressIndicator();
                return false;
            }});
        } catch (Exception e) {

            graph.hideProgressIndicator();
        }
        return vortexFuture;
    }

    @Override
    public VortexFuture<List<Integer>> hideSelection() {
        VortexFuture<List<Integer>> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            String uuid = graph.getUuid();
            String dataviewUuid = graph.getDataviewUuid();
            vortexFuture.addEventHandler(new AbstractVortexEventHandler<List<Integer>>() {
                
                @Override
                public void onSuccess(List<Integer> result) {
                    graph.getGraphSurface().getToolTipManager().hideCorrespondingTooltips(result);
                }
            });

            vortexFuture.execute(GraphActionServiceProtocol.class).hideSelection(dataviewUuid, uuid);
        } catch (Exception e) {
        }
        return vortexFuture;
    }

    @Override
    public VortexFuture<Void> save() {// TODO: rename to saveState?
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).saveGraph(graph.getUuid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vortexFuture;
    }

    @Override
    public VortexFuture<Void> saveSettings() {
        return saveSettings(false);
    }

    
    @Override
    public VortexFuture<Void> saveSettings(boolean structural) {
        relGraphViewDef.setOldSelection(selectionModel);
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            
            vortexFuture.execute(VisualizationActionsServiceProtocol.class).saveSettings(relGraphViewDef,
                    graph.getDataviewUuid(), structural);

        } catch (Exception myException) {
            
            Dialog.showException(myException);
        }
        // save();
        return vortexFuture;
    }

    @Override
    public String getTitle() {
        return relGraphViewDef.getName();
    }

    @Override
    public RelGraphViewDef getRelGraphViewDef() {
        return relGraphViewDef;
    }


    @Override
    public String getName() {
        return relGraphViewDef.getName();
    }

    @Override
    public boolean getLoadAfterSave() {
        return loadAfterSave;
    }

    @Override
    public String getTheme() {
        return relGraphViewDef.getThemeUuid();
    }

    @Override
    public void setTheme(String themeUuid) {
        relGraphViewDef.setThemeUuid(themeUuid);
    }

    @Override
    public AbstractDataViewPresenter getDataview() {
        return graph.getDataview();
    }

    @Override
    public void setTitle(String value) {
        relGraphViewDef.setName(value);
    }

    @Override
    public boolean hasSelection() {
        return !selectionModel.isCleared();
    }

    @Override
    public VortexFuture<SelectionModel> getSelectionModel() {
        VortexFuture<SelectionModel> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class)
                    .getSelectionModel(selectionModel.id, graph.getUuid());
        } catch (CentrifugeException e) {
        }
        vortexFuture.addEventHandler(setSelectionModel);
        return vortexFuture;
    }

    @Override
    public VortexFuture<Void> bundleSelection(String text) {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).manuallyBundleSelection(graph.getUuid(),
                    graph.getDataviewUuid(), text);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        return vortexFuture;
    }

    @Override
    public List<BundleDef> getBundleDefs() {
        return relGraphViewDef.getBundleDefs();

    }

    @Override
    public VortexFuture<Void> bundleEntireGraph() {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).bundleEntireGraphBySpec(graph.getUuid(),
                    graph.getDataviewUuid());
        } catch (Exception e) {
        }

        vortexFuture.addEventHandler(noOpeventHandler);
        return vortexFuture;

    }

    @Override
    public VortexFuture<Void> bundleSelectionBySpec() {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).bundleSelectionBySpec(graph.getUuid(),
                    graph.getDataviewUuid());
        } catch (Exception e) {
        }

        vortexFuture.addEventHandler(noOpeventHandler);
        return vortexFuture;

    }

    @Override
    public VortexFuture<SelectionModel> deselect(final Integer id, final String objectType) {
        final VortexFuture<SelectionModel> createFuture = WebMain.injector.getVortex().createFuture();
        final GraphRequest graphRequest = new GraphRequest();
        getSelectionModel().addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {

            @Override
            public void onSuccess(SelectionModel result) {
                if(ObjectAttributes.EDGES_OBJECT_TYPE.equals(objectType)) {
                    result.links.remove(id);
                }
                else{
                    result.nodes.remove(id);
                }
                graphRequest.nodes = Lists.newArrayList(result.nodes);
                graphRequest.links = Lists.newArrayList(result.links);
                try {
                    createFuture.execute(GraphActionServiceProtocol.class).select(graph.getUuid(), true, false, false,
                            graphRequest);
                } catch (CentrifugeException e) {
                }
            }
        });
        return createFuture;
    }

    @Override
    public VortexFuture<SelectionModel> deselectAll() {
        VortexFuture<SelectionModel> vortexFuture = WebMain.injector.getVortex().createFuture();
        final GraphRequest graphRequest = new GraphRequest();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).select(graph.getUuid(), true, false, false,
                    graphRequest);
        } catch (Exception e) {
        }
        vortexFuture.addEventHandler(setSelectionModel);
        return vortexFuture;
    }

    @Override
    public VortexFuture<Void> showOnlySelected() {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        vortexFuture.execute(GraphActionServiceProtocol.class).showOnlySelection(graph.getDataviewUuid(),
                graph.getUuid());
        selectionModel.reset();
        graph.fireEvent(GraphEvents.SELECTION_CHANGED);
        return vortexFuture;
    }

    @Override
    public GraphPlayerSettings getPlayerSettings() {
        return relGraphViewDef.getPlayerSettings();
    }

    @Override
    public void setCurrentSelectionAsOldSelection() {
        // relGraphViewDef.setOldSelection(selectionModel);

    }

    @Override
    public VortexFuture<Void> setShadowSelection() {
        VortexFuture<SelectionModel> future = getSelectionModel();
        final VortexFuture<Void> finished = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {

            @Override
            public void onSuccess(SelectionModel result) {
                SelectionModel shadow = new SelectionModel();
                if (result != null) {
                    shadow.nodes = Sets.newTreeSet(result.nodes);
                    shadow.links = Sets.newTreeSet(result.links);
                    relGraphViewDef.setShadowSelection(shadow);
                    VortexFuture<Void> saveSettings = saveSettings();
                    saveSettings.addEventHandler(new AbstractVortexEventHandler<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            finished.fireSuccess(null);
                        }
                    });
                }
            }
        });
        return finished;
    }
    
    @SuppressWarnings("rawtypes")
    private AbstractVortexEventHandler noOpeventHandler = new AbstractVortexEventHandler(){

        @Override
        public void onSuccess(Object result) {
            // TODO Auto-generated method stub
            
        }
    
        @Override
        public boolean onError(Throwable t){
            return true;
            
        }};
}
