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

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfoBean;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.MultiPageCheckboxSelectionModel;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.LoadCallback;
import csi.client.gwt.widget.gxt.grid.paging.RemoteLoadingGridComponentManager;
import csi.server.common.model.BundledFieldReference;
import csi.server.common.model.filter.FilterPickEntry;
import csi.server.common.service.api.FilterActionsServiceProtocol;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PickListDialog extends WatchingParent {
	
    private Dialog dialog;
    private BundledFieldReference fieldReference;
    private String dataViewUuid;
    private Grid<FilterPickEntry> resultsGrid;
    private MultiPageCheckboxSelectionModel<FilterPickEntry> selectionModel;
    private SelectedItemsCallback selectedItemsCallback;
    private boolean initialized = false;
    @UiField
    GridContainer gridContainer;

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    interface FilterPickEntryProperties extends PropertyAccess<FilterPickEntry> {

        @Path("value")
        ModelKeyProvider<FilterPickEntry> key();

        ValueProvider<FilterPickEntry, String> value();

        ValueProvider<FilterPickEntry, Integer> frequency();
    }

    private static FilterPickEntryProperties filterPickEntryProperties = GWT.create(FilterPickEntryProperties.class);

    interface SpecificUiBinder extends UiBinder<Dialog, PickListDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public interface SelectedItemsCallback {

        void onSuccess(List<String> selection);
    }

    public PickListDialog(CanBeShownParent parentIn) {
        super(parentIn);
        dialog = uiBinder.createAndBindUi(this);
        dialog.hideOnCancel();
        initGrid();
        addHandlers();
    }

    public void setSelectedItemsCallback(SelectedItemsCallback selectedItemsCallback) {
        this.selectedItemsCallback = selectedItemsCallback;
    }

    private void addHandlers() {
        // FIXME: Add buttons for global select all and deselect all.
        
        dialog.getActionButton().setText(CentrifugeConstantsLocator.get().select());
        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                getSelectedItems(PickListDialog.this.selectedItemsCallback);
                destroy();
            }
        });
        dialog.getCancelButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                destroy();
            }
        });
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

    public BundledFieldReference getFieldReference() {
        return fieldReference;
    }

    public void setFieldReference(BundledFieldReference fieldReference) {
        this.fieldReference = fieldReference;
    }

    public String getDataViewUuid() {
        return dataViewUuid;
    }

    public void setDataViewUuid(String dataViewUuid) {
        this.dataViewUuid = dataViewUuid;
    }

    @SuppressWarnings("unchecked")
    private void initGrid() {
        final RemoteLoadingGridComponentManager<FilterPickEntry> manager = WebMain.injector.getGridFactory()
                .createRemoteLoading(filterPickEntryProperties.key(), FilterActionsServiceProtocol.class,
                        new LoadCallback<FilterActionsServiceProtocol, FilterPickEntry>() {

                            @Override
                            public PagingLoadResult<FilterPickEntry> onLoadCallback(
                                    FilterActionsServiceProtocol service, FilterPagingLoadConfig loadConfig) {
                                return service.getPickList(getDataViewUuid(), getFieldReference(), loadConfig);
                            }
                        });
        selectionModel = manager.getCheckboxSelectionModel();

        manager.create(filterPickEntryProperties.value(), 200, i18n.pickListValueColumn());
        ColumnConfig<FilterPickEntry, Integer> freqCol = manager.create(filterPickEntryProperties.frequency(), 100,
                i18n.pickListFrequencyColumn());
        freqCol.setHorizontalAlignment(HorizontalAlignmentConstant.endOf(Direction.LTR));

        List<ColumnConfig<FilterPickEntry, ?>> columns = manager.getColumnConfigList();
        columns.add(0, selectionModel.getColumn());
        ColumnModel<FilterPickEntry> cm = new ColumnModel<FilterPickEntry>(columns);
        ListStore<FilterPickEntry> gridStore = manager.getStore();
        resultsGrid = new ResizeableGrid<FilterPickEntry>(gridStore, cm);

        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<FilterPickEntry>> loader = manager.getLoader();
        
//        loader.addSortInfo(new SortInfoBean("Frequency", SortDir.DESC));
        loader.addSortInfo(new SortInfoBean("Value", SortDir.ASC));
        resultsGrid.setLoader(loader);
        resultsGrid.setSelectionModel(selectionModel);
        GridHelper.setDefaults(resultsGrid);

        final PagingToolBar pager = manager.getPagingToolbar(50);
        gridContainer.setGrid(resultsGrid);
        gridContainer.setPager(pager);
    }

    public void show() {

        if (null != getParent()) {

            getParent().hide();
        }
        if (!initialized) {

            dialog.show();
            resultsGrid.getLoader().load();
            initialized = true;
        }
    }

    private void getSelectedItems(final SelectedItemsCallback callback) {
        if (selectionModel.isGlobalSelectAll()) {
            // Get the selection minus the selection qualifier.
            WebMain.injector
                    .getVortex()
                    .execute(new Callback<List<String>>() {

                        @Override
                        public void onSuccess(List<String> result) {
                            callback.onSuccess(result);
                        }
                    }, FilterActionsServiceProtocol.class)
                    .getPickListSelection(getDataViewUuid(), getFieldReference(),
                            selectionModel.getSelectionQualifierList());
        } else {
            callback.onSuccess(selectionModel.getSelectionQualifierList());
        }
    }
}
