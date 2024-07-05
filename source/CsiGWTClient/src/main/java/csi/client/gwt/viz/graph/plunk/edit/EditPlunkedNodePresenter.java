package csi.client.gwt.viz.graph.plunk.edit;

import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.data.shared.ListStore;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.feedback.ErrorLabelValidationFeedback;
import csi.client.gwt.validation.feedback.InfoRestoredValueBoxErrorLabelValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.feedback.ValueBoxErrorLabelValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidator;
import csi.client.gwt.validation.multi.MultiValidatorShowingFirstFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.IntegerValidator;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.validation.validator.NumberValidator;
import csi.client.gwt.validation.validator.RangeValidator;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.legend.GraphLegendInfo;
import csi.server.business.visualization.legend.GraphNodeLegendItem;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.visualization.graph.PlunkedNode;
import csi.server.common.service.api.GraphActionServiceProtocol;

/**
 * Contains the logic for populating the edit plunked node form and saving it to the server.
 * @author Centrifuge Systems, Inc.
 */
public class EditPlunkedNodePresenter {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final String vizUuid;
    private final EditPlunkedNodeView editPlunkedNodeView;

    private final PlunkedNode plunkedNode;
    private final MultiValidator multiValidator = new MultiValidatorShowingFirstFeedback();

    public EditPlunkedNodePresenter(String vizUuid, PlunkedNode plunkedNode) {
        this.vizUuid = vizUuid;
        this.plunkedNode = plunkedNode;
        this.editPlunkedNodeView = new EditPlunkedNodeView(this);

        editPlunkedNodeView.apply(EditPlunkedNodePresenter.createModelFrom(plunkedNode));
        initValidation();
    }

    public IsWidget getView() {
        return editPlunkedNodeView;
    }

    public PlunkedNode save(){
        EditPlunkedNodeModel model = editPlunkedNodeView.createModelFromUI();
        plunkedNode.setNodeName(model.getLabel());
        plunkedNode.setNodeType((model.getType() == null) ? "" : model.getType());
        plunkedNode.setSize(model.getSize());
        plunkedNode.setTransparency(model.getTransparency());
        plunkedNode.setColor(model.getColor());
        plunkedNode.setShape((model.getShape() == null) ? null : model.getShape().toString());
        plunkedNode.setIcon(model.getIcon());
        return plunkedNode;
    }


    public void addLegendItemsToListStore(final ListStore<String> store) {
        VortexFuture<GraphLegendInfo> future = getAvailableTypes(vizUuid);
        future.addEventHandler(new AbstractVortexEventHandler<GraphLegendInfo>() {
            @Override
            public void onSuccess(GraphLegendInfo result) {
                for (GraphNodeLegendItem graphNodeLegendItem : result.getNodeLegendItems()) {
                    if(graphNodeLegendItem.typeName != null) {
                        store.add(graphNodeLegendItem.typeName);
                    }
                }
            }
        });
    }

    public boolean validate() {
        return multiValidator.validate();
    }

