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

import csi.client.gwt.etc.BaseCsiEventHandler;
import csi.client.gwt.viz.Visualization;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class VisualizationBarSelectionEventHandler extends BaseCsiEventHandler {

    public abstract void onSelect(Visualization visualization);
}
