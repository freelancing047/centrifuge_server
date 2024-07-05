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

import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;

/**
 * Captures the column, the table it belongs to, and the data source editor's model object
 *  
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColumnDefContext {

    private ColumnDef columnDef;
    private SqlTableDef tableDef;
    private DataSourceEditorModel model;

    public ColumnDefContext(ColumnDef columnDef, SqlTableDef tableDef, DataSourceEditorModel model) {
        super();
        this.columnDef = columnDef;
        this.tableDef = tableDef;
        this.model = model;
    }

    public ColumnDef getColumnDef() {
        return columnDef;
    }

    public SqlTableDef getTableDef() {
        return tableDef;
    }

    public DataSourceEditorModel getModel() {
        return model;
    }

}
