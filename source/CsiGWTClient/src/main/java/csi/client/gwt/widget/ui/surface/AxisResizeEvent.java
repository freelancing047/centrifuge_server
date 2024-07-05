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
package csi.client.gwt.widget.ui.surface;

import com.google.gwt.event.logical.shared.ResizeEvent;


/**
 * The super-class ResizeEvent has a protected constructor. This class is simply to get a public constructor.
 * @author Centrifuge Systems, Inc.
 *
 */
public class AxisResizeEvent extends ResizeEvent {

    public AxisResizeEvent() {
        super(0, 0);
    }

}
