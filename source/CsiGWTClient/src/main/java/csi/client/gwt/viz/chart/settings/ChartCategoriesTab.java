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
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.ColumnWidthChangeEvent;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
import com.sencha.gxt.widget.core.client.event.ReconfigureEvent;
import com.sencha.gxt.widget.core.client.event.StartEditEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.settings.VisualizationSettings;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.gxt.form.ComboBoxFactory;
import csi.client.gwt.widget.gxt.form.SelectingComboBoxCell;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.ui.bundle.BundleFunctionEditCell;
import csi.client.gwt.widget.ui.form.DragCell;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.BundleFunctionParameter;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.LabelDefinition;
import csi.server.util.sql.api.BundleFunction;
import csi.shared.core.visualization.chart.ChartType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartCategoriesTab extends ChartSettingsComposite {

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private static CategoryDefinitionPropertyAccess propertyAccess = GWT.create(CategoryDefinitionPropertyAccess.class);

    ChartMeasuresTab measuresTab;
    private LabelDefinitionCell labelCell;
    private SelectingComboBoxCell<ChartType> combo;
    private BundleFunctionEditCell<CategoryDefinition> bundleCell;

    public void setMeasureTab(ChartMeasuresTab tabMeasures) {
        this.measuresTab = tabMeasures;
    }

    public Grid<CategoryDefinition> getGrid() {
        return grid;
    }

    interface SpecificUiBinder extends UiBinder<Widget, ChartCategoriesTab> {
    }

    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private Grid<CategoryDefinition> grid;

    private ColumnModel<CategoryDefinition> columnModel;
    @UiField(provided = true)
    FieldDefComboBox fieldList;

    @UiField
    GridContainer gridContainer;

    @UiField
    Button buttonAddCategory;

    private FieldDef tempFieldDefForEditor;

    private static final Comparator<? super FieldDef> COMPARE_BY_ORDINAL = new Comparator<FieldDef>() {
        @Override
        public int compare(FieldDef o1, FieldDef o2) {
            return Integer.valueOf(o1.getOrdinal()).compareTo(o2.getOrdinal());
        }
    };

    interface CategoryDefinitionPropertyAccess extends PropertyAccess<CategoryDefinition> {
        @Path("uuid")
        public ModelKeyProvider<CategoryDefinition> key();

        @Path("fieldDef")
        public ValueProvider<CategoryDefinition, FieldDef> field();

        public ValueProvider<CategoryDefinition, ChartType> chartType();

        public ValueProvider<CategoryDefinition, BundleFunction> bundleFunction();

        public ValueProvider<CategoryDefinition, LabelDefinition> labelDefinition();

        public ValueProvider<CategoryDefinition, Boolean> allowNulls();

        public ValueProvider<CategoryDefinition, List<BundleFunctionParameter>> bundleFunctionParameters();
    }


    public ChartCategoriesTab() {
        super();
        fieldList = new FieldDefComboBox();
        initWidget(uiBinder.createAndBindUi(this));
        initGrid();
        initAddButton();
        fieldList.setAllowMultiselect(true);
    }

    private void initAddButton() {
        buttonAddCategory.setIcon(IconType.CIRCLE_ARROW_DOWN);
        buttonAddCategory.setType(ButtonType.LINK);
        buttonAddCategory.setSize(ButtonSize.LARGE);
        Style buttonStyle = buttonAddCategory.getElement().getStyle();
        buttonStyle.setFontSize(26.0D, Unit.PX);
        buttonStyle.setTextDecoration(Style.TextDecoration.NONE);
        buttonStyle.setPaddingLeft(0, Unit.PX);
        buttonStyle.setPaddingTop(0, Unit.PX);
        buttonStyle.setMarginBottom(0, Unit.PX);
        buttonStyle.setMarginTop(0, Unit.PX);


        fieldList.addSelectionHandler(new SelectionHandler<FieldDef>() {
            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                FieldDef newCategory = event.getSelectedItem();
                if (newCategory != null) {
                    addCategoty(fieldList.getCurrentValue(), true);
                }
            }
        });

    }

    private ChartType preSelectionValue = null;

    @SuppressWarnings("unchecked")
    private void initGrid() {
        final GridComponentManager<CategoryDefinition> manager = WebMain.injector.getGridFactory().create(propertyAccess.key());

        ColumnConfig<CategoryDefinition, FieldDef> dragCol = manager.create(propertyAccess.field(), 20, _constants.chartCategoriesTab_dragCol(), false, true);
        ColumnConfig<CategoryDefinition, FieldDef> nameCol = manager.create(propertyAccess.field(), 150, _constants.chartCategoriesTab_nameCol(), false, true);
        ColumnConfig<CategoryDefinition, BundleFunction> bundle = manager.create(propertyAccess.bundleFunction(), 130, _constants.chartCategoriesTab_bundle(), false, true);
        ColumnConfig<CategoryDefinition, ChartType> chartType = manager.create(propertyAccess.chartType(), 110, _constants.chartCategoriesTab_chartType(), false, true);
        ColumnConfig<CategoryDefinition, LabelDefinition> displayAsCol = manager.create(propertyAccess.labelDefinition(), 150, _constants.chartCategoriesTab_displayAsCol(), false, true);
        ColumnConfig<CategoryDefinition, Boolean> allowNulls = manager.create(propertyAccess.allowNulls(), 40, _constants.chartCategoriesTab_allowNulls(), false, true);

        {
            allowNulls.setFixed(true);
            dragCol.setFixed(true);
        }

        dragCol.setCell(DragCell.<FieldDef>create());
        dragCol.setResizable(false);

        nameCol.setCell(new FieldDefNameCell());
        bundleCell = new BundleFunctionEditCell<>(manager.getStore(), propertyAccess.bundleFunctionParameters(), null);
        bundleCell.setWidth(125);
        bundle.setCell(bundleCell);

        ChartTypeLabelProvider chartLabels = new ChartTypeLabelProvider();
        ChartType[] values = ChartType.values();
        values = Arrays.stream(values).filter(chartType1 -> {
            switch (chartType1) {
                case COLUMN:
                case BAR:
                case DONUT:
                case POLAR:
                case SPIDER:
                case PIE:
                    return true;
                case LINE:
                case AREA:
                case AREA_SPLINE:
                    return false;
            }
            return false;
        }).toArray(ChartType[]::new);
        combo = ComboBoxFactory.typedEnumCellFrom(values, chartLabels);
        combo.setWidth(110);

        combo.addSelectionHandler(event -> {
            if (preSelectionValue != null && measuresTab.getCurrentMeasures().size() > 0 && !getDrillChartSettings().isUseCountStarForMeasure()) {
                List<ButtonDef> myButtonList = Arrays.asList(new ButtonDef("Okay"));
                List<ChartType> oldNew = new ArrayList<>();
                oldNew.add(preSelectionValue);
                oldNew.add(event.getSelectedItem());

                //"[" + preSelectionValue + " -> " + event.getSelectedItem() + " ]" +
                /*//TODO i18n
                new DecisionDialog("Warning", "Measures will be reset. \n Press cancel to keep previous category type. ", myButtonList, new ChoiceMadeEventHandler() {
                    @Override
                    public void onChoiceMade(ChoiceMadeEvent eventIn) {
                        List<ChartType> data = (List<ChartType>) eventIn.getData();
                        if (eventIn.getChoice() == 0) {
                            grid.getStore().getModifiedRecords().iterator().forEachRemaining(record -> record.revert());
                        } else {
                            measuresTab.resetToDefault();
                        }
                        preSelectionValue = null;
                    }
                }, oldNew, 80).show();*/
            }

        });

        combo.getListView().getSelectionModel().addBeforeSelectionHandler(event -> {
            if (preSelectionValue == null) {
                preSelectionValue = event.getItem();
            }
        });


        combo.setWidth(100);
        chartType.setCell(combo);


        labelCell = new LabelDefinitionCell(key -> manager.getStore().findModelWithKey(key.toString()).getLabelDefinition());
        labelCell.setWidth(150);
        displayAsCol.setCell(labelCell);
        allowNulls.setColumnStyle(SafeStylesUtils.forTextAlign(TextAlign.CENTER));
        allowNulls.setCell(new CheckboxCell());

        List<ColumnConfig<CategoryDefinition, ?>> columns = manager.getColumnConfigList();
        columnModel = new ColumnModel<CategoryDefinition>(columns);

        ListStore<CategoryDefinition> gridStore = manager.getStore();

        grid = new ResizeableGrid<CategoryDefinition>(gridStore, columnModel);


        grid.getColumnModel().addColumnWidthChangeHandler(new ColumnWidthChangeEvent.ColumnWidthChangeHandler() {
            @Override
            public void onColumnWidthChange(ColumnWidthChangeEvent event) {
                Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                    @Override
                    public boolean execute() {
                        bundleCell.setWidth(grid.getColumnModel().getColumnWidth(2)-25);
                        combo.setWidth((grid.getColumnModel().getColumnWidth(3)));
                        labelCell.setWidth(grid.getColumnModel().getColumnWidth(4)-25);
                        grid.getView().refresh(true);
                        return false;
                    }
                },50);

            }
        });
        grid.getView().setAutoExpandColumn(nameCol);
        GridHelper.setDraggableRowsDefaults(grid);

        gridContainer.setGrid(grid);
    }

    private void doSelectionCheck() {
    }

    private void createEditor() {
        final GridInlineEditing<CategoryDefinition> editing = new GridInlineEditing<CategoryDefinition>(grid);
        addFieldEditor(editing);
        editing.addStartEditHandler(new StartEditEvent.StartEditHandler<CategoryDefinition>() {
            @Override
            public void onStartEdit(StartEditEvent<CategoryDefinition> event) {
                CategoryDefinition categoryDefinition = grid.getStore().get(editing.getActiveCell().getRow());
                tempFieldDefForEditor = categoryDefinition.getFieldDef();
            }
        });
        editing.addCompleteEditHandler(new CompleteEditEvent.CompleteEditHandler<CategoryDefinition>() {
            @Override
            public void onCompleteEdit(CompleteEditEvent<CategoryDefinition> event) {
                grid.getStore().commitChanges();
                CategoryDefinition categoryDefinition = grid.getStore().get(editing.getActiveCell().getRow());
                FieldDef fieldDef = categoryDefinition.getFieldDef();
                if (fieldDef == null) {
                    categoryDefinition.setFieldDef(tempFieldDefForEditor);
                    grid.getStore().commitChanges();
                    fieldDef = tempFieldDefForEditor;
                }
                if (fieldDef != null) {
                    if (!categoryDefinition.getBundleFunction().isApplicableFor(fieldDef.getValueType())) {
                        categoryDefinition.setBundleFunction(BundleFunction.NONE);
                    }
                }
                grid.getView().refresh(false);
            }
        });

    }

    private void addFieldEditor(GridInlineEditing<CategoryDefinition> editing) {
        ColumnConfig config = columnModel.getColumn(1);
        final FieldDefComboBox combo = new FieldDefComboBox();
        combo.getElement().getStyle().setPaddingTop(5, Unit.PX);
        combo.removeDefaultStyle();
//
        ArrayList<FieldDef> fieldDefs = Lists.newArrayList(getVisualizationSettings().getDataViewDefinition().getModelDef().getFieldDefs());
//        java.util.Collections.sort(fieldDefs, COMPARE_BY_ORDINAL);

        combo.getStore().addAll(fieldDefs);
        combo.setAllowBlank(false);
        combo.setForceSelection(true);
        editing.addEditor(config, combo);
    }

    @Override
    public void updateViewFromModel() {

        //sort fields by ordinal
        ArrayList<FieldDef> fieldDefs = Lists.newArrayList(getVisualizationSettings().getDataViewDefinition().getModelDef().getFieldDefs());
//        java.util.Collections.sort(fieldDefs, COMPARE_BY_ORDINAL);

        fieldList.getStore().addAll(fieldDefs);
        fieldList.setSelectedIndex(0);

        for (CategoryDefinition cd : getDrillChartSettings().getCategoryDefinitions()) {
            grid.getStore().add(cd);
        }
    }

    @Override
    public void updateModelWithView() {
        grid.getStore().commitChanges();

        getDrillChartSettings().getCategoryDefinitions().clear();
        int i = 0;
        for (CategoryDefinition cd : grid.getStore().getAll()) {
            cd.setListPosition(i++);
            getDrillChartSettings().getCategoryDefinitions().add(cd);
        }
    }

    @Override
    public void setVisualizationSettings(VisualizationSettings visualizationSettings) {
        super.setVisualizationSettings(visualizationSettings);
        createEditor();
    }

    @UiHandler("buttonAddCategory")
    public void handleAddCategory(ClickEvent event) {
        if (fieldList.getCurrentValue() != null) {
            addCategoty(fieldList.getCurrentValue(), true);
        }
    }

    private void addCategoty(FieldDef categoryField, boolean autoIncrement) {
        CategoryDefinition definition = new CategoryDefinition();

        definition.setFieldDef(fieldList.getCurrentValue());
        definition.setChartType(ChartType.COLUMN);
        grid.getStore().add(definition);
        // Increment to next item.

        if (autoIncrement)
            fieldList.incrementSelected();
    }


    @UiHandler("buttonDeleteCategory")
    public void handleDeleteCategory(ClickEvent event) {
        List<CategoryDefinition> selected = grid.getSelectionModel().getSelection();
        for (CategoryDefinition cd : selected) {
            grid.getStore().remove(cd);
        }
    }

    public List<CategoryDefinition> getCurrentCategories() {
        //TODO figure out if we need to commit here, because what happens to edited items??
        grid.getStore().commitChanges();
        return grid.getStore().getAll();
    }
}
