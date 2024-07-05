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
package csi.client.gwt.viz.chart.settings;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.ColumnWidthChangeEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.settings.VisualizationSettings;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.gxt.form.ComboBoxFactory;
import csi.client.gwt.widget.gxt.form.SelectingComboBoxCell;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.ui.form.ColorPickerCell;
import csi.client.gwt.widget.ui.form.DragCell;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.LabelDefinition;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.util.sql.api.AggregateFunction;
import csi.shared.core.visualization.chart.ChartType;
import csi.shared.core.visualization.chart.MeasureChartType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartMeasuresTab extends ChartSettingsComposite {
    public static final String ONLY_ONE_PIE_DONUT_MEASURE_IS_ALLOWED = "Only one Pie/Donut measure is allowed";
    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    @UiField(provided = true)
    FieldDefComboBox fieldList;
    @UiField
    GridContainer gridContainer;
    @UiField
    Button buttonAddCategory, buttonDeleteCategory;

    @UiField
    CheckBox alignAxisCheckBox;
    @UiField
    InlineLabel selectLabel;
    private List<CategoryDefinition> categoryList;
    private Grid<MeasureDefinition> grid;
    private ColumnModel<MeasureDefinition> columnModel;
    private FieldDef tempFieldDefForEditor;
    private AggregateFunctionComboCell aggregateCombo;
    private LabelDefinitionCell labelCell;
    private SelectingComboBoxCell<MeasureChartType> cell;

    interface MeasureDefinitionPropertyAccess extends PropertyAccess<MeasureDefinition> {

        @Path("uuid")
        ModelKeyProvider<MeasureDefinition> key();

        @Path("fieldDef")
        ValueProvider<MeasureDefinition, FieldDef> field();

        ValueProvider<MeasureDefinition, AggregateFunction> aggregateFunction();

        ValueProvider<MeasureDefinition, String> color();

        ValueProvider<MeasureDefinition, LabelDefinition> labelDefinition();

        ValueProvider<MeasureDefinition, MeasureChartType> measureChartType();
    }

    private static MeasureDefinitionPropertyAccess propertyAccess = GWT.create(MeasureDefinitionPropertyAccess.class);

    interface SpecificUiBinder extends UiBinder<Widget, ChartMeasuresTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private List<MeasureDefinition> prevMeasureDefs = null;

    public ChartMeasuresTab() {
        super();
        fieldList = new FieldDefComboBox();
        fieldList.setAllowMultiselect(true);

        initWidget(uiBinder.createAndBindUi(this));
        initGrid(null);
        selectLabel.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);
        fieldList.getElement().getStyle().setMarginLeft(4, Unit.PX);
        alignAxisCheckBox.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.BASELINE);
        fieldList.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);
        buttonDeleteCategory.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);
        buttonAddCategory.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);
        alignAxisCheckBox.setInline(true);
        alignAxisCheckBox.getElement().getStyle().setMarginRight(10, Unit.PX);
        buttonDeleteCategory.getElement().getStyle().setFloat(Style.Float.RIGHT);
        buttonDeleteCategory.getElement().getStyle().setMarginTop(3, Unit.PX);
        styleButton();


        fieldList.setTypeAhead(true);
        fieldList.addSelectionHandler(event -> {
            addNew();
            fieldList.incrementSelected();
        });
    }

    /**
     * Customize error message for incorrect measure selection here.
     */
    private void showOnlyOneAllowed() {
        new InfoDialog(ONLY_ONE_PIE_DONUT_MEASURE_IS_ALLOWED).show();
    }

    private void addNew() {
        grid.getStore().commitChanges();
        /*if (grid.getStore().size() >= 1 && grid.getStore().getAll().stream().anyMatch(measureDefinition ->
                measureDefinition.getMeasureChartType().equals(MeasureChartType.PIE) || measureDefinition.getMeasureChartType().equals(MeasureChartType.DONUT))) {
            showOnlyOneAllowed();
        } else if(grid.getStore().size() >= 1 && grid.getStore().get(0).getMeasureChartType().equals(MeasureChartType.DEFAULT) && (categoryList != null && isTypeOrType(categoryList, ChartType.PIE, ChartType.DONUT))){
            // also dont add?
            showOnlyOneAllowed();
        }else {*/
        MeasureDefinition definition = new MeasureDefinition();
        definition.setFieldDef(fieldList.getCurrentValue());
        definition.setAllowNulls(true);
        definition.setAggregateFunction(AggregateFunction.COUNT);
        definition.setColor("#124356"); //$NON-NLS-1$
        grid.getStore().add(definition);
//        }
    }


    @UiHandler("buttonAddCategory")
    public void handleAddCategory(ClickEvent event) {
        addNew();
        int currentIndex = fieldList.getSelectedIndex();
        fieldList.setSelectedIndex((currentIndex + 1) % fieldList.getItemCount());
    }

    @SuppressWarnings("Duplicates")
    private void styleButton() {
        {
//                cg.getElement().getStyle().setMarginBottom(0,Unit.PX);
        }

        buttonAddCategory.setIcon(IconType.CIRCLE_ARROW_DOWN);
        buttonAddCategory.setType(ButtonType.LINK);
        buttonAddCategory.setSize(ButtonSize.LARGE);
        Style buttonStyle = buttonAddCategory.getElement().getStyle();
        buttonStyle.setFontSize(26.0D, Unit.PX);
        buttonStyle.setTextDecoration(Style.TextDecoration.NONE);
//        buttonStyle.setPaddingLeft(4, Unit.PX);
        buttonStyle.setPaddingTop(0, Unit.PX);
        buttonStyle.setMarginRight(30, Unit.PX);
        buttonStyle.setMarginBottom(0, Unit.PX);
        buttonStyle.setMarginTop(0, Unit.PX);
    }


    @SuppressWarnings("unchecked")
    public void initGrid(ListStore<MeasureDefinition> store) {
        final GridComponentManager<MeasureDefinition> manager = WebMain.injector.getGridFactory().create(propertyAccess.key());

        ColumnConfig<MeasureDefinition, FieldDef> dragCol = manager.create(propertyAccess.field(), 20, _constants.chartMeasuresTab_dragCol(), false, true);
        ColumnConfig<MeasureDefinition, FieldDef> nameCol = manager.create(propertyAccess.field(), 138, _constants.chartMeasuresTab_nameCol(), false, true);
        ColumnConfig<MeasureDefinition, AggregateFunction> measureType = manager.create(propertyAccess.aggregateFunction(), 160, _constants.chartMeasuresTab_measureType(), false, true);
        ColumnConfig<MeasureDefinition, String> colorCol = manager.create(propertyAccess.color(), 70, _constants.chartMeasuresTab_colorCol(), false, true);
        ColumnConfig<MeasureDefinition, MeasureChartType> chartType = manager.create(propertyAccess.measureChartType(), 138, "Line Type", false, true);//FIXME: static string
        ColumnConfig<MeasureDefinition, LabelDefinition> displayAsCol = manager.create(propertyAccess.labelDefinition(), 205, _constants.chartMeasuresTab_displayAsCol(), false, true);

        dragCol.setCell(DragCell.create());
        dragCol.setResizable(false);
        dragCol.setFixed(true);

        nameCol.setCell(new FieldDefNameCell());

        aggregateCombo = new AggregateFunctionComboCell(manager.getStore());
        aggregateCombo.addSelectionHandler(event -> {
            getDrillChartSettings().setUseCountStarForMeasure(grid.getStore().size() == 0);
            updateModelWithView();
        });
        measureType.setCell(aggregateCombo);

        colorCol.setCell(new ColorPickerCell());


        MeasureChartType[] allowedValues = getAllowedChartMeasures();

        cell = ComboBoxFactory.typedEnumCellFromWithWidth(allowedValues, new MeasureChartLabelProvider(), 100);

        // This is another vital piece of logic - basically we can apply rules and not select the item clicked for this cell in the model.
        // might have to change stuff here for complete feature...
        /*        cell.addBeforeSelectionHandler(event -> {
         *//*
            if (grid.getStore().size() != 1
                    && (event.getItem().equals(MeasureChartType.PIE) || event.getItem().equals(MeasureChartType.DONUT)) ||
                    categoryList != null && isTypeOrType(categoryList, ChartType.PIE, ChartType.DONUT) && event.getItem().equals(MeasureChartType.DEFAULT)) {
                grid.getStore().rejectChanges();
                grid.getStore().commitChanges();
                grid.getView().refresh(false);
                event.cancel();

                showOnlyOneAllowed();
            }*//*
        });*/
        chartType.setCell(cell);

        labelCell = new LabelDefinitionCell(key -> manager.getStore().findModelWithKey(key.toString()).getLabelDefinition());
        labelCell.setWidth(160);
        displayAsCol.setCell(labelCell);

        columnModel = new ColumnModel<>(manager.getColumnConfigList());

        grid = new ResizeableGrid<>(manager.getStore(), columnModel);
        if (store != null) {
            grid.getStore().addAll(store.getAll());
        }
        grid.getColumnModel().addColumnWidthChangeHandler(new ColumnWidthChangeEvent.ColumnWidthChangeHandler() {
            @Override
            public void onColumnWidthChange(ColumnWidthChangeEvent event) {
                Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                    @Override
                    public boolean execute() {
                        aggregateCombo.setWidth(grid.getColumnModel().getColumnWidth(2));
                        cell.setWidth(grid.getColumnModel().getColumnWidth(4));
                        labelCell.setWidth(grid.getColumnModel().getColumnWidth(5)-25);
                        grid.getView().refresh(true);
                        return false;
                    }
                }, 50);
            }
        });
        grid.getView().setAutoExpandColumn(nameCol);

        GridHelper.setDraggableRowsDefaults(grid);


        gridContainer.setGrid(grid);
    }

    /**
     * We will recreate out column config on each open of this tab of settings.
     *
     * If there are more rules that need to be implemented limiting which category types go to what measure types,
     * it should be fairly easy to do that here...
     *
     * @return
     */
    private MeasureChartType[] getAllowedChartMeasures() {
        List<MeasureChartType> types = Lists.newArrayList(MeasureChartType.values());


        Stream<MeasureChartType> measureChartTypeStream = types.stream().filter(measureChartType -> {
            switch (measureChartType) {
                case COLUMN:
                case AREA:
                case AREA_SPLINE:
                case LINE:
                    return true;
                case PIE:
                case DEFAULT:
                case DONUT:
                    return false;
            }
            return false;

            /*if (categoryList == null) return true;
            if (isPieOrSpider(categoryList)) {
                return getValidForSpider().contains(measureChartType);
//            } else if (isTypeOrType(categoryList, ChartType.PIE, ChartType.DONUT)) {
//                return getValidForSpider().contains(measureChartType);
            }else if(isType(categoryList, ChartType.BAR)){
                return !measureChartType.equals(MeasureChartType.COLUMN);

            } else{
                return measureChartType.equals(measureChartType);
            }*/
        });

        return measureChartTypeStream.toArray(MeasureChartType[]::new);
    }

    private boolean isTypeOrType(List<CategoryDefinition> categoryList, ChartType pie, ChartType donut) {
        return categoryList.stream().anyMatch(categoryDefinition -> categoryDefinition.getChartType().equals(pie) || categoryDefinition.getChartType().equals(donut));
    }

    private boolean isType(List<CategoryDefinition> categoryList, ChartType type) {
        return categoryList.stream().anyMatch(categoryDefinition -> categoryDefinition.getChartType().equals(type));
    }

    private boolean isPieOrSpider(List<CategoryDefinition> categoryList) {
        return isTypeOrType(categoryList, ChartType.SPIDER, ChartType.POLAR);
    }

    private List<MeasureChartType> getValidForSpider() {
        ArrayList<MeasureChartType> types = new ArrayList<>();

        types.add(MeasureChartType.DEFAULT);
        types.add(MeasureChartType.AREA);
        types.add(MeasureChartType.AREA_SPLINE);
        types.add(MeasureChartType.COLUMN);
        types.add(MeasureChartType.LINE);

        return types;
    }

    public void setCategoryList(List<CategoryDefinition> defs) {
        if (defs != this.categoryList) {
            this.categoryList = defs;
            initGrid(grid.getStore());
        }
    }

    public void resetToDefault() {
        prevMeasureDefs = grid.getStore().getAll().stream().collect(Collectors.toList());
        this.grid.getStore().clear();
        this.grid.getStore().commitChanges();
    }

    private void deleteGrid() {
        gridContainer.remove(grid);
        grid = null;

    }

    @Override
    public void updateViewFromModel() {
        fieldList.getStore().addAll(Lists.newArrayList(getVisualizationSettings().getDataViewDefinition().getModelDef().getFieldDefs()));
        fieldList.setSelectedIndex(0);

        for (MeasureDefinition md : getDrillChartSettings().getMeasureDefinitions()) {
            grid.getStore().add(md);
        }


        handleMeasureTypeFieldSelected();
        alignAxisCheckBox.setValue(getDrillChartSettings().isAlignAxes());
    }

    @Override
    public void updateModelWithView() {
        grid.getStore().commitChanges();

        getDrillChartSettings().getMeasureDefinitions().clear();

        int i = 0;
        for (MeasureDefinition md : grid.getStore().getAll()) {
            md.setListPosition(i++);
            getDrillChartSettings().getMeasureDefinitions().add(md);
        }

        getDrillChartSettings().setUseCountStarForMeasure(grid.getStore().size() == 0);
        getDrillChartSettings().setAlignAxes(alignAxisCheckBox.getValue());
    }

    @UiHandler("buttonDeleteCategory")
    public void handleDeleteCategory(ClickEvent event) {
        List<MeasureDefinition> selected = grid.getSelectionModel().getSelection();
        for (MeasureDefinition md : selected) {
            grid.getStore().remove(md);
        }
    }

    @Override
    public void setVisualizationSettings(VisualizationSettings visualizationSettings) {
        super.setVisualizationSettings(visualizationSettings);
        createEditor();
    }

    private void createEditor() {
        final GridInlineEditing<MeasureDefinition> editing = new GridInlineEditing<MeasureDefinition>(grid);
        addFieldEditor(editing);
        editing.addStartEditHandler(event -> {
            MeasureDefinition measureDefinition = grid.getStore().get(editing.getActiveCell().getRow());
            tempFieldDefForEditor = measureDefinition.getFieldDef();
        });

        editing.addCompleteEditHandler(event -> {
            grid.getStore().commitChanges();
            MeasureDefinition measureDefinition = grid.getStore().get(editing.getActiveCell().getRow());
            FieldDef fieldDef = measureDefinition.getFieldDef();
            MeasureChartType measureChartType = measureDefinition.getMeasureChartType();

            if (fieldDef == null) {
                measureDefinition.setFieldDef(tempFieldDefForEditor);
                grid.getStore().commitChanges();
            }

            if (!measureDefinition.getAggregateFunction().isApplicableFor(measureDefinition.getFieldDef().getValueType())) {
                measureDefinition.setAggregateFunction(AggregateFunction.COUNT);
            }


            grid.getView().refresh(false);
        });
    }

    private void addFieldEditor(GridInlineEditing<MeasureDefinition> editing) {
        ColumnConfig config = columnModel.getColumn(1);
        FieldDefComboBox combo = new FieldDefComboBox();
        combo.removeDefaultStyle();
        combo.getElement().getStyle().setPaddingTop(5, Unit.PX);
        combo.getStore().addAll(Lists.newArrayList(getVisualizationSettings().getDataViewDefinition().getModelDef().getFieldDefs()));
        combo.setAllowBlank(false);
        editing.addEditor(config, combo);
    }

    public List<MeasureDefinition> getCurrentMeasures() {
        return grid.getStore().getAll();
    }

    public boolean isMeasureTypeFieldSelected() {
        return grid.getStore().size() != 0;
    }

    private void handleMeasureTypeFieldSelected() {
        grid.setEnabled(true);
        fieldList.setEnabled(true);
        buttonAddCategory.setEnabled(true);
        buttonDeleteCategory.setEnabled(true);
    }

    public class MeasureChartLabelProvider implements LabelProvider<MeasureChartType> {

        @Override
        public String getLabel(MeasureChartType item) {
            String label = ""; //$NON-NLS-1$
            switch (item) {
                case AREA:
                    label = _constants.chartMeasuresTypeArea(); //$NON-NLS-1$
                    break;
                case AREA_SPLINE:
                    label = _constants.chartMeasuresTypeAreaSpline(); //$NON-NLS-1$
                    break;
                case COLUMN:
                    label = _constants.chartTypeBar(); //$NON-NLS-1$
                    break;
                case DEFAULT:
                    label = _constants.chartMeasuresTypeDefault(); //$NON-NLS-1$
                    break;
                case DONUT:
                    label = _constants.chartMeasuresTypeDonut(); //$NON-NLS-1$
                    break;
                case LINE:
                    label = _constants.chartMeasuresTypeLIne(); //$NON-NLS-1$
                    break;
                case PIE:
                    label = _constants.chartMeasuresTypePie(); //$NON-NLS-1$
                    break;
            }
            return label;
        }

    }

}
