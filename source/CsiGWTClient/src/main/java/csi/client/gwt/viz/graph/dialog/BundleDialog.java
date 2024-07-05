package csi.client.gwt.viz.graph.dialog;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class BundleDialog {

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    final Graph graph;
    protected Button actionButton;
    protected Button cancelButton;
    protected Dialog dialog;
    @UiField
    protected ControlGroup nameControlGroup;
    @UiField
    protected ControlGroup bundleWhatControlGroup;
    @UiField
    protected TextBox nameTextBox;
    @UiField
    protected RadioButton bundleEntireGraphRadioButton;
    @UiField
    protected RadioButton bundleSelectionRadioButton;
    @UiField
    protected RadioButton bundleSpecificationRadioButton;
    @UiField
    protected RadioButton bundleTogetherRadioButton;
    @UiField
    protected HelpBlock nameHelpBlock;
    @UiField
    protected HelpBlock bundleWhatHelpBlock;
    CentrifugeConstants constants = CentrifugeConstantsLocator.get();

    public BundleDialog(Graph graph) {
        checkNotNull(graph);
        this.graph = graph;
        dialog = uiBinder.createAndBindUi(this);
        dialog.hideOnCancel();
        actionButton = dialog.getActionButton();
        actionButton.setText(constants.bundleDialog_create());
        actionButton.addClickHandler(new ActionClickHandler());
        cancelButton = dialog.getCancelButton();
        cancelButton.addClickHandler(new CancelClickHandler());
        if (graph.hasBundleSpecification()) {
            bundleEntireGraphRadioButton.setValue(true, true);
            bundleSpecificationRadioButton.setValue(true, true);

        } else {
            bundleSpecificationRadioButton.setEnabled(false);
            bundleEntireGraphRadioButton.setEnabled(false);
        }
        graph.getModel().getSelectionModel().addEventHandler(new AbstractVortexEventHandler<SelectionModel>() {

            @Override
            public void onSuccess(SelectionModel result) {
                if (result.nodes.isEmpty()) {
                    bundleSelectionRadioButton.setEnabled(false);
                    bundleSelectionRadioButton.setValue(false);
                    if (bundleEntireGraphRadioButton.isEnabled() == false) {
                        actionButton.setEnabled(false);
                        bundleSpecificationRadioButton.setEnabled(false);
                        bundleTogetherRadioButton.setEnabled(false);
                        bundleWhatControlGroup.setType(ControlGroupType.ERROR);
                        bundleWhatHelpBlock.setText(constants.bundleDialog_requireSelection());
                    }
                } else {
                    bundleSelectionRadioButton.setValue(true, true);
                    bundleTogetherRadioButton.setValue(true, true);
                }
            }
        });
    }

    @UiHandler("bundleSelectionRadioButton")
    protected void onBundleSelection(ValueChangeEvent<Boolean> event) {
        checkNotNull(event);
        if (event.getValue()) {
            bundleTogetherRadioButton.setEnabled(true);
        }
    }

    @UiHandler("bundleEntireGraphRadioButton")
    protected void onBundleGraph(ValueChangeEvent<Boolean> event) {
        checkNotNull(event);
        if (event.getValue()) {
            bundleTogetherRadioButton.setEnabled(false);
        }
    }

    @UiHandler("bundleSpecificationRadioButton")
    protected void onSpecification(ValueChangeEvent<Boolean> event) {
        checkNotNull(event);
        if (event.getValue()) {
            nameTextBox.setEnabled(false);
        }
    }

    @UiHandler("bundleTogetherRadioButton")
    protected void onTogether(ValueChangeEvent<Boolean> event) {
        checkNotNull(event);
        if (event.getValue()) {
            nameTextBox.setEnabled(true);
        }
    }

    public void show() {
        dialog.show();
    }

    interface SpecificUiBinder extends UiBinder<Dialog, BundleDialog> {
    }

    private final class ActionClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            if (bundleEntireGraphRadioButton.getValue()) {
                // always by specification
                VortexFuture<Void> bundleEntireGraph = graph.getModel().bundleEntireGraph();
                graph.getGraphSurface().refresh(bundleEntireGraph);
                graph.refreshTabs(bundleEntireGraph);
                dialog.hide();
                graph.getGraphSurface().getToolTipManager().removeAllToolTips();
                graph.getLegend().load();
            }
            // if not entire graph, must be bundling selection
            else if (bundleSpecificationRadioButton.getValue()) {
                // selection by spec
                VortexFuture<Void> bundleSpec = graph.getModel().bundleSelectionBySpec();
                graph.getGraphSurface().refresh(bundleSpec);
                graph.refreshTabs(bundleSpec);
                dialog.hide();
                graph.getGraphSurface().getToolTipManager().removeAllToolTips();
                graph.getLegend().load();
            } else {
                // if not by spec, must be manual bundling nodes together

                final String suggestedName = nameTextBox.getText();
                if (suggestedName.isEmpty()) {
                    displayErrorMessage();
                } else {

                    VortexFuture<Boolean> future = WebMain.injector.getVortex().createFuture();
                    future.addEventHandler(new AbstractVortexEventHandler<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            if (result) {
                                dialog.hide();
                                refreshAfterBundling(suggestedName);
                            } else {
                                nameControlGroup.setType(ControlGroupType.ERROR);
                                nameHelpBlock.setText(constants.bundleDialog_bundleNameMustBeUnique());
                            }
                        }
                    });
                    future.execute(GraphActionServiceProtocol.class).validateBundleName(graph.getUuid(), suggestedName);
                }
            }
        }

        private void refreshAfterBundling(String suggestedName) {
            VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
            try {
                future.execute(GraphActionServiceProtocol.class).manuallyBundleSelection(graph.getUuid(), graph.getDataviewUuid(), suggestedName);
                future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        graph.getLegend().load();
                        graph.getGraphSurface().getToolTipManager().removeAllToolTips();
                    }
                });
                graph.getGraphSurface().refresh(future);
                graph.refreshTabs(future);
            } catch (CentrifugeException e) {
            }
        }

        private void displayErrorMessage() {
            nameControlGroup.setType(ControlGroupType.ERROR);
            nameHelpBlock.setText(constants.bundleDialog_nameCannotBeBlank());
        }
    }

    private final class CancelClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            dialog.hide();
        }
    }

}
