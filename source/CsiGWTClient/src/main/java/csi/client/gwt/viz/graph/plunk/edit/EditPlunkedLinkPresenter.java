package csi.client.gwt.viz.graph.plunk.edit;

import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.feedback.InfoRestoredValueBoxErrorLabelValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.feedback.ValueBoxErrorLabelValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidator;
import csi.client.gwt.validation.multi.MultiValidatorShowingFirstFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.IntegerValidator;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.validation.validator.RangeValidator;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.business.visualization.legend.GraphLegendInfo;
import csi.server.business.visualization.legend.GraphLinkLegendItem;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.PlunkedLink;
import csi.server.common.service.api.GraphActionServiceProtocol;

/**
 * @author Centrifuge Systems, Inc.
 */
public class EditPlunkedLinkPresenter {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final String vizUuid;
    private final EditPlunkedLinkView editPlunkedLinkView;

    private final PlunkedLink plunkedLink;
    private final MultiValidator multiValidator = new MultiValidatorShowingFirstFeedback();

    public EditPlunkedLinkPresenter(String vizUuid, PlunkedLink plunkedLink) {
        this.vizUuid = vizUuid;
        this.plunkedLink = plunkedLink;
        this.editPlunkedLinkView = new EditPlunkedLinkView(this);

        this.editPlunkedLinkView.apply(createModelFrom(plunkedLink));
        initValidation();
    }

    public PlunkedLink save() {
        EditPlunkedLinkModel model = editPlunkedLinkView.createModelFromUI();
        plunkedLink.setLabel(model.getLabel());
        plunkedLink.setSize(model.getSize());
        plunkedLink.setTransparency(model.getTransparency());
        plunkedLink.setColor(model.getColor());
        plunkedLink.setLinkDirection(model.getLinkDirection());
        plunkedLink.setLinkType(model.getLinkType());
        return plunkedLink;
    }

    public IsWidget getView() {
        return editPlunkedLinkView;
    }

    public boolean validate() {
        return multiValidator.validate();
    }

    public void addLegendItemsToListStore(final ListStore<String> store) {
        VortexFuture<GraphLegendInfo> future = getAvailableTypes(vizUuid);
        future.addEventHandler(new AbstractVortexEventHandler<GraphLegendInfo>() {
            @Override
            public void onSuccess(GraphLegendInfo result) {
                for (GraphLinkLegendItem graphLinkLegendItem : result.getLinkLegendItems()) {
                    if(graphLinkLegendItem.typeName != null)
                        store.add(graphLinkLegendItem.typeName);
                }
            }
        });
    }

    private VortexFuture<GraphLegendInfo> getAvailableTypes(String vizUuid){
        VortexFuture<GraphLegendInfo> future = WebMain.injector.getVortex().createFuture();
        try {
            future.execute(GraphActionServiceProtocol.class).legendData(vizUuid);
        } catch (CentrifugeException e) {
        }
        return future;
    }

    private void initValidation() {
        ValidationFeedback sizeBoxFeedback = new ValueBoxErrorLabelValidationFeedback(editPlunkedLinkView.getSizeTextBox(), editPlunkedLinkView.getSizeError(), i18n.validator_MustBeInteger());
        ValidationFeedback rangeFeedback = new InfoRestoredValueBoxErrorLabelValidationFeedback(i18n.plunking_Width_Help_Info(), editPlunkedLinkView.getSizeTextBox(), editPlunkedLinkView.getSizeError(), i18n.validator_Integer_Between1And5());
        ValidationFeedback transparencyBoxFeedback = new ValueBoxErrorLabelValidationFeedback(editPlunkedLinkView.getTransparencyTextBox(), editPlunkedLinkView.getTransparencyError(), i18n.validator_MustBeInteger());
        ValidationFeedback transparencyRangeFeedback = new InfoRestoredValueBoxErrorLabelValidationFeedback(i18n.plunking_Transparency_Help_Info(), editPlunkedLinkView.getTransparencyTextBox(), editPlunkedLinkView.getTransparencyError(), i18n.plunking_Transparency_Error_Text());

        ValidationAndFeedbackPair sizeExistsValidator = new ValidationAndFeedbackPair(new NotBlankValidator(editPlunkedLinkView.getSizeTextBox()), sizeBoxFeedback);
        ValidationAndFeedbackPair sizeValidator = new ValidationAndFeedbackPair(new IntegerValidator(editPlunkedLinkView.getSizeTextBox()), sizeBoxFeedback);
        ValidationAndFeedbackPair sizeRangeValidator = new ValidationAndFeedbackPair(new RangeValidator(editPlunkedLinkView.getSizeTextBox(), 1, 5), rangeFeedback);
        ValidationAndFeedbackPair transparencyExistsValidator = new ValidationAndFeedbackPair(new NotBlankValidator(editPlunkedLinkView.getTransparencyTextBox()), transparencyBoxFeedback);
        ValidationAndFeedbackPair transparencyValidator = new ValidationAndFeedbackPair(new IntegerValidator(editPlunkedLinkView.getTransparencyTextBox()), transparencyBoxFeedback);
        ValidationAndFeedbackPair transparencyRangeValidator = new ValidationAndFeedbackPair(new RangeValidator(editPlunkedLinkView.getTransparencyTextBox(), 10, 100), transparencyRangeFeedback);

        multiValidator.addValidationAndFeedback(sizeExistsValidator);
        multiValidator.addValidationAndFeedback(sizeValidator);
        multiValidator.addValidationAndFeedback(sizeRangeValidator);
        multiValidator.addValidationAndFeedback(transparencyExistsValidator);
        multiValidator.addValidationAndFeedback(transparencyValidator);
        multiValidator.addValidationAndFeedback(transparencyRangeValidator);
    }

    private static EditPlunkedLinkModel createModelFrom(PlunkedLink plunkedLink) {
        EditPlunkedLinkModel model = new EditPlunkedLinkModel();
        model.setLabel(plunkedLink.getLabel());
        model.setSize(plunkedLink.getSize());
        model.setTransparency(plunkedLink.getTransparency());
        model.setColor(plunkedLink.getColor());
        model.setLinkDirection(plunkedLink.getLinkDirection());
        model.setLinkType(plunkedLink.getLinkType());
        return model;
    }

}
