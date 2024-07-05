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

import com.google.inject.Inject;
import com.sencha.gxt.data.shared.ModelKeyProvider;

import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentFactory;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.gxt.grid.paging.LoadCallback;
import csi.client.gwt.widget.gxt.grid.paging.RemoteLoadingGridComponentManager;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class GridComponentFactoryImpl implements GridComponentFactory {

    private Vortex vortex;

    public Vortex getVortex() {
        return vortex;
    }

    @Inject
    public void setVortex(Vortex vortex) {
        this.vortex = vortex;
    }

    @Override
    public <T, V extends VortexService> RemoteLoadingGridComponentManager<T> createRemoteLoading(ModelKeyProvider<T> modelKeyProvider,
            final Class<V> vortexClass, final LoadCallback<V, T> loadCallback) {
        return new RemoteLoadingGridComponentManagerImpl<>(vortex, modelKeyProvider, vortexClass, loadCallback);
    }
    
    @Override
    public <T> GridComponentManager<T> create(ModelKeyProvider<T> modelKeyProvider) {
        return new GridComponentManagerImpl<>(modelKeyProvider);
    }

}
