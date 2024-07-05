package csi.client.gwt.viz.graph.tab.path;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.Tab;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.events.GraphEvent;
import csi.client.gwt.viz.graph.events.GraphEventHandler;
import csi.client.gwt.viz.graph.events.GraphEvents;
import csi.client.gwt.viz.graph.tab.GraphTab;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.MultiPageCheckboxSelectionModel;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentFactory;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.dto.graph.path.FindPathResponse;
import csi.server.common.dto.graph.path.PathMeta;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class PathTabView {

    private static final PathMetaProperties pathProps = GWT.create(PathMetaProperties.class);
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    @UiField
    NavLink addSelectNavLink;
    @UiField
    CheckBox autoHighlightCheckBox;
    @UiField
    FluidRow controlLayer;
    @UiField(provided = true)
    GridContainer gridContainer;
    @UiField
    NavLink highlightNavLink;
    @UiField
    CheckBox linkCheckBox;
    @UiField
    CheckBox nodeCheckBox;
    @UiField
    GraphTab pathsTab;
    @UiField
    Button searchButton;
    @UiField
    NavLink selectNavLink;
    @UiField
    NavLink showNavLink;
    @UiField(provided = true)
    LayoutPanel layoutPanel;
    @UiField
    Button clearHighlightsButton;
    private Graph graph;
    private PathTabSearchPanel pathTabSearchPanel;
    private ResizeableGrid<PathMeta> resultsGrid;
    private ListStore<PathMeta> resultsStore;

    public PathTabView(final Graph graph) {
        checkNotNull(graph);
        this.graph = graph;
        gridContainer = new GridContainer();
        gridContainer.setPager(null);
        layoutPanel = new LayoutPanel(){
            @Override
            public void onResize() {
                if(layoutPanel.getOffsetWidth()<500){
                    nodeCheckBox.setVisible(false);
                    linkCheckBox.setVisible(false);
                }
                else{
                    nodeCheckBox.setVisible(true);
                    linkCheckBox.setVisible(true);
                }
                super.onResize();
            }
        };
        layoutPanel.setWidth("100%");
        layoutPanel.setHeight("100%");
        pathsTab = uiBinder.createAndBindUi(this);
        graph.addGraphEventHandler(GraphEvents.GRAPH_LOAD_COMPLETE, new GraphEventHandler() {

            @Override
            public void onGraphEvent(GraphEvent event) {
                onGraphLoad();
            }
        });

        pathTabSearchPanel = new PathTabSearchPanel(graph, new PathResultHandler() {

            @Override
            public void onPathsLoaded(FindPathResponse response) {
                if (response.getFoundPaths().size() > 0) {
                    pathTabSearchPanel.hide();
                    resultsGrid.setVisible(true);
                    resultsStore.clear();
                    resultsStore.addAll(response.getFoundPaths());
                }
            }
        });
        controlLayer.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
        pathsTab.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                graph.showControlBar(false);
            }
        });

    }

    public Tab getTab() {
        return pathsTab;
    }

    @UiHandler("addSelectNavLink")
    void onAddSelect(ClickEvent event) {
        VortexFuture<SelectionModel> selectVF = WebMain.injector.getVortex().createFuture();
        List<String> pathIds = Lists.newArrayList();

        List<PathMeta> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (PathMeta pathMeta : selectedItems) {
            pathIds.add(pathMeta.id);
        }

        boolean selectNodes = nodeCheckBox.getValue();
        boolean selectLinks = linkCheckBox.getValue();
        try {
            selectVF.execute(GraphActionServiceProtocol.class).selectPaths(graph.getUuid(), pathIds, "true",
                    "" + selectNodes, "" + selectLinks);
        } catch (CentrifugeException ignored) {
        }
        graph.getGraphSurface().refresh(selectVF);
    }

    @UiHandler("searchButton")
    public void onClick(ClickEvent event) {
        final Graph.Model model = graph.getModel();
        Collection<? extends Integer> selectNodes = model.getSelectNodes();
        if (selectNodes.size() > 1) {
            pathTabSearchPanel.show();
            return;
        }
        model.getSelectionModel().addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {
            @Override
            public void onSuccess(SelectionModel result) {
                Collection<? extends Integer> selectNodes = model.getSelectNodes();
                if (selectNodes.size() > 1) {
                    pathTabSearchPanel.show();
                    return;
                }
                CentrifugeConstants centrifugeConstants = CentrifugeConstantsLocator.get();
                String dialogTitle = centrifugeConstants.findingPathsRequiresASelection_errorHeader();
                String dialogMessage = centrifugeConstants.findingPathsRequiresASelection_errorBody();
                InfoDialog id = new InfoDialog(dialogTitle, dialogMessage);
                id.show();
            }
        });
    }

    @UiHandler("clearHighlightsButton")
    void onClearHighlights(ClickEvent event) {
        List<String> pathIds = Lists.newArrayList();
        List<PathMeta> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        VortexFuture<Boolean> highlightVF = WebMain.injector.getVortex().createFuture();
        try {
            highlightVF.execute(GraphActionServiceProtocol.class).highlightPaths(graph.getUuid(), pathIds);
        } catch (CentrifugeException ignored) {
        }
        graph.getGraphSurface().refresh(highlightVF);
    }

    private void onGraphLoad() {

        IdentityValueProvider<PathMeta> identity = new IdentityValueProvider<PathMeta>();
        final MultiPageCheckboxSelectionModel<PathMeta> sm = new MultiPageCheckboxSelectionModel<PathMeta>(identity, pathProps.key());
        // create ColumnConfigs
        ColumnConfig<PathMeta, String> nameCol = new ColumnConfig<PathMeta, String>(pathProps.name(), 100, CentrifugeConstantsLocator.get().pathResults_pathName());
        nameCol.setComparator(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                int retVal = 0;
                try {
                    String is1 = CharMatcher.DIGIT.retainFrom(o1);
                    String is2 = CharMatcher.DIGIT.retainFrom(o2);
                    int i1 = Integer.parseInt(is1);
                    int i2 = Integer.parseInt(is2);
                    retVal = i1 - i2;
                } catch (Exception e) {
                    // Exception ignored
                }
                return retVal;
            }

        });
        ColumnConfig<PathMeta, String> lengthCol = new ColumnConfig<PathMeta, String>(pathProps.lengthString(), 100,
                CentrifugeConstantsLocator.get().pathResults_length());
        ColumnConfig<PathMeta, String> sourceCol = new ColumnConfig<PathMeta, String>(pathProps.source(), 100, CentrifugeConstantsLocator.get().pathResults_source());
        ColumnConfig<PathMeta, String> targetsCol = new ColumnConfig<PathMeta, String>(pathProps.target(), 100,
                CentrifugeConstantsLocator.get().pathResults_targets());
        ColumnConfig<PathMeta, String> waypointsCol = new ColumnConfig<PathMeta, String>(pathProps.waypoints(), 100,
                CentrifugeConstantsLocator.get().pathResults_waypoints());
        // Add Column to List in order
        List<ColumnConfig<PathMeta, ?>> columnList = new ArrayList<ColumnConfig<PathMeta, ?>>();
        columnList.add(sm.getColumn());
        columnList.add(nameCol);
        columnList.add(lengthCol);
        columnList.add(sourceCol);
        columnList.add(targetsCol);
        columnList.add(waypointsCol);

        sm.getColumn().setGroupable(false);
        sm.getColumn().setHideable(false);

        GridComponentFactory componentFactory = WebMain.injector.getGridFactory();
        GridComponentManager<PathMeta> gridComponentManager = componentFactory.create(pathProps.key());

        // Set Column Model
        ColumnModel<PathMeta> cm = new ColumnModel<PathMeta>(columnList);
        resultsStore = gridComponentManager.getStore();

        resultsGrid = new ResizeableGrid<PathMeta>(resultsStore, cm);
        resultsGrid.getView().setStripeRows(true);
        resultsGrid.getView().setColumnLines(true);
        resultsGrid.getView().setAutoExpandColumn(nameCol);
        resultsGrid.setBorders(false);
        resultsGrid.setColumnReordering(true);
        resultsGrid.setLoadMask(true);
        resultsGrid.setSelectionModel(sm);

        // selectionAlert = new Alert();
        // selectionAlert.setHeading("Warning: ");
        // selectionAlert.setText("Nodes must be selected to search paths.");
        // selectionAlert.setType(AlertType.WARNING);
        // selectionAlert.setClose(false);
        // selectionAlert.setHeight("30px");

        gridContainer.setGrid(resultsGrid);

        // graph.addGraphEventHandler(GraphEvents.SELECTION_CHANGED, new GraphEventHandler() {
        //
        // @Override
        // public void onGraphEvent(GraphEvent event) {
        // if (event.getGraph().getModel().getSelectNodes().size() == 0) {
        // selectionAlert.setHeading("Warning: ");
        // selectionAlert.setText("Nodes must be selected to search paths.");
        // selectionAlert.setType(AlertType.WARNING);
        // selectionAlert.removeStyleName("clickable");
        // } else {
        // selectionAlert.setHeading("");
        // selectionAlert.setText("Click here to perform a search now.");
        // selectionAlert.setType(AlertType.INFO);
        // selectionAlert.addStyleName("clickable");
        // }
        // }
        // });

        resultsGrid.getSelectionModel().addSelectionChangedHandler(
                new SelectionChangedEvent.SelectionChangedHandler<PathMeta>() {

                    private ArrayList<PathMeta> lastSelection = Lists.newArrayList();

                    @Override
                    public void onSelectionChanged(SelectionChangedEvent<PathMeta> event) {
                        if (lastSelection.containsAll(event.getSelection())) {
                            if (event.getSelection().containsAll(lastSelection)) {
                                return;
                            }
                        }
                        lastSelection = Lists.newArrayList(event.getSelection());
                        if (autoHighlightCheckBox.getValue()) {
                            onHighlight(null);
                        }
                    }
                });
    }

    @UiHandler("highlightNavLink")
    void onHighlight(ClickEvent event) {
        List<String> pathIds = Lists.newArrayList();
        List<PathMeta> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        VortexFuture<Boolean> highlightVF = WebMain.injector.getVortex().createFuture();
        for (PathMeta pathMeta : selectedItems) {
            pathIds.add(pathMeta.id);
        }
        try {
            highlightVF.execute(GraphActionServiceProtocol.class).highlightPaths(graph.getUuid(), pathIds);
        } catch (CentrifugeException ignored) {
        }
        graph.getGraphSurface().refresh(highlightVF);
    }

    @UiHandler("selectNavLink")
    void onSelect(ClickEvent event) {
        VortexFuture<SelectionModel> selectVF = WebMain.injector.getVortex().createFuture();
        List<String> pathIds = Lists.newArrayList();

        List<PathMeta> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (PathMeta pathMeta : selectedItems) {
            pathIds.add(pathMeta.id);
        }
        boolean selectNodes = nodeCheckBox.getValue();
        boolean selectLinks = linkCheckBox.getValue();
        try {
            selectVF.execute(GraphActionServiceProtocol.class).selectPaths(graph.getUuid(), pathIds, "false",
                    "" + selectNodes, "" + selectLinks);
        } catch (CentrifugeException ignored) {
        }
        graph.getGraphSurface().refresh(selectVF);
    }

    @UiHandler("showNavLink")
    void onShowOnly(ClickEvent event) {
        VortexFuture<SelectionModel> selectVF = WebMain.injector.getVortex().createFuture();
        List<String> pathIds = Lists.newArrayList();

        List<PathMeta> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
        for (PathMeta pathMeta : selectedItems) {
            pathIds.add(pathMeta.id);
        }
        graph.getGraphSurface().refresh(selectVF);
        selectVF.execute(GraphActionServiceProtocol.class).showOnlyPaths(graph.getUuid(), pathIds);
    }

    public void setPathResults(List<PathMeta> paths) {
        resultsStore.clear();
        resultsStore.addAll(paths);
    }

    interface MyUiBinder extends UiBinder<GraphTab, PathTabView> {
    }
}
