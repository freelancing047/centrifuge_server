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
public class PublishDataViewEvent extends BaseCsiEvent<PublishDataViewEventHandler> {

    public static final GwtEvent.Type<PublishDataViewEventHandler> type = new GwtEvent.Type<PublishDataViewEventHandler>();

    private String dataViewUuid;

    public PublishDataViewEvent(String dataViewUuid) {
        super();
        this.dataViewUuid = dataViewUuid;
    }

    public String getDataViewUuid() {
        return dataViewUuid;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<PublishDataViewEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(PublishDataViewEventHandler handler) {
        handler.onPublish(this);
    }

}
