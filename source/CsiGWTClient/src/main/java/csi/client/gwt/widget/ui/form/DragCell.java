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
package csi.client.gwt.widget.ui.form;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DragCell<T> extends AbstractCell<T> {

    public static <M> DragCell<M> create() {
        return new DragCell<M>();
    }

    private DragCell() {
        super();
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, T value, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<i class='icon-reorder'/>"); //$NON-NLS-1$
    }
}
