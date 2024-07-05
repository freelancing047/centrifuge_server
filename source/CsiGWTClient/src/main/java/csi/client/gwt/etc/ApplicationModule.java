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
package csi.client.gwt.etc;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.impl.VortexImpl;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentFactory;
import csi.client.gwt.widget.gxt.grid.paging.impl.GridComponentFactoryImpl;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ApplicationModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(EventBus.class).to(ApplicationEventBus.class).in(Singleton.class);
        bind(Vortex.class).to(VortexImpl.class).in(Singleton.class);
        bind(GridComponentFactory.class).to(GridComponentFactoryImpl.class).in(Singleton.class);

        // Core application components.
        bind(MainPresenter.class).in(Singleton.class);
    }
}