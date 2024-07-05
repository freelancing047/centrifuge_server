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
package csi.client.gwt.worksheet;

import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.widget.core.client.Window;

import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.viewer.Viewer;
import csi.server.common.model.visualization.VisualizationType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface WorksheetView extends IsWidget {

    /**
     * @return Name of this worksheet.
     */
    String getName();
    
    public int getOffsetHeight();
    
    public int getOffsetWidth();

    /**
     * @param visualization Visualization to remove.
     */
    void remove(Visualization visualization);

    /**
     * @param type
     * @return Visualizations of the given type that are in the worksheet.
     */
    public List<Visualization> getVisualizations(VisualizationType type);

    /**
     * @return Worksheet level event bus
     */
    public EventBus getEventBus();

    /**
     * @param visualization Visualization to add.
     */
    void add(Visualization visualization);

    /**
     * @param vz Visualization
     * @return true if it is minimized.
     */
    boolean isMinimized(Visualization vz);

    void add(Window w);

    public boolean isReadOnly();

    void saveState();

    Viewer getViewer();
}
