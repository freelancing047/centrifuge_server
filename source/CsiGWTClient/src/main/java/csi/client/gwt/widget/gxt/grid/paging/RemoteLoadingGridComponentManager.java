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

import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

import csi.client.gwt.viz.table.ScrollingToolBar;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface RemoteLoadingGridComponentManager<M> extends GridComponentManager<M> {

    public PagingToolBar getPagingToolbar(int pageSize);
    
    public ScrollingToolBar getScrollingToolbar(int pageSize);

    public GridFilters<M> getGridFilters();

    public RpcProxy<FilterPagingLoadConfig, PagingLoadResult<M>> getProxy();

    public PagingLoader<FilterPagingLoadConfig, PagingLoadResult<M>> getLoader();
    

}
