package csi.client.gwt.viz.graph;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.Tab;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.dev.util.Pair;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.button.DefaultModeHandler;
import csi.client.gwt.viz.graph.button.FitToSizeHandler;
import csi.client.gwt.viz.graph.button.PanModeHandler;
import csi.client.gwt.viz.graph.button.SelectModeHandler;
import csi.client.gwt.viz.graph.button.VizButtonHandler;
import csi.client.gwt.viz.graph.button.ZoomModeHandler;
import csi.client.gwt.viz.graph.controlbar.GraphControlBar;
import csi.client.gwt.viz.graph.events.GraphEventHandler;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.graph.tab.player.TimePlayer;
import csi.client.gwt.viz.graph.window.annotation.GraphAnnotation;
import csi.client.gwt.viz.graph.window.legend.GraphLegend;
import csi.client.gwt.viz.graph.window.transparency.TransparencySettings;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.shared.core.visualization.graph.GraphLayout;
import csi.shared.gwt.viz.graph.MultiTypeInfo;
import csi.shared.gwt.vortex.CsiPair;

public interface Graph extends Visualization {

    AbstractMenuManager getMenuManager();

    Widget getGraphControlBar();

    GraphControlBar getGraphControlBarAsGraphControlBar();

    TimePlayer getTimePlayer();

    void showControlBar(boolean b);

    <T> void refreshTabs(VortexFuture<T> vortexFuture);

    void showNodesTabSnaColumns();

    Tab getStatisticsTab();

    Tab getPatternTab();

    Map<String, String> getVisItems();

    void setHiddenItemIndicator(Boolean hiddenItems);


    interface Model {

        boolean getLoadAfterSave();

        FillStrokeStyle getBackgroundColor();

        int getBackgroundColorInt();

        VortexFuture<FindItemDTO> findItemAt(int x, int y, boolean small);

        VortexFuture<SelectionModel> selectAll();

        VortexFuture<SelectionModel> clearSelection();

        VortexFuture<SelectionModel> invertSelection();

        VortexFuture<SelectionModel> selectPoint(int x, int y);

        VortexFuture<SelectionModel> selectRegion(int x1, int y1, int x2, int y2);
        
        VortexFuture<SelectionModel> deselectRegion(int x1, int y1, int x2, int y2);

        Collection<? extends Integer> getSelectNodes();

        Collection<? extends Integer> getSelectLinks();

        VortexFuture<Void> unhideAll();

        VortexFuture<List<CsiMap<String, String>>> applyLayout(GraphLayout layout);
        
        void checkLayout(GraphLayout layout);

        VortexFuture<Void> clearMergeHighlights();

        VortexFuture<Void> computeSNA();

        VortexFuture<Void> save();

        VortexFuture<List<Integer>> hideSelection();

        String getTitle();

        RelGraphViewDef getRelGraphViewDef();

        String getName();

        String getTheme();

        AbstractDataViewPresenter getDataview();

        void setTheme(String theme);

        void setBackgroundColor(int color);

        VortexFuture<Void> saveSettings();

        void setTitle(String value);

        boolean hasSelection();

        VortexFuture<SelectionModel> getSelectionModel();

        VortexFuture<Void> bundleSelection(String text);

        List<BundleDef> getBundleDefs();

        VortexFuture<Void> bundleEntireGraph();

        VortexFuture<Void> bundleSelectionBySpec();

        VortexFuture<SelectionModel> deselect(Integer id, String objectType);

        VortexFuture<SelectionModel> deselectAll();

        VortexFuture<Void> showOnlySelected();

        GraphPlayerSettings getPlayerSettings();

        void setPlayerSettings(GraphPlayerSettings playerSettings);

        void setCurrentSelectionAsOldSelection();

        VortexFuture setShadowSelection();

        VortexFuture<Void> saveSettings(boolean structural);

        VortexFuture<List<CsiMap<String,String>>> applyLayout(GraphLayout applyForce, int i);
        
        void applyLayoutBeforeLoad(GraphLayout layout);

        VortexFuture<FindItemDTO> getItem(FindItemDTO item);

        VortexFuture<MultiTypeInfo> findItemTypes(int x, int y);

        VortexFuture<CsiPair<Boolean, Boolean>> showLinkupHighlights();
    }

    interface Presenter {

    }

    interface View {
    }

    void load();

    void setLoaded(boolean loaded);

    boolean isLoaded();
    
    boolean isLoadedOnce();

    GraphLegend getLegend();

    GraphAnnotation getGraphAnnotation();

    void setVizUuid(String vizUuid);

    void setPresenter(Presenter presenter);

    VizButtonHandler getZoomOutHandler();

    ZoomModeHandler getZoomModeHandler();

    VizButtonHandler getZoomInHandler();

    String getDataviewUuid();

    Visualization getVisualization();

    Tab getTimePlayerTab();

    SelectModeHandler getSelectModeHandler();

    Tab getPathTab();

    PanModeHandler getPanModeHandler();

    DefaultModeHandler getDefaultModeHandler();

    Tab getNodesTab();

    Model getModel();

    Tab getLinksTab();

    GraphSurface getGraphSurface();

    GraphLegend getGraphLegend();

    FitToSizeHandler getFitToSizeHandler();

    FillStrokeStyle getBackgroundColor();

    GraphActivityManager getActivityManager();

    void addGraphEventHandler(GraphEvents type, GraphEventHandler handler);

    void hideLegend();

    void showLegend();

    AbstractDataViewPresenter getDataview();

    void fireEvent(GraphEvents event);

    boolean hasBundleSpecification();

    GraphPlayerSettings getPlayerSettings();

    void setPlayerSettings(GraphPlayerSettings playerSettings);

    void showTransparencyWindow();

    VizButtonHandler getTransparencyHandler();

    TransparencySettings getTransparency();

    void showProgressIndicator(VortexFuture vortexFuture, ClickHandler cancelHandler);

    void setProgressIndicatorText(String message);
    
    void hideProgressIndicator();

    void hideAnnotation();

    void showAnnotation();

    void respondToCancel();

    NodeStyle findNodeStyle(String typeName);

    void setTheme(GraphTheme result);

    GraphTheme getTheme();

    void clearStyles();

    void hideIfEmpty();

    void refresh();

    
}
