package csi.client.gwt.viz.graph.plunk.edit;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.plunk.util.PlunkNodeUtils;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.PlunkedLink;
import csi.server.common.model.visualization.graph.PlunkedNode;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.service.api.GraphActionServiceProtocol;

/**
 * Sets up the editing of either a plunked node or plunked link.
 *
 * @author Centrifuge Systems, Inc.
 */
public class EditPlunkedItemPresenter {

    private final GraphSurface graphSurface;
    private final EditPlunkedItemDialog view;
    private final boolean editingLink;
    private EditPlunkedNodePresenter nodeEditor;
    private EditPlunkedLinkPresenter linkEditor;

    public EditPlunkedItemPresenter(GraphSurface graphSurface, String itemKey, String objectType) {
        this.graphSurface = graphSurface;
        this.editingLink = ObjectAttributes.EDGES_OBJECT_TYPE.equals(objectType);
        setupEditor(graphSurface.getGraph().getModel().getRelGraphViewDef(), itemKey);
        this.view = new EditPlunkedItemDialog(getActiveEditorView());

        addHandlers();
    }

    private IsWidget getActiveEditorView() {
        if (editingLink) {
            return linkEditor.getView();
        } else {
            return nodeEditor.getView();
        }
    }

    private void addHandlers() {
        view.addSaveClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (editingLink) {
                    if (linkEditor.validate()) {
                        saveLink();
                    }
                } else {
                    if (nodeEditor.validate()) {
                        saveNode();
                    }
                }
            }
        });
    }

    private void saveNode() {
        PlunkedNode plunkedNode = nodeEditor.save();
        NodeDef nodeDef = findNodeDef(plunkedNode.getNodeType());
        plunkedNode.setNodeType(caseNodeType(plunkedNode.getNodeType(), nodeDef));
        savePlunkedNode(plunkedNode);
        view.hide();
        view.remove();
    }

    private void saveLink() {
        PlunkedLink plunkedLink = linkEditor.save();
        savePlunkedLink(plunkedLink);
        view.hide();
    }

    private String caseNodeType(String type, NodeDef nodeDef) {
        if (!type.equalsIgnoreCase(nodeDef.getName())) {
            type = type.toUpperCase();
        } else {
            type = nodeDef.getName();
        }
        return type;
    }

    private NodeDef findNodeDef(String type) {
        for (NodeDef nodeDef : graphSurface.getGraph().getModel().getRelGraphViewDef().getNodeDefs()) {
            if (nodeDef.getName().equalsIgnoreCase(type))
                return nodeDef;
        }
        return new NodeDef();
    }

    private void savePlunkedNode(final PlunkedNode plunkedNode) {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).savePlunkedNode(graphSurface.getVizUuid(), plunkedNode);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                plunkedNode.setHasBeenEdited(true);
                graphSurface.getGraph().getLegend().load();
                PlunkNodeUtils.checkForDuplicateNode(graphSurface, plunkedNode.getNodeName(), plunkedNode.getNodeType(), plunkedNode.getNodeKey());
            }
        });
        graphSurface.refresh(vortexFuture);
    }

    private void savePlunkedLink(final PlunkedLink plunkedLink) {
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(GraphActionServiceProtocol.class).savePlunkedLink(graphSurface.getVizUuid(), plunkedLink);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                graphSurface.getGraph().getLegend().load();
            }
        });
        graphSurface.refresh(vortexFuture);
    }

    private void setupEditor(RelGraphViewDef relGraphViewDef, String itemKey) {
        if (editingLink) {
            PlunkedLink plunkedLink = findPlunkedLinkDef(relGraphViewDef, itemKey);
            linkEditor = new EditPlunkedLinkPresenter(relGraphViewDef.getUuid(), plunkedLink);
        } else {
            PlunkedNode plunkedNode = findPlunkedNodeDef(relGraphViewDef, itemKey);
            nodeEditor = new EditPlunkedNodePresenter(relGraphViewDef.getUuid(), plunkedNode);
        }
    }

    private PlunkedNode findPlunkedNodeDef(RelGraphViewDef relGraphViewDef, String itemKey) {
        for (PlunkedNode plunkedNode : relGraphViewDef.getPlunkedNodes()) {
            if (plunkedNode.getNodeKey().equals(itemKey))
                return plunkedNode;
        }
        return null;
    }

    private PlunkedLink findPlunkedLinkDef(RelGraphViewDef relGraphViewDef, String itemKey) {
        for (PlunkedLink plunkedLink : relGraphViewDef.getPlunkedLinks()) {
            if (plunkedLink.buildItemKey().equals(itemKey))
                return plunkedLink;
        }
        return null;
    }

    public void show() {
        view.show();
    }
}
