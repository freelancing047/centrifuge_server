/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.viz.matrix.settings;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.validation.validator.VisualizationUniqueNameValidator;
import csi.client.gwt.viz.shared.settings.AbstractSettingsPresenter;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.client.gwt.viz.shared.settings.VisualizationSettingsModal;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.matrix.Axis;
import csi.server.common.model.visualization.matrix.MatrixCategoryDefinition;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixSortDefinition;
import csi.server.common.model.visualization.matrix.MatrixViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MatrixSettingsPresenter extends AbstractSettingsPresenter<MatrixViewDef> {

    @UiField
    MatrixCategoriesTab tabCategories;
    @UiField
    MatrixMeasuresTab tabMeasure;
    @UiField
    MatrixSortTab tabSort;
    @UiField
    ColorsTab tabColors;
    @UiField
    MatrixGeneralTab generalTab;

    @UiField
    MatrixFilterTab tabFilter;

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    @UiTemplate("MatrixSettingsView.ui.xml")
    interface SpecificUiBinder extends UiBinder<VisualizationSettingsModal, MatrixSettingsPresenter> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public MatrixSettingsPresenter(SettingsActionCallback<MatrixViewDef> settingsActionCallback) {
        super(settingsActionCallback);
    }

    @Override
    protected void bindUI() {
        uiBinder.createAndBindUi(this);
        tabCategories.setSortTab(tabSort);
        tabSort.setCategoriesTab(tabCategories);
        tabSort.setMeasuresTab(tabMeasure);
        generalTab.setPresenter(this);

        tabFilter.setMeasuresTab(tabMeasure);
    }

    @Override
    protected MatrixViewDef createNewVisualizationDef() {
        MatrixViewDef def = new MatrixViewDef();
        def.setBroadcastListener(WebMain.getClientStartupInfo().isListeningByDefault());

        MatrixSettings settings = new MatrixSettings();
        settings.setColorModel(null);
        def.setMatrixSettings(settings);

        String name = UniqueNameUtil.getDistinctName(UniqueNameUtil.getVisualizationNames(dataViewPresenter), i18n.matrixDefaultName());
        def.setName(name);
        return def;
    }

    @Override
    protected void saveVisualizationToServer() {
        MatrixSettings settings = getVisualizationDef().getMatrixSettings();

        //Clean out the array for the quick sort defs..
        settings.setAxisQuickSortDefinitions(new ArrayList<MatrixSortDefinition>());
        settings.setMeasureQuickSortOrder(SortOrder.NONE);

        settings.getAxisSortDefinitions();
        settings.getAxisCategories();

        super.saveVisualizationToServer();
    }

    private Integer minScaleSizeValue;
    private Integer maxScaleSizeValue;

    @Override
    protected void initiateValidator() {
        NotBlankValidator notBlankValidator = new NotBlankValidator(generalTab.chartName);
        VisualizationUniqueNameValidator visualizationUniqueNameValidator = new VisualizationUniqueNameValidator(
                getDataViewDef().getModelDef().getVisualizations(), generalTab.chartName, getVisualizationDef().getUuid());

        Validator tabFilterValidator = () -> tabFilter.view.isCriteriaValid();

        Validator xAxisValidator = () -> {
            List<MatrixCategoryDefinition> categories = tabCategories.getCurrentCategories();
            for (MatrixCategoryDefinition mcd : categories) {
                if (mcd.getAxis() == Axis.X) {
                    return true;
                }
            }
            // no X-axis found
            return false;
        };

        Validator yAxisValidator = () -> {
            List<MatrixCategoryDefinition> categories = tabCategories.getCurrentCategories();
            for (MatrixCategoryDefinition mcd : categories) {
                if (mcd.getAxis() == Axis.Y) {
                    return true;
                }
            }
            // no Y-axis found
            return false;
        };

        Validator measureField = () -> {
            return tabMeasure.isFieldValid();
        };
        Validator measureFunctionField = () -> {
            return tabMeasure.isAggregateValid();
        };

        Validator minScaleSizeValidator = new Validator() {

            @Override
            public boolean isValid() {
                boolean isValid = false;
                String min = tabMeasure.scaleMin.getValue();
                try {
                    minScaleSizeValue = Integer.parseInt(min);
                    isValid = true;
                } catch (NumberFormatException nfe) {
                }
                return isValid;
            }
        };

        Validator maxScaleSizeValidator = new Validator() {

            @Override
            public boolean isValid() {
                boolean isValid = false;
                String max = tabMeasure.scaleMax.getValue();
                try {
                    maxScaleSizeValue = Integer.parseInt(max);
                    isValid = true;
                } catch (NumberFormatException nfe) {
                }
                return isValid;
            }
        };



        ValidationFeedback minScaleSizeFeedback = new StringValidationFeedback(CentrifugeConstantsLocator.get().matrixSettingsValidatingMinScaleSize());
        ValidationFeedback maxScaleSizeFeedback = new StringValidationFeedback(CentrifugeConstantsLocator.get().matrixSettingsValidatingMaxScaleSize());
        ValidationFeedback validationFeedbackX = new StringValidationFeedback(CentrifugeConstantsLocator.get().matrixSettingsPersenter_validationFeedbackX());
        ValidationFeedback validationFeedbackY = new StringValidationFeedback(CentrifugeConstantsLocator.get().matrixSettingsPersenter_validationFeedbackY());

        ValidationFeedback measureFieldIllegeal = new StringValidationFeedback(CentrifugeConstantsLocator.get().matrixSettingsPersenter_validationFeedbackMeasureField());
        ValidationFeedback measureFunctionIllegeal = new StringValidationFeedback(CentrifugeConstantsLocator.get().matrixSettingsPersenter_validationFeedbackMeasureFunction());
        ValidationFeedback tabFilterFeedback = new StringValidationFeedback(i18n.matrixSettingsPresenter_filterFeedback());

        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(minScaleSizeValidator, minScaleSizeFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(maxScaleSizeValidator, maxScaleSizeFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(xAxisValidator, validationFeedbackX));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(yAxisValidator, validationFeedbackY));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(measureField, measureFieldIllegeal));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(measureFunctionField,  measureFunctionIllegeal));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator, StringValidationFeedback.getEmptyVisualizationFeedback()));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(visualizationUniqueNameValidator, StringValidationFeedback.getDuplicateVisualizationFeedback()));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(tabFilterValidator, tabFilterFeedback));

    }
    

    public void updateColor(){
        tabColors.updateViewFromModel();
    }
}
