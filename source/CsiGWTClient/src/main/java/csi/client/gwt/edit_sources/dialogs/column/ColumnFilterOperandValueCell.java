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

import java.util.ArrayList;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store.Change;

import csi.client.gwt.edit_sources.dialogs.column.ColumnDefFilterCollectionDialog.ColumnFilterPropertyAccess;
import csi.client.gwt.edit_sources.dialogs.common.ParameterPresenter;
import csi.client.gwt.util.FieldDefUtils;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.ColumnFilter;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.query.QueryParameterDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColumnFilterOperandValueCell extends AbstractCell<ColumnFilter> {

    private ListStore<ColumnFilter> gridStore;
    private ColumnFilterPropertyAccess propertyAccess;
    private SqlTableDef tableDef;
    private ParameterPresenter _parameterPresenter;

    interface ValueTemplate extends XTemplates {

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{dataUri}\"/> {name}")
        SafeHtml template(SafeUri dataUri, String name);
    }

    private static final ValueTemplate valueTemplate = GWT.create(ValueTemplate.class);

    public ColumnFilterOperandValueCell(ListStore<ColumnFilter> gridStore, ColumnFilterPropertyAccess propertyAccess,
            SqlTableDef tableDef, ParameterPresenter parameterPresenterIn) {
        this.gridStore = gridStore;
        this.propertyAccess = propertyAccess;
        this.tableDef = tableDef;
        _parameterPresenter = parameterPresenterIn;
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, ColumnFilter value, SafeHtmlBuilder sb) {
        FilterOperandType operand = getCurrentOperand(value);
        if (operand != null) {
            switch (operand) {
                case STATIC: {
                    sb.appendEscaped(getCurrentStaticValues(value).toString());
                    break;
                }
                case COLUMN: {
                    for (ColumnDef column : tableDef.getColumns()) {
                        if (column.getLocalId().equals(getCurrentLocalColumnId(value))) {
                            sb.append(valueTemplate.template(
                                    FieldDefUtils.getColumnDataTypeImage(column).getSafeUri(),
                                    column.getColumnName()));
                            break;
                        }
                    }
                    break;
                }
                case PARAMETER: {
                    for (QueryParameterDef param : _parameterPresenter.getParameters()) {
                        if (param.getLocalId().equals(getCurrentParamLocalId(value))) {
                            sb.append(valueTemplate.template(
                                    FieldDefUtils.getDataTypeImage(param.getType())
                                            .getSafeUri(), param.getName()));
                            break;
                        }
                    }
                    break;
                }
            } // end case
        }
    }

    private FilterOperandType getCurrentOperand(ColumnFilter columnFilter) {
        Change<ColumnFilter, FilterOperandType> change = gridStore.getRecord(columnFilter).getChange(
                propertyAccess.operandType());
        if (change != null) {
            return change.getValue();
        } else {
            return columnFilter.getOperandType();
        }
    }

    private ArrayList<String> getCurrentStaticValues(ColumnFilter columnFilter) {
        Change<ColumnFilter, ArrayList<String>> change = gridStore.getRecord(columnFilter).getChange(
                propertyAccess.staticValues());
        if (change != null) {
            return change.getValue();
        } else {
            return columnFilter.getStaticValues();
        }
    }

    private String getCurrentLocalColumnId(ColumnFilter columnFilter) {
        Change<ColumnFilter, String> change = gridStore.getRecord(columnFilter).getChange(
                propertyAccess.localColumnId());
        if (change != null) {
            return change.getValue();
        } else {
            return columnFilter.getLocalColumnId();
        }
    }

    private String getCurrentParamLocalId(ColumnFilter columnFilter) {
        Change<ColumnFilter, String> change = gridStore.getRecord(columnFilter)
                .getChange(propertyAccess.paramLocalId());
        if (change != null) {
            return change.getValue();
        } else {
            return columnFilter.getParamLocalId();
        }
    }
}