    private void initValidation() {
        ValidationFeedback nameBoxFeedback = new ValueBoxErrorLabelValidationFeedback(editPlunkedNodeView.getNameTextBox(), editPlunkedNodeView.getLabelError(), i18n.validator_RequiredName());
        ValidationFeedback typeBoxFeedback = new ErrorLabelValidationFeedback(editPlunkedNodeView.getTypeError(), i18n.validator_RequiredType());
        ValidationFeedback sizeBoxFeedback = new ValueBoxErrorLabelValidationFeedback(editPlunkedNodeView.getSizeTextBox(), editPlunkedNodeView.getSizeError(), i18n.validator_MustBeNumber());
        ValidationFeedback sizeRangeFeedback = new InfoRestoredValueBoxErrorLabelValidationFeedback(i18n.plunking_Size_Help_Info(), editPlunkedNodeView.getSizeTextBox(), editPlunkedNodeView.getSizeError(), i18n.plunking_Size_Error_Text());
        ValidationFeedback transparencyBoxFeedback = new ValueBoxErrorLabelValidationFeedback(editPlunkedNodeView.getTransparencyTextBox(), editPlunkedNodeView.getTransparencyError(), i18n.validator_MustBeInteger());
        ValidationFeedback transparencyRangeFeedback = new InfoRestoredValueBoxErrorLabelValidationFeedback(i18n.plunking_Transparency_Help_Info(), editPlunkedNodeView.getTransparencyTextBox(), editPlunkedNodeView.getTransparencyError(), i18n.plunking_Transparency_Error_Text());

        ValidationAndFeedbackPair nameValidator = new ValidationAndFeedbackPair(new NotBlankValidator(editPlunkedNodeView.getNameTextBox()), nameBoxFeedback);
        ValidationAndFeedbackPair typeValidator = new ValidationAndFeedbackPair(new NotBlankValidator(editPlunkedNodeView.getTypeComboBox()), typeBoxFeedback);
        ValidationAndFeedbackPair sizeExistsValidator = new ValidationAndFeedbackPair(new NotBlankValidator(editPlunkedNodeView.getSizeTextBox()), sizeBoxFeedback);
        ValidationAndFeedbackPair sizeValidator = new ValidationAndFeedbackPair(new NumberValidator(editPlunkedNodeView.getSizeTextBox()), sizeBoxFeedback);
        ValidationAndFeedbackPair sizeRangeValidator = new ValidationAndFeedbackPair(new RangeValidator(editPlunkedNodeView.getSizeTextBox(), 0.2, 5), sizeRangeFeedback);
        ValidationAndFeedbackPair transparencyExistsValidator = new ValidationAndFeedbackPair(new NotBlankValidator(editPlunkedNodeView.getTransparencyTextBox()), transparencyBoxFeedback);
        ValidationAndFeedbackPair transparencyValidator = new ValidationAndFeedbackPair(new IntegerValidator(editPlunkedNodeView.getTransparencyTextBox()), transparencyBoxFeedback);
        ValidationAndFeedbackPair transparencyRangeValidator = new ValidationAndFeedbackPair(new RangeValidator(editPlunkedNodeView.getTransparencyTextBox(), 10, 100), transparencyRangeFeedback);

        multiValidator.addValidationAndFeedback(nameValidator);
        multiValidator.addValidationAndFeedback(typeValidator);
        multiValidator.addValidationAndFeedback(sizeExistsValidator);
        multiValidator.addValidationAndFeedback(sizeValidator);
        multiValidator.addValidationAndFeedback(sizeRangeValidator);
        multiValidator.addValidationAndFeedback(transparencyExistsValidator);
        multiValidator.addValidationAndFeedback(transparencyValidator);
        multiValidator.addValidationAndFeedback(transparencyRangeValidator);

    }

    private VortexFuture<GraphLegendInfo> getAvailableTypes(String vizUuid){
        VortexFuture<GraphLegendInfo> future = WebMain.injector.getVortex().createFuture();
        try {
            future.execute(GraphActionServiceProtocol.class).legendData(vizUuid);
        } catch (CentrifugeException ignored) {
        }
        return future;
    }

    private static EditPlunkedNodeModel createModelFrom(PlunkedNode plunkedNode) {
        if(plunkedNode == null){
            return null;
        }
        EditPlunkedNodeModel model = new EditPlunkedNodeModel();
        model.setLabel(plunkedNode.getNodeName());
        model.setType(plunkedNode.getNodeType());
        model.setSize(plunkedNode.getSize());
        model.setTransparency(plunkedNode.getTransparency());
        model.setColor(plunkedNode.getColor());
        model.setShape(ShapeType.getShape(plunkedNode.getShape()));
        model.setIcon(plunkedNode.getIcon());
        return model;
    }
}
