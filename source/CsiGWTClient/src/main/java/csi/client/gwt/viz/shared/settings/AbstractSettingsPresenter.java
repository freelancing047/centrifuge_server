/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.viz.shared.settings;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiField;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.MultiNotificationPopup;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;

/**
 * @author Centrifuge Systems, Inc.
 */
public abstract class AbstractSettingsPresenter<T extends VisualizationDef> implements VisualizationSettingsPresenter {

    protected SettingsActionCallback<T> settingsActionCallback;

    protected String dataViewUuid;
    protected String worksheetUuid;
    protected boolean createMode = false;
    protected Visualization visualization;
    protected AbstractDataViewPresenter dataViewPresenter;
    protected WorksheetPresenter worksheetPresenter;
    protected MultiValidatorCollectingErrors validator = new MultiValidatorCollectingErrors();


    // UiBinder can only work with a parent in a different class if the fields are marked public. Ugly.
    @UiField
    public VisualizationSettingsModal vizSettings;

    @UiField(provided = true)
    public DataViewDef dataViewDef;

    @UiField(provided = true)
    public T visualizationDef;

    public AbstractSettingsPresenter(SettingsActionCallback<T> settingsActionCallback) {
        this.settingsActionCallback = settingsActionCallback;
    }

    public String getDataViewUuid() {
        return dataViewUuid;
    }

    @Override
    public void setDataView(DataView dataView) {
        this.dataViewUuid = dataView.getUuid();
        this.dataViewDef = dataView.getMeta();
    }

    public String getWorksheetUuid() {
        return worksheetUuid;
    }

    @Override
    public void setWorksheetUuid(String worksheetUuid) {
        this.worksheetUuid = worksheetUuid;
    }

    public DataViewDef getDataViewDef() {
        return dataViewDef;
    }

    public T getVisualizationDef() {
        return visualizationDef;
    }

    public void setVisualizationDef(T visualizationDef) {
        this.visualizationDef = visualizationDef;
    }

    public Visualization getVisualization() {
        return visualization;
    }

    public void setVisualization(Visualization visualization) {
        this.visualization = visualization;
    }

    @Override
    public void show() {
        // Important: This needs to happen before the ui-bind so that visualizationModal picks up a non-null
        // visualization setting.
        if (getVisualizationDef() == null) {
            // Create scenario.
            T def = createNewVisualizationDef();
            setVisualizationDef(def);
            createMode = true;
        }

        bindUI();

        setupHandlers();
        initiateValidator();

        vizSettings.show();

        vizSettings.enable();
        vizSettings.updateViewFromModel();

    }

    protected abstract T createNewVisualizationDef();

    protected abstract void bindUI();

    /**
     * Create and attache the SettingValidator
     */
    protected abstract void initiateValidator();

    protected boolean isValid() {
        if (validator.validate()) {
            return true;
        } else {
            String errors = "Please fix the following errors";
            MultiNotificationPopup dialog = new MultiNotificationPopup("Settings Error", errors, validator.getErrors());
            dialog.show();
            return false;
        }
    }

    protected void preCheck() {

    }

    protected void setupHandlers() {
        vizSettings.registerSaveClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                preCheck();
                if (!isValid()) {
                    return;
                }
                if (vizSettings.getButtonSave().isEnabled()) {
                    vizSettings.disable();
                    vizSettings.updateModelWithView();
                    // FIXME: Better way to discriminate?
                    if (createMode) {
                        addVisualizationToServer();
                    } else {
                        saveVisualizationToServer();
                    }
                }
            }
        });

        vizSettings.registerCancelClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                settingsActionCallback.onCancel();
                // FIXME: Prompt user if they really want to cancel.
                vizSettings.hide();
            }
        });
    }

    /**
     * Called to save a new visualization.
     */
    protected void addVisualizationToServer() {
        Vortex vortex = WebMain.injector.getVortex();
        // FIXME: Show some indicator for activity.
        try {

            vortex.execute((Callback<Void>) result -> {
                vizSettings.hide();
                settingsActionCallback.onSaveComplete(getVisualizationDef(), vizSettings.isSuppressLoadOnSave());
            }, VisualizationActionsServiceProtocol.class).addVisualization(getVisualizationDef(), getDataViewUuid(),
                    getWorksheetUuid());

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    /**
     * Called to save updates to an existing visualization.
     */
    protected void saveVisualizationToServer() {
        VortexFuture<Void> future = getVisualization().saveSettings(true);
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                vizSettings.hide();
                settingsActionCallback.onSaveComplete(getVisualizationDef(), vizSettings.isSuppressLoadOnSave());
            }
        });
    }

    public AbstractDataViewPresenter getDataViewPresenter() {
        return dataViewPresenter;
    }

    public void setDataViewPresenter(AbstractDataViewPresenter dataViewPresenter) {
        this.dataViewPresenter = dataViewPresenter;
    }

    public WorksheetPresenter getWorksheetPresenter() {
        return worksheetPresenter;
    }

    public void setWorksheetPresenter(WorksheetPresenter worksheetPresenter) {
        this.worksheetPresenter = worksheetPresenter;
    }
}
