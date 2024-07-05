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
package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class CloseDataViewEvent extends BaseCsiEvent<CloseDataViewEventHandler> {

    public static final GwtEvent.Type<CloseDataViewEventHandler> type = new GwtEvent.Type<CloseDataViewEventHandler>();

    private String dataViewUuid;
    private boolean abort = false;

    public CloseDataViewEvent(String dataViewUuid) {
        super();
        this.dataViewUuid = dataViewUuid;
    }

    public CloseDataViewEvent(String dataViewUuidIn, boolean abortIn) {
        this(dataViewUuidIn);
        abort = abortIn;
    }

    public boolean isAbort() {
        return abort;
    }

    public String getDataViewUuid() {
        return dataViewUuid;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<CloseDataViewEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(CloseDataViewEventHandler handler) {
        handler.onCloseDataView(this);
    }

}
