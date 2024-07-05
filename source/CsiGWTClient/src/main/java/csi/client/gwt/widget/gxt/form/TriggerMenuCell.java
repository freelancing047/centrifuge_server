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
package csi.client.gwt.widget.gxt.form;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.menu.Menu;

/**
 * A cell that displays text value and shows a trigger for displaying a menu to change the cell value.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class TriggerMenuCell<T> extends TriggerBaseCell<T> {

    private Menu menu = new Menu();

    protected Menu getMenu() {
        return menu;
    }

    @Override
    protected void onTriggerClick(Context context, XElement parent, NativeEvent event, T value, ValueUpdater<T> updater) {
        super.onTriggerClick(context, parent, event, value, updater);
        menu.show(parent.<Element> cast(), new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT, true));
    }

}
