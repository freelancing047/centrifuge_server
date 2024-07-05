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
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.sencha.gxt.cell.core.client.form.TriggerFieldCell;
import com.sencha.gxt.core.client.dom.XElement;

/**
 * A cell that displays text value and shows a trigger for displaying a menu to change the cell value.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class TriggerBaseCell<T> extends TriggerFieldCell<T> {

    public interface TriggerCellAppearance extends TriggerFieldAppearance {

    }

    
    private T currentValue;
    private ValueUpdater<T> valueUpdater;
    private Context currentContext;

    public TriggerBaseCell() {
        super(GWT.<TriggerFieldAppearance>create(TriggerCellAppearance.class));
    }

    protected T getCurrentValue() {
        return currentValue;
    }

    protected ValueUpdater<T> getValueUpdater() {
        return valueUpdater;
    }

    protected Context getCurrentContext() {
        return currentContext;
    }

    @Override
    protected void onTriggerClick(Context context, XElement parent, NativeEvent event, T value, ValueUpdater<T> updater) {
        super.onTriggerClick(context, parent, event, value, updater);
        this.currentValue = value;
        this.valueUpdater = updater;
        this.currentContext = context;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> updater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        this.currentValue = value;
        this.valueUpdater = updater;
        this.currentContext = context;
    }

}
