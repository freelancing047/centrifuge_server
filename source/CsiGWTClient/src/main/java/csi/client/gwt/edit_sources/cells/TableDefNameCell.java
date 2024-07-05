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
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.XTemplates;

import csi.client.gwt.dataview.resources.DataSourceClientUtil;
import csi.server.common.model.SqlTableDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TableDefNameCell extends AbstractCell<SqlTableDef> {

    interface NameTemplate extends XTemplates {

        @XTemplate("<img src=\"{uri}\"/> {name}")
        SafeHtml template(SafeUri uri, String name);
    }

    private static final NameTemplate nameTemplate = GWT.create(NameTemplate.class);

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, SqlTableDef value, SafeHtmlBuilder sb) {
        sb.append(nameTemplate.template(DataSourceClientUtil.get(value, false).getSafeUri(), value.getTableName()));
    }

}
