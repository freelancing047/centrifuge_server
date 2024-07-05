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
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.widget.cells.ComboBoxEditCell;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class StringComboBox extends ComboBox<String> {


    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span title=\"{name}\">&nbsp;&nbsp;{name}</span>")
        SafeHtml html(String name);
    }

    private static final ComboBoxTemplates comboBoxTemplates = GWT.create(ComboBoxTemplates.class);

    public StringComboBox() {
        super(
            new ListStore<String>(new ModelKeyProvider<String>(){

                @Override
                public String getKey(String item) {
                    return item.toString();
                }}),
            new LabelProvider<String>() {

                @Override
                public String getLabel(String item) { return item; }
            },
            new AbstractSafeHtmlRenderer<String>() {

                public SafeHtml render(String item) {
                    return comboBoxTemplates.html(item);
                }
            }
        );
        addStyleName("string-combo-style");
        initialize();
    }

    public StringComboBox(ListStore<String> listStore, LabelProvider<String> stringLabelProvider) {
        super(listStore, stringLabelProvider);
            addStyleName("string-combo-style");
            initialize();
    }

    public StringComboBox(ComboBoxCell<String> comboBoxCell) {
        super(comboBoxCell);
        addStyleName("string-combo-style");
        initialize();
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
    
    public String getCurrentCellText() {
        
        return ((ComboBoxEditCell<String>)getCell()).getSelectionText();
    }

    private void initialize() {
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
        getStore().applySort(true);
    }

}
