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
package csi.client.gwt.edit_sources.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.XTemplates;

import csi.server.common.model.column.ColumnDef;

/**
 * @author Centrifuge Systems, Inc.
 */
public class FilterAwareColumnNameCell extends AbstractCell<ColumnDef> {

    interface filteredTemplate extends XTemplates {

        @XTemplate("<span title='{name}' style='color:#00C0C0'>{name}</span>")
        SafeHtml display(String name);
    }

    interface unfilteredTemplate extends XTemplates {

        @XTemplate("<span title='{name}'>{name}</span>")
        SafeHtml display(String name);
    }

    private static final filteredTemplate filteredItem = GWT.create(filteredTemplate.class);
    private static final unfilteredTemplate unfilteredItem = GWT.create(unfilteredTemplate.class);

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, ColumnDef value, SafeHtmlBuilder sb) {
        if ((null != value.getColumnFilters()) && (0 < value.getColumnFilters().size())) {
            sb.append(filteredItem.display(value.getColumnName()));
        } else {
            sb.append(unfilteredItem.display(value.getColumnName()));
        }
    }

}
