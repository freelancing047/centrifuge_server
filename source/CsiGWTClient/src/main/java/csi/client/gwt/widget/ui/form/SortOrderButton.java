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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.FlowPanel;

import csi.client.gwt.resources.SortResource;
import csi.server.common.model.SortOrder;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SortOrderButton extends FlowPanel {

    interface CellTemplate extends SafeHtmlTemplates {

        @Template("<div style=\"text-align: left;display:inline;\"><img src=\"{0}\"></img></div>")
        SafeHtml templateOrder(SafeUri uri);
    }

    private static final CellTemplate cellTemplate = GWT.create(CellTemplate.class);

    private SortOrder value;

    public SortOrderButton() {
        super();
        this.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setValue(getValue().toggle());
            }
        }, ClickEvent.getType());
    }

    public SortOrder getValue() {
        return value;
    }

    public void setValue(SortOrder value) {
        this.value = value;
        SafeUri uri = value == SortOrder.ASC ? SortResource.IMPL.sortAscending().getSafeUri() : SortResource.IMPL
                .sortDescending().getSafeUri();
        getElement().setInnerSafeHtml(cellTemplate.templateOrder(uri));
    }

}
