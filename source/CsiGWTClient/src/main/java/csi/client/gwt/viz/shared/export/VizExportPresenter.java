package csi.client.gwt.viz.shared.export;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.viz.shared.export.model.VisualizationExportable;
import csi.client.gwt.viz.shared.export.settings.ExportSettings;
import csi.client.gwt.viz.shared.export.view.widget.ExportSize;
import csi.client.gwt.viz.shared.export.view.widget.VizExport;
import csi.client.gwt.widget.boot.MultiNotificationPopup;
import csi.server.common.model.visualization.map.MapViewDef;

/**
 * Handles everything for the VizExport View
 */
public class VizExportPresenter {

    private final VisualizationExportable exportable;
    private final VizExport exportView;

    protected MultiValidatorCollectingErrors validator = new MultiValidatorCollectingErrors();
    protected static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public VizExportPresenter(VisualizationExportable exportModel, VizExport exportView) {
        this.exportable = exportModel;
        this.exportView = exportView;

        initializeView();
    }

    public void initializeView(){
        attachHandlersAndValidators();

        exportView.selectType(AvailableExportTypes.getDefaultExportType(exportable));

        // set the title and the export file name
        exportView.setExportDialogTitle(exportable.getVisualization().getName());
        exportView.setExportFileName(exportable.getName());

        exportView.show();


    }

    public void attachHandlersAndValidators(){
        attachActionButtonHandler();
        attachCancelButtonHandler();

        addValidation();
    }

    public void handleSave(){
        doExport();
    }

    public void doExport(){
        if (!isValid()) {
            return;
        }
        //map is completely different - we call the map to get us the images, and anything here is no longer used after that ( the mask that ExportReq shows is in the MapPresenter)
        if(AvailableExportTypes.getVisualisationDef(exportable) instanceof MapViewDef && getExportSettings().getExportType() == ExportType.PNG){
            MapPresenter map = (MapPresenter) exportable.getVisualization();
            map.getMapPNGExport();
        }else {
            ExportRequestor req = new ExportRequestor(exportable, getExportSettings());
            req.downloadExport();
        }

        exportView.destroy();
    }

    private ExportSettings getExportSettings(){
        ExportSettings exportSettings = exportView.getExportSettings();
//        exportSettings.setName(exportable.getName());
        return exportSettings;
    }

    protected boolean isValid() {
        if (validator.validate()) {
            return true;
        } else {
            MultiNotificationPopup dialog = new MultiNotificationPopup(i18n.export_error_title(), i18n.export_default_error_message(), validator.getErrors());
            dialog.show();
            return false;
        }
    }

    private void addValidation(){
        Validator selectionValidator = () -> exportView.getExportSize() != ExportSize.SELECTION_ONLY || exportable.getVisualization().hasSelection();
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(selectionValidator,
                                        new StringValidationFeedback(i18n.export_no_selection_error())));

        Validator nonNullName = () -> !exportView.getExportFileName().isEmpty();

        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(nonNullName,
                new StringValidationFeedback(i18n.namePrompt())));

    }

    private void attachActionButtonHandler(){
        exportView.getActionButton().addClickHandler(event -> handleSave());
    }

    private void attachCancelButtonHandler(){
        exportView.getCancelButton().addClickHandler(event -> exportView.destroy());
    }

    protected VizExport getExportView() {
        return exportView;
    }

    protected VisualizationExportable getExportable() {
        return exportable;
    }

    protected MultiValidatorCollectingErrors getValidator() {
        return validator;
    }

    protected void setValidator(MultiValidatorCollectingErrors validator) {
        this.validator = validator;
    }
}
