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
package csi.client.gwt.widget.gxt.grid.paging.impl;

import java.util.ArrayList;
import java.util.List;

import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;

import csi.client.gwt.widget.gxt.grid.MultiPageCheckboxSelectionModel;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class GridComponentManagerImpl<M> implements GridComponentManager<M> {

    private ModelKeyProvider<M> modelKeyProvider;
    private MultiPageCheckboxSelectionModel<M> checkboxSelectionModel;
    private List<ColumnConfig<M, ?>> columnConfigList = new ArrayList<ColumnConfig<M, ?>>();
    private ListStore<M> store;

    public GridComponentManagerImpl(ModelKeyProvider<M> modelKeyProvider) {
        this.modelKeyProvider = modelKeyProvider;
        store = new ListStore<M>(modelKeyProvider);
    }

    @Override
    public MultiPageCheckboxSelectionModel<M> getCheckboxSelectionModel() {
        if (checkboxSelectionModel == null) {
            IdentityValueProvider<M> identity = new IdentityValueProvider<M>();
            checkboxSelectionModel = new MultiPageCheckboxSelectionModel<M>(identity, modelKeyProvider);
        }
        return checkboxSelectionModel;
    }

    @Override
    public <N> ColumnConfig<M, N> create(ValueProvider<M, N> valueProvider, int width, String label) {
        return create(valueProvider, width, label, true, false);
    }

    @Override
    public <N> ColumnConfig<M, N> create(ValueProvider<M, N> valueProvider, int width, String label, boolean sortable,
            boolean menuDisabled) {
        ColumnConfig<M, N> cc = new ColumnConfig<M, N>(valueProvider, width, label);
        cc.setSortable(sortable);
        cc.setMenuDisabled(menuDisabled);
        columnConfigList.add(cc);
        return cc;
    }

    @Override
    public List<ColumnConfig<M, ?>> getColumnConfigList() {
        return columnConfigList;
    }

    public ListStore<M> getStore() {
        return store;
    }
}
