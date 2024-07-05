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
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.resources.ApplicationResources;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentFactory;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@GinModules(ApplicationModule.class)
public interface ApplicationInjector extends Ginjector {

    public Vortex getVortex();

    public EventBus getEventBus();

    public ApplicationResources getResources();

    @SuppressWarnings("rawtypes")
    public GridComponentFactory getGridFactory();

    public void injectInfrastructureAware(InfrastructureAware instrastructureAware);

    public MainPresenter getMainPresenter();
    
}
