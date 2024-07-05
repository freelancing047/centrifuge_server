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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.dataview.resources.DataSourceClientUtil;
import csi.server.common.model.column.ColumnDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColumnTableNameCell extends AbstractCell<String> {

    private ListStore<ColumnDef> store;

    interface Template extends XTemplates {

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;{name}")
        SafeHtml html(SafeUri dataUri, String name);

    }

    private static Template template = GWT.create(Template.class);

    public ColumnTableNameCell(ListStore<ColumnDef> gridStore) {
        this.store = gridStore;
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
        ColumnDef def = store.findModelWithKey(context.getKey().toString());
        sb.append(template.html(DataSourceClientUtil.get(def.getTableDef(), false).getSafeUri(), value));
    }
}
