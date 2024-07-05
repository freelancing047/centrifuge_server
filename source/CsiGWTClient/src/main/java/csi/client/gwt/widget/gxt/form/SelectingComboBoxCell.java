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
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.event.CellSelectionEvent;
import csi.shared.core.visualization.chart.ChartType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SelectingComboBoxCell<T> extends ComboBoxCell<T> {

    /**
     * @param store
     * @param labelProvider
     */
    public SelectingComboBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider) {
        super(store, labelProvider);
        init();
    }

    private void init() {
        setTriggerAction(TriggerAction.ALL);
        setAllowBlank(false);
        setForceSelection(true);
        
        addSelectionHandler(event -> {
            SelectionEvent<T> cse = event;
            lastValueUpdater.update(cse.getSelectedItem());
        });
    }
    
    public void force(T value, boolean force){
//        ValueUpdater<T> lastValueUpdater = lastValueUpdater;
        lastValueUpdater.update(store.get(0));
    }



}
