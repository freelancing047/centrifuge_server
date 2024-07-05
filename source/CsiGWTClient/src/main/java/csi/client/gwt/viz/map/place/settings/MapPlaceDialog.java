package csi.client.gwt.viz.map.place.settings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.viz.map.settings.MapSettingsPresenter;
import csi.client.gwt.widget.boot.MultiNotificationPopup;
import csi.client.gwt.widget.boot.SizeProvidingModal;
import csi.server.common.model.visualization.map.MapPlace;

public class MapPlaceDialog {
	private static MapPlaceDialogUiBinder uiBinder = GWT.create(MapPlaceDialogUiBinder.class);

	interface MapPlaceDialogUiBinder extends UiBinder<Widget, MapPlaceDialog> {
	}

	@UiField
	AppearanceTab appearanceTab;
	@UiField
	TooltipTab tooltipTab;
	@UiField
	SizeProvidingModal settingsModal;

	private MapPlaceSettingsPresenter mapPlaceSettingsPresenter;
	private MultiValidatorCollectingErrors validator = new MultiValidatorCollectingErrors();

	private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

	public MapPlaceDialog(MapSettingsPresenter presenter) {
		uiBinder.createAndBindUi(this);
		mapPlaceSettingsPresenter = new MapPlaceSettingsPresenter();
		mapPlaceSettingsPresenter.setMapSettingsPresenter(presenter);
		appearanceTab.setPresenter(mapPlaceSettingsPresenter);
		tooltipTab.setPresenter(mapPlaceSettingsPresenter);
		initializeValidation();
	}

	private void initializeValidation() {
		appearanceTab.initializeValidation(validator);
	}

	@UiHandler("buttonDelete")
	public void onDeleteClicked(ClickEvent clickEvent) {
		mapPlaceSettingsPresenter.removePlace();
		clearSelection();
		hide();
	}

	private void clearSelection() {
		appearanceTab.clearSelection();
		tooltipTab.clearSelection();
	}

	public void hide() {
		settingsModal.hide();
	}

	@UiHandler("buttonSave")
	public void onSaveClicked(ClickEvent clickEvent) {
		if (isValid()) {
			appearanceTab.updateMapPlace();
			tooltipTab.updateMapPlace();
			mapPlaceSettingsPresenter.savePlace();
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
		mapPlaceSettingsPresenter.cancelMapPlaceDefinition();
		clearSelection();
	}

	public void show() {
		RootPanel.get().add(settingsModal);
		settingsModal.show();
	}

	public void setSelection(MapPlace mapPlace) {
		mapPlaceSettingsPresenter.setMapPlace(mapPlace);

		appearanceTab.setSelection();
		tooltipTab.setSelection();
	}
}