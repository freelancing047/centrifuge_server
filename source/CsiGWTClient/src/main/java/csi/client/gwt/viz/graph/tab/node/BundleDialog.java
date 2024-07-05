package csi.client.gwt.viz.graph.tab.node;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.graph.gwt.NodeListDTO;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class BundleDialog {

    private final class ActionClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            String suggestedName = nameTextBox.getText();
            if (suggestedName.isEmpty()) {
                displayErrorMessage();
            } else {
                VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
                ArrayList<Integer> nodeIds = Lists.newArrayList();
                Grid<NodeListDTO> resultsGrid = nodesTabImpl.getResultsGrid();
                List<NodeListDTO> selectedItems = resultsGrid.getSelectionModel().getSelectedItems();
                for (NodeListDTO nodeListDTO : selectedItems) {
                    nodeIds.add(nodeListDTO.getID());
                }
                future.execute(GraphActionServiceProtocol.class).manuallyBundleNodesById(graph.getUuid(), nodeIds,
                        suggestedName);
                graph.getGraphSurface().refresh(future);
                graph.refreshTabs(future);
                dialog.hide();
                graph.getGraphSurface().getToolTipManager().removeAllToolTips();
                future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        graph.getLegend().load();
                    }
                });
            }
        }
        private void displayErrorMessage() {
            nameControlGroup.setType(ControlGroupType.ERROR);
            nameHelpBlock.setText(CentrifugeConstantsLocator.get().bundleDialog_nameCannotBeBlank());
        }
    }

    private final class CancelClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            dialog.hide();
        }
    }

    interface SpecificUiBinder extends UiBinder<Dialog, BundleDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    protected Button actionButton;
    protected Button cancelButton;
    protected Dialog dialog;
    final Graph graph;
    @UiField
    protected ControlGroup nameControlGroup;
    @UiField
    protected TextBox nameTextBox;
    @UiField
    protected HelpBlock nameHelpBlock;

    private NodesTabImpl nodesTabImpl;

    public BundleDialog(Graph graph, NodesTabImpl nodesTabImpl) {
        this.nodesTabImpl = checkNotNull(nodesTabImpl);
        this.graph = checkNotNull(graph);
        dialog = uiBinder.createAndBindUi(this);
        dialog.hideOnCancel();
        actionButton = dialog.getActionButton();
        actionButton.setText(CentrifugeConstantsLocator.get().bundleDialog_create());
        actionButton.addClickHandler(new ActionClickHandler());
        cancelButton = dialog.getCancelButton();
        cancelButton.addClickHandler(new CancelClickHandler());
    }

    public Dialog asDialog() {
        return dialog;
    }

    private Graph getGraph() {
        return graph;
    }
}
