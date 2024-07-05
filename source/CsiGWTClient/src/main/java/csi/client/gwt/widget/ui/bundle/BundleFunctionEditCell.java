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

import java.text.ParseException;
import java.util.List;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.GXTLogConfiguration;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.TextMetrics;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.Store.Change;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;

import csi.client.gwt.widget.gxt.form.TriggerBaseCell;
import csi.client.gwt.widget.misc.WidgetCallback;
import csi.server.common.model.visualization.BundleFunctionParameter;
import csi.server.util.sql.api.BundleFunction;
import csi.server.util.sql.api.HasBundleFunction;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class BundleFunctionEditCell<H extends HasBundleFunction> extends TriggerBaseCell<BundleFunction> {

    private ListStore<H> gridStore;
    private ValueProvider<H, List<BundleFunctionParameter>> bundleFunctionParameterValueProvider;
    private ValueProvider<H, Boolean> isSelectionValueProvider;
    private BundleFunctionDialog dialog = new BundleFunctionDialog();

    public BundleFunctionEditCell(ListStore<H> store,
            ValueProvider<H, List<BundleFunctionParameter>> bundleFunctionParameterValueProvider,
            ValueProvider<H, Boolean> isSelectionValueProvider) {
        this.gridStore = store;
        this.bundleFunctionParameterValueProvider = bundleFunctionParameterValueProvider;
        this.isSelectionValueProvider = isSelectionValueProvider;

        setWidth(100);
        setHideTrigger(true);

        setPropertyEditor(new PropertyEditor<BundleFunction>() {

            @Override
            public BundleFunction parse(CharSequence text) throws ParseException {
                return null;
            }

            @Override
            public String render(BundleFunction object) {
                String bundleDescription = object == null ? "" : BundleFunctionNameUtil.getName(object);
                if(getWidth() > 0) {
                    while (TextMetrics.get().getWidth(bundleDescription) > getWidth()&& bundleDescription.length()>0) {
                        bundleDescription = bundleDescription.substring(0, bundleDescription.length() - 2);
                    }
                } else {
                    bundleDescription = "";
                }
                return bundleDescription;
            }
        });
        dialog.setCell(this);
    }

    public ListStore<H> getGridStore() {
        return gridStore;
    }

    @Override
    public com.google.gwt.cell.client.Cell.Context getCurrentContext() {
        return super.getCurrentContext();
    }

    @Override
    protected void onTriggerClick(com.google.gwt.cell.client.Cell.Context context, XElement parent, NativeEvent event,
            BundleFunction value, ValueUpdater<BundleFunction> updater) {
        super.onTriggerClick(context, parent, event, value, updater);

        List<BundleFunctionParameter> paramValues = null;
        Change<H, List<BundleFunctionParameter>> change = getRecord().getChange(bundleFunctionParameterValueProvider);
        if (change == null) {
            paramValues = getRecord().getValue(bundleFunctionParameterValueProvider);
        } else {
            paramValues = change.getValue();
        }
        dialog.show(value, paramValues);
        dialog.setCallback(new WidgetCallback(){

            @Override
            public void action() {
                getRecord().addChange(BundleFunctionEditCell.this.bundleFunctionParameterValueProvider,
                        dialog.getParameterValues());
                getValueUpdater().update(dialog.getSelectedBundleFunction());
                dialog.hide();                
            }
        });

    }

    @Override
    public void onBrowserEvent(Context context, Element parent, BundleFunction value, NativeEvent event, ValueUpdater<BundleFunction> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        String eventType = event.getType();

        if ("click".equals(eventType)) {
            List<BundleFunctionParameter> paramValues = null;
            Change<H, List<BundleFunctionParameter>> change = getRecord().getChange(bundleFunctionParameterValueProvider);
            if (change == null) {
                paramValues = getRecord().getValue(bundleFunctionParameterValueProvider);
            } else {
                paramValues = change.getValue();
            }
            dialog.show(value, paramValues);
            dialog.setCallback(new WidgetCallback() {

                @Override
                public void action() {
                    getRecord().addChange(BundleFunctionEditCell.this.bundleFunctionParameterValueProvider,
                            dialog.getParameterValues());
                    getValueUpdater().update(dialog.getSelectedBundleFunction());
                    dialog.hide();
                }
            });
        }

    }

    /**
     * NOTE: Should not be called within onTrigger before super is called in it.
     * @return
     */
    private Store<H>.Record getRecord() {
        H cd = gridStore.findModelWithKey(getCurrentContext().getKey().toString());
        return gridStore.getRecord(cd);
    }
    
    @Override
    public void render(Context context, BundleFunction value, SafeHtmlBuilder sb) {
    	boolean selectionValueProviderNull = isSelectionValueProvider == null; 
    	if (selectionValueProviderNull) {
    		super.render(context, value, sb);
    	} else {
    		boolean isSelectionFilter = (Boolean) gridStore.getRecord(gridStore.findModelWithKey(context.getKey().toString())).getValue(isSelectionValueProvider); 
    		if (!isSelectionFilter) {
    			super.render(context, value, sb);
    		}
    	}
    }
}
