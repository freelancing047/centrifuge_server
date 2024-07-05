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
public class WindowTileEvent extends BaseCsiEvent<WindowTileEventHandler> {

    public static final GwtEvent.Type<WindowTileEventHandler> type = new GwtEvent.Type<WindowTileEventHandler>();

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<WindowTileEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(WindowTileEventHandler handler) {
        handler.onTile();
    }

}
