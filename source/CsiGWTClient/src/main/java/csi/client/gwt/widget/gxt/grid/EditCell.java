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
package csi.client.gwt.widget.gxt.grid;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class EditCell<T> extends AbstractCell<T> {

    public EditCell() {
        super("click"); //$NON-NLS-1$
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {

        String eventType = event.getType();
        if (BrowserEvents.CLICK.equals(eventType)) {
            onClickEvent(context, parent, value, event, valueUpdater);
        } else {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
        }
    }

    protected abstract void onClickEvent(Context context, Element parent, T value, NativeEvent event,
            ValueUpdater<T> valueUpdater);

    
    
    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, T value, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<i class=\"icon-edit\"></i>"); //$NON-NLS-1$
    }
}
