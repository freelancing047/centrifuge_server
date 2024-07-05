/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.viz.chart.settings;

import com.github.gwtbootstrap.client.ui.Tab;
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
import csi.client.gwt.validation.validator.*;
import csi.client.gwt.viz.shared.settings.AbstractSettingsPresenter;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.client.gwt.viz.shared.settings.VisualizationSettingsModal;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.common.model.visualization.selection.DrillCategory;

import java.util.ArrayList;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChartSettingsPresenter extends AbstractSettingsPresenter<DrillChartViewDef> {
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    @UiField
    ChartCategoriesTab tabCategories;
    @UiField
    ChartMeasuresTab tabMeasures;
    @UiField
    ChartSortTab tabSort;
    @UiField
    ChartGeneralTab generalTab;
    @UiField
    ChartFilterTab tabFilter;
    @UiField
    Tab tabFilterContainer;
    @UiField
    ChartAdvancedTab tabAdvanced;
    @UiField
    Tab measuresNav;

    VariableNotEmptyCollectionValidator tabCategoriesValidator = null;
    VariableNotEmptyCollectionValidator tabMeasuresValidator = null;

    @UiTemplate("ChartSettingsView.ui.xml")
    interface SpecificUiBinder extends UiBinder<VisualizationSettingsModal, ChartSettingsPresenter> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public ChartSettingsPresenter(SettingsActionCallback<DrillChartViewDef> settingsActionCallback) {
        super(settingsActionCallback);
    }

    @Override
    protected void bindUI() {
        uiBinder.createAndBindUi(this);

        tabSort.setCategoriesTab(tabCategories);
        tabSort.setMeasuresTab(tabMeasures);
        tabFilter.setMeasuresTab(tabMeasures);

        tabFilterContainer.addClickHandler(event -> tabFilter.updateHeaders());

        setupCategoryMeasureSync();

    }

    private void setupCategoryMeasureSync() {
        tabCategories.setMeasureTab(tabMeasures);
//        tabMeasures.setCategoryTab(tabCategories);

        measuresNav.addClickHandler((e) -> tabMeasures.setCategoryList(tabCategories.getCurrentCategories()));

    }

    @Override
    protected DrillChartViewDef createNewVisualizationDef() {
        DrillChartViewDef def = new DrillChartViewDef();
        def.setBroadcastListener(WebMain.getClientStartupInfo().isListeningByDefault());
        def.setChartSettings(new ChartSettings());
        String name = UniqueNameUtil.getDistinctName(UniqueNameUtil.getVisualizationNames(dataViewPresenter), i18n.chartSettingsPresenter_name());
        def.setName(name);
        def.setHideOverview(WebMain.getClientStartupInfo().isChartHideOverviewByDefault());
        return def;
    }

    @Override
    protected void saveVisualizationToServer() {
        // Clear out DrillSelection and the Selection, as well as quick sort def

        getVisualizationDef().setDrillSelection(new DrillCategory());
        getVisualizationDef().getChartSettings().setQuickSortDef(new ArrayList<>());
//        getVisualization().

        VortexFuture<Void> future = getVisualization().saveSettings(false);
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                vizSettings.hide();
                settingsActionCallback.onSaveComplete(getVisualizationDef(), vizSettings.isSuppressLoadOnSave());
            }
        });
    }

    protected void preCheck() {

        tabCategoriesValidator.replaceCollection(tabCategories.getCurrentCategories());
        tabMeasuresValidator.replaceCollection(tabMeasures.getCurrentMeasures());
    }

    @Override
    protected void initiateValidator() {
        NotBlankValidator notBlankValidator = new NotBlankValidator(generalTab.chartName);
        VisualizationUniqueNameValidator visualizationUniqueNameValidator = new VisualizationUniqueNameValidator(
                getDataViewDef().getModelDef().getVisualizations(), generalTab.chartName, getVisualizationDef().getUuid());
        Validator tabFilterValidator = () -> tabFilter.isCriteriaValid();
        Validator percentThresholdValidator = new NumberValidator(tabAdvanced.getPiechartLabelThreshold());
        NotBlankValidator percentThresholdNotBlankValidator = new NotBlankValidator(tabAdvanced.getPiechartLabelThreshold());

        Validator chartAggregateEmpty = () -> {
            tabMeasures.updateModelWithView();
            for (MeasureDefinition m : getVisualizationDef().getChartSettings().getMeasureDefinitions()) {
                if (m.getAggregateFunction() == null) {
                    return false;
                }
            }
            return true;
        };
        Validator chartTypeWrong = () -> {
            tabMeasures.updateModelWithView();
            return true;
        };

        Validator chartCategoryType = () -> {
            tabCategories.updateModelWithView();
            for (CategoryDefinition categoryDefinition : getVisualizationDef().getChartSettings().getCategoryDefinitions()) {
                if (categoryDefinition.getChartType() == null) {
                    return false;
                }
            }
            return true;
        };
        tabCategoriesValidator = new VariableNotEmptyCollectionValidator(tabCategories.getCurrentCategories());
        tabMeasuresValidator = new VariableNotEmptyCollectionValidator(tabMeasures.getCurrentMeasures()) {
            @Override
            public boolean isValid() {
                if (tabMeasures.isMeasureTypeFieldSelected()) {
                    return super.isValid();
                }
                return true;
            }
        };


        ValidationFeedback tabCategoriesFeedback = new StringValidationFeedback(i18n.chartSettingsPresenter_tabCategoriesFeedback());
        ValidationFeedback categoriesType = new StringValidationFeedback(i18n.chartSettingsPresenter_incorrectChartType());
        ValidationFeedback fixAggregateFunction = new StringValidationFeedback(i18n.chartSettingsPresenter_incorrectAggregateFunction());
        ValidationFeedback measuresFeedback = new StringValidationFeedback(i18n.chartSettingsPresenter_measuresFeedback());
        ValidationFeedback percentThresholdFeedback = new StringValidationFeedback(i18n.chartSettingsPresenterNumberError());
        ValidationFeedback tabFilterFeedback = new StringValidationFeedback(i18n.chartSettingsPresenter_filterFeedback());

        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(percentThresholdNotBlankValidator, percentThresholdFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(chartCategoryType, categoriesType));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(percentThresholdValidator, percentThresholdFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(tabCategoriesValidator, tabCategoriesFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(chartAggregateEmpty, fixAggregateFunction));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(chartTypeWrong, fixAggregateFunction));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(tabMeasuresValidator, measuresFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator, StringValidationFeedback.getEmptyVisualizationFeedback()));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(visualizationUniqueNameValidator, StringValidationFeedback.getDuplicateVisualizationFeedback()));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(tabFilterValidator, tabFilterFeedback));
    }

}
