package csi.client.gwt.viz.map.association.settings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.viz.map.settings.MapSettingsPresenter;
import csi.client.gwt.widget.boot.MultiNotificationPopup;
import csi.client.gwt.widget.boot.SizeProvidingModal;
import csi.server.common.model.visualization.map.MapAssociation;

public class MapAssociationDialog extends Composite {
	private static MapAssociationDialogUiBinder uiBinder = GWT.create(MapAssociationDialogUiBinder.class);

	interface MapAssociationDialogUiBinder extends UiBinder<Widget, MapAssociationDialog> {
	}

	@UiField
	AppearanceTab appearanceTab;
	@UiField
	SizeProvidingModal settingsModal;

	private MapAssociationSettingsPresenter mapAssociationSettingsPresenter;
	private MultiValidatorCollectingErrors validator = new MultiValidatorCollectingErrors();

	private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

	public MapAssociationDialog(MapSettingsPresenter presenter) {
		uiBinder.createAndBindUi(this);
		mapAssociationSettingsPresenter = new MapAssociationSettingsPresenter();
		mapAssociationSettingsPresenter.setMapSettingsPresenter(presenter);
		appearanceTab.setPresenter(mapAssociationSettingsPresenter);
		initializeValidation();
	}

	private void initializeValidation() {
		appearanceTab.initializeValidation(validator);

		Validator sameAssociationInExistenceValidator = new Validator() {
			@Override
			public boolean isValid() {
				MapAssociation mapAssociation = mapAssociationSettingsPresenter.getMapAssociation();
				String source = appearanceTab.getSourceValue();
				String destination = appearanceTab.getDestinationValue();
				for (MapAssociation existingMapAssociation : mapAssociationSettingsPresenter.getMapAssociations()) {
					if (mapAssociation != existingMapAssociation && source.equals(existingMapAssociation.getSource())
							&& destination.equals(existingMapAssociation.getDestination())) {
						return false;
					}
				}
				return true;
			}
		};
		ValidationFeedback sameAssociationInExistenceValidatorFeedback = new StringValidationFeedback(
				_constants.mapSettingsView_sameAssociationInExistenceValidatorFeedbackText()); // $NON-NLS-1$
		validator.addValidationAndFeedback(new ValidationAndFeedbackPair(sameAssociationInExistenceValidator,
				sameAssociationInExistenceValidatorFeedback));
	}

	@UiHandler("buttonDelete")
	public void onDeleteClicked(ClickEvent clickEvent) {
		mapAssociationSettingsPresenter.removeMapAssociation();
		clearSelection();
		hide();
	}

	private void clearSelection() {
		appearanceTab.clearSelection();
	}

	public void hide() {
		settingsModal.hide();
	}

	@UiHandler("buttonSave")
	public void onSaveClicked(ClickEvent clickEvent) {
		if (isValid()) {
			appearanceTab.updateMapAssociation();
			mapAssociationSettingsPresenter.saveMapAssociation();
			clearSelection();
		}
	}

	protected boolean isValid() {
		if (validator.validate()) {
			return true;
		} else {
			String errors = _constants.mapSettingsView_ErrorsIntro();
			MultiNotificationPopup dialog = new MultiNotificationPopup(_constants.mapSettingsView_ErrorsTitle(), errors,
					validator.getErrors());
			dialog.show();
			return false;
		}
	}

	@UiHandler("buttonCancel")
	public void onCancelClicked(ClickEvent clickEvent) {
		mapAssociationSettingsPresenter.cancelMapAssociation();
		clearSelection();
	}

	public void show() {
		RootPanel.get().add(settingsModal);
		settingsModal.show();
	}

	public void setSelection(MapAssociation mapAssociation) {
		mapAssociationSettingsPresenter.setMapAssociation(mapAssociation);
		appearanceTab.setSelection();
	}

}
