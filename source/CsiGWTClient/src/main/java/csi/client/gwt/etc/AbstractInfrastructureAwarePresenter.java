/** 
 *  Copyright (c) 2008-2013 Centrifuge Systems, Inc. 
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
import com.google.inject.Inject;

import csi.client.gwt.vortex.Vortex;

/**
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class AbstractInfrastructureAwarePresenter implements InfrastructureAwarePresenter {

    private Vortex vortex;
    private EventBus eventBus;

    public Vortex getVortex() {
        return vortex;
    }

    @Override
    @Inject
    public void setVortex(Vortex vortex) {
        this.vortex = vortex;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    @Inject
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

}
