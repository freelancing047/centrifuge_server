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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

import csi.client.gwt.viz.table.ScrollingToolBar;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.ExceptionHandler;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.widget.gxt.grid.paging.LoadCallback;
import csi.client.gwt.widget.gxt.grid.paging.RemoteLoadingGridComponentManager;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class RemoteLoadingGridComponentManagerImpl<M> extends GridComponentManagerImpl<M> implements
        RemoteLoadingGridComponentManager<M> {

    private Vortex vortex;

    private RpcProxy<FilterPagingLoadConfig, PagingLoadResult<M>> proxy;
    private PagingLoader<FilterPagingLoadConfig, PagingLoadResult<M>> remoteLoader;

    private PagingToolBar pagingToolBar;
    private ScrollingToolBar scrollingToolBar;
    private GridFilters<M> gridFilters;

    public <V extends VortexService> RemoteLoadingGridComponentManagerImpl(Vortex vortex,
            ModelKeyProvider<M> modelKeyProvider, final Class<V> vortexClass, final LoadCallback<V, M> loadCallback) {
        super(modelKeyProvider);
        this.vortex = vortex;

        // Setup the proxy
        proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<M>>() {

            @Override
            public void load(FilterPagingLoadConfig loadConfig, final AsyncCallback<PagingLoadResult<M>> callback) {
                V vortexServiceInstance = RemoteLoadingGridComponentManagerImpl.this.vortex.execute(

                        new ExceptionHandler() {

                            @Override
                            public boolean handle(Throwable myException) {

                                callback.onFailure(myException);
                                return false;
                            }
                        },
                        new Callback<PagingLoadResult<M>>() {

                            @Override
                            public void onSuccess(PagingLoadResult<M> result) {
                                callback.onSuccess(result);
                            }
                        }, vortexClass);

                PagingLoadResult<M> result = loadCallback.onLoadCallback(vortexServiceInstance, loadConfig);
                // If we did get synchronous response (the grid did not really use the vortex service to fetch the
                // data ...
                if (result != null) {
                    callback.onSuccess(result);
                }
            }
        };

        remoteLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<M>>(proxy) {

            @Override
            protected FilterPagingLoadConfig newLoadConfig() {
                return new FilterPagingLoadConfigBean();
            }
        };
        remoteLoader.setRemoteSort(true);
        remoteLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, M, PagingLoadResult<M>>(
                getStore()));
    }

    public Vortex getVortex() {
        return vortex;
    }

    @Inject
    public void setVortex(Vortex vortex) {
        this.vortex = vortex;
    }

    public PagingToolBar getPagingToolbar(int pageSize) {
        if (pagingToolBar == null || pagingToolBar.getPageSize() != pageSize) {
            pagingToolBar = new PagingToolBar(pageSize);
            pagingToolBar.bind(remoteLoader);
        }
        return pagingToolBar;
    }
    
    public ScrollingToolBar getScrollingToolbar(int pageSize) {
        if (scrollingToolBar == null || scrollingToolBar.getPageSize() != pageSize) {
            scrollingToolBar = new ScrollingToolBar(pageSize);
            scrollingToolBar.bind(remoteLoader);
        }
        return scrollingToolBar;
    }
    
    public GridFilters<M> getGridFilters() {
        if (gridFilters == null) {
            gridFilters = new GridFilters<M>(remoteLoader);
        }
        return gridFilters;
    }

    public RpcProxy<FilterPagingLoadConfig, PagingLoadResult<M>> getProxy() {
        return proxy;
    }

    public PagingLoader<FilterPagingLoadConfig, PagingLoadResult<M>> getLoader() {
        return remoteLoader;
    }

}
