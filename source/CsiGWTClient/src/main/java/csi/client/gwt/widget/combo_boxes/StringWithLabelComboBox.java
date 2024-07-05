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
package csi.client.gwt.widget.combo_boxes;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.form.ComboBox;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class StringWithLabelComboBox extends ComboBox<String> {
    
    DualStringLabelProvider labelProvider = new DualStringLabelProvider();

    static interface StringProperty extends PropertyAccess<String> {

        ModelKeyProvider<String> uuid();
    }

    private static StringProperty StringProperty = GWT.create(StringProperty.class);

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span title=\"{name}\">&nbsp;&nbsp;{name}</span>")
        SafeHtml html(String name);
    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);

    public StringWithLabelComboBox(DualStringLabelProvider labelProvider) {
        super(
                new ListStore<String>(new ModelKeyProvider<String>(){

                    @Override
                    public String getKey(String item) {
                        return item.toString();
                    }}),
                labelProvider
            );
        addStyleName("string-combo-style");
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
    }
    
    public StringWithLabelComboBox() {
        super(
                new ListStore<String>(new ModelKeyProvider<String>(){

                    @Override
                    public String getKey(String item) {
                        return item.toString();
                    }}),
                new DualStringLabelProvider()
            );
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
    }
    
    public void addLabelValuePair(String value, String label){
        labelProvider.addPair(value, label);
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

   
}
