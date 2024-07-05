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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import csi.server.common.model.filter.FilterOperandType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FilterOperandTypeCell extends AbstractCell<FilterOperandType> {

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, FilterOperandType value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.appendEscaped(value.getLabel());
        } else{
            sb.appendEscaped(FilterOperandType.NULL_LABEL);
        }
    }
}
