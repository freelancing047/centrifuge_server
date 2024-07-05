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
package csi.client.gwt.viz.graph.node.settings.appearance;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.viz.graph.node.settings.SizingAttribute;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SizingAttributeComboBox extends ComboBox<SizingAttribute> {

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;{name}")
        SafeHtml html(SafeUri dataUri, String name);

    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT
            .create(ComboBoxTemplates.class);

    public SizingAttributeComboBox() {
        super(new ListStore<SizingAttribute>(new ModelKeyProvider<SizingAttribute>(){
            @Override
            public String getKey(SizingAttribute item) {
                // TODO Auto-generated method stub
                return item.getName();
            }}),

        new LabelProvider<SizingAttribute>() {

            @Override
            public String getLabel(SizingAttribute item) {
                return item.getLabel();
            }
        });
        addStyleName("string-combo-style");
        setTypeAhead(false);
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
        
        getStore().clear();
        for (SizingAttribute dataType : SizingAttribute.values()) {
            getStore().add(dataType);
        }
    }

    public int getItemCount() {
        return getStore().size();
    }

    public int getSelectedIndex() {
        return getStore().indexOf(getCurrentValue());
    }

    public void setSelectedIndex(int i) {
        setValue(getStore().get(i));
    }

    @Override
    public void setValue(SizingAttribute value, boolean fireEvents) {
        if(getStore().getAll().contains(value))
            setValue(value, fireEvents, true);
     }
}
