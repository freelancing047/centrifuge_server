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
package csi.client.gwt.viz.shared;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DeleteVisualizationEvent extends BaseCsiEvent<DeleteVisualizationEventHandler> {

    public static final GwtEvent.Type<DeleteVisualizationEventHandler> type = new GwtEvent.Type<DeleteVisualizationEventHandler>();
    private String visualizationUuid;

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<DeleteVisualizationEventHandler> getAssociatedType() {
        return type;
    }

    public DeleteVisualizationEvent(String visualizationUuid) {
        super();
        this.visualizationUuid = visualizationUuid;
    }

    @Override
    protected void dispatch(DeleteVisualizationEventHandler handler) {
        handler.deleteVisualization(visualizationUuid);
    }

}
