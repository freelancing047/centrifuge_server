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

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.event.HideEvent;
import com.github.gwtbootstrap.client.ui.event.HideHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.CsiModal;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.server.common.model.filter.Filter;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FilterSelectionDialog extends AbstractFilterListDisplayingDialog implements HasSelectionHandlers<Filter> {

    private EventBus eventBus = new SimpleEventBus();
    
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    @UiField
    Button buttonManage, buttonRemove;
    @UiField
    FilterDisplayWidget filterDisplayWidget;
    @UiField
    CardLayoutContainer detailContainer;
//    @UiField
//    StringComboBox availableFilters;
    
    private Filter currentFilter;

    interface SpecificUiBinder extends UiBinder<Dialog, FilterSelectionDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public FilterSelectionDialog(Filter currentFilter, String dataViewUuid) {
        this(null, currentFilter, dataViewUuid);
    }

    public FilterSelectionDialog(CanBeShownParent parentIn, Filter currentFilter, String dataViewUuid) {
        super(parentIn, dataViewUuid);
        this.currentFilter = currentFilter;
        addHandlers();
        availableFilters.removeStyleName("string-combo-style");
    }

    protected Dialog getDialog() {

        return uiBinder.createAndBindUi(this);
    }

    private void addHandlers() {
        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                SelectionEvent.fire(FilterSelectionDialog.this, currentFilter);
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

    @Override
    protected void onAvailableFilterValueChange(Filter filter) {
        detailContainer.setActiveWidget(filterDisplayWidget);
        filterDisplayWidget.display(filter);
        currentFilter = filter;
        dialog.getActionButton().setEnabled(true);
    }

    @Override
    protected void onFilterPopulation(List<Filter> result) {
        Filter selectedFilter = null;
        for (Filter filter : result) {
            if (filter.getUuid().equals(currentFilter.getUuid())) {
                selectedFilter = filter;
                break;
            }
        }
        if (selectedFilter != null) {
            getAvailableFilters().setValue(selectedFilter, true);
        }
    }

    @Override
    public void show() {
        dialog.getActionButton().setText(_constants.select());
        dialog.getActionButton().setEnabled(false);
        buttonRemove.setEnabled(currentFilter != null);
        super.show();
    }

    @Override
    public Filter getSelectedFilter() {
        return currentFilter;
    }

    @UiHandler("buttonManage")
    public void handleManageButtonClick(ClickEvent e) {
        ManageFilterDialog myDialog = new ManageFilterDialog(currentFilter, getDataViewUuid());
        myDialog.show();
        myDialog.addHideHandler(new HideHandler() {

            @Override
            public void onHide(HideEvent hideEvent) {
                populateAvailableFilters();
            }
        });
    }

    @UiHandler("buttonRemove")
    public void handleButtonRemove(ClickEvent e) {
        WarningDialog warning = new WarningDialog(_constants.filterSelectionDialog_WarningTitle(), _constants.filterSelectionDialog_WarningMessage());
        warning.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                hide();
                SelectionEvent.fire(FilterSelectionDialog.this, null);
            }
        });
        warning.show();
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<Filter> handler) {
        return eventBus.addHandlerToSource(SelectionEvent.getType(), this, handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEventFromSource(event, this);
    }
}
