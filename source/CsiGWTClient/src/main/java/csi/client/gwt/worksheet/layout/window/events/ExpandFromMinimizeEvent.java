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
public class ExpandFromMinimizeEvent extends BaseCsiEvent<ExpandFromMinimizeEventHandler> {

    public static final GwtEvent.Type<ExpandFromMinimizeEventHandler> type = new GwtEvent.Type<ExpandFromMinimizeEventHandler>();

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ExpandFromMinimizeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(ExpandFromMinimizeEventHandler handler) {
        handler.onExpandFromMinimize(this);
    }

}
