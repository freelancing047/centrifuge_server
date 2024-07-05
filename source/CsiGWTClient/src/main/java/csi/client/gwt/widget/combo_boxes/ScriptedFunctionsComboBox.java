
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
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctions;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ScriptedFunctionsComboBox extends ComboBox<ScriptedFunctions> {

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span title=\"{name}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{dataUri}\"/>&nbsp;&nbsp;{name}</span>")
        SafeHtml html(SafeUri dataUri, String name);

    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT
            .create(ComboBoxTemplates.class);

    public ScriptedFunctionsComboBox() {
        super(new ListStore<ScriptedFunctions>(new ModelKeyProvider<ScriptedFunctions>(){

            @Override
            public String getKey(ScriptedFunctions item) {
                return item.name();
            }}),

        new LabelProvider<ScriptedFunctions>() {

            @Override
            public String getLabel(ScriptedFunctions item) {
                return item.getTitle();
            }
        });
        addStyleName("string-combo-style");
        setTypeAhead(false);
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
        

        for (ScriptedFunctions df : ScriptedFunctions.values()) {
            getStore().add(df);
        }
//        dynamicFunctionsDropdown.addItem(ScriptedFunctions.ADVANCED_FUNCTION.getTitle());
        setValue(ScriptedFunctions.ADVANCED_FUNCTION, true);
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
