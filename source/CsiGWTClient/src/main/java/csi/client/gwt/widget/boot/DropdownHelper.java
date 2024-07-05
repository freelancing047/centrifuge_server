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
package csi.client.gwt.widget.boot;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sencha.gxt.core.client.dom.XDOM;

import csi.client.gwt.viz.shared.menu.CsiDropdown;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DropdownHelper {

    /**
     * Setup correct z-index
     * @param dropdown
     */
    public static void setupZ(final CsiDropdown dropdown) {
        dropdown.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                dropdown.getMenuWiget().getElement().getStyle().setZIndex(XDOM.getTopZIndex());
            }
        });
    }
}
