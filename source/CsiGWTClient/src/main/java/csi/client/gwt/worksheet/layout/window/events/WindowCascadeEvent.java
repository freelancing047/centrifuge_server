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
package csi.client.gwt.worksheet.layout.window.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class WindowCascadeEvent extends BaseCsiEvent<WindowCascadeEventHandler> {

    public static final GwtEvent.Type<WindowCascadeEventHandler> type = new GwtEvent.Type<WindowCascadeEventHandler>();

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<WindowCascadeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(WindowCascadeEventHandler handler) {
        handler.onCascade();
    }

}
