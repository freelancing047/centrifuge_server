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
import csi.client.gwt.viz.Visualization;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class VisualizationBarSelectionEvent extends BaseCsiEvent<VisualizationBarSelectionEventHandler> {

    public static final GwtEvent.Type<VisualizationBarSelectionEventHandler> type = new GwtEvent.Type<VisualizationBarSelectionEventHandler>();
    private Visualization visualization;

    public VisualizationBarSelectionEvent(Visualization value) {
        super();
        this.visualization = value;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<VisualizationBarSelectionEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(VisualizationBarSelectionEventHandler handler) {
        handler.onSelect(visualization);
    }

}
