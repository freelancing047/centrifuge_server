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
package csi.client.gwt.viz.matrix.settings;

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
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.ColumnWidthChangeEvent;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
import com.sencha.gxt.widget.core.client.event.StartEditEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.settings.LabelDefinitionCell;
import csi.client.gwt.viz.chart.settings.LabelDefinitionCell.LabelDefinitionRetriever;
import csi.client.gwt.viz.shared.settings.VisualizationSettings;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.gxt.form.ComboBoxFactory;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.ui.bundle.BundleFunctionEditCell;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.BundleFunctionParameter;
import csi.server.common.model.visualization.chart.LabelDefinition;
import csi.server.common.model.visualization.matrix.Axis;
import csi.server.common.model.visualization.matrix.MatrixCategoryDefinition;
import csi.server.util.sql.api.BundleFunction;
import csi.shared.core.util.Native;

import java.util.HashMap;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MatrixCategoriesTab extends MatrixSettingsComposite {

    private ComboBoxCell<Axis> combo;
    private LabelDefinitionCell labelCell;
    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private static CategoryDefinitionPropertyAccess propertyAccess = GWT.create(CategoryDefinitionPropertyAccess.class);
    @UiField
    FieldDefComboBox fieldList;
    @UiField
    GridContainer gridContainer;
    @UiField
    Button buttonAddCategory;
    @UiField
    Button btnToggleAxis;
    private Grid<MatrixCategoryDefinition> grid;
    private ColumnModel<MatrixCategoryDefinition> columnModel;
    private FieldDef tempEditfieldDef;
    private MatrixSortTab tabSort;
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private BundleFunctionEditCell<MatrixCategoryDefinition> bundleCell;

    public MatrixCategoriesTab() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
        initGrid();
        initAddButton();
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
        // might add the instant add callback here.
    }

    @SuppressWarnings("unchecked")
    private void initGrid() {
        final GridComponentManager<MatrixCategoryDefinition> manager = WebMain.injector.getGridFactory().create(propertyAccess.key());

        ColumnConfig<MatrixCategoryDefinition, FieldDef> nameCol = manager.create(propertyAccess.field(), 150, _constants.matrixCategoriesTab_field(), false, true);
        ColumnConfig<MatrixCategoryDefinition, BundleFunction> bundle = manager.create(propertyAccess.bundleFunction(), 130, _constants.matrixCategoriesTab_bundle(), false, true);
        ColumnConfig<MatrixCategoryDefinition, Axis> axisType = manager.create(propertyAccess.axis(), 110, _constants.matrixCategoriesTab_axis(), false, true);
        ColumnConfig<MatrixCategoryDefinition, LabelDefinition> displayAsCol = manager.create(propertyAccess.labelDefinition(), 150, _constants.matrixCategoriesTab_displayAs(), false, true);
        ColumnConfig<MatrixCategoryDefinition, Boolean> allowNulls = manager.create(propertyAccess.allowNulls(), 40, _constants.matrixCategoriesTab_nulls(), false, true);

        allowNulls.setFixed(true);
        allowNulls.setResizable(false);

        nameCol.setCell(new FieldDefNameCell());
        bundleCell = new BundleFunctionEditCell<>(
                manager.getStore(), propertyAccess.bundleFunctionParameters(), null);
        bundle.setCell(bundleCell);

        combo = ComboBoxFactory.typedEnumCellFrom(Axis.values(), new AxisLabelProvider());
        combo.setWidth(100);
        axisType.setCell(combo);

        labelCell = new LabelDefinitionCell(key -> manager.getStore().findModelWithKey(key.toString()).getLabelDefinition());
        labelCell.setWidth(150);
        displayAsCol.setCell(labelCell);
        allowNulls.setColumnStyle(SafeStylesUtils.forTextAlign(TextAlign.CENTER));
        allowNulls.setCell(new CheckboxCell());

        List<ColumnConfig<MatrixCategoryDefinition, ?>> columns = manager.getColumnConfigList();
        columnModel = new ColumnModel<MatrixCategoryDefinition>(columns);
        ListStore<MatrixCategoryDefinition> gridStore = manager.getStore();

        grid = new ResizeableGrid<MatrixCategoryDefinition>(gridStore, columnModel);
        grid.getColumnModel().addColumnWidthChangeHandler(new ColumnWidthChangeEvent.ColumnWidthChangeHandler() {
            @Override
            public void onColumnWidthChange(ColumnWidthChangeEvent event) {
                Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                    @Override
                    public boolean execute() {
                        bundleCell.setWidth(grid.getColumnModel().getColumnWidth(1)-25);
                        combo.setWidth(grid.getColumnModel().getColumnWidth(2));
                        labelCell.setWidth(grid.getColumnModel().getColumnWidth(3)-25);
                        grid.getView().refresh(true);
                        return false;
                    }
                }, 50);
            }
        });

        grid.getView().setAutoExpandColumn(nameCol);
        GridHelper.setDefaults(grid);

        gridContainer.setGrid(grid);
    }

    @Override
    public void updateViewFromModel() {
        fieldList.setAllowMultiselect(true);
        fieldList.getStore().addAll(Lists.newArrayList(getVisualizationSettings().getDataViewDefinition().getModelDef().getFieldDefs()));
        fieldList.setSelectedIndex(0);
        fieldList.addSelectionHandler(new SelectionHandler<FieldDef>() {
            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                FieldDef fieldDef = event.getSelectedItem();

                if (fieldDef != null) {
                    addAxis(fieldList.getCurrentValue(), true);
                }

//                fieldList.expand();
            }
        });

        int ii = 0;
        for (MatrixCategoryDefinition mcd : getMatrixSettings().getAxisCategories()) {
            mcd.setListPosition(ii);
            grid.getStore().add(mcd);
            ii++;
        }
    }

    @Override
    public void updateModelWithView() {
        grid.getStore().commitChanges();

        getMatrixSettings().getAxisCategories().clear();
        int ii = 0;
        for (MatrixCategoryDefinition mcd : grid.getStore().getAll()) {

            mcd.setListPosition(ii);
            getMatrixSettings().getAxisCategories().add(mcd);
            ii++;
        }
    }

    @UiHandler("buttonAddCategory")
    public void handleAddCategory(ClickEvent event) {
        if (fieldList.getCurrentValue() != null) {
            addAxis(fieldList.getCurrentValue(), true);
            fieldList.kill();
        }
    }

    /**
     * Adds the passed in fielddef to the AxisDefitions
     *
     * @param newAxis - FieldDef to add
     */
    private void addAxis(FieldDef newAxis, boolean increment) {
        if (grid.getStore().size() == 2) {
            InfoDialog errorDialog = new InfoDialog(
                    _constants.matrixCategoriesTab_errorTitle(),
                    _constants.matrixCategoriesTab_errorMessge());
            errorDialog.show();
            fieldList.kill();
            return;
        }

        MatrixCategoryDefinition definition = new MatrixCategoryDefinition();

        definition.setFieldDef(newAxis);
        grid.getStore().commitChanges();

        if (grid.getStore().size() > 0 && grid.getStore().get(0).getAxis().equals(Axis.X)) {
            definition.setAxis(Axis.Y);
        } else
            definition.setAxis(Axis.X);

        grid.getStore().add(definition);

        // Increment to next item.
        if (increment) {
            fieldList.incrementSelected();
        }

        if (grid.getStore().size() == 2) {
            fieldList.setAllowMultiselect(false);
            fieldList.kill();
        }
    }

    @UiHandler("btnToggleAxis")
    public void toggleAxisXY(ClickEvent event) {
        List<MatrixCategoryDefinition> all = grid.getStore().getAll();
//        grid.getStore().clear();
        for (MatrixCategoryDefinition c : all) {
            c.setAxis(c.getAxis().flipAxis());
            grid.getStore().update(c);
        }
//        grid.getStore().addAll(all);
    }

    @UiHandler("buttonDeleteCategory")
    public void handleDeleteCategory(ClickEvent event) {
        List<MatrixCategoryDefinition> selected = grid.getSelectionModel().getSelection();
        for (MatrixCategoryDefinition mcd : selected) {
            grid.getStore().remove(mcd);
        }

        grid.getStore().commitChanges();
        this.tabSort.removeCategory();

        //TODO need to move this into the combo
        if (grid.getStore().size() < 2) {
            Native.log("Allowing multiselect");
            fieldList.setAllowMultiselect(true);

        }

    }

    public List<MatrixCategoryDefinition> getCurrentCategories() {
        grid.getStore().commitChanges();
        return grid.getStore().getAll();
    }

    @Override
    public void setVisualizationSettings(VisualizationSettings visualizationSettings) {
        super.setVisualizationSettings(visualizationSettings);
        createEditor();
    }

    private void createEditor() {
        final GridInlineEditing<MatrixCategoryDefinition> editing = new GridInlineEditing<MatrixCategoryDefinition>(grid);
        addFieldEditor(editing);
        editing.addStartEditHandler(new StartEditEvent.StartEditHandler<MatrixCategoryDefinition>() {

            @Override
            public void onStartEdit(StartEditEvent<MatrixCategoryDefinition> event) {
                MatrixCategoryDefinition matrixCategoryDefinition = grid.getStore().get(editing.getActiveCell().getRow());
                tempEditfieldDef = matrixCategoryDefinition.getFieldDef();
            }
        });
        editing.addCompleteEditHandler(new CompleteEditEvent.CompleteEditHandler<MatrixCategoryDefinition>() {
            @Override
            public void onCompleteEdit(CompleteEditEvent<MatrixCategoryDefinition> event) {
                grid.getStore().commitChanges();

                MatrixCategoryDefinition matrixCategoryDefinition = grid.getStore().get(editing.getActiveCell().getRow());
                FieldDef fieldDef = matrixCategoryDefinition.getFieldDef();
                if (fieldDef == null) {
                    matrixCategoryDefinition.setFieldDef(tempEditfieldDef);
                    grid.getStore().commitChanges();
                }
                if (!matrixCategoryDefinition.getBundleFunction().isApplicableFor(matrixCategoryDefinition.getFieldDef().getValueType())) {
                    matrixCategoryDefinition.setBundleFunction(BundleFunction.NONE);
                }

                grid.getStore().add(editing.getActiveCell().getRow(), matrixCategoryDefinition.copy(new HashMap()));

                grid.getStore().remove(matrixCategoryDefinition);
                grid.getStore().commitChanges();
                grid.getView().refresh(false);
            }
        });

    }

    private void addFieldEditor(GridInlineEditing<MatrixCategoryDefinition> editing) {
        ColumnConfig config = columnModel.getColumn(0);
        FieldDefComboBox combo = new FieldDefComboBox();
        combo.removeStyleName("string-combo-style");
        combo.getElement().getStyle().setPaddingTop(5, Unit.PX);
        combo.getStore().addAll(Lists.newArrayList(getVisualizationSettings().getDataViewDefinition().getModelDef().getFieldDefs()));
        combo.setAllowBlank(false);
        editing.addEditor(config, combo);
    }

    public void setSortTab(MatrixSortTab tabSort) {
        this.tabSort = tabSort;
    }

    interface CategoryDefinitionPropertyAccess extends PropertyAccess<MatrixCategoryDefinition> {

        @Path("uuid")
        ModelKeyProvider<MatrixCategoryDefinition> key();

        @Path("fieldDef")
        ValueProvider<MatrixCategoryDefinition, FieldDef> field();

        ValueProvider<MatrixCategoryDefinition, Axis> axis();

        ValueProvider<MatrixCategoryDefinition, BundleFunction> bundleFunction();

        ValueProvider<MatrixCategoryDefinition, LabelDefinition> labelDefinition();

        ValueProvider<MatrixCategoryDefinition, Boolean> allowNulls();

        ValueProvider<MatrixCategoryDefinition, List<BundleFunctionParameter>> bundleFunctionParameters();

    }

    interface SpecificUiBinder extends UiBinder<Widget, MatrixCategoriesTab> {

    }

    public class AxisLabelProvider implements LabelProvider<Axis> {

        @Override
        public String getLabel(Axis item) {
            String label = ""; //$NON-NLS-1$

            switch (item) {
                case X:
                    label = i18n.axisLabelsX(); //$NON-NLS-1$
                    break;
                case Y:
                    label = i18n.axisLabelsY(); //$NON-NLS-1$
                    break;
            }
            return label;
        }
    }

}
