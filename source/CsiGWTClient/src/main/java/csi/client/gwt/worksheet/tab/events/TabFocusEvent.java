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
package csi.client.gwt.worksheet.tab.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.etc.BaseCsiEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TabFocusEvent extends BaseCsiEvent<TabFocusEventHandler> {

    private Widget tabContentWidget;

    public static final GwtEvent.Type<TabFocusEventHandler> type = new GwtEvent.Type<TabFocusEventHandler>();

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<TabFocusEventHandler> getAssociatedType() {
        return type;
    }

    public void setTabContentWidget(Widget tabContentWidget) {
        this.tabContentWidget = tabContentWidget;
    }

    @Override
    protected void dispatch(TabFocusEventHandler handler) {
        handler.onFocus(tabContentWidget);
    }
}
