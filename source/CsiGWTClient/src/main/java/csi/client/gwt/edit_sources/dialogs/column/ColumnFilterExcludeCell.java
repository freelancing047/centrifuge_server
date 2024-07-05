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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColumnFilterExcludeCell extends AbstractCell<Boolean> {

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, Boolean value, SafeHtmlBuilder sb) {
        sb.appendEscaped(value ? i18n.columnFilterExcludeCellNotLabel() : ""); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
