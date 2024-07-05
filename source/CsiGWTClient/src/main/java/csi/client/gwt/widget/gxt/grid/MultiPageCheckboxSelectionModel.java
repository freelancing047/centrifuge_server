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
package csi.client.gwt.widget.gxt.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.shared.event.GroupingHandlerRegistration;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreAddEvent.StoreAddHandler;
import com.sencha.gxt.data.shared.event.StoreClearEvent;
import com.sencha.gxt.data.shared.event.StoreClearEvent.StoreClearHandler;
import com.sencha.gxt.data.shared.event.StoreRecordChangeEvent;
import com.sencha.gxt.data.shared.event.StoreRecordChangeEvent.StoreRecordChangeHandler;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent.StoreRemoveHandler;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent.StoreUpdateHandler;
import com.sencha.gxt.widget.core.client.event.HeaderClickEvent;
import com.sencha.gxt.widget.core.client.event.RefreshEvent;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnHeader;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * Adaptation of Sencha's grid-selection model that supports a virtual "select-all" on a remote-paged grid.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class MultiPageCheckboxSelectionModel<M> extends CheckBoxSelectionModel<M> {

	  protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private boolean globalSelectAll;
    private ModelKeyProvider<M> modelKeyProvider;
    /* When globalSelectAll is  true, selection Qualifier tells us what is not selected. If 
     * globalSelectAll is false, then
     * selectionQualifier is the list of entries that are set.
     */
    private Set<String> selectionQualifier = new HashSet<String>();
    private boolean disableGlobalClear;

    protected GroupingHandlerRegistration handlerRegistration = new GroupingHandlerRegistration();
    private StoreHandler handler = new StoreHandler();

    // used to overide incorrect handling of mouse down events in IE10
    private boolean invalidIeVersion = false;
    private int headerHeight= 14;

    public int getHeaderHeight() {
        return headerHeight;
    }

    public void setHeaderHeight(int headerHeight) {
        this.headerHeight = headerHeight;
    }

    /**
     * @param valueProvider
     */
    public MultiPageCheckboxSelectionModel(IdentityValueProvider<M> valueProvider, ModelKeyProvider<M> modelKeyProvider) {
        super(valueProvider);
        this.modelKeyProvider = modelKeyProvider;
        invalidIeVersion = Window.Navigator.getUserAgent().contains("MSIE 10"); //$NON-NLS-1$
        GWT.log(i18n.multiPageCheckboxSelectionModelBadMsIe() + invalidIeVersion); //$NON-NLS-1$

    }

    @Override
    protected void doSingleSelect(M model, boolean suppressEvent) {
        super.doSingleSelect(model, suppressEvent);
        changeSelection(model, true);
    }

    @Override
    public void bind(Store<M> store) {
        if (store instanceof ListStore) {
            listStore = (ListStore<M>) store;
        } else {
            listStore = null;
        }

        if (this.store != null) {
            handlerRegistration.removeHandler();
        }
        this.store = store;
        if (store != null) {
            if (handlerRegistration == null) {
                handlerRegistration = new GroupingHandlerRegistration();
            }
            handlerRegistration.add(store.addStoreAddHandler(handler));
            handlerRegistration.add(store.addStoreRemoveHandler(handler));
            handlerRegistration.add(store.addStoreClearHandler(handler));
            handlerRegistration.add(store.addStoreUpdateHandler(handler));
            handlerRegistration.add(store.addStoreRecordChangeHandler(handler));
        }
    }

    @Override
    protected void doMultiSelect(List<M> models, boolean keepExisting, boolean suppressEvent) {
        // When one page is fully selected and we navigate to a different page and select the header check-box, then
        // the multi-select with keepExisting = false will cause us to call setGlobalSelectAll(false) which will in
        // turn drop the previous selection. So on a header click we disable global clear to prevent that.
        if (!keepExisting && !disableGlobalClear) {
            // A multi-select that should force clearing of current global-select.
            setGlobalSelectAll(false);
        }
        super.doMultiSelect(models, keepExisting, suppressEvent);
        changeSelection(models, true);
    }

    @Override
    protected void doDeselect(List<M> models, boolean supressEvent) {
        super.doDeselect(models, supressEvent);
        changeSelection(models, false);
    }

    private void changeSelection(M model, boolean selectRows) {
        changeSelection(Collections.singletonList(model), selectRows);
    }

    private void changeSelection(Collection<M> models, boolean selectRows) {
        for (M model : models) {
            String id = modelKeyProvider.getKey(model);
            if (selectRows && !globalSelectAll) {
                selectionQualifier.add(id);
            } else if (!selectRows && globalSelectAll) {
                selectionQualifier.add(id);
            } else if (!selectRows && !globalSelectAll) {
                selectionQualifier.remove(id);
            } else if (selectRows && globalSelectAll) {
                selectionQualifier.remove(id);
            }
        }
    }

    public void setGlobalSelectAll(boolean newGlobalSelectAll) {
        this.globalSelectAll = newGlobalSelectAll;
        if (this.globalSelectAll) {
            selectionQualifier.clear();
            select(store.getAll(), true);
        }
    }

    public boolean isGlobalSelectAll() {
        return globalSelectAll;
    }

    public Set<String> getSelectionQualifier() {
        return selectionQualifier;
    }

    public List<String> getSelectionQualifierList() {
        return new ArrayList<String>(selectionQualifier);
    }

    public void setSelectionQualifier(List<String> selectionQualifier) {
        this.selectionQualifier = new HashSet<String>(selectionQualifier);
    }

    @Override
    protected void handleHeaderClick(HeaderClickEvent event) {
        disableGlobalClear = true;
        super.handleHeaderClick(event);
        disableGlobalClear = false;
    }

    @Override
    public void refresh() {
        // This is called when we move from one page to another. The super.refresh() attempts to keep current selection
        // and sends a call to select with keepExisting false. This ends up in doMulti ... wherein we will end up
        // clearing global select. So we disable global clear on refresh.
        disableGlobalClear = true;
        super.refresh();
        // Gxt doesn't retain any information on what was selected in a particular page. So we have to read out
        // for current page what should be marked as selected and record it.
        List<M> models = store.getAll();
        List<M> selected = new ArrayList<M>();
        for (M m : models) {
            String id = modelKeyProvider.getKey(m);
            // Add to selection list if this is in qualifier list and we don't have global select ... or ...
            // this is not in qualifier list and we do have global select (qualifier then is exclusions)
            boolean sqContained = selectionQualifier.contains(id);
            if (sqContained && !globalSelectAll || !sqContained && globalSelectAll) {
                selected.add(m);
            }
        }
        select(selected, true);
        disableGlobalClear = false;
    }

    @Override
    protected void onRefresh(RefreshEvent event) {
        // we use the header box for an the "all selected" field.
        updateHeaderCheckBox();
        ColumnHeader<M>.Head head = grid.getView().getHeader().getHead(grid.getColumnModel().getColumns().indexOf(getColumn()));
        if (head != null) {
            XElement hd = head.getElement();
            hd.getStyle().setHeight(headerHeight, Style.Unit.PX);
        }
    }

    private class StoreHandler implements StoreAddHandler<M>, StoreRemoveHandler<M>, StoreClearHandler<M>,
            StoreRecordChangeHandler<M>, StoreUpdateHandler<M> {

        @Override
        public void onAdd(StoreAddEvent<M> event) {
            MultiPageCheckboxSelectionModel.this.onAdd(event.getItems());
        }

        @Override
        public void onClear(StoreClearEvent<M> event) {
            MultiPageCheckboxSelectionModel.this.onClear(event);
        }

        @Override
        public void onRecordChange(final StoreRecordChangeEvent<M> event) {
            // run defer to ensure the code runs after grid view refreshes row
            Scheduler.get().scheduleFinally(new ScheduledCommand() {

                @Override
                public void execute() {
                    MultiPageCheckboxSelectionModel.this.onRecordChange(event);
                }
            });
        }

        @Override
        public void onRemove(StoreRemoveEvent<M> event) {
            MultiPageCheckboxSelectionModel.this.onRemove(event.getItem());
        }

        @Override
        public void onUpdate(StoreUpdateEvent<M> event) {
            final List<M> update = event.getItems();
            // run defer to ensure the code runs after grid view refreshes row
            Scheduler.get().scheduleFinally(new ScheduledCommand() {

                @Override
                public void execute() {
                    for (int i = 0; i < update.size(); i++) {
                        MultiPageCheckboxSelectionModel.this.onUpdate(update.get(i));
                    }
                }
            });
        }

    }

}
