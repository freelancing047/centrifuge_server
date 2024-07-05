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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.XTemplates;

import csi.client.gwt.util.FieldDefUtils;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.query.QueryParameterDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class QueryParameterNameCell extends AbstractCell<QueryParameterDef> {

    public interface Template extends XTemplates {

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;{name}")
        SafeHtml html(SafeUri dataUri, String name);

    }

    private static Template template = GWT.create(Template.class);

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, QueryParameterDef value, SafeHtmlBuilder sb) {
        CsiDataType type = value.getType();
        sb.append(template.html(
                FieldDefUtils.getDataTypeImage(type).getSafeUri(),
                value.getName()));
    }

}
