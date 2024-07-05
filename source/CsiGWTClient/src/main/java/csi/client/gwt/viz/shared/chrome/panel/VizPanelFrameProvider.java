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
package csi.client.gwt.viz.shared.chrome.panel;

import com.google.gwt.user.client.ui.Widget;


/**
 * Provides framing (and optionally the ability to title the visualization and add buttons) for the visualization.
 * @author Centrifuge Systems, Inc.
 *
 */
public interface VizPanelFrameProvider {

    /**
     * @param name Name of the visualization.
     */
    public void setName(String name);
    
    /**
     * @param button Button to add to the frame.
     */
    public void addButton(Widget button);

    public boolean isInFocus();

    public void setInFocus(boolean isFocused);
}
