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

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.edit_sources.dialogs.column.ColumnFilterDialog.ColumnFilterMode;
import csi.client.gwt.edit_sources.dialogs.common.ParameterPresenter;
import csi.client.gwt.widget.gxt.grid.EditCell;
import csi.server.common.model.column.ColumnFilter;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FilterEditCell extends EditCell<ColumnFilter> {

    private ColumnDefContext _cdContext;
    private ClickHandler _foreignHandler;
    private ColumnFilterDialog _dialog = null;
    ListStore<ColumnFilter> _gridStore;
    private ParameterPresenter _parameterPresenter;

    private ClickHandler handleFilterUpdate
    = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {
            
            if (null != _dialog) {
            
                if (null != _foreignHandler) {
                    
                    _foreignHandler.onClick(eventIn);
                }
                _dialog = null;
            }
        }
    };

    public FilterEditCell(ListStore<ColumnFilter> gridStoreIn,
            ParameterPresenter parameterPresenterIn, ColumnDefContext cdContext) {
        
        _gridStore = gridStoreIn;
        _parameterPresenter = parameterPresenterIn;
        _cdContext = cdContext;
    }

    @Override
    protected void onClickEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, ColumnFilter value,
            NativeEvent event, ValueUpdater<ColumnFilter> valueUpdater) {

        _dialog = new ColumnFilterDialog(ColumnFilterMode.EDIT, value, _gridStore, _cdContext.getTableDef(),
                                        _cdContext.getColumnDef(), _parameterPresenter);
        _dialog.setActionClickHandler(handleFilterUpdate);
        _dialog.show();
    }

    
    public void setClickHandler(ClickHandler clickHandlerIn) {
        _foreignHandler = clickHandlerIn;
    }
}
