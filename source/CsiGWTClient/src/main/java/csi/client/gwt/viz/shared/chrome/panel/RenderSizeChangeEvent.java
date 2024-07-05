/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 **/
package csi.client.gwt.viz.shared.chrome.panel;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class RenderSizeChangeEvent extends BaseCsiEvent<RenderSizeChangeEventHandler> {

    private RenderSize renderSize;

    public static final GwtEvent.Type<RenderSizeChangeEventHandler> type = new GwtEvent.Type<RenderSizeChangeEventHandler>();

    public RenderSizeChangeEvent(RenderSize renderSize) {
        super();
        this.renderSize = renderSize;
    }

    public RenderSize getRenderSize() {
        return renderSize;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<RenderSizeChangeEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(RenderSizeChangeEventHandler handler) {
        handler.onAttach(this);
    }
}
