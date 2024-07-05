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
package csi.client.gwt.edit_sources.dialogs.common;

import java.util.Map;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import csi.client.gwt.widget.gxt.grid.EditCell;
import csi.server.common.model.query.QueryParameterDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class QueryParameterDefEditCell extends EditCell<QueryParameterDef> {
    
    Map<String, QueryParameterDef> _nameMap;
    
    public QueryParameterDefEditCell(Map<String, QueryParameterDef> nameMapIn) {
        
        super();
        _nameMap = nameMapIn;
    }

    @Override
    protected void onClickEvent(com.google.gwt.cell.client.Cell.Context context, Element parent,
            QueryParameterDef valueIn, NativeEvent event, final ValueUpdater<QueryParameterDef> valueUpdater) {

        if (!valueIn.isSystemParam()) {

            // Record management for change at the grid store happens based on immutability. So we pass a clone of the
            // QueryParameterDef to the editor dialog.
            final QueryParameterDef myParameter = valueIn;
            final QueryParameterDef myClone = valueIn.fullClone();
            final QueryParameterDialog myDialog = new QueryParameterDialog(QueryParameterDialog.Mode.EDIT, myClone, _nameMap);
            myDialog.setSaveClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    myClone.cloneValues(myParameter);
                    valueUpdater.update(myClone);
                    myDialog.hide();
                }
            });
            myDialog.show();
        }
    }

    @Override
    public void render(Context contextIn, QueryParameterDef valueIn, SafeHtmlBuilder htmlBuilderIn) {

        if (!valueIn.isSystemParam()) {

            super.render(contextIn, valueIn, htmlBuilderIn);
        }
    }
}
