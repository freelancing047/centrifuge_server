package csi.client.gwt.viz.graph.tab.path;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Slider;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.server.common.dto.graph.path.FindAllNodesMetaRequest;
import csi.server.common.dto.graph.path.FindPathRequest;
import csi.server.common.dto.graph.path.FindPathResponse;
import csi.server.common.dto.graph.search.NodeInfo;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class PathTabSearchPanel {

    @UiField
    Dialog searchDialog;
    @UiField
    Slider maximumPaths;
    @UiField
    SliderTextField maximumPathsText;
    @UiField
    Slider minimumLength;
    @UiField
    SliderTextField minimumLengthText;
    @UiField
    Slider maximumLength;
    @UiField
    SliderTextField maximumLengthText;
    @UiField
    Slider pathsMatchNodes;
    @UiField
    SliderTextField pathsMatchingText;
    @UiField
    CheckBox includeDirection;

    // @UiField
    // Label errorLabel;

    @UiField(provided = true)
    Grid<NodeMeta> nodesGrid;

    //    private GraphEventHandler selectionChangedHandler;
    private ListStore<NodeMeta> nodesStore;
    private static final NodeMetaProperties nodeProps = GWT.create(NodeMetaProperties.class);
    private Graph graph;

    private PathResultHandler handler;

    interface MyUiBinder extends UiBinder<Widget, PathTabSearchPanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    public PathTabSearchPanel(Graph graph, PathResultHandler handler) {
        this.graph = graph;
        this.handler = handler;

        //        selectionChangedHandler = new SelectionChangedHandler();
        //        graph.addGraphEventHandler(GraphEvents.SELECTION_CHANGED, selectionChangedHandler);

        ColumnConfig<NodeMeta, String> nodeNameCol = new ColumnConfig<>(nodeProps.name(), 50, CentrifugeConstantsLocator.get().name());

        List<ColumnConfig<NodeMeta, ?>> nodeListConfig = new ArrayList<>();
        nodeListConfig.add(nodeNameCol);
        ColumnModel<NodeMeta> nodesCm = new ColumnModel<>(nodeListConfig);
        nodesStore = new ListStore<>(nodeProps.key());

        nodesGrid = new Grid<>(nodesStore, nodesCm);
        nodesGrid.setId("nodes-grid");//NON-NLS
        nodesGrid.setHeight(150);
        nodesGrid.getView().setAutoExpandColumn(nodeNameCol);
        nodesGrid.getView().setStripeRows(true);
        nodesGrid.getView().setColumnLines(true);
        nodesGrid.setBorders(true);
        nodesGrid.setSelectionModel(null);
        nodesGrid.setColumnReordering(false);

        uiBinder.createAndBindUi(this);

        searchDialog.hideOnCancel();
        searchDialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // TODO: validate input
                int minimumLengthInt = minimumLength.getValue();
                int maximumLengthInt = maximumLength.getValue();
                int pathsMatchNodesInt = pathsMatchNodes.getValue();
                int maximumPathsInt = maximumPaths.getValue();

                findPaths(minimumLengthInt, maximumLengthInt, pathsMatchNodesInt, maximumPathsInt,
                        includeDirection.getValue(), nodesGrid.getStore().getAll());
                searchDialog.hide();
            }
        });
        
        minimumLengthText.addSlider(minimumLength);
        maximumLengthText.addSlider(maximumLength);
        maximumPathsText.addSlider(maximumPaths);
        pathsMatchingText.addSlider(pathsMatchNodes);
    }

    public void show() {
        reset();
        searchDialog.show();
    }

    private void findPaths(int minLength, int maxLength, int pathsMatchNodes, int maxPaths, boolean includeDirection,
            List<NodeMeta> selectedNodes) {
        String vizId = graph.getGraphSurface().getVizUuid();

        FindPathRequest request = new FindPathRequest();
        request.setNumPaths(maxPaths);
        request.setMatchNodes(pathsMatchNodes);
        request.setMaxLength(maxLength);
        request.setMinLength(minLength);

        if (selectedNodes.isEmpty()) {
            // errorLabel.setText("At least 2 nodes must be selected to search.");
            // errorLabel.setVisible(true);
            return;
        }

        List<Integer> nodeIds = new ArrayList<>();
        for (NodeMeta nodeMeta : selectedNodes) {
            nodeIds.add(Integer.parseInt(nodeMeta.getId()));
        }

        request.setSelectedNodes(nodeIds);
        request.setIncludeDirection(includeDirection);

        try {
            VortexFuture<FindPathResponse> future = WebMain.injector.getVortex().createFuture();

            graph.showProgressIndicator(future, null);
            future.execute(GraphActionServiceProtocol.class).findPaths(vizId, request);
            future.addEventHandler(new AbstractVortexEventHandler<FindPathResponse>() {
                
                @Override
                public void onSuccess(FindPathResponse result) {
                    graph.hideProgressIndicator();
                    if (result.getFoundPaths().isEmpty()) {
                        new InfoDialog(CentrifugeConstantsLocator.get().pathTab_noneFound_Header(), CentrifugeConstantsLocator.get().pathTab_noneFound_Body()).show();
                    } else {
                        handler.onPathsLoaded(result);
                    }
                }

                @Override
                public boolean onError(Throwable t) {
                    graph.hideProgressIndicator();
                    return false;
                }
            });
            
        } catch (CentrifugeException ignored) {
            graph.hideProgressIndicator();
        }
    }

    void reset() {
        // errorLabel.setVisible(false);
        minimumLength.setValue(1);
        minimumLengthText.setValue("1");
        maximumLength.setValue(9);
        maximumLengthText.setValue("9");
        maximumPaths.setValue(20);
        maximumPathsText.setValue("20");
        pathsMatchNodes.setValue(0);
        pathsMatchingText.setValue("0");
        includeDirection.setValue(false);
        setNodesStore();
    }

    private void setNodesStore() {
        final FindAllNodesMetaRequest request = new FindAllNodesMetaRequest();
        graph.getModel().getSelectionModel().addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {
            @Override
            public void onSuccess(SelectionModel result) {
                request.findPathNodes = new ArrayList<>(graph.getModel().getSelectNodes());
                request.vizUUID = graph.getUuid();

                try {
                    WebMain.injector.getVortex().execute(new Callback<List<NodeInfo>>() {

                        @Override
                        public void onSuccess(List<NodeInfo> nodeList) {
                            nodesStore.clear();
                            for (NodeInfo result : nodeList) {
                                nodesStore.add(new NodeMeta("" + result.id, result.label));
                            }
                        }

                    }, GraphActionServiceProtocol.class).findAllNodesMeta(request);
                } catch (CentrifugeException ignored) {

                }
            }
        });
    }
    
    

    //    private class SelectionChangedHandler extends GraphEventHandler {
    //
    //        @Override
    //        public void onGraphEvent(GraphEvent event) {
    //            setNodesStore();
    //        }
    //    }

    public void hide() {
        searchDialog.hide();
    }
}
