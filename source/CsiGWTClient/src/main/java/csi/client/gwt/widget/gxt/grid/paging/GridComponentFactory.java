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

import com.sencha.gxt.data.shared.ModelKeyProvider;

import csi.shared.gwt.vortex.VortexService;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface GridComponentFactory {

    public <T, V extends VortexService> RemoteLoadingGridComponentManager<T> createRemoteLoading(ModelKeyProvider<T> modelKeyProvider,
            Class<V> vortexClass, LoadCallback<V, T> loadCallback);


    public <T> GridComponentManager<T> create(ModelKeyProvider<T> modelKeyProvider);
}
