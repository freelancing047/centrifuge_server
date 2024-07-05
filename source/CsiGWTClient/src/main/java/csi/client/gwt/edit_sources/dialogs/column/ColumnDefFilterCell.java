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

import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.client.gwt.util.AuthorizationSource;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.gxt.grid.EditCell;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColumnDefFilterCell extends EditCell<ColumnDef> {

    private DataSourceEditorModel _model;
    private SqlTableDef _tableDef;
    private ClickHandler _foreignHandler;
    private ColumnDefFilterCollectionDialog _dialog = null;
    private AuthorizationSource _authorization = null;
    
    private ClickHandler handleFilterUpdate
    = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {
            
            if (null != _dialog) {
                
                _model.replaceQueryParameters(_dialog.getParameters());
            
                if (null != _foreignHandler) {
                    
                    _foreignHandler.onClick(eventIn);
                }
                _dialog = null;
            }
        }
    };

    public ColumnDefFilterCell(DataSourceEditorModel model, SqlTableDef tableDef) {
        _model = model;
        _tableDef = tableDef;
    }

    public ColumnDefFilterCell(DataSourceEditorModel model, SqlTableDef tableDef,
                               ClickHandler updateFiltersHandler, AuthorizationSource authorizationIn) {
        this(model,tableDef);
        _foreignHandler = updateFiltersHandler;
        _authorization = authorizationIn;
    }

    @Override
    protected void onClickEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, ColumnDef value,
            NativeEvent event, ValueUpdater<ColumnDef> valueUpdater) {

        if (_authorization.isOk(null)) {

            try {

                _dialog = new ColumnDefFilterCollectionDialog(new ColumnDefContext(value, _tableDef, _model));
                _dialog.setActionClickHandler(handleFilterUpdate);
                _dialog.show();

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }
    }
}
