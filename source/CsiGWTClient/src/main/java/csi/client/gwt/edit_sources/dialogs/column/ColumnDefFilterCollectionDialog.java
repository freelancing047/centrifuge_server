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
package csi.client.gwt.edit_sources.dialogs.column;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.edit_sources.cells.FilterOperandTypeCell;
import csi.client.gwt.edit_sources.cells.FilterOperatorTypeCell;
import csi.client.gwt.edit_sources.dialogs.column.ColumnFilterDialog.ColumnFilterMode;
import csi.client.gwt.edit_sources.dialogs.common.ParameterPresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.widget.buttons.MiniRedButton;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.ColumnFilter;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.filter.FilterOperatorType;
import csi.server.common.model.query.QueryParameterDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColumnDefFilterCollectionDialog {

    private Grid<ColumnFilter> grid;
    private ColumnDefContext _cdContext;
    private Dialog dialog;
    private ParameterPresenter _parameterPresenter;


    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

	@UiField(provided = true)
	String filterTitle = i18n.columnDefFilterCollectionDialogFilter(); //$NON-NLS-1$
	@UiField(provided = true)
	String sourceLabel = i18n.columnDefFilterCollectionDialogSourceLabel();  //$NON-NLS-1$
	@UiField(provided = true)
	String typeLabel = i18n.columnDefFilterCollectionDialogTypeLabel();  //$NON-NLS-1$
	@UiField(provided = true)
	String addButton = i18n.columnDefFilterCollectionDialogAddButton();  //$NON-NLS-1$
	@UiField(provided = true)
	String deleteButton = i18n.columnDefFilterCollectionDialogDeleteButton();  //$NON-NLS-1$
    
    @UiField
    Column sourceTypeContainer, typeContainer;
    @UiField
    Paragraph filterMessage;
    @UiField
    GridContainer gridContainer;
    @UiField
    MiniRedButton buttonDelete;

    interface ColumnFilterPropertyAccess extends PropertyAccess<ColumnFilter> {

        @Path("uuid")
        public ModelKeyProvider<ColumnFilter> key();

        public ValueProvider<ColumnFilter, Boolean> exclude();

        public ValueProvider<ColumnFilter, FilterOperatorType> operator();

        public ValueProvider<ColumnFilter, FilterOperandType> operandType();

        public ValueProvider<ColumnFilter, ArrayList<String>> staticValues();

        public ValueProvider<ColumnFilter, String> localColumnId();

        public ValueProvider<ColumnFilter, String> paramLocalId();
    }

    interface SpecificUiBinder extends UiBinder<Dialog, ColumnDefFilterCollectionDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private ColumnFilterPropertyAccess propertyAccess = GWT.create(ColumnFilterPropertyAccess.class);

    public ColumnDefFilterCollectionDialog(ColumnDefContext cdContextIn) {
        super();
        _cdContext = cdContextIn;
        dialog = uiBinder.createAndBindUi(this);
        dialog.hideOnCancel();
        
        _parameterPresenter = _cdContext.getModel().createParameterPresenter();
        
        initHeader();
        initGrid();
        initHandlers();
    }
    
    public List<QueryParameterDef> getParameters() {
        
        return _parameterPresenter.getParameters();
    }

    private void initHandlers() {
        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                List<ColumnFilter> myFilterList = _cdContext.getColumnDef().getColumnFilters();
                
                grid.getStore().commitChanges();
                
                if (null != myFilterList) {
                    
                    myFilterList.clear();
                    
                } else {
                    
                    myFilterList = new ArrayList<ColumnFilter>();
                    _cdContext.getColumnDef().setColumnFilters(myFilterList);
                }
                myFilterList.addAll(grid.getStore().getAll());
                dialog.hide();
                _cdContext.getModel().setChanged();
            }
        });

    }

    private void initHeader() {
        dialog.setTitle(i18n.columnDefFilterCollectionDialogFilterTitle() + _cdContext.getColumnDef().getColumnName()); //$NON-NLS-1$
        Label sourceTypeLabel = new Label(_cdContext.getColumnDef().getDataTypeName());
        sourceTypeContainer.add(sourceTypeLabel);
        Label typeLabel = new Label(_cdContext.getColumnDef().getCsiType().getLabel());
        typeContainer.add(typeLabel);
        filterMessage.setText(i18n.columnDefFilterCollectionDialogFilterMessage(_cdContext.getColumnDef().getColumnName())); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    private void initGrid() {
        final GridComponentManager<ColumnFilter> manager = WebMain.injector.getGridFactory().create(
                propertyAccess.key());
        ListStore<ColumnFilter> gridStore = manager.getStore();
         List<ColumnFilter> myFilterList = _cdContext.getColumnDef().getColumnFilters();

        ColumnConfig<ColumnFilter, Boolean> excludeCol = manager.create(propertyAccess.exclude(), 40, "", false, true); //$NON-NLS-1$
        excludeCol.setCell(new ColumnFilterExcludeCell());
        ColumnConfig<ColumnFilter, FilterOperatorType> operatorCol = manager.create(propertyAccess.operator(), 180,
                i18n.columnDefFilterCollectionDialogOperatorLabel(), false, true); //$NON-NLS-1$
        operatorCol.setCell(new FilterOperatorTypeCell());
        ColumnConfig<ColumnFilter, FilterOperandType> operandCol = manager.create(propertyAccess.operandType(), 150,
                i18n.columnDefFilterCollectionDialogOperandLabel(), false, true); //$NON-NLS-1$
        operandCol.setCell(new FilterOperandTypeCell());

        ColumnConfig<ColumnFilter, ColumnFilter> valueCol = manager.create(new IdentityValueProvider<ColumnFilter>(),
                200, i18n.columnDefFilterCollectionDialogValueLabel(), false, true); //$NON-NLS-1$
        valueCol.setCell(new ColumnFilterOperandValueCell(gridStore, propertyAccess,
                            _cdContext.getTableDef(), _parameterPresenter));

        ColumnConfig<ColumnFilter, ColumnFilter> editCol = manager.create(new IdentityValueProvider<ColumnFilter>(),
                40, i18n.columnDefFilterCollectionDialogEditLabel()); //$NON-NLS-1$
        editCol.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        List<ColumnConfig<ColumnFilter, ?>> columns = manager.getColumnConfigList();
        ColumnModel<ColumnFilter> cm = new ColumnModel<ColumnFilter>(columns);

        grid = new ResizeableGrid<ColumnFilter>(gridStore, cm);
        editCol.setCell(new FilterEditCell(gridStore, _parameterPresenter, _cdContext));
        grid.getView().setAutoExpandColumn(operandCol);
        GridHelper.setDraggableRowsDefaults(grid);

        gridContainer.setGrid(grid);

        if (null != myFilterList) {
            
            for (ColumnFilter filter : myFilterList) {
                gridStore.add(filter);
            }
        }
    }

    public void show() {
        dialog.show();
    }

    @UiHandler("buttonAdd")
    public void handleAdd(ClickEvent event) {

        final ColumnFilter myFilter = new ColumnFilter();
        final ListStore<ColumnFilter> myStore = grid.getStore();
        final ColumnDef myColumn = _cdContext.getColumnDef();
        final ColumnFilterDialog dialog = new ColumnFilterDialog(ColumnFilterMode.CREATE, myFilter,
                                            myStore, _cdContext.getTableDef(), myColumn,
                                            _parameterPresenter);
        dialog.show();
        dialog.setActionClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                myFilter.setParent(myColumn);
                buttonDelete.setEnabled(true);
            }
        });
    }

    @UiHandler("buttonDelete")
    public void handleDelete(ClickEvent event) {
        if (grid.getSelectionModel().getSelectedItems().size() == 0) {
            (new InfoDialog(i18n.columnDefFilterCollectionDialogDeleteLabel(), i18n.columnDefFilterCollectionDialogDeleteMessage())).show(); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            WarningDialog dialog = new WarningDialog(i18n.columnDefFilterCollectionDialogDeleteLabel(), i18n.columnDefFilterCollectionDialogDeleteConfirmationMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            dialog.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    for (ColumnFilter filter : grid.getSelectionModel().getSelectedItems()) {
                        _parameterPresenter.removeFilterItem(filter);
                        grid.getStore().remove(filter);
                    }
                    buttonDelete.setEnabled(grid.getStore().getAll().size() > 0);
                }
            });
            dialog.show();
        }
    }

    public void setActionClickHandler(ClickHandler actionClickHandler) {
        dialog.getActionButton().addClickHandler(actionClickHandler);
    }
}
