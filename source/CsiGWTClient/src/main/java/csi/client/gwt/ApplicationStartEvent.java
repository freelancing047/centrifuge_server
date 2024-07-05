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
package csi.client.gwt;

import com.google.gwt.event.shared.GwtEvent;
import com.sencha.gxt.widget.core.client.container.Viewport;

import csi.client.gwt.etc.BaseCsiEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ApplicationStartEvent extends BaseCsiEvent<ApplicationStartEventHandler> {

    public static final GwtEvent.Type<ApplicationStartEventHandler> type = new GwtEvent.Type<ApplicationStartEventHandler>();

    private Viewport viewport;

    public ApplicationStartEvent(Viewport viewport) {
        this.viewport = viewport;
    }

    public Viewport getViewport() {
        return viewport;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ApplicationStartEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(ApplicationStartEventHandler handler) {
        handler.onApplicationStart(this);
    }
}
