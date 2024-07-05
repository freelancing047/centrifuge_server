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
package csi.client.gwt.widget.gxt.grid.paging;

import java.util.List;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;

import csi.client.gwt.widget.gxt.grid.MultiPageCheckboxSelectionModel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface GridComponentManager<M> {

    MultiPageCheckboxSelectionModel<M> getCheckboxSelectionModel();

    /**
     * Creates a column config. Also adds to a column config list in the order in which they were created.
     * @param valueProvider
     * @param width
     * @param label
     * @return
     */
    <N> ColumnConfig<M, N> create(ValueProvider<M, N> valueProvider, int width, String label);
    
    /**
     * 
     * @param valueProvider
     * @param width
     * @param label
     * @param sortable true to allow sorting via header click.
     * @param menuDisabled true to disable header menu.
     * @return
     */
    <N> ColumnConfig<M, N> create(ValueProvider<M, N> valueProvider, int width, String label, boolean sortable, boolean menuDisabled);

    List<ColumnConfig<M, ?>> getColumnConfigList();

    public ListStore<M> getStore();
}
