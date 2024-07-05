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
package csi.client.gwt.widget.ui.bundle;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.ListStore;

import csi.server.common.model.filter.FilterExpression;
import csi.server.util.sql.api.BundleFunction;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class BundleFunctionDisplayCell extends AbstractCell<BundleFunction> {

	private ListStore<FilterExpression> gridStore;
    private ValueProvider<FilterExpression, Boolean> isSelectionValueProvider;

    interface BundleFunctionTemplate extends XTemplates {

        @XTemplate("{name}")
        SafeHtml template(String name);
    }
    
    public BundleFunctionDisplayCell(ListStore<FilterExpression> gridStore,
            ValueProvider<FilterExpression, Boolean> isSelectionValueProvider) {
        this.gridStore = gridStore;
        this.isSelectionValueProvider = isSelectionValueProvider;
    }

    private static final BundleFunctionTemplate bundleFunctionTemplate = GWT.create(BundleFunctionTemplate.class);

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, BundleFunction value, SafeHtmlBuilder sb) {
		boolean isSelectionFilter = (Boolean) gridStore.getRecord(gridStore.findModelWithKey(context.getKey().toString())).getValue(isSelectionValueProvider); 
		if (!isSelectionFilter) {
	        sb.append(bundleFunctionTemplate.template(BundleFunctionNameUtil.getName(value)));
		}
    }
}
