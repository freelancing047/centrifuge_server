package csi.client.gwt.viz.graph.plunk;

import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.feedback.ErrorLabelValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.feedback.ValueBoxErrorLabelValidationFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.plunk.util.PlunkNodeUtils;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.legend.GraphLegendInfo;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.PlunkedNode;

/**
 * Handles logic for user actions.
 * @author Centrifuge Systems, Inc.
 */
public class PlunkNodePresenter {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final Graph graph;
    private final PlunkNodeModel model;
    private final PlunkNodeDialog view;

    private final ValidationAndFeedbackPair nameValidator;
    private final ValidationAndFeedbackPair typeValidator;

    public PlunkNodePresenter(Graph graph, int x, int y) {
        this.graph = graph;
        this.model = new PlunkNodeModel(graph.getUuid(), x, y);
        this.view = new PlunkNodeDialog(this);

        ValidationFeedback nameBoxFeedback = new ValueBoxErrorLabelValidationFeedback(view.getNameTextBox(), view.getNameErrorLabel(), i18n.validator_RequiredName());
        ValidationFeedback typeBoxFeedback = new ErrorLabelValidationFeedback(view.getTypeErrorLabel(), i18n.validator_RequiredType());
        nameValidator = new ValidationAndFeedbackPair(new NotBlankValidator(view.getNameTextBox()), nameBoxFeedback);
        typeValidator = new ValidationAndFeedbackPair(new NotBlankValidator(view.getTypeComboBox()), typeBoxFeedback);
    }

    public void show() {
        view.show();
    }

    public void addLegendItemsToListStore(final ListStore<String> listStore) {
        VortexFuture<GraphLegendInfo> future = model.getLegendItems();
        future.addEventHandler(new AbstractVortexEventHandler<GraphLegendInfo>() {
            @Override
            public void onSuccess(GraphLegendInfo result) {
                for (GraphNodeLegendItem graphNodeLegendItem : result.getNodeLegendItems()) {
                    if (graphNodeLegendItem.typeName != null) {
                        listStore.add(graphNodeLegendItem.typeName);
                    }
                }
            }
        });
    }

    public boolean validateName() {
        return nameValidator.validateWithFeedback();
    }

    public void createPlunkedNode() {
        final String name = view.getNameTextBox().getValue().trim();
        String type = view.getTypeComboBox().getValue();
        if (validateInputValues()) {
            NodeDef nodeDef = findNodeDef(type);
            final String newType = caseNodeType(type, nodeDef);

            VortexFuture<PlunkedNode> future = model.plunkNewNode(name, newType, nodeDef);
            future.addEventHandler(new AbstractVortexEventHandler<PlunkedNode>() {
                @Override
                public void onSuccess(PlunkedNode result) {
                    graph.getModel().getRelGraphViewDef().getPlunkedNodes().add(result);
                    graph.getLegend().load();
                    PlunkNodeUtils.checkForDuplicateNode(graph.getGraphSurface(), name, newType, result.getNodeKey());
                }
            });

            graph.getGraphSurface().refresh(future);
            view.hide();
        }
    }

    private String caseNodeType(String type, NodeDef nodeDef) {
        if (!type.equalsIgnoreCase(getTypeFromNode(nodeDef))) {
            type = type.toUpperCase();
        } else {
            type = getTypeFromNode(nodeDef);
        }
        return type;
    }

    private NodeDef findNodeDef(String type) {
        for (NodeDef nodeDef : graph.getModel().getRelGraphViewDef().getNodeDefs()) {
            String nodeDefType = getTypeFromNode(nodeDef);
            if (nodeDefType.equalsIgnoreCase(type)) {
                return nodeDef;
            }
        }
        return new NodeDef();
    }

    private String getTypeFromNode(NodeDef nodeDef) {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        if ((attributeDef != null) && (attributeDef.getFieldDef() != null) && attributeDef.getFieldDef().isAnonymous()) {
            return attributeDef.getFieldDef().getStaticText();
        }
        return "";
    }

    private boolean validateInputValues() {
        boolean validName = validateName();
        boolean validType = validateType();
        return validName && validType;
    }

    private boolean validateType() {
        return typeValidator.validateWithFeedback();
    }

}
