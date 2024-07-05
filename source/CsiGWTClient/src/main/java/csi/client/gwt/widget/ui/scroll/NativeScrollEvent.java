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
package csi.client.gwt.widget.ui.scroll;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class NativeScrollEvent extends BaseCsiEvent<NativeScrollEventHandler> {

    private int delta;
    private double ratio;

    public static final GwtEvent.Type<NativeScrollEventHandler> type = new GwtEvent.Type<NativeScrollEventHandler>();

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<NativeScrollEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(NativeScrollEventHandler handler) {
        handler.onScroll(this);
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

}
