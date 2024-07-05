package csi.client.gwt.viz.graph.plunk;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.plunk.util.PlunkNodeUtils;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.server.business.visualization.graph.base.ObjectAttributes;

/**
 * @author Centrifuge Systems, Inc.
 */
public class DuplicateNodeDialog {

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private final String name;
    private final String type;
    private final String nodeKey;
    private final GraphSurface graphSurface;

    private HTML titleHtml = new HTML("<h4>"+ CentrifugeConstantsLocator.get().userAddNode_duplicateNodeFound() +"</h4>");
    private Label label = new Label();

    @UiField
    Dialog dialog;

    public DuplicateNodeDialog(GraphSurface graphSurface, String name, String type, String nodeKey) {

        this.graphSurface = graphSurface;
        this.name = name;
        this.type = type;
        this.nodeKey = nodeKey;
        uiBinder.createAndBindUi(this);
        buildUI();
        setupButtons();
        initializeHandlers();
    }

    private void buildUI() {
        VerticalPanel vp = new VerticalPanel();
        vp.add(titleHtml);
        vp.add(label);

        label.setText(CentrifugeConstantsLocator.get().userAddNode_duplicateNodeMessage(name, type));
        dialog.add(vp);
    }

    private void setupButtons() {
        dialog.hideOnCancel();
        dialog.hideOnAction();
        dialog.getCancelButton().setText(Dialog.txtCloseButton);
        Button actionButton = dialog.getActionButton();
        actionButton.setText(CentrifugeConstantsLocator.get().delete());
        actionButton.setType(ButtonType.DANGER);
    }

    private void initializeHandlers() {
        dialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialog.hide();
            }
        });

        dialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                deleteNode();
            }
        });
    }

    private void deleteNode() {
        PlunkNodeUtils.deleteItem(graphSurface, nodeKey, ObjectAttributes.NODES_OBJECT_TYPE);
    }

    public void show() {
        dialog.show();
    }

    interface SpecificUiBinder extends UiBinder<Dialog, DuplicateNodeDialog> {
    }
}
