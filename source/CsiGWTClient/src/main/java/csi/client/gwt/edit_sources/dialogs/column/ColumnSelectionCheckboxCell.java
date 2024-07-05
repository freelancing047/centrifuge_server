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
package csi.client.gwt.edit_sources.dialogs.column;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.server.common.model.column.ColumnDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColumnSelectionCheckboxCell extends CheckboxCell {

    public interface CallBack {

        public void onChange();
    }

    private ListStore<ColumnDef> gridStore;
    private DataSourceEditorModel model;
    private CallBack _callback = null;

    public ColumnSelectionCheckboxCell(ListStore<ColumnDef> gridStore, DataSourceEditorModel model) {
        super();
        this.gridStore = gridStore;
        this.model = model;
    }

    public ColumnSelectionCheckboxCell(ListStore<ColumnDef> gridStore, DataSourceEditorModel model, CallBack callbackIn) {
        super();
        this.gridStore = gridStore;
        this.model = model;
        _callback = callbackIn;
    }

    @Override
    public void onBrowserEvent(final Context context, Element parent, Boolean value, NativeEvent event,
            final ValueUpdater<Boolean> valueUpdater) {
        String type = event.getType();

        if (BrowserEvents.CHANGE.equals(type)) {
            final InputElement input = parent.getFirstChild().cast();
            final ColumnDef column = gridStore.findModelWithKey(context.getKey().toString());
            final Boolean isChecked = input.isChecked();

            updateCheckbox(context, valueUpdater, input, isChecked, column);
        } // end if
    }

    private void updateCheckbox(Context context, ValueUpdater<Boolean> valueUpdater, InputElement input,
            Boolean isChecked, ColumnDef column) {

        input.setChecked(isChecked);
        setViewData(context.getKey(), isChecked);

        if (valueUpdater != null) {
            valueUpdater.update(isChecked);
        }

        if (column != null) {
            if (isChecked) {
                model.addField(column, true);
            } else {
                model.removeField(column, false);
            }
        } // end if (column != null) ...

        if (null != _callback) {

            _callback.onChange();
        }
    }
}
