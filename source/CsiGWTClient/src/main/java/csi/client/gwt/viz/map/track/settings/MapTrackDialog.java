package csi.client.gwt.viz.map.track.settings;

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
import csi.server.common.model.visualization.map.MapTrack;

public class MapTrackDialog extends Composite {
    private static MapTrackDialogUiBinder uiBinder = GWT.create(MapTrackDialogUiBinder.class);
    @UiField
    AppearanceTab appearanceTab;
    @UiField
    SizeProvidingModal settingsModal;
    private MapTrackSettingsPresenter mapTrackSettingsPresenter;
    private MultiValidatorCollectingErrors validator = new MultiValidatorCollectingErrors();
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    public MapTrackDialog(MapSettingsPresenter presenter) {
        uiBinder.createAndBindUi(this);
        mapTrackSettingsPresenter = new MapTrackSettingsPresenter();
        mapTrackSettingsPresenter.setMapSettingsPresenter(presenter);
//		appearanceTab.setPresenter(mapTrackSettingsPresenter);
        initializeValidation();
    }

    private void initializeValidation() {
        appearanceTab.initializeValidation(validator);

        Validator sameTrackInExistenceValidator = () -> {
            MapTrack mapTrack = mapTrackSettingsPresenter.getMapTrack();
            String place = appearanceTab.getPlaceValue();
            for (MapTrack existingMapTrack : mapTrackSettingsPresenter.getMapTracks()) {
                if (mapTrack != existingMapTrack && place.equals(existingMapTrack.getPlace())) {
                    return false;
                }
            }
            return true;
        };
        ValidationFeedback sameTrackInExistenceValidatorFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_sameTrackInExistenceValidatorFeedbackText()); // $NON-NLS-1$
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(sameTrackInExistenceValidator, sameTrackInExistenceValidatorFeedback));
    }

    @UiHandler("buttonDelete")
    public void onDeleteClicked(ClickEvent clickEvent) {
        mapTrackSettingsPresenter.removeMapTrack();
        clearSelection();
        hide();
    }

    private void clearSelection() {
//		appearanceTab.clearSelection();
    }

    public void hide() {
        settingsModal.hide();
    }

    @UiHandler("buttonSave")
    public void onSaveClicked(ClickEvent clickEvent) {
        if (isValid()) {
//			appearanceTab.updateMapTrack();
            mapTrackSettingsPresenter.saveMapTrack();
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
        mapTrackSettingsPresenter.cancelMapTrack();
        clearSelection();
    }

    public void show() {
        RootPanel.get().add(settingsModal);
        settingsModal.show();
    }

    public void setSelection(MapTrack mapTrack) {
        mapTrackSettingsPresenter.setMapTrack(mapTrack);
//		appearanceTab.setSelection();
    }

    interface MapTrackDialogUiBinder extends UiBinder<Widget, MapTrackDialog> {
    }

}
