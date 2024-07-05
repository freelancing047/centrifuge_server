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
package csi.client.gwt.widget.ui;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;


/**
 * Resizable absolute panel that also resizes its children (which is odd for an absolute panel to do; except that 
 * there are special cases such as a child may want to resize itself to the full size of the parent.
 * @author Centrifuge Systems, Inc.
 *
 */
public class ResizeableAbsolutePanel extends AbsolutePanel implements RequiresResize, ProvidesResize {

    @Override
    public void onResize() {
        for (int i = 0; i < getWidgetCount(); i++) {
            Widget w = getWidget(i);
            if (w instanceof RequiresResize) {
                ((RequiresResize)w).onResize();
            }
        }
    }
}
