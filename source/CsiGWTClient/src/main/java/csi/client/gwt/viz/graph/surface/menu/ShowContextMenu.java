package csi.client.gwt.viz.graph.surface.menu;

import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.ADD_ANNOTATION;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.BUNDLE;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.DELETE_PLUNKED_ITEM;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.DESELECT;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.DESELECT_ALL;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.EDIT_PLUNKED_ITEM;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.HIDE_SELECTION;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.PLUNK_LINK;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.PLUNK_NODE;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.QUICK_REVEAL_NEIGHBORS;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.QUICK_SELECT_NEIGHBORS;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.QUICK_UNBUNDLE;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.SELECT;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.SELECT_ALL;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.SHOW_ONLY;
import static csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum.UNHIDE_ALL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sencha.gxt.core.client.dom.XDOM;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.menu.BundleDialog;
import csi.client.gwt.viz.graph.plunk.PlunkLinkActivity;
import csi.client.gwt.viz.graph.plunk.PlunkNodePresenter;
import csi.client.gwt.viz.graph.plunk.edit.EditPlunkedItemPresenter;
import csi.client.gwt.viz.graph.plunk.util.PlunkNodeUtils;
import csi.client.gwt.viz.graph.surface.AbstractGraphSurfaceActivity;
import csi.client.gwt.viz.graph.surface.ContextMenuInfo;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.graph.surface.annotation.AnnotationPresenter;
import csi.client.gwt.viz.graph.surface.menu.GraphContextMenu.GraphContextActionEnum;
import csi.client.gwt.viz.viewer.Viewer;
import csi.server.common.model.visualization.viewer.Objective;
import csi.client.gwt.viz.viewer.objective.graph.GraphObjective;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.dto.graph.GraphRequest;
import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.dto.graph.gwt.NeighborsDTO;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class ShowContextMenu extends AbstractGraphSurfaceActivity implements ContextMenuPresenter {

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private int surfaceY;
    private int surfaceX;
    private PopupPanel popupPanel;
    private PopupPanel submenuPopupPanel;
    private GraphContextMenu contextMenu;
    private Composite submenu;
    private FindItemDTO item;
    private SelectionModel selectionModel;
    private boolean readOnly = false;

    public ShowContextMenu(GraphSurface graphSurface) {
        super(graphSurface);
    }

    @Override
    public void showMenuAt(int windowX, int windowY, int surfaceX, int surfaceY) {
        this.surfaceX = surfaceX;
        this.surfaceY = surfaceY;
        String vizUuid = graphSurface.getVizUuid();

        popupPanel = new PopupPanel(true);
        popupPanel.setGlassEnabled(true);
        popupPanel.setPopupPosition(windowX, windowY);

        if (!isBackground(surfaceX, surfaceY)) {
            setContextMenuForFoundItem(surfaceX, surfaceY, vizUuid);
        } else {
            setContextMenuForSloppy(surfaceX, surfaceY, vizUuid);
        }
    }

    @Override
    public void handleMouseOverAction(final int x, final int y, GraphContextActionEnum action) {
        switch (action) {
            case QUICK_REVEAL_NEIGHBORS:
                hideSubmenu();
                createRevealNeighborsSubmenu();
                showSubmenu(x, y);
                break;
            default:
                hideSubmenu();
                break;
        }
    }

    public void setReadOnly() {

        readOnly = true;
        if (null != contextMenu) {

            contextMenu.setReadOnly();
        }
    }

    private void hideSubmenu() {
        if (submenuPopupPanel != null) {
            submenuPopupPanel.hide();
        }
    }

    private void createRevealNeighborsSubmenu() {
        ArrayList<String> typesWithNodes = getTypesWithNodes();

        submenu = new RevealNeighborsSubmenu(typesWithNodes, new GraphContextSubmenuCallback() {

            @Override
            public void execute(String type) {
                revealOneLevelOfNeighbors(type);
                hideSubmenu();
                popupPanel.hide();
            }

        });
    }

    private ArrayList<String> getTypesWithNodes() {
        ArrayList<String> typesWithNodes = new ArrayList<String>();
        // if (item.isSelected()) {
        // Graph graph = graphSurface.getGraph();
        // VortexFuture<Void> neighborFutureA = getNeighborFutureA();
        // try {
        // Map<String, List<Map<String, String>>> selectionInfo = neighborFutureA.execute(GraphActionServiceProtocol.class).selectionInfo(graph.getUuid());
        // } catch (CentrifugeException e) {
        // }
        // } else {
        for (String type : item.neighborTypeCounts.keySet()) {
            if (item.neighborTypeCounts.get(type) > 0) {
                typesWithNodes.add(type);
            }
        }
        // }
        return typesWithNodes;
    }

    private void revealOneLevelOfNeighbors(String type) {
        Graph graph = graphSurface.getGraph();
        //FIXME: I think this is broken.
        if (type.equals("All")) {//FIXME:i18n
            revealOneLevelOfNeighbors(graph);
        } else {
            try {
                VortexFuture<Void> neighborFutureA = getNeighborFutureA();
                if (item.isSelected()) {
                    neighborFutureA.execute(GraphActionServiceProtocol.class).showAdjacentFor(graph.getUuid(), type);
                } else {
                    neighborFutureA.execute(GraphActionServiceProtocol.class).showAdjacentFor(graph.getUuid(),
                            item.itemId, type);
                }
            } catch (CentrifugeException ignored) {
            }
        }
    }

    private VortexFuture<Void> getNeighborFutureA() {
        VortexFuture<Void> neighborFutureA = WebMain.injector.getVortex().createFuture();
        graphSurface.refresh(neighborFutureA);
        return neighborFutureA;
    }

    private void showSubmenu(int x, int y) {
        x = ensureSubmenuWithinWindowWidth(x);
        y = ensureSubmenuWithinWindowHeight(y);
        submenuPopupPanel = new PopupPanel(true);
        submenuPopupPanel.setPopupPosition(x, y);
        submenuPopupPanel.add(submenu);
        submenuPopupPanel.show();
        submenuPopupPanel.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
    }

    private int ensureSubmenuWithinWindowWidth(int x) {
        if ((x + ContextMenuInfo.CONTEXT_MENU_WIDTH) > Window.getClientWidth()) {
            x = Window.getClientWidth() - ContextMenuInfo.CONTEXT_MENU_WIDTH * 2;
        }
        return x;
    }

    private int ensureSubmenuWithinWindowHeight(int y) {
        if (((y + ContextMenuInfo.CONTEXT_MENU_HEIGHT) - 90) > Window.getClientHeight()) {
            y = Window.getClientHeight() - ContextMenuInfo.CONTEXT_MENU_HEIGHT + 90;
        }
        return y;
    }

    @Override
    public void handleSelectedAction(GraphContextActionEnum action) {
        // What to do when each button is pressed
        Graph graph = graphSurface.getGraph();
        switch (action) {
            case BUNDLE:
                if (WebMain.injector.getMainPresenter().canUpdate()) {
                    new BundleDialog(graph).show();
                }
                break;
            case DESELECT:
                graphSurface.refresh(graph.getModel().deselect(item.getID(), item.getObjectType()));
                break;
            case DESELECT_ALL:
                graphSurface.refresh(graph.getModel().deselectAll());
                break;
            case HIDE_SELECTION:
                graphSurface.refresh(graph.getModel().hideSelection());
                break;
            case QUICK_REVEAL_NEIGHBORS:
                revealOneLevelOfNeighbors(graph);
                break;
            case QUICK_SELECT_NEIGHBORS:
                selectOneLevelOfNeighbors();
                break;
            case QUICK_UNBUNDLE:
                if (WebMain.injector.getMainPresenter().canUpdate()) {
                    unbundleNode(graph);
                }
                break;
            case SELECT: {
                VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();

                GraphRequest request = new GraphRequest();
                if (item.objectType.equals(ObjectAttributes.EDGES_OBJECT_TYPE)) {
                    request.links.add(item.getID());
                } else {
                    request.nodes.add(item.getID());
                }
                try {
                    future.execute(GraphActionServiceProtocol.class).select(graph.getUuid(), false, false, false, request);
                } catch (CentrifugeException e) {
                }
                graph.getGraphSurface().refresh(future);
                break;
            }
            case DETAILS:
                /*Viewer viewer = WebMain.injector.getMainPresenter().getDataViewPresenter(true).getViewer();

                GraphObjective.create(graph,surfaceX,surfaceY).addEventHandler(new AbstractVortexEventHandler<Objective>() {
                    @Override
                    public void onSuccess(Objective result) {
                        viewer.view(result);
                    }
                });
                viewer.loading();*/
                //Links are at 0,0 by default.
                if (item.getX() != 0 && item.getY() != 0) {
                    graphSurface.getToolTipManager().createTooltip(item);
                }else {
                    graphSurface.getToolTipManager().createTooltip(surfaceX, surfaceY);
                }
                break;
            case SELECT_ALL:
                graphSurface.refresh(graph.getModel().selectAll());
                break;
            case SHOW_ONLY:
                showOnlySelectedItems();
                break;
            case PLUNK_NODE:
                if (WebMain.injector.getMainPresenter().canUpdate()) {
                    new PlunkNodePresenter(graph, surfaceX, surfaceY).show();
                }
                break;
            case PLUNK_LINK:
                if (WebMain.injector.getMainPresenter().canUpdate()) {
                    drawLinkFrom();
                }
                break;
            case EDIT_PLUNKED_ITEM:
                if (WebMain.injector.getMainPresenter().canUpdate()) {
                    new EditPlunkedItemPresenter(graphSurface, item.getItemKey(), item.getObjectType()).show();
                }
                break;
            case DELETE_PLUNKED_ITEM:
                if (WebMain.injector.getMainPresenter().canUpdate()) {
                    PlunkNodeUtils.deleteItem(graphSurface, item.getItemKey(), item.getObjectType());
                }
                break;
            case ADD_ANNOTATION:
                if (WebMain.injector.getMainPresenter().canUpdate()) {
                    new AnnotationPresenter(graph, item).show();
                }
                break;
            case UNHIDE_ALL:
                if (WebMain.injector.getMainPresenter().canUpdate()) {
                    VortexFuture<Void> future = graphSurface.getGraph().getModel().unhideAll();
                    graphSurface.refresh(future);
                }
                break;
            default:
                GWT.log("Unhandled GraphContextAction");//NON-NLS log messages are not externalized
                break;
        }
        popupPanel.hide();
    }

    private void showContextMenuInPopup() {
        popupPanel.setWidget(contextMenu);
        popupPanel.show();
        popupPanel.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
    }

    private void setContextMenuForFoundItem(int surfaceX, int surfaceY, String vizUuid) {
        try {
            WebMain.injector.getVortex().execute(new Callback<FindItemDTO>() {
                @Override
                public void onSuccess(FindItemDTO result) {
                    item = result;
                    if ((result != null) && (result.objectType != null)) {
                        if (result.objectType.equals(ObjectAttributes.EDGES_OBJECT_TYPE)) {
                            contextMenu = createLinkContextMenu();
                        } else {
                            contextMenu = createNodeContextMenu();
                        }
                        showContextMenuInPopup();
                    }else{
                        setContextMenuForSloppy(surfaceX, surfaceY, vizUuid);
                    }
                }
            }, GraphActionServiceProtocol.class).gwtFindItem(null, "" + surfaceX, "" + surfaceY, vizUuid);
        } catch (CentrifugeException ignored) {
        }
    }
    private void setContextMenuForSloppy(int surfaceX, int surfaceY, String vizUuid) {
        WebMain.injector.getVortex().execute(new Callback<FindItemDTO>() {
            @Override
            public void onSuccess(FindItemDTO result) {
                item = result;
                if ((result != null) && (result.objectType != null)) {
                    if (result.objectType.equals(ObjectAttributes.EDGES_OBJECT_TYPE)) {
                        contextMenu = createLinkContextMenu();
                    } else {
                        contextMenu = createNodeContextMenu();
                    }
                    showContextMenuInPopup();
                }
                else{
                    contextMenu = createBackgroundContextMenu();
                    showContextMenuInPopup();
                }
            }
        }, GraphActionServiceProtocol.class).gwtFindItemNear(null, "" + surfaceX, "" + surfaceY, vizUuid);
    }

    private GraphContextMenu createDisabiguateContextMenu() {
        final GraphContextMenu graphContextMenu = new GraphContextMenu(graphSurface);
        graphContextMenu.show(DESELECT_ALL);
        graphContextMenu.show(SELECT_ALL);
        return graphContextMenu;
    }

    private GraphContextMenu createNodeContextMenu() {
        final GraphContextMenu graphContextMenu = new GraphContextMenu(graphSurface);
        if (item.isSelected()) {
            graphContextMenu.show(DESELECT);
        } else {
            graphContextMenu.show(SELECT);
        }

        graphContextMenu.show(GraphContextActionEnum.DETAILS);
        graphContextMenu.show(SHOW_ONLY);
        if (!item.neighbors.isEmpty()) {
            ArrayList<String> typesWithNodes = getTypesWithNodes();
            if (typesWithNodes.isEmpty()) {
                // do nothing
            } else {
                graphContextMenu.show(QUICK_REVEAL_NEIGHBORS);
            }
            graphContextMenu.show(QUICK_SELECT_NEIGHBORS);
        }
        if (!readOnly) {

            graphContextMenu.show(PLUNK_LINK);
            if (item.isBundle()) {
                graphContextMenu.show(QUICK_UNBUNDLE);
            }
            if (item.isPlunked()) {
                graphContextMenu.show(EDIT_PLUNKED_ITEM);
                graphContextMenu.show(DELETE_PLUNKED_ITEM);
            }

            graphContextMenu.show(ADD_ANNOTATION);
        }

        VortexFuture<SelectionModel> selectionModelVF = graphSurface.getGraph().getModel().getSelectionModel();
        selectionModelVF.addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {

            @Override
            public void onSuccess(SelectionModel result) {
                selectionModel = result;
                TreeSet<Integer> nodes = result.nodes;
                if (nodes.contains(item.getID())) {
                    if (!nodes.isEmpty()) {
                        graphContextMenu.show(HIDE_SELECTION);
                    }
                    if (nodes.size() > 1) {
                        if (!readOnly) {

                            graphContextMenu.show(BUNDLE);
                        }
                        graphContextMenu.show(HIDE_SELECTION);
                    }
                }
            }
        });

        return graphContextMenu;
    }

    private GraphContextMenu createLinkContextMenu() {
        final GraphContextMenu graphContextMenu = new GraphContextMenu(graphSurface);
        VortexFuture<SelectionModel> selectionModelVF = graphSurface.getGraph().getModel().getSelectionModel();
        selectionModelVF.addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {

            @Override
            public void onSuccess(SelectionModel result) {
                selectionModel = result;
                if (selectionModel.links.contains(item.getID())) {
                    graphContextMenu.show(DESELECT);
                } else {
                    graphContextMenu.show(SELECT);
                }
            }

        });

        if ((!readOnly) && (item.isPlunked())) {
            graphContextMenu.show(EDIT_PLUNKED_ITEM);
            graphContextMenu.show(DELETE_PLUNKED_ITEM);
        }

        graphContextMenu.show(GraphContextActionEnum.DETAILS);
        if (!readOnly) {
            graphContextMenu.show(ADD_ANNOTATION);
        }
        return graphContextMenu;
    }

    private GraphContextMenu createBackgroundContextMenu() {
        final GraphContextMenu graphContextMenu = new GraphContextMenu(graphSurface);
        graphContextMenu.show(DESELECT_ALL);
        graphContextMenu.show(SELECT_ALL);
        if (!readOnly) {
            graphContextMenu.show(PLUNK_NODE);
        }

        VortexFuture<SelectionModel> selectionModelVF = graphSurface.getGraph().getModel().getSelectionModel();
        selectionModelVF.addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {

            @Override
            public void onSuccess(SelectionModel result) {
                selectionModel = result;
                TreeSet<Integer> nodes = result.nodes;
                if (!nodes.isEmpty()) {
                    graphContextMenu.show(HIDE_SELECTION);
                }
                if ((!readOnly) && (nodes.size() > 1)) {
                    graphContextMenu.show(BUNDLE);
                }
            }
        });

        graphContextMenu.show(UNHIDE_ALL);
        return graphContextMenu;
    }

    private void drawLinkFrom() {
        if (WebMain.injector.getMainPresenter().canUpdate()) {
            CSIActivityManager manager = graphSurface.getActivityManager();
            manager.setActivity(new PlunkLinkActivity(graphSurface, item.getDisplayX(), item.getDisplayY()));
        }
    }

    private void showOnlySelectedItems() {
        final GraphRequest graphRequest = new GraphRequest();
        VortexFuture<SelectionModel> selectVF3 = WebMain.injector.getVortex().createFuture();
        selectVF3.addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {

            @Override
            public void onSuccess(SelectionModel result) {
                graphSurface.refresh(graphSurface.getGraph().getModel().showOnlySelected());
            }
        });

        if (!item.isSelected()) {
            graphRequest.nodes.add(item.getID());
            try {
                selectVF3.execute(GraphActionServiceProtocol.class).select(graphSurface.getGraph().getUuid(), true, true, true, graphRequest);
            } catch (CentrifugeException e) {
                e.printStackTrace();
            }
        } else {
            selectVF3.fireSuccess(selectionModel);
        }
    }

    private void unbundleNode(Graph graph) {
        if (WebMain.injector.getMainPresenter().canUpdate()) {
            VortexFuture<Void> unbundleVF = WebMain.injector.getVortex().createFuture();
            ArrayList<Integer> nodeIds = Lists.newArrayList();
            nodeIds.add(item.getID());
            try {
                unbundleVF.execute(GraphActionServiceProtocol.class).unbundleNodesById(graph.getUuid(), nodeIds);
                unbundleVF.addEventHandler(new AbstractVortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        graphSurface.getToolTipManager().removeAllToolTips();
                        graphSurface.getGraph().getLegend().load();
                    }
                });
            } catch (CentrifugeException ignored) {
            }
            graph.getGraphSurface().refresh(unbundleVF);
            graph.refreshTabs(unbundleVF);
        }
    }

    private void selectOneLevelOfNeighbors() {
        VortexFuture<SelectionModel> selectVF = WebMain.injector.getVortex().createFuture();
                    selectVF.execute(GraphActionServiceProtocol.class).selectVisibleNeighbors(graphSurface.getGraph().getUuid(), 1,item.getID());
                graphSurface.refresh(selectVF);


    }

    private void revealOneLevelOfNeighbors(Graph graph) {
        ArrayList<Integer> nodeNeighborIds = Lists.newArrayList();
        VortexFuture<Void> neighborFutureA = getNeighborFutureA();
//        if (item.isSelected()) {
//            neighborFutureA.execute(GraphActionServiceProtocol.class).revealNeighborsOfSelectedNodes(graph.getUuid());
//        } else {
//            for (NeighborsDTO neighbor : item.getNeighbors()) {
//                nodeNeighborIds.add(neighbor.getID());
//            }
//            neighborFutureA.execute(GraphActionServiceProtocol.class).unhideNodeById(graph.getUuid(), nodeNeighborIds);
//        }

        try {
            if (item.isSelected()) {
                    neighborFutureA.execute(GraphActionServiceProtocol.class).showAdjacentFor(graph.getUuid(), null);
                
            } else {
                neighborFutureA.execute(GraphActionServiceProtocol.class).showAdjacentFor(graph.getUuid(),
                        item.itemId, null);
            }
        
        } catch (CentrifugeException e) {
        }
    }
}
