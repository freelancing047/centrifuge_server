/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.viz.matrix.settings;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.util.FieldDefUtils.SortOrder;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.gxt.form.ComboBoxFactory;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.chart.LabelDefinition;
import csi.server.common.model.visualization.matrix.MatrixMeasureDefinition;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.util.sql.api.AggregateFunction;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MatrixMeasuresTab extends MatrixSettingsComposite {

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    @UiField
    FieldDefComboBox fieldList;
    @UiField(provided = true)
    ComboBox<AggregateFunction> aggregateFunction;
    @UiField
    TextBox scaleMin, scaleMax;
    @UiField
    TextBox displayAs;
    @UiField
    RadioButton measureTypeCount, measureTypeField;

    public MatrixMeasuresTab() {
        super();
        preInit();
        initWidget(uiBinder.createAndBindUi(this));
        postInit();
    }

    private void preInit() {
        aggregateFunction = ComboBoxFactory.typedEnumFrom(AggregateFunction.values(), new AggregateFunctionLabelProvider());
        aggregateFunction.addStyleName("string-combo-style");
        aggregateFunction.setWidth(230);
    }

    private void postInit() {
        fieldList.addSelectionHandler(event -> {
            FieldDef field = event.getSelectedItem();
            filterFunctionsByField(field);
        });
        aggregateFunction.addSelectionHandler(event -> {
           AggregateFunction aggregateFunction = event.getSelectedItem();
           updateModelWithView();
        });
        measureTypeCount.addClickHandler(event -> setMeasureFieldState(false));
        measureTypeField.addClickHandler(event -> setMeasureFieldState(true));


        fieldList.setAllowBlank(false);
        aggregateFunction.setAllowBlank(false);

    }

    private void filterFunctionsByField(FieldDef field) {
        CsiDataType dataType = field.getValueType();
        final Set<AggregateFunction> allowed = new HashSet<>(AggregateFunction.forType(dataType));
        aggregateFunction.getStore().removeFilters();
        aggregateFunction.getStore().addFilter((store, parent, item) -> allowed.contains(item));
        if (!allowed.contains(aggregateFunction.getCurrentValue())) {
            aggregateFunction.setValue(allowed.iterator().next());
        }
    }

    private void setMeasureFieldState(boolean enable) {
        fieldList.setEnabled(enable);
        aggregateFunction.setEnabled(enable);
        scaleMin.setEnabled(enable);
        scaleMax.setEnabled(enable);
        displayAs.setEnabled(enable);
    }

    @Override
    public void updateViewFromModel() {
        fieldList.getStore().addAll(
                FieldDefUtils.getSortedNonStaticFields(getVisualizationSettings().getDataViewDefinition().getModelDef(),
                        SortOrder.ALPHABETIC));
        fieldList.setSelectedIndex(0);

        MatrixViewDef def = getVisualizationSettings().getVisualizationDefinition();
        MatrixSettings settings = def.getMatrixSettings();
        MatrixMeasureDefinition mmd = settings.getMatrixMeasureDefinition();
        if (mmd != null) {
            fieldList.setValue(mmd.getFieldDef());
            aggregateFunction.setValue(mmd.getAggregateFunction());
            scaleMin.setValue(Integer.toString(mmd.getMeasureScaleMin()));
            scaleMax.setValue(Integer.toString(mmd.getMeasureScaleMax()));
            displayAs.setValue(mmd.getLabelDefinition().getStaticLabel());
        } else {
            aggregateFunction.setValue(AggregateFunction.COUNT);
            scaleMin.setValue("5"); //$NON-NLS-1$
            scaleMax.setValue("100"); //$NON-NLS-1$
            displayAs.setValue(""); //$NON-NLS-1$
        }
        measureTypeCount.setValue(settings.isUseCountForMeasure());
        measureTypeField.setValue(!settings.isUseCountForMeasure());
        setMeasureFieldState(!settings.isUseCountForMeasure());

        filterFunctionsByField(fieldList.getCurrentValue());
    }

    @Override
    public void updateModelWithView() {
        MatrixViewDef def = getVisualizationSettings().getVisualizationDefinition();
        MatrixSettings settings = def.getMatrixSettings();
        settings.setMatrixMeasureDefinition(getCurrentMeasure(settings));
        settings.setUseCountForMeasure(measureTypeCount.getValue());
    }

    MatrixMeasureDefinition getCurrentMeasure(MatrixSettings settings) {
        MatrixMeasureDefinition mmd;
        if (settings.getMatrixMeasureDefinition() == null) {
            mmd = new MatrixMeasureDefinition();
        } else {
            mmd = settings.getMatrixMeasureDefinition();
        }
        mmd.setAggregateFunction(aggregateFunction.getCurrentValue());
        FieldDef selected = fieldList.getValue();
        mmd.setFieldDef(selected);
        mmd.setLabelDefinition(new LabelDefinition().setStaticLabel(displayAs.getValue()));
        mmd.setMeasureScaleMax(Integer.parseInt(scaleMax.getValue()));
        mmd.setMeasureScaleMin(Integer.parseInt(scaleMin.getValue()));
        return mmd;
    }

    boolean isUseCountForMeasure() {
        return measureTypeCount.getValue();
    }

    boolean isFieldValid() {
        return fieldList.isValid();
    }

    boolean isAggregateValid() {
        return aggregateFunction.isValid();
    }

    interface SpecificUiBinder extends UiBinder<Widget, MatrixMeasuresTab> {
    }

    public static final class AggregateFunctionLabelProvider implements LabelProvider<AggregateFunction> {

        @Override
        public String getLabel(AggregateFunction item) {
            String label = ""; //$NON-NLS-1$
            switch (item) {
                case COUNT: label = i18n.matrixMeasuresTabCount(); //$NON-NLS-1$
                break;
                case STD_DEV: label = i18n.matrixMeasuresTabStdDev(); //$NON-NLS-1$
                break;
                case VARIANCE: label = i18n.matrixMeasuresTabVariance(); //$NON-NLS-1$
                break;
                case MINIMUM: label = i18n.matrixMeasuresTabMin(); //$NON-NLS-1$
                break;
                case MAXIMUM: label = i18n.matrixMeasuresTabMax(); //$NON-NLS-1$
                break;
                case SUM: label = i18n.matrixMeasuresTabSum(); //$NON-NLS-1$
                break;
                case AVERAGE: label = i18n.matrixMeasuresTabAverage(); //$NON-NLS-1$
                break;
                case COUNT_DISTINCT: label = i18n.matrixMeasuresTabCountDistinct(); //$NON-NLS-1$
                break;
                case UNITY: label = i18n.matrixMeasuresTabUnity(); //$NON-NLS-1$
                break;
                case MEDIAN: label = i18n.matrixMeasuresTabMedian(); //$NON-NLS-1$
                break;
            }
            return label;
        }

    }

}
