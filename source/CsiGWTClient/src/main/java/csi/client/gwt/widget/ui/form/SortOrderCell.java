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

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;

import csi.client.gwt.resources.SortResource;
import csi.server.common.model.SortOrder;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SortOrderCell extends SelectionHandlingCell<SortOrder> {

    private boolean requireDoubleClick = false;

    interface CellTemplate extends SafeHtmlTemplates {

        @Template("<div style=\"text-align: center;\"><img src=\"{0}\"></img></div>")
        SafeHtml templateOrder(SafeUri uri);
    }

    private static final CellTemplate cellTemplate = GWT.create(CellTemplate.class);

    public SortOrderCell() {
        super(BrowserEvents.CLICK, BrowserEvents.DBLCLICK);
    }

    public SortOrderCell requireDoubleClick() {

        requireDoubleClick = true;
        return this;
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, SortOrder value, SafeHtmlBuilder sb) {
        if (value == null || value == SortOrder.NONE) {
            sb.appendHtmlConstant("<div />"); //$NON-NLS-1$
        } else {
            SafeUri uri = value == SortOrder.ASC ? SortResource.IMPL.sortAscending().getSafeUri() : SortResource.IMPL
                    .sortDescending().getSafeUri();
            sb.append(cellTemplate.templateOrder(uri));
        }
    }

    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, SortOrder value,
            NativeEvent event, ValueUpdater<SortOrder> valueUpdater) {
        if (BrowserEvents.CLICK.equals(event.getType()) && !requireDoubleClick) {
            valueUpdater.update(value.toggle());
        } else if (BrowserEvents.DBLCLICK.equals(event.getType())) {
            valueUpdater.update(value.toggle());
        }
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
    }
}
