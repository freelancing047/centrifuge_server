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
import csi.server.common.model.visualization.VisualizationType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class NewVisualizationEvent extends BaseCsiEvent<NewVisualizationEventHandler> {

    public static final GwtEvent.Type<NewVisualizationEventHandler> type = new GwtEvent.Type<NewVisualizationEventHandler>();
    private VisualizationType visualizationType;

    public NewVisualizationEvent(VisualizationType visualizationType) {
        super();
        this.visualizationType = visualizationType;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<NewVisualizationEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(NewVisualizationEventHandler handler) {
        handler.onRequest(visualizationType);
    }

}
