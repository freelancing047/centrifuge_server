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
import com.github.gwtbootstrap.client.ui.event.HideHandler;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.filter.CreateEditFilterDialog.FilterSaveCallback;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.service.api.DataViewActionServiceProtocol;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ManageFilterDialog extends AbstractFilterListDisplayingDialog {

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    //    private String currentFilterUuid;
    private Filter currentFilter;

    @UiField
    CardLayoutContainer detailContainer;
    @UiField
    Widget noFiltersMessage;
    @UiField
    Button buttonNew, buttonCopyEdit, buttonEdit, buttonDelete;

    @UiField
    FilterDisplayWidget filterDisplayWidget;
    @UiField
    Button buttonClear;

    interface SpecificUiBinder extends UiBinder<Dialog, ManageFilterDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public ManageFilterDialog(Filter initialFilter, String dataViewUuid) {
        this(null, initialFilter, dataViewUuid);
    }

    public ManageFilterDialog(CanBeShownParent parentIn, Filter initialFilter, String dataViewUuid) {
        super(parentIn, dataViewUuid);

        this.currentFilter = initialFilter;
        initComponents();
    }

    protected Dialog getDialog() {

        return uiBinder.createAndBindUi(this);
    }

    private void initComponents() {
        buttonCopyEdit.setText(CentrifugeConstantsLocator.get().manageFilterDialog_copyAndEdit());
        buttonCopyEdit.setEnabled(false);
        buttonEdit.setEnabled(false);
        buttonDelete.setEnabled(false);
        addHandlers();
    }

    private void addHandlers() {
        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
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

    public HandlerRegistration addHideHandler(HideHandler handler) {
        return dialog.addHideHandler(handler);
    }

    @Override
    protected void onAvailableFilterValueChange(Filter filter) {
        if(filter == null)
        {
            return;
        }
        detailContainer.setActiveWidget(filterDisplayWidget);
        filterDisplayWidget.display(filter);
        currentFilter = filter;
    }

    @Override
    public void show() {
        dialog.getActionButton().setVisible(false);
        dialog.getCancelButton().setText(CentrifugeConstantsLocator.get().close());
        super.show();
    }

    @Override
    public Filter getSelectedFilter() {
        return currentFilter;
    }

    @Override
    protected void onFilterPopulation(List<Filter> result) {
        Filter selectedFilter = null;
        for (Filter filter : result) {
            if (currentFilter != null) {
                if (filter.getUuid().equals(currentFilter.getUuid())) {
                    selectedFilter = filter;
                    filterDisplayWidget.display(selectedFilter);
                    break;
                }
            }
        }
        if (selectedFilter != null) {
            currentFilter = selectedFilter;
            getAvailableFilters().setValue(selectedFilter, true);
        } /*else if (result.size() > 0) {
            getAvailableFilters().setValue(getAvailableFilters().getStore().get(0), true);
            currentFilter = getAvailableFilters().getStore().get(0);
        } */else {
            detailContainer.setActiveWidget(noFiltersMessage);
            currentFilter = null;
            currentFilter = null;
        }

        boolean filtersAvailable = result.size() > 0;
        buttonCopyEdit.setEnabled(filtersAvailable);
        buttonEdit.setEnabled(filtersAvailable);
        buttonDelete.setEnabled(filtersAvailable);
    }

    @UiHandler("buttonNew")
    public void handleNewButtonClick(ClickEvent e) {
        CreateEditFilterDialog myDialog = new CreateEditFilterDialog(this, null, getDataViewUuid(), new FilterSaveCallback() {

            @Override
            public void onSave(Filter filter) {
                currentFilter = filter;
                populateAvailableFilters();
            }
        });
        myDialog.show();
    }

    @UiHandler("buttonEdit")
    public void handleEditButtonClick(ClickEvent e) {
        CreateEditFilterDialog myDialog = new CreateEditFilterDialog(this, currentFilter, getDataViewUuid(),
                new FilterSaveCallback() {

                    @Override
                    public void onSave(Filter filter) {
                        currentFilter = filter;
                        populateAvailableFilters();
                    }
                });
        myDialog.show();
    }

    @UiHandler("buttonCopyEdit")
    public void handleCopyEditButtonClick(ClickEvent e) {
        if(currentFilter != null && !(getAvailableFilters().getText().isEmpty())){
            CreateEditFilterDialog myDialog = new CreateEditFilterDialog(this, currentFilter, getDataViewUuid(),
                    new FilterSaveCallback() {
    
                        @Override
                        public void onSave(Filter filter) {
                            currentFilter = filter;
                            populateAvailableFilters();
                        }
                    }, true);
            myDialog.show();
        }
    }

    @UiHandler("buttonDelete")
    public void handleDeleteButtonClick(ClickEvent e) {

        Display.continueDialog(i18n.manageFilterDialog_deleteFilterTitle(),
                                i18n.manageFilterDialog_deleteFilterMessage(),
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {

                        deleteFilter();
                    }
                },
                false
        );
    }

    private void deleteFilter() {

        if (currentFilter == null || (getAvailableFilters().getText().isEmpty())) {
            return;
        }
        getAvailableFilters().remove(currentFilter);
        if(getAvailableFilters().getText().equals(currentFilter.getName())){
            getAvailableFilters().setText("");
        }
        DataViewDef dataViewDef = DataViewRegistry.getInstance().getDataViewByUuid(getDataViewUuid()).getMeta();

        List<Visualization> visualizations = DataViewRegistry.getInstance()
                .dataViewPresenterForDataView(getDataViewUuid()).getVisualizations();
        for (Visualization visualization : visualizations) {
            if (visualization instanceof FilterCapableVisualizationPresenter) {
                VisualizationDef visualizationDef = visualization.getVisualizationDef();
                if (visualizationDef != null) {
                    if (currentFilter.getUuid().equals(visualizationDef.getFilterUuid())) {
                        visualizationDef.setFilter(null);
                        visualization.saveSettings(true);
                        if(visualization instanceof Graph) {//FIXME: why is graph different?
                            visualization.reload();
                        }
                    }
                }
            }
        }

        dataViewDef.getFilters().remove(currentFilter);

        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        Set<Filter> filters = Sets.newHashSet(dataViewDef.getFilters());
        try {
            
            future.execute(DataViewActionServiceProtocol.class).saveFilters(getDataViewUuid(), filters);
            future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                @Override
                public void onSuccess(Void result) {
                    currentFilter = null;
                    populateAvailableFilters();

                }
            });
            
        } catch (Exception myException) {

            Display.error(myException);
        }
    }

    @UiHandler("buttonClear")
    public void onClear(ClickEvent e){
        availableFilters.clear();
        currentFilter = null;
        detailContainer.setActiveWidget(noFiltersMessage);
    }
}
