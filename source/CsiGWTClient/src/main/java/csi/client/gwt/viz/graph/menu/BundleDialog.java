package csi.client.gwt.viz.graph.menu;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class BundleDialog {

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private Dialog dialog;
    private final Graph graph;

    @UiField
    ControlGroup nameControlGroup;
    @UiField
    TextBox nameTextBox;
    @UiField
    HelpBlock nameHelpBlock;

    public BundleDialog(Graph graph) {
        this.graph = checkNotNull(graph);
        dialog = uiBinder.createAndBindUi(this);
        setupButtons();
    }

    public void show() {
        dialog.show();
    }

    private void setupButtons() {
        dialog.hideOnCancel();
        Button actionButton = dialog.getActionButton();
        actionButton.setText(CentrifugeConstantsLocator.get().bundleDialog_create());
        actionButton.addClickHandler(new ActionClickHandler());
    }

    interface SpecificUiBinder extends UiBinder<Dialog, BundleDialog> {
    }

    private final class ActionClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
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
                            nameHelpBlock.setText(CentrifugeConstantsLocator.get().bundleDialog_bundleNameMustBeUnique());
                        }
                    }
                });
                future.execute(GraphActionServiceProtocol.class).validateBundleName(graph.getUuid(), suggestedName);
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

            } catch (CentrifugeException e) {
            }
        }

        private void displayErrorMessage() {
            nameControlGroup.setType(ControlGroupType.ERROR);
            nameHelpBlock.setText(CentrifugeConstantsLocator.get().bundleDialog_nameCannotBeBlank());
        }

    }

}
