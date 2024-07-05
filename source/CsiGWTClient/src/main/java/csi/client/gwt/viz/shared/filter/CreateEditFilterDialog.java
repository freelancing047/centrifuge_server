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
package csi.client.gwt.viz.shared.filter;

import java.util.List;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.util.FieldDefUtils.SortOrder;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.client.gwt.widget.boot.MaskDialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.cells.readonly.FieldDefValueCell;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.ui.bundle.BundleFunctionEditCell;
import csi.server.common.enumerations.OperandCardinality;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.UUID;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.filter.FilterDefinition;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.filter.MultiValueDefinition;
import csi.server.common.model.filter.NullValueDefinition;
import csi.server.common.model.filter.ValueDefinition;
import csi.server.common.model.visualization.BundleFunctionParameter;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.util.ValuePair;
import csi.server.util.sql.api.BundleFunction;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class CreateEditFilterDialog extends WatchingParent {
    interface FilterExpressionPropertyAccess extends PropertyAccess<FilterExpression> {
        @Path("uuid")
        public ModelKeyProvider<FilterExpression> key();

        @Path("expressionIdLabel")
        public ValueProvider<FilterExpression, String> reference();

        @Path("fieldDef")
        public ValueProvider<FilterExpression, FieldDef> field();

        @Path("fieldDefValue")
        public ValueProvider<FilterExpression, ValuePair<Boolean, FieldDef>> fieldDefValue();

        public ValueProvider<FilterExpression, Boolean> isSelectionFilter();

        public ValueProvider<FilterExpression, BundleFunction> bundleFunction();

        public ValueProvider<FilterExpression, List<BundleFunctionParameter>> bundleFunctionParameters();

        public ValueProvider<FilterExpression, Boolean> negated();

        public ValueProvider<FilterExpression, RelationalOperator> operator();

        public ValueProvider<FilterExpression, ValueDefinition> valueDefinition();
    }
    interface SpecificUiBinder extends UiBinder<Dialog, CreateEditFilterDialog> {    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private static FilterExpressionPropertyAccess propertyAccess = GWT.create(FilterExpressionPropertyAccess.class);
	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public interface FilterSaveCallback {
        public void onSave(Filter filter);
    }

    public interface FilterCancelCallback {
    	public void onCancel();
    }

    @UiField
    GridContainer gridContainer;
    @UiField
    FieldDefComboBox fieldList;
    @UiField
    TextBox filterNameField;

    @UiField
    Button buttonAdd;

    private Filter filterToEdit;
    private String dataViewUuid;
    private FilterSaveCallback filterSaveCallback;
    private FilterCancelCallback filterCancelCallback;
    private Dialog dialog;
    private DataModelDef dataModelDef;
    private MaskDialog mask;
    private Grid<FilterExpression> grid;
    private boolean initialized = false;

    public CreateEditFilterDialog(CanBeShownParent parentIn, Filter filterToEdit, String dataViewUuid,
                                  FilterSaveCallback filterSaveCallback) {
        this(parentIn, filterToEdit, dataViewUuid, filterSaveCallback, null);
    }

    public CreateEditFilterDialog(CanBeShownParent parentIn, Filter filterToEdit, String dataViewUuid, FilterSaveCallback filterSaveCallback, boolean cloneFilter) {
        this(parentIn, filterToEdit, dataViewUuid, filterSaveCallback, null, cloneFilter);
    }

    public CreateEditFilterDialog(CanBeShownParent parentIn, Filter filterToEdit, String dataViewUuid, FilterSaveCallback filterSaveCallback, FilterCancelCallback filterCancelCallback) {
        this(parentIn, filterToEdit, dataViewUuid, filterSaveCallback, filterCancelCallback, false);
    }

    public CreateEditFilterDialog(CanBeShownParent parentIn, Filter filterToEdit, String dataViewUuid, FilterSaveCallback filterSaveCallback, FilterCancelCallback filterCancelCallback, boolean cloneFilter) {
        super(parentIn);
        this.filterToEdit = filterToEdit;
        if (cloneFilter) {
            this.filterToEdit = this.filterToEdit.cloneThis();
            // Modify name and uuid.
            this.filterToEdit.setName(i18n.createEditFilterDialog_copyOf() + " " + this.filterToEdit.getName());
            this.filterToEdit.setUuid(UUID.uuid());
        }
        this.dataViewUuid = dataViewUuid;
        this.filterSaveCallback = filterSaveCallback;
        this.filterCancelCallback = filterCancelCallback;

        dialog = uiBinder.createAndBindUi(this);

//        dialog.hideOnAction();
//        dialog.hideOnCancel();

        dataModelDef = DataViewRegistry.getInstance().getDataViewByUuid(dataViewUuid).getMeta().getModelDef();

        mask = new MaskDialog(i18n.savingChanges());
        hideMask();
        initGrid();
        initAddButton();
        addHandlers();

    }

    public void hide() {

        dialog.hide();
    }

    public void destroy() {

        if (null != getParent()) {

            getParent().show();
        }
        dialog.hide();
    }

    private void initAddButton() {
        buttonAdd.setIcon(IconType.CIRCLE_ARROW_DOWN);
        buttonAdd.setType(ButtonType.LINK);
        buttonAdd.setSize(ButtonSize.LARGE);
        Style buttonStyle = buttonAdd.getElement().getStyle();
        buttonStyle.setFontSize(26.0D, Style.Unit.PX);
        buttonStyle.setTextDecoration(Style.TextDecoration.NONE);
        buttonStyle.setPaddingLeft(0, Style.Unit.PX);
        buttonStyle.setPaddingTop(0, Style.Unit.PX);
        buttonStyle.setMarginBottom(0, Style.Unit.PX);
        buttonStyle.setMarginTop(0, Style.Unit.PX);
    }

    @SuppressWarnings("unchecked")
    private void initGrid() {
        final GridComponentManager<FilterExpression> manager = WebMain.injector.getGridFactory().create(propertyAccess.key());

        manager.create(propertyAccess.reference(), 30, i18n.filter_ref(), false, true);

        ColumnConfig<FilterExpression, ValuePair<Boolean, FieldDef>> nameCol = manager.create(propertyAccess.fieldDefValue(), 150, i18n.filter_field(), false, true);
        nameCol.setCell(new FieldDefValueCell());

        ColumnConfig<FilterExpression, BundleFunction> bundle = manager.create(propertyAccess.bundleFunction(), 130, i18n.filter_bundle(), false, true);
        bundle.setCell(new BundleFunctionEditCell<FilterExpression>(manager.getStore(), propertyAccess.bundleFunctionParameters(), propertyAccess.isSelectionFilter()));

        ColumnConfig<FilterExpression, Boolean> negator = manager.create(propertyAccess.negated(), 30, i18n.filter_not(), false, true);
        negator.setCell(new FilterCheckBoxCell(manager.getStore(), propertyAccess.isSelectionFilter()));

        ColumnConfig<FilterExpression, RelationalOperator> operator = manager.create(propertyAccess.operator(), 130, i18n.filter_operator(), false, true);
        operator.setCell(new RelationalOperatorCell(manager.getStore(), propertyAccess.valueDefinition(), propertyAccess.isSelectionFilter(), 130));

        ColumnConfig<FilterExpression, ValueDefinition> valueDefinition = manager.create(propertyAccess.valueDefinition(), 200, i18n.filter_value(), false, true);
        valueDefinition.setCell(new ValueDefinitionCell(this, manager.getStore(), propertyAccess, dataModelDef, dataViewUuid, 200));

        List<ColumnConfig<FilterExpression, ?>> columns = manager.getColumnConfigList();
        ColumnModel<FilterExpression> cm = new ColumnModel<FilterExpression>(columns);
        ListStore<FilterExpression> gridStore = manager.getStore();

        grid = new ResizeableGrid<FilterExpression>(gridStore, cm);
        grid.getView().setAutoExpandColumn(nameCol);
        GridHelper.setDraggableRowsDefaults(grid);

        gridContainer.setGrid(grid);
    }

    private void addHandlers() {
        dialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showMask();
                if (isErrorFree()) {
                    grid.getStore().commitChanges();

                    if (filterToEdit == null) {
                        // Create case.
                        filterToEdit = new Filter();
                        FilterDefinition filterDef = new FilterDefinition();
                        filterToEdit.setFilterDefinition(filterDef);
                        DataViewRegistry.getInstance().getDataViewByUuid(dataViewUuid).getMeta().getFilters().add(filterToEdit);
                    } else {
                        List<Filter> filters = DataViewRegistry.getInstance().getDataViewByUuid(dataViewUuid).getMeta().getFilters();

                        // copy and edit case - we have a filter coming in that isn't in Filters on the dataview.
                        if(!filters.contains(filterToEdit)){
                            DataViewRegistry.getInstance().getDataViewByUuid(dataViewUuid).getMeta().getFilters().add(filterToEdit);
                        }

                        // Edit case
                        int index = 0;
                        for (Filter filter : filters) {
                            if (filter.getUuid().equals(filterToEdit.getUuid())) {
                                break;
                            }
                            index++;
                        }

                            filters.set(index, filterToEdit);
                    }

                    filterToEdit.setName(filterNameField.getValue());
                    filterToEdit.getFilterDefinition().getFilterExpressions().clear();

                    for (FilterExpression fe : grid.getStore().getAll()) {
                        filterToEdit.getFilterDefinition().getFilterExpressions().add(fe);
                    }
                    VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();

                    future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                        	hideMask();
                            // Iterate through visualizations that use this filter. Ask them to update.
                            List<Visualization> visualizations = DataViewRegistry.getInstance().dataViewPresenterForDataView(dataViewUuid).getVisualizations();
                            for (Visualization visualization : visualizations) {
                                if (visualization instanceof FilterCapableVisualizationPresenter) {
                                    VisualizationDef visualizationDef = visualization.getVisualizationDef();
                                    if (visualizationDef != null) {
                                        if (filterToEdit.getUuid().equals(visualizationDef.getFilterUuid())) {//TODO: check logic
                                            visualizationDef.setFilter(filterToEdit); //TODO: this seems to suggest the object graph got out of sync... we should avoid this.
                                            if(visualization instanceof Graph) {//FIXME: why is graph different?
                                                visualization.reload();
                                            } else {
                                                visualization.reload();
                                            }
                                        }
                                    }
                                }
                            } // end for loop on visualizations.
//
                            filterSaveCallback.onSave(filterToEdit);
                            dialog.destroy();
                        }

                        @Override
                        public boolean onError(Throwable t) {
                        	hideMask();
                            return true;
                        }
                    });

                    Set<Filter> filters = Sets.newHashSet(DataViewRegistry.getInstance().getDataViewByUuid(dataViewUuid).getMeta().getFilters());
                    try {
                        future.execute(DataViewActionServiceProtocol.class).saveFilters(dataViewUuid, filters);
                        destroy();
                    } catch (Exception myException) {
                        Dialog.showException(myException);
                    }
                } else {
                	hideMask();
                }
            }
        });

        dialog.getCancelButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
            	if (filterCancelCallback != null) {
            		filterCancelCallback.onCancel();
            	}
                destroy();
            }
        });


        fieldList.addSelectionHandler(new SelectionHandler<FieldDef>() {
            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                FieldDef newExpressionValue = event.getSelectedItem();
                if(newExpressionValue != null) {
                    addNewExpression(newExpressionValue, true);
                }
            }
        });
    }

    protected boolean isErrorFree() {
        // FIXME: Error check (ensure operators are selected for each expression and value definition is of
        // appropriate cardinality and data type).
        if (filterNameField.getValue().isEmpty()) {
            new ErrorDialog(i18n.createEditFilterDialog_haveNameTitle(), i18n.createEditFilterDialog_haveNameTitle()).show();
            Style style = filterNameField.getElement().getStyle();
            final String oldColor = style.getBorderColor();
            style.setBorderColor("red");
            filterNameField.addValueChangeHandler(new ValueChangeHandler<String>() {
                public boolean onceAlready;

                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    if(!onceAlready) {
                        filterNameField.getElement().getStyle().setBorderColor(oldColor);
                        onceAlready = true;
                    }
                }
            });
            return false;
        }
        {//Validate name is unique
            List<Filter> filters = DataViewRegistry.getInstance().getDataViewByUuid(dataViewUuid).getMeta().getFilters();
            String proposedName = filterNameField.getValue();
            for (Filter filter : filters) {

                if ((filterToEdit == null) || !filter.getUuid().equals(filterToEdit.getUuid())) {
                    if (proposedName.equals(filter.getName())) {
                        new ErrorDialog(i18n.createEditFilterDialog_haveUniqueNameTitle(), i18n.createEditFilterDialog_haveUniqueNameBody()).show();
                        Style style = filterNameField.getElement().getStyle();
                        final String oldColor = style.getBorderColor();
                        style.setBorderColor("red");
                        filterNameField.addValueChangeHandler(new ValueChangeHandler<String>() {
                            public boolean onceAlready;

                            @Override
                            public void onValueChange(ValueChangeEvent<String> event) {
                                if (!onceAlready) {
                                    filterNameField.getElement().getStyle().setBorderColor(oldColor);
                                    onceAlready = true;
                                }
                            }
                        });
                        return false;
                    }
                }
            }
        }
        //TODO: make this more better, a bit rushed, but this validation prevents serious damage.
        grid.getStore().commitChanges();
        for (FilterExpression fe : grid.getStore().getAll()) {
            if(fe.getOperator() == null){
                new ErrorDialog(i18n.createEditFilterDialog_haveOpTitle(), i18n.createEditFilterDialog_haveOpMsg()).show();

                return false;
            }
            if(fe.getOperator().getCardinality() == OperandCardinality.NONE){
                fe.setValueDefinition(new NullValueDefinition());
            }
            if(fe.getValueDefinition() == null){
                new ErrorDialog(i18n.createEditFilterDialog_haveValueTitle(), i18n.createEditFilterDialog_haveValueMsg()).show();
                return false;
            } else if(fe.getValueDefinition() instanceof MultiValueDefinition){
                MultiValueDefinition valueDefinition = (MultiValueDefinition) fe.getValueDefinition();
                if((valueDefinition.getValues() == null) || valueDefinition.getValues().isEmpty()){
                    fe.setValueDefinition(null);
                    new ErrorDialog(i18n.createEditFilterDialog_haveValueTitle(), i18n.createEditFilterDialog_haveValueMsg()).show();
                    return false;
                }
            }
        }

        return true;
    }

    public void showMask() {
        if(mask != null) {
         mask.show();
      }
    }

    public void hideMask() {
        if(mask != null) {
         mask.hide();
      }
    }

    public void show() {
        if (!initialized) {

            fieldList.getStore().addAll(FieldDefUtils.getSortedNonStaticFields(dataModelDef, SortOrder.ALPHABETIC));
            fieldList.setSelectedIndex(0);
            if (filterToEdit == null) {
                dialog.setTitle(i18n.createEditFilterDialog_createFilterTitle());
                String name = UniqueNameUtil.getDistinctName(UniqueNameUtil.getFilterNames(dataViewUuid), i18n.filter_name());
                filterNameField.setValue(name);
            } else {
                dialog.setTitle(i18n.createEditFilterDialog_editFilterTitle() + " " + filterToEdit.getName());
                filterNameField.setValue(filterToEdit.getName());
                for (FilterExpression fe : filterToEdit.getFilterDefinition().getFilterExpressions()) {
                    grid.getStore().add(fe);
                }
            }
            dialog.getActionButton().setText(i18n.save());
            initialized = true;
        }
        dialog.show();
        if (null != getParent()) {

            getParent().hide();
        }
    }

    public Button getCancelButton() {
        return dialog.getCancelButton();
    }

    @UiHandler("buttonAdd")
    public void handleAddNewExpressionClick(ClickEvent event) {
        FieldDef newExpressionValue = fieldList.getCurrentValue();
        if(newExpressionValue != null) {
            addNewExpression(newExpressionValue, true);
        }
    }


    private void addNewExpression(FieldDef expressionFieldDef, boolean autoIncrement){
        FilterExpression newExpression = new FilterExpression();
        // Find smallest id.
        int id = 1;
        for (FilterExpression fe : grid.getStore().getAll()) {
            if (id <= fe.getExpressionId()) {
                id = fe.getExpressionId() + 1;
            }
        }
        newExpression.setExpressionId(id);
        newExpression.setFieldDef(expressionFieldDef);
        newExpression.setNegated(false);
        grid.getStore().add(newExpression);

        if(autoIncrement) {
            fieldList.incrementSelected();
        }
    }

    @UiHandler("buttonDelete")
    public void handleDeleteExpressionClick(ClickEvent event) {
        List<FilterExpression> selected = grid.getSelectionModel().getSelection();
        for (FilterExpression fe : selected) {
            grid.getStore().remove(fe);
        }
    }
}
